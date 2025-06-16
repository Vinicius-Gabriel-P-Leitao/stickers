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

public class FetchStickerPackException extends AppCoreStateException {
    public FetchStickerPackException(@NonNull String message) {
        super(message, "FETCH_STICKERPACK");
    }

    public FetchStickerPackException(@NonNull String message, @Nullable Throwable cause, @Nullable Object[] details) {
        super(message, cause, "FETCH_STICKERPACK", details);
    }
}
