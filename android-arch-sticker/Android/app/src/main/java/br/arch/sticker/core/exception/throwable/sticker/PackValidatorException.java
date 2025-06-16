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

import br.arch.sticker.core.exception.throwable.base.AppCoreStateException;

// @formatter:off
public class PackValidatorException extends AppCoreStateException {

    public PackValidatorException(@NonNull String message, @NonNull String  errorCode) {
        super(message, errorCode);
    }

    public PackValidatorException(String message, @Nullable Throwable cause, @NonNull String  errorCode) {
        super(message, cause, errorCode);
    }

    public enum ErrorCode {
        INVALID_IDENTIFIER(""),
        INVALID_PUBLISHER(""),
        INVALID_STICKERPACK_NAME(""),
        STICKERPACK_SIZE(""),
        INVALID_THUMBNAIL(""),
        INVALID_ANDROID_URL_SITE(""),
        INVALID_IOS_URL_SITE(""),
        INVALID_WEBSITE(""),
        INVALID_EMAIL("");

        private final String message;

        ErrorCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
