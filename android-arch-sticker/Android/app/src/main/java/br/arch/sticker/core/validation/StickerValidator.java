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

import br.arch.sticker.R;
import br.arch.sticker.core.error.code.BaseErrorCode;
import br.arch.sticker.core.error.code.StickerAssetErrorCode;
import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.core.error.throwable.base.InternalAppException;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;
import br.arch.sticker.core.error.throwable.sticker.StickerFileException;
import br.arch.sticker.core.error.throwable.sticker.StickerValidatorException;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.service.fetch.FetchStickerAssetService;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

public class StickerValidator {
    private final static String TAG_LOG = StickerValidator.class.getSimpleName();
    private static final int MAX_STATIC_STICKER_A11Y_TEXT_CHAR_LIMIT = 125;
    private static final int MAX_ANIMATED_STICKER_A11Y_TEXT_CHAR_LIMIT = 255;
    public static final int STATIC_STICKER_FILE_LIMIT_KB = 100;
    public static final int ANIMATED_STICKER_FILE_LIMIT_KB = 500;
    public static final int IMAGE_HEIGHT = 512;
    public static final int IMAGE_WIDTH = 512;
    public static final long KB_IN_BYTES = 1024;
    public static final int ANIMATED_STICKER_FRAME_DURATION_MIN = 8;
    public static final int ANIMATED_STICKER_TOTAL_DURATION_MAX = 10 * 1000; //ms

    private final ApplicationTranslate applicationTranslate;
    private final FetchStickerAssetService fetchStickerAssetService;

    public StickerValidator(Context context) {
        this.fetchStickerAssetService = new FetchStickerAssetService(context);
        this.applicationTranslate = new ApplicationTranslate(context.getResources());
    }

    public void verifyStickerValidity(@NonNull final String stickerPackIdentifier, @NonNull final Sticker sticker, final boolean animatedStickerPack)
            throws StickerValidatorException, StickerFileException, InternalAppException {

        if (!sticker.stickerIsValid.isEmpty()) {
            throw new StickerFileException(
                    applicationTranslate.translate(R.string.error_invalid_sticker)
                            .log(TAG_LOG, Level.ERROR).get(), StickerAssetErrorCode.ERROR_FILE_SIZE,
                    stickerPackIdentifier, null
            );
        }

        if (TextUtils.isEmpty(sticker.imageFileName)) {
            throw new StickerValidatorException(
                    applicationTranslate.translate(R.string.error_sticker_file_not_found,
                            stickerPackIdentifier
                    ).log(TAG_LOG, Level.ERROR).get(), StickerAssetErrorCode.INVALID_STICKER_PATH,
                    stickerPackIdentifier, null
            );
        }

        final String accessibilityText = sticker.accessibilityText;
        if (isInvalidAccessibilityText(accessibilityText, animatedStickerPack)) {
            throw new StickerValidatorException(
                    applicationTranslate.translate(R.string.error_accessibility_text_length,
                            stickerPackIdentifier, sticker.imageFileName
                    ).log(TAG_LOG, Level.ERROR).get(),
                    StickerPackErrorCode.INVALID_STICKER_ACCESSIBILITY, stickerPackIdentifier,
                    sticker.imageFileName
            );
        }

        validateStickerFile(stickerPackIdentifier, sticker.imageFileName, animatedStickerPack);
    }

