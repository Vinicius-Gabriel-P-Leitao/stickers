/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

package com.vinicius.sticker.core.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vinicius.sticker.core.exception.main.AppCoreStateException;

public class PackValidatorException extends AppCoreStateException {
    public PackValidatorException(@NonNull String message) {
        super(message, "PACK_VALIDATOR_ERROR");
    }

    public PackValidatorException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause, "PACK_VALIDATOR_ERROR");
    }
}
