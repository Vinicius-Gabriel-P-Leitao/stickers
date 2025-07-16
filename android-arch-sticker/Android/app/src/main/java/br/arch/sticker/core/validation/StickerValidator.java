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

import java.util.Locale;

import br.arch.sticker.core.error.code.BaseErrorCode;
import br.arch.sticker.core.error.code.StickerAssetErrorCode;
import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.core.error.throwable.base.InternalAppException;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;
import br.arch.sticker.core.error.throwable.sticker.StickerFileException;
import br.arch.sticker.core.error.throwable.sticker.StickerValidatorException;
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

    private final FetchStickerAssetService fetchStickerAssetService;

    public StickerValidator(Context context) {
        this.fetchStickerAssetService = new FetchStickerAssetService(
                context.getApplicationContext());
    }

    public void verifyStickerValidity(@NonNull final String stickerPackIdentifier, @NonNull final Sticker sticker, final boolean animatedStickerPack)
            throws IllegalStateException {

        if (!sticker.stickerIsValid.isEmpty()) {
            throw new StickerFileException("Figurinha inválida.",
                    StickerAssetErrorCode.ERROR_FILE_SIZE, stickerPackIdentifier, null);
        }

        if (TextUtils.isEmpty(sticker.imageFileName)) {
            throw new StickerValidatorException(String.format(
                    "Nenhum caminho de figurinha para o figurinha, identificador do pacote de figurinhas: %s",
                    stickerPackIdentifier), StickerAssetErrorCode.INVALID_STICKER_PATH,
                    stickerPackIdentifier, null);
        }

        final String accessibilityText = sticker.accessibilityText;
        if (isInvalidAccessibilityText(accessibilityText, animatedStickerPack)) {
            throw new StickerValidatorException(String.format(
                    "Comprimento do texto de acessibilidade excedeu o limite, identificador do pacote de figurinhas: %s, arquivo: %s",
                    stickerPackIdentifier, sticker.imageFileName),
                    StickerPackErrorCode.INVALID_STICKER_ACCESSIBILITY, stickerPackIdentifier,
                    sticker.imageFileName);
        }

        validateStickerFile(stickerPackIdentifier, sticker.imageFileName, animatedStickerPack);
    }

    private static boolean isInvalidAccessibilityText(final @Nullable String accessibilityText, final boolean isAnimatedStickerPack) {
        if (accessibilityText == null) {
            return false;
        }

        final int length = accessibilityText.length();
        return isAnimatedStickerPack && length > MAX_ANIMATED_STICKER_A11Y_TEXT_CHAR_LIMIT ||
                !isAnimatedStickerPack && length > MAX_STATIC_STICKER_A11Y_TEXT_CHAR_LIMIT;
    }

    public void validateStickerFile(@NonNull String stickerPackIdentifier, @NonNull final String fileName, final boolean animatedStickerPack)
            throws IllegalStateException {
        try {
            final byte[] stickerInBytes = fetchStickerAssetService.fetchStickerAsset(
                    stickerPackIdentifier, fileName);

            if (!animatedStickerPack &&
                    stickerInBytes.length > STATIC_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw new StickerFileException(String.format(Locale.ROOT,
                        "A figurinha estática deve ser menor que %d KB, o arquivo atual tem %d KB, identificador do pacote: %s, arquivo: %s",
                        STATIC_STICKER_FILE_LIMIT_KB, Math.toIntExact(
                                stickerInBytes.length /
                                        KB_IN_BYTES), stickerPackIdentifier, fileName),
                        StickerAssetErrorCode.ERROR_FILE_SIZE, stickerPackIdentifier, fileName);
            }

            if (animatedStickerPack &&
                    stickerInBytes.length > ANIMATED_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw new StickerFileException(String.format(Locale.ROOT,
                        "A figurinha animada deve ser menor que %d KB, o arquivo atual tem %d KB, identificador do pacote: %s, arquivo: %s",
                        ANIMATED_STICKER_FILE_LIMIT_KB, Math.toIntExact(
                                stickerInBytes.length /
                                        KB_IN_BYTES), stickerPackIdentifier, fileName),
                        StickerAssetErrorCode.ERROR_FILE_SIZE, stickerPackIdentifier, fileName);
            }

            try {
                final WebPImage webPImage = WebPImage.createFromByteArray(stickerInBytes,
                        ImageDecodeOptions.defaults());

                if (webPImage.getHeight() != IMAGE_HEIGHT) {
                    throw new StickerFileException(String.format(Locale.ROOT,
                            "A altura da figurinha deve ser %d, a altura atual é %d, identificador do pacote: %s, arquivo: %s",
                            IMAGE_HEIGHT, webPImage.getHeight(), stickerPackIdentifier, fileName),
                            StickerAssetErrorCode.ERROR_SIZE_STICKER, stickerPackIdentifier,
                            fileName);
                }

                if (webPImage.getWidth() != IMAGE_WIDTH) {
                    throw new StickerFileException(String.format(Locale.ROOT,
                            "A largura da figurinha deve ser %d, a largura atual é %d, identificador do pacote: %s, arquivo: %s",
                            IMAGE_WIDTH, webPImage.getWidth(), stickerPackIdentifier, fileName),
                            StickerAssetErrorCode.ERROR_SIZE_STICKER, stickerPackIdentifier,
                            fileName);
                }

                if (animatedStickerPack) {
                    if (webPImage.getFrameCount() <= 1) {
                        throw new StickerFileException(String.format(Locale.ROOT,
                                "Este pacote está marcado como animado, todas as figurinhas devem ser animadas. Identificador do pacote: %s, arquivo: %s",
                                stickerPackIdentifier, fileName),
                                StickerAssetErrorCode.ERROR_STICKER_TYPE, stickerPackIdentifier,
                                fileName);
                    }

                    checkFrameDurationsForAnimatedSticker(webPImage.getFrameDurations(),
                            stickerPackIdentifier, fileName);

                    if (webPImage.getDuration() > ANIMATED_STICKER_TOTAL_DURATION_MAX) {
                        throw new StickerFileException(String.format(Locale.ROOT,
                                "A duração máxima da animação é: %d ms, a duração atual é: %d ms, identificador do pacote: %s, arquivo: %s",
                                ANIMATED_STICKER_TOTAL_DURATION_MAX, webPImage.getDuration(),
                                stickerPackIdentifier, fileName),
                                StickerAssetErrorCode.ERROR_STICKER_DURATION, stickerPackIdentifier,
                                fileName);
                    }
                } else if (webPImage.getFrameCount() > 1) {
                    throw new StickerFileException(String.format(Locale.ROOT,
                            "Este pacote não está marcado como animado, todas as figurinhas devem ser estáticas. Identificador do pacote: %s, arquivo: %s",
                            stickerPackIdentifier, fileName),
                            StickerAssetErrorCode.ERROR_STICKER_TYPE, stickerPackIdentifier,
                            fileName);
                }

            } catch (IllegalArgumentException exception) {
                throw new StickerFileException(String.format(Locale.ROOT,
                        "Erro ao processar a imagem WebP. Identificador do pacote: %s, arquivo: %s",
                        stickerPackIdentifier, fileName), StickerAssetErrorCode.ERROR_FILE_TYPE,
                        stickerPackIdentifier, fileName);
            }
        } catch (FetchStickerException exception) {
            throw new InternalAppException(String.format(
                    "Não foi possível abrir o arquivo da figurinha. Identificador do pacote: %s, arquivo: %s",
                    stickerPackIdentifier, fileName), exception,
                    BaseErrorCode.ERROR_OPERATION_NOT_POSSIBLE);
        }
    }

    private static void checkFrameDurationsForAnimatedSticker(@NonNull final int[] frameDurations, @NonNull final String identifier, @NonNull final String fileName) {
        for (int frameDuration : frameDurations) {
            if (frameDuration < ANIMATED_STICKER_FRAME_DURATION_MIN) {
                throw new StickerFileException(String.format(Locale.ROOT,
                        "O limite mínimo de duração de um quadro da figurinha animada é %d ms. Pacote: %s, Arquivo: %s",
                        ANIMATED_STICKER_FRAME_DURATION_MIN, identifier, fileName),
                        StickerAssetErrorCode.ERROR_STICKER_DURATION, identifier, fileName);
            }
        }
    }
}