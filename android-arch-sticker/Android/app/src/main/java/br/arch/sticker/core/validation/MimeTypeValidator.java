/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.validation;

public class MimeTypeValidator {
    public static boolean validateUniqueMimeType(String mimeType, String[] mimeTypesList) {
        for (String type : mimeTypesList) {
            if (type.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }
}
