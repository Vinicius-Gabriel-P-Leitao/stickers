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

extern "C" {
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
#include <libavutil/frame.h>
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
    buffer.reset(av_malloc(bufferSize));
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

    int srcWidth = frame->width;
    int srcHeight = frame->height;

    int cropWidth = std::min(targetWidth, srcWidth);
    int cropHeight = std::min(targetHeight, srcHeight);

    int cropX = (srcWidth - cropWidth) / 2;
    int cropY = (srcHeight - cropHeight) / 2;
    if (cropX < 0) cropX = 0;
    if (cropY < 0) cropY = 0;

    SwsContext *swsCtx = sws_getContext(
            srcWidth, srcHeight, (AVPixelFormat) frame->format,
            cropWidth, cropHeight, AV_PIX_FMT_RGB24,
            SWS_BILINEAR, nullptr, nullptr, nullptr);

    if (!swsCtx) {
        LOGIRCF("Erro ao criar SwsContext");
        return false;
    }

    if (!dstFrame.allocate(env, exClass, cropWidth, cropHeight, AV_PIX_FMT_RGB24)) {
        sws_freeContext(swsCtx);
        return false;
    }

    const uint8_t *srcSlice[AV_NUM_DATA_POINTERS] = {nullptr};
    int srcStride[AV_NUM_DATA_POINTERS] = {0};
    int pixelSize = av_get_bits_per_pixel(av_pix_fmt_desc_get((AVPixelFormat) frame->format)) / 8;

    for (int i = 0; i < AV_NUM_DATA_POINTERS && frame->data[i]; ++i) {
        srcSlice[i] = frame->data[i] + cropY * frame->linesize[i] + cropX * pixelSize;
        srcStride[i] = frame->linesize[i];
    }

    sws_scale(swsCtx, srcSlice, srcStride, 0,
              cropHeight, dstFrame.frame->data, dstFrame.frame->linesize);
    sws_freeContext(swsCtx);

    if (av_frame_copy_props(dstFrame.frame.get(), frame) != 0) {
        LOGIRCF("Erro ao copiar propriedades do frame");
        return false;
    }

    return true;
}

void ProcessFramesToFormat::processFrame(JNIEnv *env, jclass exClass, AVFramePtr &rgbFrame, int width, int height, std::vector<FrameWithBuffer> &frames) {
    FrameWithBuffer frameWithBuffer;

    if (!cropFrame(env, exClass, rgbFrame, frameWithBuffer, width, height)) {
        LOGIRCF("Erro ao redimensionar/cortar o frame");
        return;
    }

    frames.push_back(std::move(frameWithBuffer));
    LOGIRCF("Frame %zu adicionado à lista de animação", frames.size());
}
