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
public enum BaseErrorCode implements ErrorCodeProvider {
    ERROR_UNKNOWN(R.string.error_unknown),
    ERROR_BASE_ACTIVITY(R.string.error_base_activity),
    ERROR_OPERATION_NOT_POSSIBLE(R.string.error_operation_failed);

    private final int message;

    BaseErrorCode(int message) {
        this.message = message;
    }

    @Override
    public int getMessageResId() {
        return message;
    }

    @Nullable
    public static BaseErrorCode fromName(@Nullable String name) {
        if (name == null) return null;

        try {
            return BaseErrorCode.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
