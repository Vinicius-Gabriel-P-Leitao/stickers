/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.core.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vinicius.sticker.core.exception.base.AppCoreStateException;

public class StickerFileException extends AppCoreStateException {
    private final String stickerPackIdentifier;
    @Nullable
    private final String fileName;

    public StickerFileException(@NonNull String message, @NonNull String errorCode) {
        super(message, errorCode);
        this.stickerPackIdentifier = null;
        this.fileName = null;
    }

    public StickerFileException(
            @NonNull String message, @NonNull String errorCode, @Nullable String stickerPackIdentifier, @Nullable String fileName) {
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
