/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


package com.vinicius.sticker.core.exception.sticker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vinicius.sticker.core.exception.base.AppCoreStateException;

public class PackValidatorException extends AppCoreStateException {
    public PackValidatorException(@NonNull String message) {
        super(message, "PACK_VALIDATOR_ERROR");
    }

    public PackValidatorException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause, "PACK_VALIDATOR_ERROR");
    }
}