    public void validateStickerFile(@NonNull String stickerPackIdentifier, @NonNull final String fileName, final boolean animatedStickerPack)
            throws StickerFileException, InternalAppException {
        try {
            final byte[] stickerInBytes = fetchStickerAssetService.fetchStickerAsset(
                    stickerPackIdentifier, fileName);

            if (!animatedStickerPack &&
                    stickerInBytes.length > STATIC_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw new StickerFileException(applicationTranslate.translate(
                        R.string.error_sticker_file_size,
                        STATIC_STICKER_FILE_LIMIT_KB,
                        Math.toIntExact(stickerInBytes.length / KB_IN_BYTES), stickerPackIdentifier,
                        fileName
                ).log(TAG_LOG, Level.ERROR).get(), StickerAssetErrorCode.ERROR_FILE_SIZE,
                        stickerPackIdentifier, fileName
                );
            }

            if (animatedStickerPack &&
                    stickerInBytes.length > ANIMATED_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw new StickerFileException(applicationTranslate.translate(
                        R.string.error_sticker_file_size,
                        ANIMATED_STICKER_FILE_LIMIT_KB,
                        Math.toIntExact(stickerInBytes.length / KB_IN_BYTES), stickerPackIdentifier,
                        fileName
                ).log(TAG_LOG, Level.ERROR).get(), StickerAssetErrorCode.ERROR_FILE_SIZE,
                        stickerPackIdentifier, fileName
                );
            }

            try {
                final WebPImage webPImage = WebPImage.createFromByteArray(stickerInBytes,
                        ImageDecodeOptions.defaults()
                );
                if (webPImage.getHeight() != IMAGE_HEIGHT) {
                    throw new StickerFileException(
                            applicationTranslate.translate(R.string.error_invalid_sticker_size,
                                    IMAGE_HEIGHT, webPImage.getHeight(), stickerPackIdentifier,
                                    fileName
                            ).log(TAG_LOG, Level.ERROR).get(),
                            StickerAssetErrorCode.ERROR_SIZE_STICKER, stickerPackIdentifier,
                            fileName
                    );
                }

                if (webPImage.getWidth() != IMAGE_WIDTH) {
                    throw new StickerFileException(
                            applicationTranslate.translate(R.string.error_invalid_sticker_size,
                                    IMAGE_WIDTH, webPImage.getWidth(), stickerPackIdentifier,
                                    fileName
                            ).log(TAG_LOG, Level.ERROR).get(),
                            StickerAssetErrorCode.ERROR_SIZE_STICKER, stickerPackIdentifier,
                            fileName
                    );
                }

                if (animatedStickerPack) {
                    if (webPImage.getFrameCount() <= 1) {
                        throw new StickerFileException(applicationTranslate.translate(
                                R.string.error_sticker_type_mismatch, stickerPackIdentifier,
                                fileName
                        ).log(TAG_LOG, Level.ERROR).get(), StickerAssetErrorCode.ERROR_STICKER_TYPE,
                                stickerPackIdentifier, fileName
                        );
                    }

                    checkFrameDurationsForAnimatedSticker(webPImage.getFrameDurations(),
                            stickerPackIdentifier, fileName
                    );

                    if (webPImage.getDuration() > ANIMATED_STICKER_TOTAL_DURATION_MAX) {
                        throw new StickerFileException(applicationTranslate.translate(
                                R.string.error_animated_sticker_duration,
                                ANIMATED_STICKER_TOTAL_DURATION_MAX, webPImage.getDuration(),
                                stickerPackIdentifier, fileName
                        ).log(TAG_LOG, Level.ERROR).get(),
                                StickerAssetErrorCode.ERROR_STICKER_DURATION, stickerPackIdentifier,
                                fileName
                        );
                    }
                } else if (webPImage.getFrameCount() > 1) {
                    throw new StickerFileException(applicationTranslate.translate(
                            R.string.error_sticker_type_mismatch,
                            stickerPackIdentifier, fileName
                    ).log(TAG_LOG, Level.ERROR).get(), StickerAssetErrorCode.ERROR_STICKER_TYPE,
                            stickerPackIdentifier, fileName
                    );
                }

            } catch (IllegalArgumentException exception) {
                throw new StickerFileException(
                        applicationTranslate.translate(R.string.error_webp_processing_error,
                                stickerPackIdentifier, fileName
                        ).log(TAG_LOG, Level.ERROR, exception).get(),
                        StickerAssetErrorCode.ERROR_FILE_TYPE,
                        stickerPackIdentifier, fileName
                );
            }
        } catch (FetchStickerException exception) {
            throw new InternalAppException(
                    applicationTranslate.translate(R.string.error_file_not_found,
                            stickerPackIdentifier, fileName
                    ).log(TAG_LOG, Level.ERROR, exception).get(), exception,
                    BaseErrorCode.ERROR_OPERATION_NOT_POSSIBLE
            );
        }
    }

    private void checkFrameDurationsForAnimatedSticker(@NonNull final int[] frameDurations, @NonNull final String identifier, @NonNull final String fileName) {
        for (int frameDuration : frameDurations) {
            if (frameDuration < ANIMATED_STICKER_FRAME_DURATION_MIN) {
                throw new StickerFileException(applicationTranslate.translate(
                        R.string.error_animated_sticker_duration,
                        ANIMATED_STICKER_FRAME_DURATION_MIN, identifier, fileName
                ).log(TAG_LOG, Level.ERROR).get(), StickerAssetErrorCode.ERROR_STICKER_DURATION,
                        identifier, fileName
                );
            }
        }
    }

    private static boolean isInvalidAccessibilityText(final @Nullable String accessibilityText, final boolean isAnimatedStickerPack) {
        if (accessibilityText == null) {
            return false;
        }

        final int length = accessibilityText.length();
        return isAnimatedStickerPack && length > MAX_ANIMATED_STICKER_A11Y_TEXT_CHAR_LIMIT ||
                !isAnimatedStickerPack && length > MAX_STATIC_STICKER_A11Y_TEXT_CHAR_LIMIT;
    }
}