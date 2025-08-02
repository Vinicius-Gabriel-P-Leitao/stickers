/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.throwable.sticker;

import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.base.AppCoreStateException;

public class FetchStickerException extends AppCoreStateException {
    public FetchStickerException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public FetchStickerException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }
}
