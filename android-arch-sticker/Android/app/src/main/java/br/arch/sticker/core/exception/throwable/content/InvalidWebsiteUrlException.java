/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


package br.arch.sticker.core.exception.throwable.content;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.arch.sticker.core.exception.throwable.base.AppCoreStateException;

public class InvalidWebsiteUrlException extends AppCoreStateException {

    public InvalidWebsiteUrlException(@NonNull String message, @Nullable Throwable cause, @Nullable String invalidUrl) {
        super(message, cause, "INVALID_URL", new Object[]{invalidUrl});
    }
}
