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

#include "../exception/HandlerJavaException.hpp"

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

ProcessFramesToFormat::ProcessFramesToFormat(JNIEnv *env, jclass nativeMediaException) : env(env), nativeMediaException(nativeMediaException) {}

AVFramePtr ProcessFramesToFormat::createAvFrame(int width, int height, AVPixelFormat format) {
    AVFramePtr frame(av_frame_alloc());
    if (!frame) {
        std::string msgError = fmt::format("Falha ao alocar AVFramePtr");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return nullptr;
    }

    frame->format = format;
    frame->width = width;
    frame->height = height;

    int ret = av_frame_get_buffer(frame.get(), 0);
    if (ret < 0) {
        char errBuf[128];
        av_strerror(ret, errBuf, sizeof(errBuf));
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, fmt::format("Falha ao alocar buffer do AVFrame: {}", errBuf));
        return nullptr;
    }

    return frame;
}

bool FrameWithBuffer::allocate(ProcessFramesToFormat &processor, int width, int height, AVPixelFormat format) {
    int bufferSize = av_image_get_buffer_size(format, width, height, 1);
    buffer.reset(reinterpret_cast<uint8_t *>(av_malloc(bufferSize)));

    if (!buffer) {
        LOGIRCF("Erro ao alocar buffer");
        return false;
    }

    frame = processor.createAvFrame(width, height, format);
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

void ProcessFramesToFormat::processFrame(AVFramePtr &rgbFrame, int cropX, int cropY, int width, int height, std::vector<FrameWithBuffer> &frames) {
    FrameWithBuffer frameWithBuffer;

    if (!cropFrame(rgbFrame, frameWithBuffer, cropX, cropY, width, height)) {
        std::string msgError = "Erro ao redimensionar/cortar o frame";
        HandlerJavaException::throwNativeConversionException(this->env, this->nativeMediaException, msgError);
        return;
    }

    LOGIRCF("Frame %zu adicionado à lista de animação", frames.size());
    frames.push_back(std::move(frameWithBuffer));
}

bool ProcessFramesToFormat::cropFrame(const AVFramePtr &srcFrame, FrameWithBuffer &dstFrame, int cropX, int cropY, int cropWidth, int cropHeight) {
    const AVFrame *frame = srcFrame.get();
    if (!frame || !frame->data[0]) {
        LOGIRCF("Falha ao alocar AVFrame ou os dados são nulos");
        HandlerJavaException::throwNativeConversionException(this->env, this->nativeMediaException, "Falha ao alocar AVFrame ou os dados são nulos");
        return false;
    }

    int srcWidth = frame->width;
    int srcHeight = frame->height;
    auto srcFormat = static_cast<AVPixelFormat>(frame->format);

    if (srcWidth <= 0 || srcHeight <= 0 || srcFormat != AV_PIX_FMT_RGB24) {
        std::string msgError = fmt::format("Dimensões do quadro de origem ({}x{}) ou formato ({}) inválidos",
                                           srcWidth, srcHeight, av_get_pix_fmt_name(srcFormat));
        LOGIRCF("%s", msgError.c_str());
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);
        return false;
    }

    if (cropX < 0 || cropY < 0 || cropWidth <= 0 || cropHeight <= 0 ||
        cropX + cropWidth > srcWidth || cropY + cropHeight > srcHeight) {
        std::string msgError = fmt::format("Área de recorte ({}+{}, {}+{}) fora dos limites do quadro de origem ({}x{})",
                                           cropX, cropWidth, cropY, cropHeight, srcWidth, srcHeight);
        LOGIRCF("%s", msgError.c_str());
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);
        return false;
    }

    int bufferSize = av_image_get_buffer_size(AV_PIX_FMT_RGB24, cropWidth, cropHeight, 1);
    AVBufferPtr tempData(reinterpret_cast<uint8_t *>(av_malloc(bufferSize)));
    if (!tempData) {
        LOGIRCF("Falha ao alocar buffer temporário");
        HandlerJavaException::throwNativeConversionException(this->env, this->nativeMediaException, "Falha ao alocar buffer temporário");
        return false;
    }

    uint8_t *tempDataPtr[AV_NUM_DATA_POINTERS] = {nullptr};
    int tempLineSize[AV_NUM_DATA_POINTERS] = {0};
    if (av_image_fill_arrays(tempDataPtr, tempLineSize, tempData.get(), AV_PIX_FMT_RGB24, cropWidth, cropHeight, 1) < 0) {
        LOGIRCF("Falha ao preencher ponteiros do av_image_fill_arrays");
        HandlerJavaException::throwNativeConversionException(this->env, this->nativeMediaException,
                                                             "Falha ao preencher ponteiros do av_image_fill_arrays");
        return false;
    }

    for (int orderdY = 0; orderdY < cropHeight; ++orderdY) {
        uint8_t *dst = tempDataPtr[0] + orderdY * tempLineSize[0];
        uint8_t *src = frame->data[0] + (orderdY + cropY) * frame->linesize[0] + cropX * 3;
        memcpy(dst, src, cropWidth * 3);
    }

    const int OUTPUT_SIZE = 512;
    SwsContextPtr swsContextPtr(sws_getContext(
            cropWidth, cropHeight, AV_PIX_FMT_RGB24,
            OUTPUT_SIZE, OUTPUT_SIZE, AV_PIX_FMT_RGB24,
            SWS_BILINEAR, nullptr, nullptr, nullptr));
    if (!swsContextPtr) {
        LOGIRCF("Falha ao criar SwsContext para redimensionamento");
        HandlerJavaException::throwNativeConversionException(this->env, this->nativeMediaException, "Falha ao criar contexto de redimensionamento");
        return false;
    }

    if (!dstFrame.allocate(*this, OUTPUT_SIZE, OUTPUT_SIZE, AV_PIX_FMT_RGB24)) {
        LOGIRCF("Falha ao alocar o quadro de destino");
        HandlerJavaException::throwNativeConversionException(this->env, this->nativeMediaException, "Falha ao alocar o quadro de destino");
        return false;
    }

    if (sws_scale(swsContextPtr.get(), tempDataPtr, tempLineSize, 0, cropHeight, dstFrame.frame->data, dstFrame.frame->linesize) <= 0) {
        LOGIRCF("Falha ao redimensionar o frame para 512x512");
        HandlerJavaException::throwNativeConversionException(this->env, this->nativeMediaException, "Falha ao redimensionar o frame");
        return false;
    }

    if (av_frame_copy_props(dstFrame.frame.get(), frame) != 0) {
        LOGIRCF("Falha ao copiar propriedades do frame");
        HandlerJavaException::throwNativeConversionException(this->env, this->nativeMediaException, "Falha ao copiar propriedades do frame");
        return false;
    }

    LOGIRCF("Quadro recortado e redimensionado: cropX=%d, cropY=%d, cropWidth=%d, cropHeight=%d, dstWidth=%d, dstHeight=%d",
            cropX, cropY, cropWidth, cropHeight, dstFrame.frame->width, dstFrame.frame->height);

    return true;
}