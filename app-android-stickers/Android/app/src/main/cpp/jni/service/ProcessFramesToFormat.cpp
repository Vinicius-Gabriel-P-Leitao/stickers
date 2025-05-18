//
// Created by vinicius on 17/05/2025.
//

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
        std::string msgError = fmt::format("Falha ao alocar AVFrame ou os dados são nulos");
        HandlerJavaException::throwNativeConversionException(env, exClass, msgError);

        return false;
    }

    int srcWidth = frame->width;
    int srcHeight = frame->height;
    auto srcFormat = static_cast<AVPixelFormat>(frame->format);

    if (srcWidth <= 0 || srcHeight <= 0 || srcFormat == AV_PIX_FMT_NONE) {
        LOGIRCF("Dimensões do quadro de origem (%dx%d) ou formato (%d) inválidos", srcWidth, srcHeight, srcFormat);
        HandlerJavaException::throwNativeConversionException(env, exClass, "Dimensões ou formato do quadro de origem inválidos");

        return false;
    }

    if (!dstFrame.allocate(env, exClass, targetWidth, targetHeight, AV_PIX_FMT_RGB24)) {
        LOGIRCF("Falha ao alocar o quadro de destino");
        return false;
    }
    
    constexpr int BytesPerPixel = 3;

    // Preenche o frame com pixel preto
    if (dstFrame.frame && dstFrame.frame->data[0]) {
        for (int orderedY = 0; orderedY < targetHeight; ++orderedY) {
            uint8_t *row = dstFrame.frame->data[0] + orderedY * dstFrame.frame->linesize[0];
            std::fill(row, row + targetWidth * BytesPerPixel, 0);
        }
    } else {
        LOGIRCF("dstFrame.frame or dstFrame.frame->tempDataPtr[0] É nulo após tentar preecher com pixel preto.");
        return false;
    }

    double srcAspect = (double) srcWidth / srcHeight;
    double dstAspect = (double) targetWidth / targetHeight;

    int scaledWidth, scaledHeight;
    if (srcAspect > dstAspect) {
        scaledWidth = targetWidth;
        scaledHeight = static_cast<int>(targetWidth / srcAspect);
    } else {
        scaledHeight = targetHeight;
        scaledWidth = static_cast<int>(targetHeight * srcAspect);
    }

    SwsContextPtr swsCtx(sws_getContext(
            srcWidth, srcHeight, srcFormat,
            scaledWidth, scaledHeight, AV_PIX_FMT_RGB24,
            SWS_BILINEAR, nullptr, nullptr, nullptr));

    if (!swsCtx) {
        LOGIRCF("Erro ao criar SwsContext");
        return false;
    }

    AVBufferPtr tempData(reinterpret_cast<uint8_t *>(av_malloc(scaledWidth * scaledHeight * 3)));
    if (!tempData) {
        std::string msgError = "Erro ao alocar buffer temporário para redimensionamento";
        HandlerJavaException::throwNativeConversionException(env, exClass, msgError);

        return false;
    }

    uint8_t *tempDataPtr[AV_NUM_DATA_POINTERS] = {nullptr};
    int tempLinesize[AV_NUM_DATA_POINTERS] = {0};

    av_image_fill_arrays(tempDataPtr, tempLinesize, tempData.get(), AV_PIX_FMT_RGB24, scaledWidth, scaledHeight, 1);
    sws_scale(swsCtx.get(), frame->data, frame->linesize, 0, srcHeight, tempDataPtr, tempLinesize);

    // Centraliza o frame
    int offsetX = (targetWidth - scaledWidth) / 2;
    int offsetY = (targetHeight - scaledHeight) / 2;

    for (int orderedY = 0; orderedY < scaledHeight; ++orderedY) {
        uint8_t *dst = dstFrame.frame->data[0] + (offsetY + orderedY) * dstFrame.frame->linesize[0] + offsetX * 3;
        uint8_t *src = tempData.get() + orderedY * tempLinesize[0];
        memcpy(dst, src, scaledWidth * 3);
    }

    if (av_frame_copy_props(dstFrame.frame.get(), frame) != 0) {
        LOGIRCF("Erro ao copiar propriedades do frame");
        return false;
    }

    return true;
}

void
ProcessFramesToFormat::processFrame(JNIEnv *env, jclass exClass, AVFramePtr &rgbFrame, int width, int height, std::vector<FrameWithBuffer> &frames) {
    FrameWithBuffer frameWithBuffer;

    if (!cropFrame(env, exClass, rgbFrame, frameWithBuffer, width, height)) {
        LOGIRCF("Erro ao redimensionar/cortar o frame");
        return;
    }

    LOGIRCF("Frame %zu adicionado à lista de animação", frames.size());
    frames.push_back(std::move(frameWithBuffer));
}
