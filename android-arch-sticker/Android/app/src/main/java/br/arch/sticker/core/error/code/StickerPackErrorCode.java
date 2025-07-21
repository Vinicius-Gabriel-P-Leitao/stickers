/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 *
 * which is based on the GNU General Public License v3.0,
 *  with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.code;

import androidx.annotation.Nullable;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCodeProvider;

// @formatter:off
public enum StickerPackErrorCode implements ErrorCodeProvider{
    INVALID_IDENTIFIER(R.string.error_invalid_identifier),
    DUPLICATE_IDENTIFIER(R.string.error_duplicate_identifier),
    INVALID_PUBLISHER(R.string.error_invalid_publisher),
    INVALID_STICKERPACK_NAME(R.string.error_invalid_pack_name),
    INVALID_STICKERPACK_SIZE(R.string.error_invalid_stickerpack_size),
    INVALID_THUMBNAIL(R.string.error_invalid_thumbnail),
    INVALID_ANDROID_URL_SITE(R.string.error_invalid_android_url),
    INVALID_IOS_URL_SITE(R.string.error_invalid_ios_url),
    INVALID_WEBSITE(R.string.error_invalid_website),
    INVALID_EMAIL(R.string.error_invalid_email),
    INVALID_STICKER_ACCESSIBILITY(R.string.error_accessibility_text_length);

    private final int message;

    StickerPackErrorCode(int message) {
        this.message = message;
    }

    @Override
    public int getMessageResId() {
        return message;
    }

    @Nullable
    public static StickerAssetErrorCode fromName(@Nullable String name) {
        if (name == null) return null;

        try {
            return StickerAssetErrorCode.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
