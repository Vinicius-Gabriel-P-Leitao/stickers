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

import br.arch.sticker.core.error.code.BaseErrorCode;
import br.arch.sticker.core.error.factory.StickerExceptionFactory;
import br.arch.sticker.core.error.throwable.base.InternalAppException;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;
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
            @NonNull Context context, @NonNull final String stickerPackIdentifier, @NonNull final Sticker sticker,
            final boolean animatedStickerPack
    ) throws IllegalStateException {

        if (!sticker.stickerIsValid.isEmpty()) {
            throw StickerExceptionFactory.fromStickerValidity(stickerPackIdentifier);
        }

        if (TextUtils.isEmpty(sticker.imageFileName)) {
            throw StickerExceptionFactory.missingStickerFileName(stickerPackIdentifier);
        }

        final String accessibilityText = sticker.accessibilityText;
        if (isInvalidAccessibilityText(accessibilityText, animatedStickerPack)) {
            throw StickerExceptionFactory.accessibilityTextTooLong(
                    stickerPackIdentifier,
                    sticker.imageFileName);
        }

        validateStickerFile(
                context,
                stickerPackIdentifier,
                sticker.imageFileName,
                animatedStickerPack);
    }

    private static boolean isInvalidAccessibilityText(final @Nullable String accessibilityText, final boolean isAnimatedStickerPack) {
        if (accessibilityText == null) {
            return false;
        }

        final int length = accessibilityText.length();
        return isAnimatedStickerPack && length > MAX_ANIMATED_STICKER_A11Y_TEXT_CHAR_LIMIT ||
                !isAnimatedStickerPack && length > MAX_STATIC_STICKER_A11Y_TEXT_CHAR_LIMIT;
    }

    public static void validateStickerFile(
            @NonNull Context context, @NonNull String stickerPackIdentifier, @NonNull final String fileName,
            final boolean animatedStickerPack
    ) throws IllegalStateException {
        try {
            final byte[] stickerInBytes = FetchStickerAssetService.fetchStickerAsset(stickerPackIdentifier, fileName, context);

            if (!animatedStickerPack && stickerInBytes.length > STATIC_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw StickerExceptionFactory.staticFileTooLarge(
                        stickerPackIdentifier,
                        fileName,
                        STATIC_STICKER_FILE_LIMIT_KB,
                        Math.toIntExact(stickerInBytes.length / KB_IN_BYTES));
            }

            if (animatedStickerPack && stickerInBytes.length > ANIMATED_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw StickerExceptionFactory.animatedFileTooLarge(
                        stickerPackIdentifier,
                        fileName,
                        ANIMATED_STICKER_FILE_LIMIT_KB,
                        Math.toIntExact(stickerInBytes.length / KB_IN_BYTES));
            }

            try {
                final WebPImage webPImage = WebPImage.createFromByteArray(stickerInBytes, ImageDecodeOptions.defaults());

                if (webPImage.getHeight() != IMAGE_HEIGHT) {
                    throw StickerExceptionFactory.invalidHeight(
                            stickerPackIdentifier,
                            fileName,
                            IMAGE_HEIGHT,
                            webPImage.getHeight());
                }

                if (webPImage.getWidth() != IMAGE_WIDTH) {
                    throw StickerExceptionFactory.invalidWidth(
                            stickerPackIdentifier,
                            fileName,
                            IMAGE_WIDTH,
                            webPImage.getWidth());
                }

                if (animatedStickerPack) {
                    if (webPImage.getFrameCount() <= 1) {
                        throw StickerExceptionFactory.expectedAnimatedButGotStatic(
                                stickerPackIdentifier,
                                fileName);
                    }

                    checkFrameDurationsForAnimatedSticker(webPImage.getFrameDurations(), stickerPackIdentifier, fileName);

                    if (webPImage.getDuration() > ANIMATED_STICKER_TOTAL_DURATION_MAX) {
                        throw StickerExceptionFactory.durationExceeded(
                                stickerPackIdentifier,
                                fileName,
                                ANIMATED_STICKER_TOTAL_DURATION_MAX,
                                webPImage.getDuration());
                    }
                } else if (webPImage.getFrameCount() > 1) {
                    throw StickerExceptionFactory.expectedStaticButGotAnimated(
                            stickerPackIdentifier,
                            fileName);
                }
            } catch (IllegalArgumentException exception) {
                throw StickerExceptionFactory.invalidWebP(
                        stickerPackIdentifier,
                        fileName);
            }
        } catch (FetchStickerException exception) {
            throw new InternalAppException(
                    String.format(
                            "Não foi possível abrir o arquivo da figurinha. Identificador do pacote: %s, arquivo: %s",
                            stickerPackIdentifier,
                            fileName),
                    exception,
                    BaseErrorCode.ERROR_OPERATION_NOT_POSSIBLE);
        }
    }

    private static void checkFrameDurationsForAnimatedSticker(
            @NonNull final int[] frameDurations, @NonNull final String identifier, @NonNull final String fileName) {
        for (int frameDuration : frameDurations) {
            if (frameDuration < ANIMATED_STICKER_FRAME_DURATION_MIN) {
                throw StickerExceptionFactory.durationExceeded(
                        identifier,
                        fileName,
                        ANIMATED_STICKER_FRAME_DURATION_MIN);
            }
        }
    }
}
