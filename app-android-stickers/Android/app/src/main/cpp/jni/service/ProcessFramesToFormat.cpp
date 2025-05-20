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
#include <memory>
#include <vector>
#include <format.h>
#include <android/log.h>
#include "ProcessFramesToFormat.h"

#include "../exception/HandlerJavaException.h"

#include "../raii/AVFrameDeleter.h"
#include "../raii/AVBufferDeleter.h"
#include "../raii/SwsContextDeleter.h"

extern "C" {
#include <libavutil/frame.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
}

#define LOG_TAG_RESIZE_CROP "ProcessFramesToFormat"

#define LOGIRCF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_RESIZE_CROP, __VA_ARGS__)

AVFramePtr ProcessFramesToFormat::createAvFrame(JNIEnv *env, jclass exClass, int width, int height, AVPixelFormat format) {
    AVFramePtr frame(av_frame_alloc());
    if (!frame) {
        std::string msgError = fmt::format("Falha ao alocar AVFramePtr");
        HandlerJavaException::throwNativeConversionException(env, exClass, msgError);

        return nullptr;
    }

    frame->format = format;
    frame->width = width;
    frame->height = height;

    return frame;
}

bool FrameWithBuffer::allocate(JNIEnv *env, jclass exClass, int width, int height, AVPixelFormat format) {
    int bufferSize = av_image_get_buffer_size(format, width, height, 1);
    buffer.reset(reinterpret_cast<uint8_t *>(av_malloc(bufferSize)));

    if (!buffer) {
        LOGIRCF("Erro ao alocar buffer");
        return false;
    }

    frame = ProcessFramesToFormat::createAvFrame(env, exClass, width, height, format);
    if (!frame) {
        LOGIRCF("Erro ao alocar AVFrame");
        return false;
    }

    av_image_fill_arrays(
            frame->data, frame->linesize,
            static_cast<uint8_t *>(buffer.get()),
            format, width, height, 1);

    return true;
}

