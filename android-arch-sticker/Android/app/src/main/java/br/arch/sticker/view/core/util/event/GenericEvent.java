/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.util.event;

public class GenericEvent<T> {
    private final T content;
    private boolean hasBeenHandled = false;

    public GenericEvent(T content)
        {
            this.content = content;
        }

    public T getContentIfNotHandled()
        {
            if (hasBeenHandled) return null;
            hasBeenHandled = true;
            return content;
        }

    public T peekContent()
        {
            return content;
        }
}
