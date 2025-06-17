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
    INVALID_IDENTIFIER(R.string.throw_identifier_stickerpack_invalid),
    DUPLICATE_IDENTIFIER(R.string.throw_suplicate_stickerpack_identifier),
    INVALID_PUBLISHER(R.string.throw_publisher_stickerpack_invalid),
    INVALID_STICKERPACK_NAME(R.string.throw_name_stickerpack_invalid),
    INVALID_STICKERPACK_SIZE(R.string.throw_stickerpack_size_invalid),
    INVALID_THUMBNAIL(R.string.throw_thumbnail_stickerpack_invalid),
    INVALID_ANDROID_URL_SITE(R.string.throw_url_android_stickerpack_invalid),
    INVALID_IOS_URL_SITE(R.string.throw_url_ios_stickerpack_invalid),
    INVALID_WEBSITE(R.string.throw_stickerpack_website_invalid),
    INVALID_EMAIL(R.string.throw_stickerpack_email_invalid),
    INVALID_STICKER_FILENAME(R.string.throw_invalid_sticker_filename),
    INVALID_STICKER_ACCESSIBILITY(R.string.throw_invalid_size_acessibility);

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
