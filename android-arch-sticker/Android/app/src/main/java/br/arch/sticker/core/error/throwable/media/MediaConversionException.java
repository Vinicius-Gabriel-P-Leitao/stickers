/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.throwable.media;

import androidx.annotation.NonNull;

import br.arch.sticker.core.error.code.MediaConversionErrorCode;
import br.arch.sticker.core.error.throwable.base.AppCoreStateException;

// @formatter:off
public class MediaConversionException extends AppCoreStateException {
    public MediaConversionException(@NonNull String message, MediaConversionErrorCode errorCode) {
        super(message, errorCode);
    }

    public MediaConversionException(@NonNull String message, Throwable cause, MediaConversionErrorCode errorCode) {
        super(message, cause, errorCode);
    }
}
