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
    ERROR_FILE_SIZE(R.string.throw_exceeded_file_size),
    ERROR_SIZE_STICKER(R.string.throw_invalid_sticker_size),
    ERROR_STICKER_TYPE(R.string.throw_sticker_not_match_pack),
    ERROR_STICKER_DURATION(R.string.throw_animated_sticker_exceeded),
    ERROR_FILE_TYPE(R.string.throw_unsuported_file_type),
    STICKER_FILE_NOT_EXIST(R.string.sticker_file_not_exist);

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
