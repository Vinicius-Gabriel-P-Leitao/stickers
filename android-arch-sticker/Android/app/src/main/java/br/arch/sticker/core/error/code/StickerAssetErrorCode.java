/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.code;

import androidx.annotation.Nullable;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCodeProvider;

// @formatter:off
public enum StickerAssetErrorCode implements ErrorCodeProvider{
    ERROR_FILE_SIZE(R.string.error_exceeded_file_size),
    ERROR_SIZE_STICKER(R.string.error_invalid_sticker_size),
    ERROR_STICKER_TYPE(R.string.error_sticker_type_mismatch),
    ERROR_STICKER_DURATION(R.string.error_animated_sticker_duration),
    ERROR_FILE_TYPE(R.string.error_unsupported_file_type),
    INVALID_STICKER_PATH(R.string.error_invalid_sticker_filename),
    STICKER_FILE_NOT_EXIST(R.string.error_file_not_found);

    private final int message;

    StickerAssetErrorCode(int message) {
        this.message = message;
    }

    @Override
    public int getMessageResId() {
        return message;
    }

    @Nullable
    public static StickerAssetErrorCode fromName(@Nullable String name) {
        if (name == null) return null;

        try {
            return StickerAssetErrorCode.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
