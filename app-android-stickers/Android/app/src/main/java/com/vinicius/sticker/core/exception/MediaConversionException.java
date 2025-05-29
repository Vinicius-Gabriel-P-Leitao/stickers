/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.core.exception;

import androidx.annotation.NonNull;

import com.vinicius.sticker.core.exception.base.AppCoreStateException;

public class MediaConversionException extends AppCoreStateException {
    public MediaConversionException(@NonNull String message) {
        super(message, "MEDIA_CONVERSION");
    }

    public MediaConversionException(@NonNull String message, Throwable cause) {
        super(message, cause, "MEDIA_CONVERSION");
    }
}
