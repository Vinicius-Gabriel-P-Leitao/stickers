/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.throwable.sticker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.base.AppCoreStateException;

public class UpdateStickerException extends AppCoreStateException {
    public UpdateStickerException(@NonNull String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public UpdateStickerException(@NonNull String message, ErrorCode errorCode, @Nullable Object[] details) {
        super(message, null, errorCode, details);
    }

    public UpdateStickerException(@NonNull String message, @Nullable Throwable cause, ErrorCode errorCode, @Nullable Object[] details) {
        super(message, cause, errorCode, details);
    }
}
