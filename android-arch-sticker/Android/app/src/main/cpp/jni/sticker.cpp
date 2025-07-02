/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


#include <jni.h>
#include <cmath>
#include <vector>
#include <memory>
#include <string>
#include <base.h>
#include <format.h>
#include <android/log.h>

#include "exception/HandlerJavaException.hpp"
#include "service/WebpAnimationConverter.hpp"
#include "service/ProcessFramesToFormat.hpp"
#include "service/ProcessWebpToAvFrames.hpp."

#include "raii/AVFrameDeleter.hpp"
#include "raii/AVBufferDeleter.hpp"
#include "raii/SwsContextDeleter.hpp"
#include "raii/AVCodecContextDeleter.hpp"
#include "raii/AVFormatContextDeleter.hpp"

extern "C" {
#include "mux.h"
#include "decode.h"
#include "encode.h"
#include "libavutil/frame.h"
#include "libavutil/avutil.h"
#include "libavutil/imgutils.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
}

#define LOG_TAG_RESIZE_CROP "NativeStickerConverter"
#define LOG_TAG_FFMPEG "NativeFFmpeg"

#define LOGIF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_FFMPEG, __VA_ARGS__)
#define  LOGDF(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_FFMPEG, __VA_ARGS__)

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
Java_br_arch_sticker_core_lib_NativeProcessWebp_convertToWebp(JNIEnv *env,
                                                              jobject /* this */,
                                                              jstring inputPath,
                                                              jstring outputPath) {
    jclass nativeMediaException = env->FindClass("br/arch/sticker/core/error/throwable/media/NativeConversionException");

    JniString inPath(env, inputPath);
    JniString outPath(env, outputPath);

    bool isWebP = false;
    int outputSize = 512;

    {
        FILE *file = fopen(inPath.get(), "rb");
        if (file) {
            uint8_t header[12];
            fread(header, 1, 12, file);
            fclose(file);
            if (memcmp(header, "RIFF", 4) == 0 && memcmp(header + 8, "WEBP", 4) == 0) {
                isWebP = true;
            }
        }
    }

    if (isWebP) {
        std::vector<FrameWithBuffer> vFramesWithBuffer;

        if (!ProcessWebpToAvFrames::decodeWebPAsAVFrames(env, inPath.get(), vFramesWithBuffer, outputSize, outputSize)) {
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Erro ao processar arquivo WebP");
            return JNI_FALSE;
        }

        if (vFramesWithBuffer.empty()) {
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Nenhum frame extraído do WebP");
            return JNI_FALSE;
        }

        int durationMs = 100;
        int result = WebpAnimationConverter::convertToWebp(env, outPath.get(), vFramesWithBuffer, outputSize, outputSize, durationMs);
        return result ? JNI_TRUE : JNI_FALSE;
    }

    if (!inPath.get() || !outPath.get()) {
        std::string msgError = fmt::format("Caminhos de entrada ou saída inválidos");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return JNI_FALSE;
    }

    AVFormatContext *formatContextRaw = nullptr;
    if (avformat_open_input(&formatContextRaw, inPath.get(), nullptr, nullptr) != 0) {
        std::string msgError = fmt::format("Erro ao abrir arquivo: {}", inPath.get());
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return JNI_FALSE;
    }

    AVFormatContextPtr formatContext(formatContextRaw);
    if (avformat_find_stream_info(formatContext.get(), nullptr) < 0) {
        std::string msgError = fmt::format("Erro ao encontrar informações do stream em: {}",
                                           inPath.get());
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

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
        std::string msgError = fmt::format("Nenhum stream encontrado no vídeo no arquivo: {}", inPath.get());
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return JNI_FALSE;
    }

    AVStream *videoStream = formatContext->streams[videoStreamIndex];
    const AVCodec *codec = avcodec_find_decoder(videoStream->codecpar->codec_id);
    if (!codec) {
        std::string msgError = fmt::format("Nenhum codec encontrado para o stream de vídeo");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return JNI_FALSE;
    }

    AVCodecContextPtr codecContext(avcodec_alloc_context3(codec));
    if (!codecContext) {
        std::string msgError = fmt::format("Não foi possível alocar o contexto do codec");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return JNI_FALSE;
    }

    if (avcodec_parameters_to_context(codecContext.get(), videoStream->codecpar) < 0) {
        std::string msgError = fmt::format("Erro ao configurar o contexto do codec");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return JNI_FALSE;
    }

    int retAvCodec = avcodec_open2(codecContext.get(), codec, nullptr);
    if (retAvCodec < 0) {
        char errBuf[128];
        av_strerror(retAvCodec, errBuf, sizeof(errBuf));

        std::string msgError = fmt::format("Erro ao abrir o codec: {}", errBuf);
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return JNI_FALSE;
    }

    AVFramePtr rgbFrame = ProcessFramesToFormat::createAvFrame(env, nativeMediaException, 512, 512, AV_PIX_FMT_RGB24);
    if (!rgbFrame) {
        std::string msgError = fmt::format("Erro ao alocar frame RGB");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);
        return JNI_FALSE;
    }

    AVRational frameRate = av_guess_frame_rate(formatContext.get(), videoStream, nullptr);
    double fpsOriginal = av_q2d(frameRate);
    int frameInterval = std::max(1, static_cast<int>(std::lround(fpsOriginal / 10.0)));
    int frameCount = 0;

    int width = codecContext->width;
    int height = codecContext->height;

    SwsContextPtr swsContextPtr(
            sws_getContext(width, height, codecContext->pix_fmt,
                           width, height, AV_PIX_FMT_RGB24,
                           SWS_BILINEAR, nullptr, nullptr, nullptr)
    );
    if (!swsContextPtr) {
        std::string msgError = fmt::format("Erro ao criar o contexto de redimensionamento");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return JNI_FALSE;
    }

    AVPacket *packet = av_packet_alloc();
    if (!packet) {
        std::string msgError = "Erro ao alocar packet";
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return JNI_FALSE;
    }

    packet->data = nullptr;
    packet->size = 0;

    std::vector<FrameWithBuffer> vFramesWithBuffer;
    while (av_read_frame(formatContext.get(), packet) >= 0) {
        if (packet->stream_index == videoStreamIndex) {

            // Pega os primeiros 5 segundos de vídeo
            if (packet->pts != AV_NOPTS_VALUE) {
                double seconds = static_cast<double>(packet->pts) * av_q2d(videoStream->time_base);

                if (seconds > 5.0) {
                    av_packet_unref(packet);
                    break;
                }
            }

            retAvCodec = avcodec_send_packet(codecContext.get(), packet);
            if (retAvCodec < 0) {
                char errBuf[128];
                av_strerror(retAvCodec, errBuf, sizeof(errBuf));

                std::string msgError = fmt::format("Erro ao enviar pacote para o codec: {}", errBuf);
                HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

                av_packet_unref(packet);
                break;
            }

            while (true) {
                retAvCodec = avcodec_receive_frame(codecContext.get(), rgbFrame.get());
                if (retAvCodec == AVERROR(EAGAIN) || retAvCodec == AVERROR_EOF) {
                    break;
                }

                if (retAvCodec == 0) {
                    LOGIF("Frame decodificado: formato=%d, width=%d, height=%d, linesize[0]=%d",
                          rgbFrame->format, rgbFrame->width, rgbFrame->height, rgbFrame->linesize[0]);
                }

                if (retAvCodec < 0) {
                    char errBuf[128];
                    av_strerror(retAvCodec, errBuf, sizeof(errBuf));

                    std::string msgError = fmt::format("Erro ao receber o quadro decodificado: {}", errBuf);
                    HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

                    break;
                }

                if (frameCount % frameInterval == 0) {
                    ProcessFramesToFormat::processFrame(env, nativeMediaException, rgbFrame, outputSize, outputSize, vFramesWithBuffer);
                }

                frameCount++;
            }
        }

        av_packet_unref(packet);
    }

    if (!vFramesWithBuffer.empty()) {
        int durationMs = 100;
        LOGDF("Gerando animação com %zu vFrameBuffer...", vFramesWithBuffer.size());

        if (vFramesWithBuffer.size() < 2) {
            LOGIF("Apenas %zu frame(s) capturado(s) — a animação pode parecer estática",
                  vFramesWithBuffer.size());
        }

        int result = WebpAnimationConverter::convertToWebp(env, outPath.get(), vFramesWithBuffer, outputSize, outputSize, durationMs);

        if (!result) {
            std::string msgError = fmt::format("Falha ao criar a animação WebP");
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

            return JNI_FALSE;
        }
        LOGDF("Animação WebP criada com sucesso: %s", outPath.get());
    } else {
        std::string msgError = fmt::format("Nenhum frame capturado para criar a animação");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return JNI_FALSE;
    }

    return JNI_TRUE;
}