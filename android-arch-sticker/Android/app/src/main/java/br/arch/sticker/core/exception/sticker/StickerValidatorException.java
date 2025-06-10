/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.exception.sticker;

import androidx.annotation.NonNull;

import br.arch.sticker.core.exception.base.AppCoreStateException;

public class StickerValidatorException extends AppCoreStateException {

    public StickerValidatorException(@NonNull String message) {
        super(message, "STICKER_VALIDATOR_ERROR");
    }

    public StickerValidatorException(String message, @NonNull Throwable cause) {
        super(message, cause, "STICKER_VALIDATOR_ERROR");
    }
}
