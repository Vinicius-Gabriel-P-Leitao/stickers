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
#include <base.h>
#include <format.h>
#include <android/log.h>
#include "service/WebpAnimationConverter.h"

extern "C" {
#include "libswresample/swresample.h"
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
#include "libavutil/avutil.h"
#include "decode.h"
#include "encode.h"
#include "mux.h"
}

#define LOG_TAG_JNI "NativeStickerConverter"
#define LOG_TAG_FFMPEG "NativeFFmpeg"

#define LOGEJ(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG_JNI, __VA_ARGS__)
#define LOGEF(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG_FFMPEG, __VA_ARGS__)

#define LOGIJ(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_JNI, __VA_ARGS__)
#define LOGIF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_FFMPEG, __VA_ARGS__)

#define  LOGDJ(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_JNI, __VA_ARGS__)
#define  LOGDF(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_FFMPEG, __VA_ARGS__)

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
        LOGEJ("Falha ao alocar AVFrame");
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

    [[nodiscard]] const char *get() const { return cstr; }
};

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_vinicius_sticker_domain_libs_NativeConvertToWebp_convertToWebp(JNIEnv *env,
                                                                        jobject /* this */,
                                                                        jstring inputPath,
                                                                        jstring outputPath) {
    // Armazena o input e output paths
    JniString inPath(env, inputPath);
    JniString outPath(env, outputPath);

    jclass nativeMediaException = env->FindClass(
            "com/vinicius/sticker/core/exception/NativeConversionException");

    if (nativeMediaException == nullptr) {
        jclass fallback = env->FindClass("java/lang/RuntimeException");

        if (fallback != nullptr) {
            env->ThrowNew(fallback, "Classe NativeConversionException não encontrada");
            return JNI_FALSE;
        } else {
            // Caso nem a Runtime seja encontrada
            env->FatalError("Falha crítica: nenhuma classe de exceção encontrada");
            return JNI_FALSE;
        }
    }

    if (!inPath.get() || !outPath.get()) {
        std::string msgError = fmt::format("Caminhos de entrada ou saída inválidos");

        LOGEJ("%s", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

        return JNI_FALSE;
    }

    AVFormatContext *formatContextRaw = nullptr;
    if (avformat_open_input(&formatContextRaw, inPath.get(), nullptr, nullptr) != 0) {
        std::string msgError = fmt::format("Erro ao abrir arquivo: {}", inPath.get());

        LOGEF("%s", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

        return JNI_FALSE;
    }

    AVFormatContextPtr formatContext(formatContextRaw);
    if (avformat_find_stream_info(formatContext.get(), nullptr) < 0) {
        std::string msgError = fmt::format("Erro ao encontrar informações do stream em: {}",
                                           inPath.get());

        LOGEF("%s", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

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
        std::string msgError = fmt::format("Nenhum stream encontrado no vídeo no arquivo: {}",
                                           inPath.get());

        LOGEF("%s ", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

        return JNI_FALSE;
    }

    AVStream *videoStream = formatContext->streams[videoStreamIndex];
    const AVCodec *codec = avcodec_find_decoder(videoStream->codecpar->codec_id);
    if (!codec) {
        std::string msgError = fmt::format("Nenhum codec encontrado para o stream de vídeo");

        LOGEF("%s ", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

        return JNI_FALSE;
    }

    AVCodecContextPtr codecContext(avcodec_alloc_context3(codec));
    if (!codecContext) {
        std::string msgError = fmt::format("Não foi possível alocar o contexto do codec");

        LOGEF("%s ", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

        return JNI_FALSE;
    }

    if (avcodec_parameters_to_context(codecContext.get(), videoStream->codecpar) < 0) {
        std::string msgError = fmt::format("Erro ao configurar o contexto do codec");

        LOGEF("%s ", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

        return JNI_FALSE;
    }

    int ret = avcodec_open2(codecContext.get(), codec, nullptr);
    if (ret < 0) {
        char errBuf[128];
        av_strerror(ret, errBuf, sizeof(errBuf));

        std::string msgError = fmt::format("Erro ao abrir o codec: {}", errBuf);

        LOGEF("%s ", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

        return JNI_FALSE;
    }

    AVFramePtr frame = create_av_frame();
    AVFramePtr rgbFrame = create_av_frame();
    if (!frame || !rgbFrame) {
        std::string msgError = fmt::format("Erro ao alocar frames");

        LOGEF("%s ", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

        return JNI_FALSE;
    }

    AVRational framerate = av_guess_frame_rate(formatContext.get(), videoStream, nullptr);
    double fpsOriginal = av_q2d(framerate);
    int frameInterval = std::max(1, static_cast<int>(fpsOriginal / 10.0 + 0.5));
    int frameCount = 0;

    int width = codecContext->width;
    int height = codecContext->height;
    SwsContextPtr swsContext(
            sws_getContext(width, height, codecContext->pix_fmt, width, height, AV_PIX_FMT_RGB24,
                           SWS_BILINEAR, nullptr, nullptr, nullptr));
    if (!swsContext) {
        std::string msgError = fmt::format("Erro ao criar o contexto de redimensionamento");

        LOGEF("%s ", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

        return JNI_FALSE;
    }

    AVBufferPtr buffer(av_malloc(av_image_get_buffer_size(AV_PIX_FMT_RGB24, width, height, 1)));
    if (!buffer) {
        std::string msgError = fmt::format("Erro ao alocar buffer RGB");

        LOGEF("%s ", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

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

            // Pega os primeiros 5 segundos de vídeo
            // TODO: Validar videos de -5s
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

                std::string msgError = fmt::format("Erro ao enviar pacote para o codec: {}",
                                                   errBuf);

                LOGEF("%s ", msgError.c_str());
                env->ThrowNew(nativeMediaException, msgError.c_str());

                av_packet_unref(&packet);
                break;
            }

            while (ret >= 0) {
                ret = avcodec_receive_frame(codecContext.get(), frame.get());

                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
                    break;
                }

                if (ret == 0) {
                    LOGIF("Frame decodificado: formato=%d, width=%d, height=%d, linesize[0]=%d",
                          frame->format, frame->width, frame->height, frame->linesize[0]);
                }

                if (ret < 0) {
                    char errBuf[128];
                    av_strerror(ret, errBuf, sizeof(errBuf));

                    std::string msgError = fmt::format("Erro ao receber o quadro decodificado: {}",
                                                       errBuf);

                    LOGEF("%s ", msgError.c_str());
                    env->ThrowNew(nativeMediaException, msgError.c_str());

                    break;
                }

                sws_scale(swsContext.get(), frame->data, frame->linesize,
                          0, height, rgbFrame->data, rgbFrame->linesize);

                if (frameCount % frameInterval == 0) {
                    FrameWithBuffer clone;
                    clone.frame = create_av_frame();
                    if (!clone.frame) {
                        LOGIF("Erro ao alocar frame clone");
                        continue;
                    }

                    clone.frame->format = AV_PIX_FMT_RGB24;
                    clone.frame->width = width;
                    clone.frame->height = height;

                    clone.buffer.reset(av_malloc(
                            av_image_get_buffer_size(AV_PIX_FMT_RGB24, width, height, 1)));

                    if (!clone.buffer) {
                        LOGIF("Erro ao alocar buffer clone");
                        continue;
                    }

                    av_image_fill_arrays(clone.frame->data, clone.frame->linesize,
                                         static_cast<uint8_t *>(clone.buffer.get()),
                                         AV_PIX_FMT_RGB24, width, height, 1);

                    av_image_copy(clone.frame->data, clone.frame->linesize,
                                  (const uint8_t **) rgbFrame->data, rgbFrame->linesize,
                                  AV_PIX_FMT_RGB24, width, height);

                    if (av_frame_copy_props(clone.frame.get(), frame.get()) != 0) {
                        LOGIJ("Erro ao copiar propriedades do frame");
                        continue;
                    }

                    frames.push_back(std::move(clone));
                    LOGDF("Frame %zu adicionado à lista de animação", frames.size());
                }

                frameCount++;
            }
        }
        av_packet_unref(&packet);
    }

    if (!frames.empty()) {
        int durationMs = 100;
        LOGDF("Gerando animação com %zu frames...", frames.size());

        if (frames.size() < 2) {
            LOGIF("Apenas %zu frame(s) capturado(s) — a animação pode parecer estática",
                  frames.size());
        }

        int result = WebpAnimationConverter::convertToWebp(env, outPath.get(), frames, width, height, durationMs);

        if (!result) {
            std::string msgError = fmt::format("Falha ao criar a animação WebP");

            LOGEF("%s ", msgError.c_str());
            env->ThrowNew(nativeMediaException, msgError.c_str());

            return JNI_FALSE;
        }
        LOGDF("Animação WebP criada com sucesso: %s", outPath.get());
    } else {
        std::string msgError = fmt::format("Nenhum frame capturado para criar a animação");

        LOGEF("%s ", msgError.c_str());
        env->ThrowNew(nativeMediaException, msgError.c_str());

        return JNI_FALSE;
    }

    return JNI_TRUE;
}