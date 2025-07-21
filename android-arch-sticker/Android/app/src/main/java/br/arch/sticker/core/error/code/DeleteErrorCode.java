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
public enum DeleteErrorCode implements ErrorCodeProvider {
    ERROR_PACK_DELETE_DB(R.string.error_delete_stickerpack_db),
    ERROR_STICKER_DELETE_DB(R.string.error_delete_sticker_list_db),
    ERROR_PACK_DELETE_SERVICE(R.string.error_delete_stickerpack_service),
    ERROR_PACK_DELETE_UTIL(R.string.error_delete_stickerpack_util),
    ERROR_PACK_DELETE_UI(R.string.error_delete_stickerpack_ui);

    private final int message;

    DeleteErrorCode(int message) {
        this.message = message;
    }

    @Override
    public int getMessageResId() {
        return message;
    }

    @Nullable
    public static DeleteErrorCode fromName(@Nullable String name) {
        if (name == null) return null;

        try {
            return DeleteErrorCode.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
