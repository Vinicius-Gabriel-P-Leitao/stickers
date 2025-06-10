/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.validation;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.animated.webp.WebPImage;
import com.facebook.imagepipeline.common.ImageDecodeOptions;

import java.io.IOException;

import br.arch.sticker.core.exception.base.InternalAppException;
import br.arch.sticker.core.exception.sticker.StickerFileException;
import br.arch.sticker.core.exception.sticker.StickerValidatorException;
import br.arch.sticker.core.pattern.ErrorFileCode;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.service.fetch.FetchStickerAssetService;

public class StickerValidator {
    static final int MAX_STATIC_STICKER_A11Y_TEXT_CHAR_LIMIT = 125;
    static final int MAX_ANIMATED_STICKER_A11Y_TEXT_CHAR_LIMIT = 255;
    public static final int STATIC_STICKER_FILE_LIMIT_KB = 100;
    public static final int ANIMATED_STICKER_FILE_LIMIT_KB = 500;
    public static final int IMAGE_HEIGHT = 512;
    public static final int IMAGE_WIDTH = 512;
    public static final long KB_IN_BYTES = 1024;
    public static final int ANIMATED_STICKER_FRAME_DURATION_MIN = 8;
    public static final int ANIMATED_STICKER_TOTAL_DURATION_MAX = 10 * 1000; //ms

    public static void verifyStickerValidity(
            @NonNull Context context, @NonNull final String identifier, @NonNull final Sticker sticker,
            final boolean animatedStickerPack
    ) throws IllegalStateException {

        if (!sticker.stickerIsValid.isEmpty()) {
            throw new StickerFileException("Figurinha invalida.", sticker.stickerIsValid);
        }

        if (TextUtils.isEmpty(sticker.imageFileName)) {
            throw new StickerValidatorException("Nenhum caminho de arquivo para o adesivo, identificador do pacote de figurinhas:" + identifier);
        }

        final String accessibilityText = sticker.accessibilityText;
        if (isInvalidAccessibilityText(accessibilityText, animatedStickerPack)) {
            throw new StickerValidatorException(
                    "Comprimento do texto de acessibilidade excedeu o limite, identificador do pacote de figurinhas:" + identifier + ", arquivo:" +
                            sticker.imageFileName);
        }

        validateStickerFile(context, identifier, sticker.imageFileName, animatedStickerPack);
    }

    private static boolean isInvalidAccessibilityText(final @Nullable String accessibilityText, final boolean isAnimatedStickerPack) {
        if (accessibilityText == null) {
            return false;
        }

        final int length = accessibilityText.length();
        return isAnimatedStickerPack && length > MAX_ANIMATED_STICKER_A11Y_TEXT_CHAR_LIMIT ||
                !isAnimatedStickerPack && length > MAX_STATIC_STICKER_A11Y_TEXT_CHAR_LIMIT;
    }

