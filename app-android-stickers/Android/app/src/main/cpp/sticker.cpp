/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */
#include <jni.h>
#include <vector>
#include <memory>
#include <string>
#include <android/log.h>

extern "C" {
#include <libswresample/swresample.h>
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
#include <libavutil/avutil.h>
#include <decode.h>
#include <encode.h>
#include <mux.h>
}

// RAII para AVFrame
struct AVFrameDeleter {
    void operator()(AVFrame *frame) const {
        av_frame_free(&frame);
    }
};

using AVFramePtr = std::unique_ptr<AVFrame, AVFrameDeleter>;

AVFramePtr create_av_frame() {
    AVFrame *frame = av_frame_alloc();
    if (!frame) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Falha ao alocar AVFrame");
    }

    return AVFramePtr(frame);
};

// RAII para AVCodecContext
struct AVCodecContextDeleter {
    void operator()(AVCodecContext *ctx) const {
        avcodec_free_context(&ctx);
    }
};

using AVCodecContextPtr = std::unique_ptr<AVCodecContext, AVCodecContextDeleter>;

// RAII para AVFormatContext
struct AVFormatContextDeleter {
    void operator()(AVFormatContext *ctx) const {
        avformat_close_input(&ctx);
    }
};

using AVFormatContextPtr = std::unique_ptr<AVFormatContext, AVFormatContextDeleter>;

// RAII para SwsContext
struct SwsContextDeleter {
    void operator()(SwsContext *ctx) const {
        sws_freeContext(ctx);
    }
};

using SwsContextPtr = std::unique_ptr<SwsContext, SwsContextDeleter>;

// RAII para buffer alocado com av_malloc
struct AVBufferDeleter {
    void operator()(void *ptr) const {
        av_free(ptr);
    }
};

using AVBufferPtr = std::unique_ptr<void, AVBufferDeleter>;

// RAII para AVBufferRef para referencia
struct FrameWithBuffer {
    AVFramePtr frame;
    AVBufferPtr buffer;
};

std::vector<FrameWithBuffer> frames;

// RAII para WebPAnimEncoder
struct WebPAnimEncoderDeleter {
    void operator()(WebPAnimEncoder *enc) const {
        WebPAnimEncoderDelete(enc);
    }
};

using WebPAnimEncoderPtr = std::unique_ptr<WebPAnimEncoder, WebPAnimEncoderDeleter>;

// RAII para WebPData
struct WebPDataDeleter {
    void operator()(WebPData *data) const {
        WebPDataClear(data);
    }
};

using WebPDataPtr = std::unique_ptr<WebPData, WebPDataDeleter>;

// RAII para strings JNI e converter em char*
struct JniString {
    JNIEnv *env;
    jstring jstr;
    const char *cstr;

    JniString(JNIEnv *env, jstring jstr) : env(env), jstr(jstr), cstr(nullptr) {
        if (jstr) {
            cstr = env->GetStringUTFChars(jstr, nullptr);
        }
    }

    ~JniString() {
        if (cstr) {
            env->ReleaseStringUTFChars(jstr, cstr);
        }
    }

    const char *get() const { return cstr; }
};

