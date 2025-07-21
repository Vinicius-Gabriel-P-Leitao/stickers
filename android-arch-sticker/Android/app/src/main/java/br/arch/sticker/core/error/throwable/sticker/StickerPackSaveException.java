/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.throwable.sticker;

import androidx.annotation.NonNull;

import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.base.AppCoreStateException;

// @formatter:off
public class StickerPackSaveException extends AppCoreStateException {
    public StickerPackSaveException( Exception exception) {
        super(exception);
    }

    public StickerPackSaveException(@NonNull String message, @NonNull ErrorCode errorCode) {
        super(message, errorCode);
    }

    public StickerPackSaveException(@NonNull String message, Exception exception, @NonNull ErrorCode errorCode) {
        super(message, exception, errorCode);
    }
}
