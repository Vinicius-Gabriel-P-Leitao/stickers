/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
#include "ProcessWebpToAvFrames.hpp"

#include <jni.h>
#include <vector>
#include <memory>
#include <string>
#include <fstream>
#include <android/log.h>

#include "../exception/HandlerJavaException.hpp"

#include "../raii/AVFrameDestroyer.hpp"
#include "../raii/AVBufferDestroyer.hpp"
#include "../raii/WebpDemuxerPtr.hpp"
#include "../raii/WebpIteratorPtr.hpp"

extern "C" {
#include "demux.h"
#include "decode.h"
#include "libavutil/frame.h"
#include "libavutil/imgutils.h"
}

#define LOG_TAG_WEBP_DECODE "decodeWebP"
#define LOGDW(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_WEBP_DECODE, __VA_ARGS__)

std::vector<uint8_t> ProcessWebpToAvFrames::loadFileToMemory(const std::string &path) {
    std::ifstream file(path, std::ios::binary);
    if (!file) return {};

    file.seekg(0, std::ios::end);
    size_t fileSize = file.tellg();
    file.seekg(0);

    std::streamsize safeSize = static_cast<std::streamsize>(std::min(fileSize, static_cast<size_t>(std::numeric_limits<std::streamsize>::max())));

    std::vector<uint8_t> data(fileSize);
    file.read(reinterpret_cast<char *>(data.data()), safeSize);
    return data;
}

bool ProcessWebpToAvFrames::decodeWebPAsAVFrames(
        JNIEnv *env, const std::string &inputPath, std::vector<FrameWithBuffer> &frames, int targetWidth, int targetHeight) {
    jclass nativeMediaException = env->FindClass("br/arch/sticker/core/error/throwable/media/NativeConversionException");

    std::vector<uint8_t> fileInMemory = loadFileToMemory(inputPath);
    if (fileInMemory.empty()) {
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Falha ao ler o arquivo WebP.");
        return false;
    }

    WebPData webpData;
    webpData.bytes = fileInMemory.data();
    webpData.size = fileInMemory.size();

    WebpDemuxerPtr pWebPDemuxer(WebPDemux(&webpData));
    if (!pWebPDemuxer) {
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "WebPDemux falhou.");
        return false;
    }

    uint32_t flags = WebPDemuxGetI(pWebPDemuxer.getPDemuxer(), WEBP_FF_FORMAT_FLAGS);
    bool isAnimated = (flags & ANIMATION_FLAG) != 0;

    WebpIteratorPtr iterator;
    if (!iterator.init(pWebPDemuxer.getPDemuxer(), 1)) {
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Nenhum frame encontrado no WebP.");
        return false;
    }

    int frameIndex = 0;
    do {
        WebPDecoderConfig config;
        if (!WebPInitDecoderConfig(&config)) {
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Falha ao inicializar WebPDecoderConfig.");
            return false;
        }

        if (WebPGetFeatures(iterator->fragment.bytes, iterator->fragment.size, &config.input) != VP8_STATUS_OK) {
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Erro ao obter características do WebP.");
            return false;
        }

        config.output.colorspace = MODE_RGB;
        uint8_t *webPDecodeRgb = WebPDecodeRGB(iterator->fragment.bytes, iterator->fragment.size, &config.output.width, &config.output.height);
        if (!webPDecodeRgb) {
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Falha ao decodificar frame WebP.");
            return false;
        }

        int width = config.output.width;
        int height = config.output.height;
        int stride = width * 3;
        int bufferSize = stride * height;

        AVFramePtr frame(av_frame_alloc());
        AVBufferPtr buffer(reinterpret_cast<uint8_t *>(av_malloc(bufferSize)));

        if (!frame || !buffer) {
            WebPFree(webPDecodeRgb);
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Falha ao alocar AVFrame ou buffer.");
            return false;
        }

        frame->format = AV_PIX_FMT_RGB24;
        frame->width = width;
        frame->height = height;

        av_image_fill_arrays(frame->data, frame->linesize, buffer.get(), AV_PIX_FMT_RGB24, width, height, 1);
        memcpy(frame->data[0], webPDecodeRgb, bufferSize);
        WebPFree(webPDecodeRgb);

        ProcessFramesToFormat::processFrame(env, nativeMediaException, frame, targetWidth, targetHeight, frames);
        LOGDW("Frame %d decodificado e redimensionado.", ++frameIndex);

    } while (isAnimated && iterator.next());

    return true;
}

