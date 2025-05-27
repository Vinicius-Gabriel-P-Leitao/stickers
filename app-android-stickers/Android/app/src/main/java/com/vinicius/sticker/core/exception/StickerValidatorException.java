/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.core.exception;

import androidx.annotation.NonNull;

import com.vinicius.sticker.core.exception.main.AppCoreStateException;

public class StickerValidatorException extends AppCoreStateException {
    public StickerValidatorException(@NonNull String message) {
        super(message, "sticker_VALIDATOR_ERROR");
    }
}
