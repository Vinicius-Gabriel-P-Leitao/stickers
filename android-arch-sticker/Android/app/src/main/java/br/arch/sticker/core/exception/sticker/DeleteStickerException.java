/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.exception.sticker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.arch.sticker.core.exception.base.AppCoreStateException;

public class DeleteStickerException extends AppCoreStateException {
    public DeleteStickerException(@NonNull String message) {
        super(message, "DELETE_SQL_STICKER");
    }

    public DeleteStickerException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause, "DELETE_SQL_STICKER");
    }
}