int
CreateAnimatedWebP(const char *outputPath, std::vector<FrameWithBuffer> &frames, int width,
                   int height,
                   int durationMs) {
    WebPAnimEncoderOptions encOptions;
    if (!WebPAnimEncoderOptionsInit(&encOptions)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP",
                            "Falha ao inicializar WebPAnimEncoderOptions");
        return 0;
    }

    WebPConfig webPConfig;
    if (!WebPConfigInit(&webPConfig)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Falha ao inicializar WebPConfig");
        return 0;
    }
    webPConfig.lossless = 0;
    webPConfig.quality = 20;

    WebPAnimEncoderPtr encoder(WebPAnimEncoderNew(width, height, &encOptions));
    if (!encoder) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao criar WebPAnimEncoder");
        return 0;
    }

    int timestampMs = 0;
    for (const auto &fw: frames) {
        const AVFramePtr &frame = fw.frame;

        __android_log_print(ANDROID_LOG_DEBUG, "WebP",
                            "Adicionando frame ao encoder, timestamp: %d ms", timestampMs);

        WebPPicture webPPicture;
        if (!WebPPictureInit(&webPPicture)) {
            __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao inicializar WebPPicture");
            return 0;
        }
        webPPicture.width = width;
        webPPicture.height = height;
        webPPicture.use_argb = 1;

        if (!WebPPictureImportRGB(&webPPicture, frame->data[0], frame->linesize[0])) {
            __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao importar dados RGB");
            WebPPictureFree(&webPPicture);
            return 0;
        }

        if (!WebPAnimEncoderAdd(encoder.get(), &webPPicture, timestampMs, &webPConfig)) {
            __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao adicionar frame");
            WebPPictureFree(&webPPicture);
            return 0;
        }

        WebPPictureFree(&webPPicture);
        timestampMs += durationMs;
    }

    if (!WebPAnimEncoderAdd(encoder.get(), nullptr, timestampMs, nullptr)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao finalizar frames");
        return 0;
    }

    WebPData webpData;
    WebPDataInit(&webpData);

    WebPDataPtr webp_data_ptr(&webpData); // RAII para WebPData
    if (!WebPAnimEncoderAssemble(encoder.get(), &webpData)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao montar animação");
        return 0;
    }

    FILE *pFile = fopen(outputPath, "wb");
    if (!pFile) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao abrir arquivo de saída: %s",
                            outputPath);
        return 0;
    }

    fwrite(webpData.bytes, 1, webpData.size, pFile);
    fclose(pFile);
    __android_log_print(ANDROID_LOG_INFO, "WebP",
                        "Arquivo WebP salvo com sucesso em: %s (%zu bytes)", outputPath,
                        webpData.size);

    return 1;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_vinicius_sticker_domain_libs_ConvertToWebp_convertToWebp(JNIEnv *env, jobject /* this */,
                                                                  jstring inputPath,
                                                                  jstring outputPath) {
    // Gerenciamento de strings JNI com o RAII
    JniString inPath(env, inputPath);
    JniString outPath(env, outputPath);

    if (!inPath.get() || !outPath.get()) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI", "Caminhos de entrada ou saída inválidos");
        return JNI_FALSE;
    }

    // Inicialização de contextos com RAII
    AVFormatContext *formatContextRaw = nullptr;
    if (avformat_open_input(&formatContextRaw, inPath.get(), nullptr, nullptr) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Erro ao abrir arquivo: %s", inPath.get());
        return JNI_FALSE;
    }

    AVFormatContextPtr formatContext(formatContextRaw);
    if (avformat_find_stream_info(formatContext.get(), nullptr) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                            "Erro ao encontrar informações do stream em: %s", inPath.get());
        return JNI_FALSE;
    }

    int videoStreamIndex = -1;
    for (int i = 0; i < formatContext->nb_streams; i++) {
        if (formatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoStreamIndex = i;
            break;
        }
    }

    if (videoStreamIndex == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Nenhum stream de vídeo encontrado");
        return JNI_FALSE;
    }

    AVStream *videoStream = formatContext->streams[videoStreamIndex];
    const AVCodec *codec = avcodec_find_decoder(videoStream->codecpar->codec_id);
    if (!codec) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Codec não encontrado");
        return JNI_FALSE;
    }

    AVCodecContextPtr codecContext(avcodec_alloc_context3(codec));
    if (!codecContext) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                            "Não foi possível alocar o contexto do codec");
        return JNI_FALSE;
    }

    if (avcodec_parameters_to_context(codecContext.get(), videoStream->codecpar) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Erro ao configurar o contexto do codec");
        return JNI_FALSE;
    }

    int ret = avcodec_open2(codecContext.get(), codec, nullptr);
    if (ret < 0) {
        char errBuf[128];
        av_strerror(ret, errBuf, sizeof(errBuf));
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Erro ao abrir o codec: %s", errBuf);
        return JNI_FALSE;
    }

    AVFramePtr frame = create_av_frame();
    AVFramePtr rgbFrame = create_av_frame();
    if (!frame || !rgbFrame) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Erro ao alocar frames");
        return JNI_FALSE;
    }

    AVRational framerate = av_guess_frame_rate(formatContext.get(), videoStream, nullptr);
    double fpsOriginal = av_q2d(framerate);
    int frameInterval = std::max(1, static_cast<int>(fpsOriginal / 15.0 + 0.5));
    int frameCount = 0;

    int width = 512;
    int height = 512;
    SwsContextPtr swsContext(
            sws_getContext(width, height, codecContext->pix_fmt, width, height, AV_PIX_FMT_RGB24,
                           SWS_BILINEAR, nullptr, nullptr, nullptr));
    if (!swsContext) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                            "Erro ao criar o contexto de redimensionamento");
        return JNI_FALSE;
    }

    AVBufferPtr buffer(av_malloc(av_image_get_buffer_size(AV_PIX_FMT_RGB24, width, height, 1)));
    if (!buffer) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Erro ao alocar buffer RGB");
        return JNI_FALSE;
    }
    av_image_fill_arrays(rgbFrame->data, rgbFrame->linesize, static_cast<uint8_t *>(buffer.get()),
                         AV_PIX_FMT_RGB24, width, height, 1);

    std::vector<FrameWithBuffer> frames;

    AVPacket packet;
    av_init_packet(&packet);
    packet.data = nullptr;
    packet.size = 0;

    while (av_read_frame(formatContext.get(), &packet) >= 0) {
        if (packet.stream_index == videoStreamIndex) {

            // Pega os priemeiros 5 segundos de vídeo
            if (packet.pts != AV_NOPTS_VALUE) {
                double seconds = packet.pts * av_q2d(videoStream->time_base);
                if (seconds > 5.0) {
                    av_packet_unref(&packet);
                    break;
                }
            }

            int ret = avcodec_send_packet(codecContext.get(), &packet);
            if (ret < 0) {
                char errBuf[128];
                av_strerror(ret, errBuf, sizeof(errBuf));
                __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                                    "Erro ao enviar pacote para o codec: %s", errBuf);
                av_packet_unref(&packet);
                break;
            }

            while (ret >= 0) {
                ret = avcodec_receive_frame(codecContext.get(), frame.get());

                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
                    break;
                }

                if (ret == 0) {
                    __android_log_print(ANDROID_LOG_DEBUG, "FFmpeg",
                                        "Frame decodificado: formato=%d, width=%d, height=%d, linesize[0]=%d",
                                        frame->format, frame->width, frame->height,
                                        frame->linesize[0]);
                }

                if (ret < 0) {
                    char errBuf[128];
                    av_strerror(ret, errBuf, sizeof(errBuf));
                    __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                                        "Erro ao receber o quadro decodificado: %s", errBuf);
                    break;
                }

                sws_scale(swsContext.get(), frame->data, frame->linesize,
                          0, height, rgbFrame->data, rgbFrame->linesize);

                if (frameCount % frameInterval == 0) {
                    FrameWithBuffer clone;
                    clone.frame = create_av_frame();
                    if (!clone.frame) {
                        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                                            "Erro ao alocar frame clone");
                        continue;
                    }

                    clone.frame->format = AV_PIX_FMT_RGB24;
                    clone.frame->width = width;
                    clone.frame->height = height;

                    clone.buffer.reset(av_malloc(
                            av_image_get_buffer_size(AV_PIX_FMT_RGB24, width, height, 1)));
                    if (!clone.buffer) {
                        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                                            "Erro ao alocar buffer para clone");
                        continue;
                    }

                    av_image_fill_arrays(clone.frame->data, clone.frame->linesize,
                                         static_cast<uint8_t *>(clone.buffer.get()),
                                         AV_PIX_FMT_RGB24, width, height, 1);

                    av_image_copy(clone.frame->data, clone.frame->linesize,
                                  (const uint8_t **) rgbFrame->data, rgbFrame->linesize,
                                  AV_PIX_FMT_RGB24, width, height);

                    if (av_frame_copy_props(clone.frame.get(), frame.get()) != 0) {
                        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                                            "Erro ao copiar propriedades do frame");
                        continue;
                    }

                    frames.push_back(std::move(clone));
                    __android_log_print(ANDROID_LOG_DEBUG, "FFmpeg",
                                        "Frame %zu adicionado à lista de animação", frames.size());
                }

                frameCount++;
            }
        }
        av_packet_unref(&packet);
    }

    if (!frames.empty()) {
        int durationMs = 100;
        __android_log_print(ANDROID_LOG_INFO, "WebP", "Gerando animação com %zu frames...",
                            frames.size());

        if (frames.size() < 2) {
            __android_log_print(ANDROID_LOG_WARN, "WebP",
                                "Apenas %zu frame(s) capturado(s) — a animação pode parecer estática",
                                frames.size());
        }

        int result = CreateAnimatedWebP(outPath.get(), frames, width, height, durationMs);
        if (!result) {
            __android_log_print(ANDROID_LOG_ERROR, "WebP", "Falha ao criar a animação WebP");
            return JNI_FALSE;
        }
        __android_log_print(ANDROID_LOG_INFO, "WebP", "Animação WebP criada com sucesso: %s",
                            outPath.get());
    } else {
        __android_log_print(ANDROID_LOG_ERROR, "WebP",
                            "Nenhum frame capturado para criar a animação");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}