    private static void validateStickerFile(
            @NonNull Context context, @NonNull String stickerPackIdentifier, @NonNull final String fileName,
            final boolean animatedStickerPack
    ) throws IllegalStateException {
        try {
            final byte[] stickerInBytes = FetchStickerAssetService.fetchStickerAsset(stickerPackIdentifier, fileName, context);

            if (!animatedStickerPack && stickerInBytes.length > STATIC_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw new StickerFileException(
                        String.format(
                                "A figurinha estática deve ser menor que %s KB, o arquivo atual tem %s KB, identificador do pacote: %s, arquivo: %s",
                                STATIC_STICKER_FILE_LIMIT_KB, stickerInBytes.length / KB_IN_BYTES, stickerPackIdentifier, fileName),
                        ErrorFileCode.ERROR_FILE_SIZE.getMessage(), stickerPackIdentifier, fileName);
            }

            if (animatedStickerPack && stickerInBytes.length > ANIMATED_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw new StickerFileException(
                        String.format(
                                "A figurinha animada deve ser menor que %s KB, o arquivo atual tem %s KB, identificador do pacote: %s, arquivo: %s",
                                ANIMATED_STICKER_FILE_LIMIT_KB, stickerInBytes.length / KB_IN_BYTES, stickerPackIdentifier, fileName),
                        ErrorFileCode.ERROR_FILE_SIZE.getMessage(), stickerPackIdentifier, fileName);
            }
            try {
                final WebPImage webPImage = WebPImage.createFromByteArray(stickerInBytes, ImageDecodeOptions.defaults());
                if (webPImage.getHeight() != IMAGE_HEIGHT) {
                    throw new StickerFileException(
                            String.format(
                                    "A altura da figurinha deve ser %s, a altura atual é %s, identificador do pacote: %s, arquivo: %s", IMAGE_HEIGHT,
                                    webPImage.getHeight(), stickerPackIdentifier, fileName), ErrorFileCode.ERROR_SIZE_STICKER.getMessage(), stickerPackIdentifier,
                            fileName);
                }
                if (webPImage.getWidth() != IMAGE_WIDTH) {
                    throw new StickerFileException(
                            String.format(
                                    "A largura da figurinha deve ser %s, a largura atual é %s, identificador do pacote: %s, arquivo: %s", IMAGE_WIDTH,
                                    webPImage.getWidth(), stickerPackIdentifier, fileName), ErrorFileCode.ERROR_SIZE_STICKER.getMessage(), stickerPackIdentifier, fileName);
                }
                if (animatedStickerPack) {
                    if (webPImage.getFrameCount() <= 1) {
                        throw new StickerFileException(
                                String.format(
                                        "Este pacote está marcado como animado, todas as figurinhas devem ser animadas. Identificador do pacote: %s, arquivo: %s",
                                        stickerPackIdentifier, fileName), ErrorFileCode.ERROR_STICKER_TYPE.getMessage(), stickerPackIdentifier, fileName);
                    }

                    checkFrameDurationsForAnimatedSticker(webPImage.getFrameDurations(), stickerPackIdentifier, fileName);

                    if (webPImage.getDuration() > ANIMATED_STICKER_TOTAL_DURATION_MAX) {
                        throw new StickerFileException(
                                String.format(
                                        "A duração máxima da animação é: %s ms, a duração atual é: %s ms, identificador do pacote: %s, arquivo: %s",
                                        ANIMATED_STICKER_TOTAL_DURATION_MAX, webPImage.getDuration(), stickerPackIdentifier, fileName),
                                ErrorFileCode.ERROR_STICKER_DURATION.getMessage(), stickerPackIdentifier, fileName);
                    }
                } else if (webPImage.getFrameCount() > 1) {
                    throw new StickerFileException(
                            String.format(
                                    "Este pacote não está marcado como animado, todas as figurinhas devem ser estáticas. Identificador do pacote: %s, arquivo: %s",
                                    stickerPackIdentifier, fileName), ErrorFileCode.ERROR_STICKER_TYPE.getMessage(), stickerPackIdentifier, fileName);
                }
            } catch (IllegalArgumentException exception) {
                throw new StickerFileException(
                        String.format("Erro ao processar a imagem WebP. Identificador do pacote: %s, arquivo: %s", stickerPackIdentifier, fileName),
                        ErrorFileCode.ERROR_FILE_TYPE.getMessage(), stickerPackIdentifier, fileName);
            }
        } catch (IOException exception) {
            throw new InternalAppException(
                    String.format("Não foi possível abrir o arquivo da figurinha. Identificador do pacote: %s, arquivo: %s", stickerPackIdentifier, fileName),
                    exception);
        }
    }

    private static void checkFrameDurationsForAnimatedSticker(
            @NonNull final int[] frameDurations, @NonNull final String identifier, @NonNull final String fileName) {
        for (int frameDuration : frameDurations) {
            if (frameDuration < ANIMATED_STICKER_FRAME_DURATION_MIN) {
                throw new StickerFileException(
                        String.format(
                                "O limite mínimo de duração de um quadro da figurinha animada é %s ms, identificador do pacote: %s, arquivo: %s",
                                ANIMATED_STICKER_FRAME_DURATION_MIN, identifier, fileName), ErrorFileCode.ERROR_STICKER_DURATION.getMessage(),
                        identifier, fileName);
            }
        }
    }
}
