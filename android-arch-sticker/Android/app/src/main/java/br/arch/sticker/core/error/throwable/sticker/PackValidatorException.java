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

import br.arch.sticker.core.error.ErrorCodeProvider;
import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.core.error.throwable.base.AppCoreStateException;

public class PackValidatorException extends AppCoreStateException {

    public PackValidatorException(@NonNull String message, @NonNull ErrorCodeProvider stickerPackErrorCode) {
        super(message, stickerPackErrorCode);
    }

    public PackValidatorException(String message, @Nullable Throwable cause, @NonNull ErrorCodeProvider stickerPackErrorCode) {
        super(message, cause, stickerPackErrorCode);
    }
}
