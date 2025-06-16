/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.exception.throwable.sticker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.arch.sticker.R;
import br.arch.sticker.core.exception.throwable.base.AppCoreStateException;

// @formatter:off
public class PackValidatorException extends AppCoreStateException {

    public PackValidatorException(@NonNull String message, @NonNull ErrorCode  errorCode) {
        super(message, errorCode.name());
    }

    public PackValidatorException(String message, @Nullable Throwable cause, @NonNull ErrorCode  errorCode) {
        super(message, cause, errorCode.name());
    }

    public enum ErrorCode {
        INVALID_IDENTIFIER(R.string.throw_identifier_stickerpack_invalid),
        INVALID_PUBLISHER(R.string.throw_publisher_stickerpack_invalid),
        INVALID_STICKERPACK_NAME(R.string.throw_name_stickerpack_invalid),
        STICKERPACK_SIZE(R.string.throw_stickerpack_size_invalid),
        INVALID_THUMBNAIL(R.string.throw_thumbnail_stickerpack_invalid),
        INVALID_ANDROID_URL_SITE(R.string.throw_url_android_stickerpack_invalid),
        INVALID_IOS_URL_SITE(R.string.throw_url_ios_stickerpack_invalid),
        INVALID_WEBSITE(R.string.throw_stickerpack_website_invalid),
        INVALID_EMAIL(R.string.throw_stickerpack_email_invalid);

        private final int message;

        ErrorCode(int message) {
            this.message = message;
        }

        public int getMessageResId() {
            return message;
        }

        @Nullable
        public static StickerFileException.ErrorFileCode fromName(@Nullable String name) {
            if (name == null) return null;

            try {
                return StickerFileException.ErrorFileCode.valueOf(name);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }
    }
}
