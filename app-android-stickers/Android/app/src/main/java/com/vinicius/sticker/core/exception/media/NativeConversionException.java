/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


package com.vinicius.sticker.core.exception.media;

import androidx.annotation.NonNull;

public class NativeConversionException extends RuntimeException {
    public NativeConversionException(@NonNull String message) {
        super(message);
    }

    public NativeConversionException(@NonNull String message, Throwable cause) {
        super(message, cause);
    }
}
