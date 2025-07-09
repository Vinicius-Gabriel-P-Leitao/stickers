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
    ERROR_UNKNOWN(R.string.throw_unknown_error),
    ERROR_BASE_ACTIVITY(R.string.throw_base_activity),
    ERROR_OPERATION_NOT_POSSIBLE(R.string.throw_operation_not_possible);

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
