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
#include <iostream>
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

int CreateAnimatedWebP(const char *output_path,
                       std::vector<AVFrame *> frames,
                       int width, int height,
                       int duration_ms) {
    WebPAnimEncoderOptions enc_options;
    WebPAnimEncoderOptionsInit(&enc_options);

    if (!WebPAnimEncoderOptionsInit(&enc_options)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Falha ao inicializar WebPAnimEncoderOptions.");
        return 0;
    }

    WebPConfig config;
    if (!WebPConfigInit(&config)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Falha ao inicializar WebPConfig.");
        return 0;
    }
    config.lossless = 0;
    config.quality = 20;

    WebPAnimEncoder *encoder = WebPAnimEncoderNew(width, height, &enc_options);
    if (!encoder) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao criar WebPAnimEncoder.");
        return 0;
    }

    int timestamp_ms = 0;
    for (AVFrame *frame: frames) {
        __android_log_print(ANDROID_LOG_DEBUG, "WebP",
                            "Adicionando frame ao encoder, timestamp: %d ms", timestamp_ms);

        WebPPicture pic;
        WebPPictureInit(&pic);
        pic.width = width;
        pic.height = height;
        pic.use_argb = 1;

        if (!WebPPictureImportRGB(&pic, frame->data[0], frame->linesize[0])) {
            __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao importar dados RGB.");
            WebPPictureFree(&pic);
            WebPAnimEncoderDelete(encoder);
            return 0;
        }

        if (!WebPAnimEncoderAdd(encoder, &pic, timestamp_ms, &config)) {
            __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao adicionar frame.");
            WebPPictureFree(&pic);
            WebPAnimEncoderDelete(encoder);
            return 0;
        }

        WebPPictureFree(&pic);
        timestamp_ms += duration_ms;
    }

    WebPAnimEncoderAdd(encoder, nullptr, timestamp_ms, nullptr);

    WebPData webp_data;
    WebPDataInit(&webp_data);
    if (!WebPAnimEncoderAssemble(encoder, &webp_data)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao montar animação.");
        WebPAnimEncoderDelete(encoder);
        return 0;
    }

    FILE *pFile = fopen(output_path, "wb");
    if (!pFile) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao abrir arquivo de saída.");
        WebPDataClear(&webp_data);
        WebPAnimEncoderDelete(encoder);
        return 0;
    }

    fwrite(webp_data.bytes, 1, webp_data.size, pFile);
    fclose(pFile);
    __android_log_print(ANDROID_LOG_INFO, "WebP",
                        "Arquivo WebP salvo com sucesso em: %s (%zu bytes)", output_path,
                        webp_data.size);

    WebPDataClear(&webp_data);
    WebPAnimEncoderDelete(encoder);
    for (AVFrame *frame: frames) {
        if (frame) {
            av_freep(&frame->data[0]);
            av_frame_free(&frame);
        }
    }
    return 1;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_vinicius_sticker_domain_libs_ConvertToWebp_convertToWebp(JNIEnv *env,
                                                                  jobject /* this */,
                                                                  jstring inputPath,
                                                                  jstring outputPath) {
    const char *inPath = env->GetStringUTFChars(inputPath, nullptr);
    const char *outPath = env->GetStringUTFChars(outputPath, nullptr);

    AVCodecContext *codec_ctx = nullptr;
    AVFormatContext *fmt_ctx = nullptr;
    const AVCodec *codec = nullptr;
    SwsContext *sws_ctx = nullptr;
    AVFrame *rgb_frame = nullptr;
    AVFrame *frame = nullptr;

    if (avformat_open_input(&fmt_ctx, inPath, nullptr, nullptr) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Erro ao abrir arquivo: %s", inPath);
        return JNI_FALSE;
    }

    if (avformat_find_stream_info(fmt_ctx, nullptr) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                            "Erro ao encontrar informações do stream em: %s", inPath);
        avformat_close_input(&fmt_ctx);
        return JNI_FALSE;
    }

    int video_stream_index = -1;
    for (int i = 0; i < fmt_ctx->nb_streams; i++) {
        if (fmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_stream_index = i;
            break;
        }
    }

    if (video_stream_index == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Nenhum stream de vídeo encontrado.");
        avformat_close_input(&fmt_ctx);
        return JNI_FALSE;
    }

    AVStream *video_stream = fmt_ctx->streams[video_stream_index];
    codec = avcodec_find_decoder(video_stream->codecpar->codec_id);
    if (!codec) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Codec não encontrado.");
        avformat_close_input(&fmt_ctx);
        return JNI_FALSE;
    }

    codec_ctx = avcodec_alloc_context3(codec);
    if (!codec_ctx) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                            "Não foi possível alocar o contexto do codec.");
        avformat_close_input(&fmt_ctx);
        return JNI_FALSE;
    }

    if (avcodec_parameters_to_context(codec_ctx, video_stream->codecpar) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Erro ao configurar o contexto do codec.");
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        return JNI_FALSE;
    }

    if (avcodec_open2(codec_ctx, codec, nullptr) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Erro ao abrir o codec.");
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        return JNI_FALSE;
    }

    frame = av_frame_alloc();
    rgb_frame = av_frame_alloc();
    if (!frame || !rgb_frame) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg", "Erro ao alocar frames.");
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        return JNI_FALSE;
    }

    AVRational framerate = av_guess_frame_rate(fmt_ctx, video_stream, nullptr);
    double fps_original = av_q2d(framerate);
    int frame_interval = std::max(1, static_cast<int>(fps_original / 15.0 + 0.5));
    int frame_count = 0;

    int width = 512;
    int height = 512;
    sws_ctx = sws_getContext(width, height, codec_ctx->pix_fmt, width, height, AV_PIX_FMT_RGB24,
                             SWS_BILINEAR, nullptr, nullptr, nullptr);
    if (!sws_ctx) {
        __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                            "Erro ao criar o contexto de redimensionamento.");
        av_frame_free(&frame);
        av_frame_free(&rgb_frame);
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        return JNI_FALSE;
    }

    uint8_t *buffer = (uint8_t *) av_malloc(
            av_image_get_buffer_size(AV_PIX_FMT_RGB24, width, height, 1));
    av_image_fill_arrays(rgb_frame->data, rgb_frame->linesize, buffer, AV_PIX_FMT_RGB24, width,
                         height, 1);

    std::vector<AVFrame *> frames;

    AVPacket packet;
    while (av_read_frame(fmt_ctx, &packet) >= 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "FFmpeg", "Pacote lido, tamanho: %d", packet.size);

        if (packet.pts != AV_NOPTS_VALUE) {
            double seconds = packet.pts * av_q2d(fmt_ctx->streams[video_stream_index]->time_base);
            if (seconds > 5.0) {
                av_packet_unref(&packet);
                break;
            }
        }

        if (packet.stream_index == video_stream_index) {
            int ret = avcodec_send_packet(codec_ctx, &packet);

            if (packet.pts != AV_NOPTS_VALUE) {
                double seconds =
                        packet.pts * av_q2d(fmt_ctx->streams[video_stream_index]->time_base);
                if (seconds > 5.0) {
                    av_packet_unref(&packet);
                    break;
                }
            }

            if (ret < 0) {
                __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                                    "Erro ao enviar pacote para o codec.");
                break;
            }

            while (ret >= 0) {
                ret = avcodec_receive_frame(codec_ctx, frame);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
                    break;
                if (ret < 0) {
                    __android_log_print(ANDROID_LOG_ERROR, "FFmpeg",
                                        "Erro ao receber o quadro decodificado.");
                    break;
                }

                __android_log_print(ANDROID_LOG_DEBUG, "FFmpeg", "Frame decodificado: %dx%d",
                                    frame->width, frame->height);

                sws_scale(sws_ctx, frame->data, frame->linesize, 0, height, rgb_frame->data,
                          rgb_frame->linesize);

                __android_log_print(ANDROID_LOG_DEBUG, "FFmpeg",
                                    "Frame convertido para RGB e redimensionado.");


                AVFrame *clone = av_frame_alloc();
                if (!clone) continue;

                clone->format = AV_PIX_FMT_RGB24;
                clone->width = width;
                clone->height = height;

                int buf_size = av_image_get_buffer_size(AV_PIX_FMT_RGB24, width, height, 1);
                uint8_t *buf = (uint8_t *) av_malloc(buf_size);
                av_image_fill_arrays(clone->data, clone->linesize, buf, AV_PIX_FMT_RGB24, width,
                                     height, 1);

                av_image_copy(clone->data, clone->linesize,
                              (const uint8_t **) rgb_frame->data, rgb_frame->linesize,
                              AV_PIX_FMT_RGB24, width, height);

                __android_log_print(ANDROID_LOG_DEBUG, "FFmpeg",
                                    "Frame %zu adicionado à lista de animação.", frames.size());

                if (frame == nullptr || frame->data[0] == nullptr) {
                    __android_log_print(ANDROID_LOG_ERROR, "WebP",
                                        "Erro: Frame inválido detectado!");
                    continue;
                }

                if (frame_count % frame_interval == 0) {
                    if (av_frame_copy_props(clone, frame) != 0) {
                        __android_log_print(ANDROID_LOG_ERROR, "WebP",
                                            "Erro ao copiar propriedades do frame.");
                        continue;
                    }

                    frames.push_back(clone);
                } else {
                    av_frame_free(&clone);
                }

                frame_count++;
            }
        }

        av_packet_unref(&packet);
    }

    if (!frames.empty()) {
        int duration_ms = 100;
        __android_log_print(ANDROID_LOG_INFO, "WebP", "Gerando animação com %zu frames...",
                            frames.size());


        if (frames.size() < 2) {
            __android_log_print(ANDROID_LOG_WARN, "WebP",
                                "Apenas %zu frame(s) capturado(s) — a animação pode parecer estática.",
                                frames.size());
        }

        int result = CreateAnimatedWebP(outPath, frames, width, height, duration_ms);
        if (result) {
            __android_log_print(ANDROID_LOG_INFO, "WebP",
                                "Animação WebP criada com sucesso: %s",
                                outPath);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "WebP", "Falha ao criar a animação WebP.");
        }
    }

    sws_freeContext(sws_ctx);
    av_frame_free(&rgb_frame);
    av_frame_free(&frame);
    avcodec_free_context(&codec_ctx);
    avformat_close_input(&fmt_ctx);

    return JNI_TRUE;
}