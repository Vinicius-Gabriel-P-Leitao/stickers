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

public enum UpdateErrorCode implements ErrorCodeProvider {
    ERROR_EMPTY_STICKERPACK(R.string.error_empty_stickerpack);

    private final int message;

    UpdateErrorCode(int message) {
        this.message = message;
    }

    @Override
    public int getMessageResId() {
        return message;
    }

    @Nullable
    public static UpdateErrorCode fromName(@Nullable String name) {
        if (name == null) return null;

        try {
            return UpdateErrorCode.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
