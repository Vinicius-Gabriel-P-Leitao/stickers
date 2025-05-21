/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

package com.vinicius.sticker.core.validation;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.animated.webp.WebPImage;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.vinicius.sticker.core.exception.StickerSizeFileLimitException;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.service.load.StickerLoaderService;

import java.io.IOException;

public class StickerValidator {
    static final int MAX_STATIC_STICKER_A11Y_TEXT_CHAR_LIMIT = 125;
    static final int MAX_ANIMATED_STICKER_A11Y_TEXT_CHAR_LIMIT = 255;
    public static final int STATIC_STICKER_FILE_LIMIT_KB = 100;
    public static final int ANIMATED_STICKER_FILE_LIMIT_KB = 500;
    public static final int EMOJI_MIN_LIMIT = 1;
    public static final int EMOJI_MAX_LIMIT = 3;
    public static final int IMAGE_HEIGHT = 512;
    public static final int IMAGE_WIDTH = 512;
    public static final long KB_IN_BYTES = 1024;
    public static final int ANIMATED_STICKER_FRAME_DURATION_MIN = 8;
    public static final int ANIMATED_STICKER_TOTAL_DURATION_MAX = 10 * 1000; //ms

    public static void verifyStickerValidity(
            @NonNull Context context,
            @NonNull final String identifier, @NonNull final Sticker sticker, final boolean animatedStickerPack) throws IllegalStateException {
        if (sticker.emojis.size() > EMOJI_MAX_LIMIT) {
            throw new IllegalStateException(
                    "emoji count exceed limit, sticker pack identifier: " + identifier + ", filename: " + sticker.imageFileName);
        }

        if (sticker.emojis.size() < EMOJI_MIN_LIMIT) {
            throw new IllegalStateException(
                    "To provide best user experience, please associate at least 1 emoji to this sticker, sticker pack identifier: " + identifier +
                    ", filename: " + sticker.imageFileName);
        }

        if (TextUtils.isEmpty(sticker.imageFileName)) {
            throw new IllegalStateException("no file path for sticker, sticker pack identifier:" + identifier);
        }

        final String accessibilityText = sticker.accessibilityText;
        if (isInvalidAccessibilityText(accessibilityText, animatedStickerPack)) {
            throw new IllegalStateException(
                    "accessibility text length exceed limit, sticker pack identifier: " + identifier + ", filename: " + sticker.imageFileName);
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
            @NonNull Context context,
            @NonNull String identifier, @NonNull final String fileName, final boolean animatedStickerPack) throws IllegalStateException {
        try {
            final byte[] stickerInBytes = StickerLoaderService.fetchStickerAsset(identifier, fileName, context.getContentResolver());

            if (!animatedStickerPack && stickerInBytes.length > STATIC_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw new StickerSizeFileLimitException(
                        "static sticker should be less than " + STATIC_STICKER_FILE_LIMIT_KB + "KB, current file is " +
                        stickerInBytes.length / KB_IN_BYTES + " KB, sticker pack identifier: " + identifier + ", filename: " + fileName);
            }
            if (animatedStickerPack && stickerInBytes.length > ANIMATED_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                throw new StickerSizeFileLimitException(
                        "animated sticker should be less than " + ANIMATED_STICKER_FILE_LIMIT_KB + "KB, current file is " +
                        stickerInBytes.length / KB_IN_BYTES + " KB, sticker pack identifier: " + identifier + ", filename: " +
                        fileName, identifier, fileName);
            }
            try {
                final WebPImage webPImage = WebPImage.createFromByteArray(stickerInBytes, ImageDecodeOptions.defaults());
                if (webPImage.getHeight() != IMAGE_HEIGHT) {
                    throw new IllegalStateException("sticker height should be " + IMAGE_HEIGHT + ", current height is " + webPImage.getHeight() +
                                                    ", sticker pack identifier: " + identifier + ", filename: " + fileName);
                }
                if (webPImage.getWidth() != IMAGE_WIDTH) {
                    throw new IllegalStateException(
                            "sticker width should be " + IMAGE_WIDTH + ", current width is " + webPImage.getWidth() + ", sticker pack identifier: " +
                            identifier + ", filename: " + fileName);
                }
                if (animatedStickerPack) {
                    if (webPImage.getFrameCount() <= 1) {
                        throw new IllegalStateException(
                                "this pack is marked as animated sticker pack, all stickers should animate, sticker pack identifier: " + identifier +
                                ", filename: " + fileName);
                    }
                    checkFrameDurationsForAnimatedSticker(webPImage.getFrameDurations(), identifier, fileName);
                    if (webPImage.getDuration() > ANIMATED_STICKER_TOTAL_DURATION_MAX) {
                        throw new IllegalStateException(
                                "sticker animation max duration is: " + ANIMATED_STICKER_TOTAL_DURATION_MAX + " ms, current duration is: " +
                                webPImage.getDuration() + " ms, sticker pack identifier: " + identifier + ", filename: " + fileName);
                    }
                } else if (webPImage.getFrameCount() > 1) {
                    throw new IllegalStateException(
                            "this pack is not marked as animated sticker pack, all stickers should be static stickers, sticker pack identifier: " +
                            identifier + ", filename: " + fileName);
                }
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException(
                        "Error parsing webp image, sticker pack identifier: " + identifier + ", filename: " + fileName, exception);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "cannot open sticker file: sticker pack identifier: " + identifier + ", filename: " + fileName, exception);
        }
    }

    private static void checkFrameDurationsForAnimatedSticker(
            @NonNull final int[] frameDurations, @NonNull final String identifier, @NonNull final String fileName) {
        for (int frameDuration : frameDurations) {
            if (frameDuration < ANIMATED_STICKER_FRAME_DURATION_MIN) {
                throw new IllegalStateException(
                        "animated sticker frame duration limit is " + ANIMATED_STICKER_FRAME_DURATION_MIN + ", sticker pack identifier: " +
                        identifier + ", filename: " + fileName);
            }
        }
    }
}
