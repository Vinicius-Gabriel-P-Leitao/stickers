/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.throwable.content;

public class ContentProviderException extends IllegalStateException {
    public ContentProviderException(String message) {
        super(message);
    }

    public ContentProviderException(String message, Throwable causa) {
        super(message, causa);
    }
}
