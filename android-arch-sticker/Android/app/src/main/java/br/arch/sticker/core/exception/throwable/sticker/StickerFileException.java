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

import br.arch.sticker.R;
import br.arch.sticker.core.exception.throwable.base.AppCoreStateException;

// @formatter:off
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
            @NonNull String message, @NonNull ErrorFileCode errorCode, @Nullable String stickerPackIdentifier, @Nullable String fileName) {
        super(message, errorCode.name());
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

    // @formatter:off
    public enum ErrorFileCode {
        ERROR_FILE_SIZE(R.string.throw_exceeded_file_size),
        ERROR_SIZE_STICKER(R.string.throw_invalid_sticker_size),
        ERROR_STICKER_TYPE(R.string.throw_sticker_not_match_pack),
        ERROR_STICKER_DURATION(R.string.throw_animated_sticker_exceeded),
        ERROR_FILE_TYPE(R.string.throw_unsuported_file_type);

        private final int message;

        ErrorFileCode(int message) {
            this.message = message;
        }

        public int getMessageResId() {
            return message;
        }

        @Nullable
        public static ErrorFileCode fromName(@Nullable String name) {
            if (name == null) return null;

            try {
                return ErrorFileCode.valueOf(name);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }
    }
}
