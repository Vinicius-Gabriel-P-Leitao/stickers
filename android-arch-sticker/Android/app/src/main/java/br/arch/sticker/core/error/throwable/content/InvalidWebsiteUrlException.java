/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.throwable.content;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.arch.sticker.core.error.code.InvalidUrlErrorCode;
import br.arch.sticker.core.error.throwable.base.AppCoreStateException;

// @formatter:off
public class InvalidWebsiteUrlException extends AppCoreStateException {

    public InvalidWebsiteUrlException(@NonNull String message, @Nullable Throwable cause, InvalidUrlErrorCode errorCode, @Nullable String invalidUrl) {
        super(message, cause, errorCode, new Object[]{invalidUrl});
    }
}
