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
#include <format.h>
#include <android/log.h>

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

bool
ProcessWebpToAvFrames::decodeWebPAsAVFrames(const std::string &inputPath, std::vector<FrameWithBuffer> &frames, int targetWidth, int targetHeight) {
    std::vector<uint8_t> fileInMemory = loadFileToMemory(inputPath);
    if (fileInMemory.empty()) {
        throw std::runtime_error("Falha ao ler o arquivo WebP.");
    }

    WebPData webpData;
    webpData.bytes = fileInMemory.data();
    webpData.size = fileInMemory.size();

    WebpDemuxerPtr pWebPDemuxer(WebPDemux(&webpData));
    if (!pWebPDemuxer) {
        throw std::runtime_error("WebPDemux falhou.");
    }

    uint32_t flags = WebPDemuxGetI(pWebPDemuxer.getPDemuxer(), WEBP_FF_FORMAT_FLAGS);
    bool isAnimated = (flags & ANIMATION_FLAG) != 0;

    WebpIteratorPtr iterator;
    if (!iterator.init(pWebPDemuxer.getPDemuxer(), 1)) {
        throw std::runtime_error("Nenhum frame encontrado no WebP.");
    }

    int frameIndex = 0;
    do {
        WebPDecoderConfig config;
        if (!WebPInitDecoderConfig(&config)) {
            throw std::runtime_error("Falha ao inicializar WebPDecoderConfig.");
        }

        if (WebPGetFeatures(iterator->fragment.bytes, iterator->fragment.size, &config.input) != VP8_STATUS_OK) {
            throw std::runtime_error("Erro ao obter características do WebP.");
        }

        config.output.colorspace = MODE_RGB;
        uint8_t *webPDecodeRgb = WebPDecodeRGB(iterator->fragment.bytes, iterator->fragment.size, &config.output.width, &config.output.height);
        if (!webPDecodeRgb) {
            throw std::runtime_error("Falha ao decodificar frame WebP.");
        }

        int width = config.output.width;
        int height = config.output.height;
        int stride = width * 3;
        int bufferSize = stride * height;

        AVFramePtr frame(av_frame_alloc());
        AVBufferPtr buffer(reinterpret_cast<uint8_t *>(av_malloc(bufferSize)));

        if (!frame || !buffer) {
            WebPFree(webPDecodeRgb);
            throw std::runtime_error("Falha ao alocar AVFrame ou buffer.");
        }

        frame->format = AV_PIX_FMT_RGB24;
        frame->width = width;
        frame->height = height;

        av_image_fill_arrays(frame->data, frame->linesize, buffer.get(), AV_PIX_FMT_RGB24, width, height, 1);
        memcpy(frame->data[0], webPDecodeRgb, bufferSize);
        WebPFree(webPDecodeRgb);

        ProcessFramesToFormat::processFrame(frame, frame->width, frame->height, targetWidth, targetHeight, frames);
        LOGDW("%s", fmt::format("Frame {} decodificado e redimensionado.", ++frameIndex).c_str());

    } while (isAnimated && iterator.next());

    return true;
}