bool ProcessFramesToFormat::cropFrame(JNIEnv *env, jclass exClass, const AVFramePtr &srcFrame,
                                      FrameWithBuffer &dstFrame, int targetWidth, int targetHeight) {
    const AVFrame *frame = srcFrame.get();
    if (!frame) {
        std::string msgError = "Falha ao alocar AVFrame ou os dados são nulos";
        HandlerJavaException::throwNativeConversionException(env, exClass, msgError);

        return false;
    }

    int srcWidth = frame->width;
    int srcHeight = frame->height;
    auto srcFormat = static_cast<AVPixelFormat>(frame->format);

    if (srcWidth <= 0 || srcHeight <= 0 || srcFormat == AV_PIX_FMT_NONE) {
        std::string msgError = fmt::format("Dimensões do quadro de origem ({}x{}) ou formato ({}) inválidos",
                                           srcWidth, srcHeight, static_cast<int>(srcFormat));

        LOGIRCF("%s", msgError.c_str());
        HandlerJavaException::throwNativeConversionException(env, exClass, "Dimensões ou formato do quadro de origem inválidos");

        return false;
    }

    if (!dstFrame.allocate(env, exClass, targetWidth, targetHeight, AV_PIX_FMT_RGB24)) {
        LOGIRCF("Falha ao alocar o quadro de destino");
        return false;
    }

    // Preenche o frame com pixel preto
    for (int orderedY = 0; orderedY < targetHeight; ++orderedY) {
        uint8_t *row = dstFrame.frame->data[0] + orderedY * dstFrame.frame->linesize[0];
        std::fill(row, row + targetWidth * 3, 0);
    }

    double srcAspect = static_cast<double>(srcWidth) / srcHeight;
    double dstAspect = static_cast<double>(targetWidth) / targetHeight;
    int scaledWidth, scaledHeight;

    if (srcAspect > dstAspect) {
        scaledHeight = targetHeight;
        scaledWidth = static_cast<int>(targetHeight * srcAspect);
    } else {
        scaledWidth = targetWidth;
        scaledHeight = static_cast<int>(targetWidth / srcAspect);
    }

    SwsContextPtr swsCtx(sws_getContext(
            srcWidth, srcHeight, srcFormat,
            scaledWidth, scaledHeight, AV_PIX_FMT_RGB24,
            SWS_BILINEAR, nullptr, nullptr, nullptr));

    if (!swsCtx) {
        std::string msgError = fmt::format("Falha ao criar SwsContext para %dx%d (%s) para %dx%d (RGB24)",
                                           srcWidth, srcHeight, av_get_pix_fmt_name(srcFormat), scaledWidth, scaledHeight);

        LOGIRCF("%s", msgError.c_str());
        HandlerJavaException::throwNativeConversionException(env, exClass, "Falha ao criar contexto de dimensionamento");

        return false;
    }

    // Buffer temporário
    int bufferSize = av_image_get_buffer_size(AV_PIX_FMT_RGB24, scaledWidth, scaledHeight, 1);
    AVBufferPtr tempData(reinterpret_cast<uint8_t *>(av_malloc(bufferSize)));
    if (!tempData) {
        std::string msgError = fmt::format("Falha ao alocar buffer temporário ({} bytes)", bufferSize);

        LOGIRCF("%s", msgError.c_str());
        HandlerJavaException::throwNativeConversionException(env, exClass, "Falha ao alocar buffer temporário");

        return false;
    }

    uint8_t *tempDataPtr[AV_NUM_DATA_POINTERS] = {nullptr};
    int tempLinesize[AV_NUM_DATA_POINTERS] = {0};
    if (av_image_fill_arrays(tempDataPtr, tempLinesize, tempData.get(), AV_PIX_FMT_RGB24, scaledWidth, scaledHeight, 1) < 0) {
        std::string msgError = "Falha ao preencher ponteiros do av_image_fill_arrays";
        HandlerJavaException::throwNativeConversionException(env, exClass, msgError);

        return false;
    }

    if (sws_scale(swsCtx.get(), frame->data, frame->linesize, 0, srcHeight, tempDataPtr, tempLinesize) <= 0) {
        std::string msgError = "Falha ao dimensionar o frame";
        HandlerJavaException::throwNativeConversionException(env, exClass, msgError);

        return false;
    }

    int cropX = (scaledWidth - targetWidth) / 2;
    int cropY = (scaledHeight - targetHeight) / 2;

    for (int orderedY = 0; orderedY < targetHeight; ++orderedY) {
        int srcY = orderedY + cropY;
        if (srcY < 0 || srcY >= scaledHeight) continue;

        uint8_t *dst = dstFrame.frame->data[0] + orderedY * dstFrame.frame->linesize[0];
        uint8_t *src = tempData.get() + srcY * tempLinesize[0] + cropX * 3;

        int copyWidth = std::min(targetWidth, scaledWidth - cropX);
        if (copyWidth > 0) {
            memcpy(dst, src, copyWidth * 3);
        }
    }

    // Copia as propriedades do frame
    if (av_frame_copy_props(dstFrame.frame.get(), frame) != 0) {
        std::string msgError = "Falha ao copiar propriedades do frame";
        HandlerJavaException::throwNativeConversionException(env, exClass, msgError);

        return false;
    }

    return true;
}

void
ProcessFramesToFormat::processFrame(JNIEnv *env, jclass exClass, AVFramePtr &rgbFrame, int width, int height, std::vector<FrameWithBuffer> &frames) {
    FrameWithBuffer frameWithBuffer;

    if (!cropFrame(env, exClass, rgbFrame, frameWithBuffer, width, height)) {
        std::string msgError = "Erro ao redimensionar/cortar o frame";
        HandlerJavaException::throwNativeConversionException(env, exClass, msgError);

        return;
    }

    LOGIRCF("Frame %zu adicionado à lista de animação", frames.size());
    frames.push_back(std::move(frameWithBuffer));
}
