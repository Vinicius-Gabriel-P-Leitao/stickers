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

    public PackValidatorException(@NonNull String message, @NonNull ErrorCode  errorCode) {
        super(message, errorCode.name());
    }

    public PackValidatorException(String message, @Nullable Throwable cause, @NonNull ErrorCode  errorCode) {
        super(message, cause, errorCode.name());
    }

    public enum ErrorCode {
        INVALID_IDENTIFIER("O identificador do pacote é invalido!"),
        INVALID_PUBLISHER("O campo de publisher do pacote é invalido!"),
        INVALID_STICKERPACK_NAME("O nome do pacote de figurinhas é invalido!"),
        STICKERPACK_SIZE("O tamanho do pacote de figurinhas é invalido!"),
        INVALID_THUMBNAIL("A thumbnail do pacote é invalida!"),
        INVALID_ANDROID_URL_SITE("A url ANDROID do aplicativo que está registrada no pacote é invalida!"),
        INVALID_IOS_URL_SITE("A url IOS do aplicativo que está registrada no pacote é invalida!"),
        INVALID_WEBSITE("O site vinculado ao pacote de figurinhas é invalido!"),
        INVALID_EMAIL("O e-mail vinculado ao pacote de figurinhas é invalido!");

        private final String message;

        ErrorCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
