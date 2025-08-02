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

public class StickerValidatorException extends AppCoreStateException {
    private final String stickerPackIdentifier;
    @Nullable
    private final String fileName;

    public StickerValidatorException(
            @NonNull String message, @NonNull ErrorCode errorCode, @Nullable String stickerPackIdentifier, @Nullable String fileName) {
        super(message, errorCode);
        this.stickerPackIdentifier = stickerPackIdentifier;
        this.fileName = fileName;
    }

    public String getStickerPackIdentifier() {
        return stickerPackIdentifier;
    }

    @Nullable
    public String getFileName() {
        return fileName;
    }
}
