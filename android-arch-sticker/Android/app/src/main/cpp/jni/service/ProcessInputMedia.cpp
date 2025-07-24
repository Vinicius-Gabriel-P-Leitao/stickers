/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

#include "ProcessInputMedia.hpp"

#include <jni.h>
#include <cmath>
#include <vector>
#include <memory>
#include <string>
#include <base.h>
#include <format.h>
#include <android/log.h>

#include "../service/ProcessFramesToFormat.hpp"

#include "../raii/AVFrameDestroyer.hpp"
#include "../raii/AVBufferDestroyer.hpp"
#include "../raii/SwsContextDestroyer.hpp"
#include "../raii/AVCodecContextDestroyer.hpp"
#include "../raii/AVFormatContextDestroyer.hpp"

extern "C" {
#include "libavutil/frame.h"
#include "libavutil/avutil.h"
#include "libavutil/imgutils.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
}

#define LOG_TAG_RESIZE_CROP "ProcessInputMedia"
#define LOG_TAG_FFMPEG "NativeFFmpeg"

#define LOGIF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_FFMPEG, __VA_ARGS__)
#define LOGDF(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_FFMPEG, __VA_ARGS__)

#define DURATION_MS  100
#define OUTPUT_SIZE  512

ProcessInputMedia::ProcessInputMedia(JNIEnv *env, jclass nativeMediaException) : env(env), nativeMediaException(nativeMediaException) {}

std::vector<FrameWithBuffer> ProcessInputMedia::processVideoFrames(
        const char *inPath, const char *outPath, const FrameProcessor &frameProcessor, const ParamsMap &params) {

    if (!inPath || !outPath) {
        std::string msgError = fmt::format("Caminhos de entrada ou saída inválidos");
        throw std::runtime_error(msgError);
    }

    AVFormatContext *formatContextRaw = nullptr;
    if (avformat_open_input(&formatContextRaw, inPath, nullptr, nullptr) != 0) {
        std::string msgError = fmt::format("Erro ao abrir arquivo: {}", inPath);
        throw std::runtime_error(msgError);
    }

    AVFormatContextPtr formatContext(formatContextRaw);
    if (avformat_find_stream_info(formatContext.get(), nullptr) < 0) {
        std::string msgError = fmt::format("Erro ao encontrar informações do stream em: {}", inPath);
        throw std::runtime_error(msgError);
    }

    int videoStreamIndex = -1;
    for (int counter = 0; counter < formatContext->nb_streams; ++counter) {
        if (formatContext->streams[counter]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoStreamIndex = counter;
            break;
        }
    }

    if (videoStreamIndex == -1) {
        std::string msgError = fmt::format("Nenhum stream encontrado no vídeo no arquivo: {}", inPath);
        throw std::runtime_error(msgError);
    }

    AVStream *videoStream = formatContext->streams[videoStreamIndex];
    const AVCodec *codec = avcodec_find_decoder(videoStream->codecpar->codec_id);
    if (!codec) {
        std::string msgError = "Nenhum codec encontrado para o stream de vídeo";
        throw std::runtime_error(msgError);
    }

    AVCodecContextPtr codecContext(avcodec_alloc_context3(codec));
    if (!codecContext) {
        std::string msgError = "Não foi possível alocar o contexto do codec";
        throw std::runtime_error(msgError);
    }

    if (avcodec_parameters_to_context(codecContext.get(), videoStream->codecpar) < 0) {
        std::string msgError = "Erro ao configurar o contexto do codec";
        throw std::runtime_error(msgError);
    }

    int ret = avcodec_open2(codecContext.get(), codec, nullptr);
    if (ret < 0) {
        char errBuf[128];
        av_strerror(ret, errBuf, sizeof(errBuf));
        std::string msgError = fmt::format("Erro ao abrir o codec: {}", errBuf);
        throw std::runtime_error(msgError);
    }

    ProcessFramesToFormat processFramesToFormat(env, nativeMediaException);
    AVFramePtr rgbFrame = processFramesToFormat.createAvFrame(512, 512, AV_PIX_FMT_RGB24);
    if (!rgbFrame) {
        std::string msgError = "Erro ao alocar frame RGB";
        throw std::runtime_error(msgError);
    }

    AVRational frameRate = av_guess_frame_rate(formatContext.get(), videoStream, nullptr);
    double fpsOriginal = av_q2d(frameRate);
    int frameInterval = std::max(1, static_cast<int>(std::lround(fpsOriginal / 10.0)));
    int frameCount = 0;

    int width = codecContext->width;
    int height = codecContext->height;

    SwsContextPtr swsContextPtr(
            sws_getContext(
                    width, height, codecContext->pix_fmt,
                    width, height, AV_PIX_FMT_RGB24,
                    SWS_BILINEAR, nullptr, nullptr, nullptr)
    );
    if (!swsContextPtr) {
        std::string msgError = "Erro ao criar o contexto de redimensionamento";
        throw std::runtime_error(msgError);
    }

    AVPacket *packet = av_packet_alloc();
    if (!packet) {
        std::string msgError = "Erro ao alocar packet";
        throw std::runtime_error(msgError);
    }

    packet->data = nullptr;
    packet->size = 0;

    std::vector<FrameWithBuffer> vFramesWithBuffer;
    while (av_read_frame(formatContext.get(), packet) >= 0) {
        if (packet->stream_index == videoStreamIndex) {

            if (packet->pts != AV_NOPTS_VALUE) {
                double seconds = static_cast<double>(packet->pts) * av_q2d(videoStream->time_base);
                if (seconds > 5.0) {
                    av_packet_unref(packet);
                    break;
                }
            }

            ret = avcodec_send_packet(codecContext.get(), packet);
            if (ret < 0) {
                char errBuf[128];
                av_strerror(ret, errBuf, sizeof(errBuf));
                std::string msgError = fmt::format("Erro ao enviar pacote para o codec: {}", errBuf);
                throw std::runtime_error(msgError);
                av_packet_unref(packet);
                break;
            }

            while (true) {
                ret = avcodec_receive_frame(codecContext.get(), rgbFrame.get());
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
                    break;
                }
                if (ret == 0) {
                    LOGIF("Frame decodificado: formato=%d, width=%d, height=%d, linesize[0]=%d",
                          rgbFrame->format, rgbFrame->width, rgbFrame->height, rgbFrame->linesize[0]);
                }
                if (ret < 0) {
                    char errBuf[128];
                    av_strerror(ret, errBuf, sizeof(errBuf));
                    std::string msgError = fmt::format("Erro ao receber o quadro decodificado: {}", errBuf);
                    throw std::runtime_error(msgError);
                    break;
                }

                if (frameCount % frameInterval == 0) {
                    frameProcessor(this->env, this->nativeMediaException, rgbFrame, OUTPUT_SIZE, OUTPUT_SIZE, vFramesWithBuffer, params);
                }

                frameCount++;
            }
        }

        av_packet_unref(packet);
    }

    return vFramesWithBuffer;
}
