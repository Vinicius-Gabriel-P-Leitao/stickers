/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

#include "WebpAnimationConverter.hpp"

#include <jni.h>
#include <vector>
#include <memory>
#include <string>
#include "format.h"
#include <iostream>
#include <android/log.h>

#include "../raii/AVFrameDestroyer.hpp"
#include "../raii/WebpDataDestroyer.hpp"
#include "../raii/AVBufferDestroyer.hpp"
#include "../raii/WebpAnimEncoderDestroyer.hpp"

extern "C" {
#include "mux.h"
#include "decode.h"
#include "encode.h"
#include "libswresample/swresample.h"
}

#define LOG_TAG_SERVICE "WebPAnimationConverter"
#define LOGDF(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_SERVICE, __VA_ARGS__)
#define LOGINF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_SERVICE, __VA_ARGS__)

#define DEFAULT_QUALITY 20.0f

int WebpAnimationConverter::convertToWebp(
        const char *outputPath, std::vector<FrameWithBuffer> &frames, int width, int height, int durationMs, float quality, int lossless) {
    LOGDF("%s", fmt::format("[WEBP-CONVERT] Quality recebido: {}", quality).c_str());
    LOGDF("%s", fmt::format("[WEBP-CONVERT] Lossless recebido: {}", lossless).c_str());

    WebPAnimEncoderOptions encOptions;
    if (!WebPAnimEncoderOptionsInit(&encOptions)) {
        throw std::runtime_error("Falha ao inicializar WebPAnimEncoderOptions.");
    }

    // Definindo opções de animação
    WebPAnimEncoderOptionsInit(&encOptions);
    encOptions.minimize_size = 1;

    WebPConfig webPConfig;
    if (!WebPConfigInit(&webPConfig)) {
        throw std::runtime_error("Falha ao inicializar WebPConfig.");
    }

    webPConfig.lossless = lossless ? lossless : 0;
    webPConfig.quality = (quality >= 0.0f && quality <= 100.0f) ? quality : DEFAULT_QUALITY;
    webPConfig.method = 2;
    webPConfig.filter_strength = 70;
    webPConfig.preprocessing = 2;

    LOGINF("Criando encoder.");

    WebPAnimEncoderPtr encoder(WebPAnimEncoderNew(width, height, &encOptions));
    if (!encoder) {
        throw std::runtime_error("Erro ao criar WebPAnimEncoder.");
    }

    int timestampMs = 0;
    for (const auto &frameWithBuffer: frames) {
        const AVFramePtr &frame = frameWithBuffer.frame;
        LOGDF("%s", fmt::format("Adicionando frame ao encoder, timestamp: {} ms", timestampMs).c_str());

        WebPPicture webPPicture;
        if (!WebPPictureInit(&webPPicture)) {
            throw std::runtime_error("Erro ao inicializar WebPPicture.");
        }

        webPPicture.width = width;
        webPPicture.height = height;
        webPPicture.use_argb = 1;

        if (!WebPPictureImportRGB(&webPPicture, frame->data[0], frame->linesize[0])) {
            WebPPictureFree(&webPPicture);
            throw std::runtime_error("Erro ao importar dados RGB.");
        }

        if (!WebPAnimEncoderAdd(encoder.get(), &webPPicture, timestampMs, &webPConfig)) {
            WebPPictureFree(&webPPicture);
            throw std::runtime_error(fmt::format("Erro ao adicionar frame ao encoder: {}", WebPAnimEncoderGetError(encoder.get())));
        }

        WebPPictureFree(&webPPicture);
        timestampMs += durationMs;
    }

    if (!WebPAnimEncoderAdd(encoder.get(), nullptr, timestampMs, nullptr)) {
        throw std::runtime_error(fmt::format("Erro ao finalizar vFrameBuffer: {}", WebPAnimEncoderGetError(encoder.get())));
    }

    WebPData webpData;
    WebPDataInit(&webpData);

    WebPDataPtr webp_data_ptr(&webpData);
    if (!WebPAnimEncoderAssemble(encoder.get(), &webpData)) {
        throw std::runtime_error(fmt::format("Erro ao montar animação: {}", WebPAnimEncoderGetError(encoder.get())));
    }

    FILE *outputOpenFile = fopen(outputPath, "wb");
    if (!outputOpenFile) {
        throw std::runtime_error(fmt::format("Erro ao abrir arquivo de saída: {}", outputPath));
    }

    fwrite(webpData.bytes, 1, webpData.size, outputOpenFile);
    fclose(outputOpenFile);

    LOGINF("%s", fmt::format("Arquivo WebP salvo com sucesso em: {} ({} bytes)", outputPath, webpData.size).c_str());
    return 1;
}

