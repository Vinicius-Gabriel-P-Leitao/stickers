/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import br.arch.sticker.domain.data.database.StickerDatabaseHelper;

public class DeleteStickerPackRepo {
    private final SQLiteDatabase database;

    public DeleteStickerPackRepo(SQLiteDatabase database) {
        this.database = database;
    }

    public Integer deleteStickerPackFromDatabase(String stickerPackIdentifier)
            throws IllegalArgumentException, SQLiteException {
        return database.delete(StickerDatabaseHelper.TABLE_STICKER_PACK,
                StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?",
                new String[]{stickerPackIdentifier}
        );
    }
}
