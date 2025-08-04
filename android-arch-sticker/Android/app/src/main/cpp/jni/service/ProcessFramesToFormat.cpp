/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
#include "ProcessFramesToFormat.hpp"

#include <memory>
#include <vector>
#include <format.h>
#include <android/log.h>

#include "../raii/AVFrameDestroyer.hpp"
#include "../raii/AVBufferDestroyer.hpp"
#include "../raii/SwsContextDestroyer.hpp"

extern "C" {
#include <libavutil/frame.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
}

#define LOG_TAG_RESIZE_CROP "ProcessFramesToFormat"

#define LOGIRCF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_RESIZE_CROP, __VA_ARGS__)

ProcessFramesToFormat::ProcessFramesToFormat() = default;

bool FrameWithBuffer::allocateFrameWithBuffer(int width, int height, AVPixelFormat format) {
    int bufferSize = av_image_get_buffer_size(format, width, height, 1);
    buffer.reset(reinterpret_cast<uint8_t *>(av_malloc(bufferSize)));

    if (!buffer) {
        throw std::runtime_error("Erro ao alocar buffer");
    }

    frame = ProcessFramesToFormat::createAvFrame(width, height, format);
    if (!frame) {
        throw std::runtime_error("Erro ao alocar AVFrame");
    }

    av_image_fill_arrays(
            frame->data, frame->linesize,
            static_cast<uint8_t *>(buffer.get()),
            format, width, height, 1);

    return true;
}

AVFramePtr ProcessFramesToFormat::createAvFrame(int width, int height, AVPixelFormat format) {
    AVFramePtr frame(av_frame_alloc());
    if (!frame) {
        throw std::runtime_error("Falha ao alocar AVFramePtr");
    }

    frame->format = format;
    frame->width = width;
    frame->height = height;

    int ret = av_frame_get_buffer(frame.get(), 0);
    if (ret < 0) {
        char errBuf[128];
        av_strerror(ret, errBuf, sizeof(errBuf));
        throw std::runtime_error(fmt::format("Falha ao alocar buffer do AVFrame: {}", errBuf));
    }

    return frame;
}

void ProcessFramesToFormat::processFrame(AVFramePtr &rgbFrame, int cropX, int cropY, int width, int height,
                                         std::vector<FrameWithBuffer> &frames) {
    FrameWithBuffer frameWithBuffer;

    if (!cropFrame(rgbFrame, frameWithBuffer, cropX, cropY, width, height)) {
        throw std::runtime_error("Erro ao redimensionar/cortar o frame.");
    }

    LOGIRCF("%s", fmt::format("Frame {} adicionado à lista de animação", frames.size()).c_str());
    frames.push_back(std::move(frameWithBuffer));
}

bool ProcessFramesToFormat::cropFrame(const AVFramePtr &srcFrame, FrameWithBuffer &dstFrame, int cropX, int cropY,
                                      int cropWidth, int cropHeight) {
    const AVFrame *frame = srcFrame.get();
    if (!frame || !frame->data[0]) {
        throw std::runtime_error("Falha ao alocar AVFrame ou os dados são nulos.");
    }

    int srcWidth = frame->width;
    int srcHeight = frame->height;
    auto srcFormat = static_cast<AVPixelFormat>(frame->format);

    if (srcWidth <= 0 || srcHeight <= 0 || srcFormat != AV_PIX_FMT_RGB24) {
        throw std::runtime_error(fmt::format("Dimensões do quadro de origem ({}x{}) ou formato ({}) inválidos.",
                                             srcWidth, srcHeight, av_get_pix_fmt_name(srcFormat)));
    }

    int bufferSize = av_image_get_buffer_size(AV_PIX_FMT_RGB24, cropWidth, cropHeight, 1);
    AVBufferPtr tempData(reinterpret_cast<uint8_t *>(av_malloc(bufferSize)));
    if (!tempData) {
        throw std::runtime_error("Falha ao alocar buffer temporário.");
    }

    uint8_t *tempDataPtr[AV_NUM_DATA_POINTERS] = {nullptr};
    int tempLineSize[AV_NUM_DATA_POINTERS] = {0};
    if (av_image_fill_arrays(tempDataPtr, tempLineSize, tempData.get(), AV_PIX_FMT_RGB24, cropWidth, cropHeight, 1) <
        0) {
        throw std::runtime_error("Falha ao preencher ponteiros do av_image_fill_arrays.");
    }

    for (int orderedY = 0; orderedY < cropHeight; ++orderedY) {
        uint8_t *dst = tempDataPtr[0] + orderedY * tempLineSize[0];
        int srcY = cropY + orderedY;

        if (srcY < 0 || srcY >= srcHeight) {
            memset(dst, 0, cropWidth * 3);
        } else {
            int copyStartX = std::max(0, cropX);
            int copyEndX = std::min(srcWidth, cropX + cropWidth);

            int leftPad = copyStartX - cropX;
            if (leftPad > 0)
                memset(dst, 0, leftPad * 3);

            if (copyEndX > copyStartX) {
                uint8_t *srcLine = frame->data[0] + srcY * frame->linesize[0] + copyStartX * 3;
                memcpy(dst + leftPad * 3, srcLine, (copyEndX - copyStartX) * 3);
            }

            int rightPad = (cropX + cropWidth) - copyEndX;
            if (rightPad > 0)
                memset(dst + (cropWidth - rightPad) * 3, 0, rightPad * 3);
        }
    }

    const int OUTPUT_SIZE = 512;
    SwsContextPtr swsContextPtr(sws_getContext(
            cropWidth, cropHeight, AV_PIX_FMT_RGB24,
            OUTPUT_SIZE, OUTPUT_SIZE, AV_PIX_FMT_RGB24,
            SWS_BILINEAR, nullptr, nullptr, nullptr));
    if (!swsContextPtr) {
        throw std::runtime_error("Falha ao criar SwsContext para redimensionamento.");
    }

    dstFrame.allocateFrameWithBuffer(OUTPUT_SIZE, OUTPUT_SIZE, AV_PIX_FMT_RGB24);

    if (sws_scale(swsContextPtr.get(), tempDataPtr, tempLineSize, 0, cropHeight, dstFrame.frame->data,
                  dstFrame.frame->linesize) <= 0) {
        throw std::runtime_error("Falha ao redimensionar o frame para 512x512");
    }

    if (av_frame_copy_props(dstFrame.frame.get(), frame) != 0) {
        throw std::runtime_error("Falha ao copiar propriedades do frame");
    }

    LOGIRCF("%s", fmt::format(
            "Quadro recortado e redimensionado: cropX={}, cropY={}, cropWidth={}, cropHeight={}, dstWidth={}, dstHeight={}",
            cropX, cropY, cropWidth, cropHeight, dstFrame.frame->width, dstFrame.frame->height).c_str());
    return true;
}