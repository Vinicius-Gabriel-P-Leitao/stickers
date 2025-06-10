/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.pattern;

// @formatter:off
public enum ErrorFileCode {
    ERROR_FILE_SIZE("O arquivo ultrapassou o tamanho em KB permitido."),
    ERROR_SIZE_STICKER("O tamanho da figurinha deve ser de 512x512 e ultrapassou"),
    ERROR_STICKER_TYPE("As figurinhas não são condizente com o tipo do pacote"),
    ERROR_STICKER_DURATION("A duração da figurinha animada ultrapaça o permitido"),
    ERROR_FILE_TYPE("Tipo de arquivo não suportado.");

    private final String message;

    ErrorFileCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
