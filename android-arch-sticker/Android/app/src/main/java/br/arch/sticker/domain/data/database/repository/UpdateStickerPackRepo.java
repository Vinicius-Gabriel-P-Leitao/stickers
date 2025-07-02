/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabase.FK_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabase.STICKER_FILE_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabase.STICKER_IS_VALID;
import static br.arch.sticker.domain.data.database.StickerDatabase.TABLE_STICKER;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import br.arch.sticker.domain.data.database.StickerDatabase;

public class UpdateStickerPackRepo {
    public static void updateStickerFileName(StickerDatabase dbHelper, String stickerPackIdentifier, String newFileName, String oldFileName)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(STICKER_FILE_NAME_IN_QUERY, newFileName);
                contentValues.put(STICKER_IS_VALID, ""); // NOTE: Retirar o erro

                String whereClause = FK_STICKER_PACK + " = ? AND " + STICKER_FILE_NAME_IN_QUERY + " = ?";
                String[] whereArgs = {stickerPackIdentifier, oldFileName};

                db.update(TABLE_STICKER, contentValues, whereClause, whereArgs);
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }

    public static void updateInvalidSticker(StickerDatabase dbHelper, String stickerPackIdentifier, String fileName, String errorMessage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(STICKER_IS_VALID, errorMessage);

            String whereClause = FK_STICKER_PACK + " = ? AND " + STICKER_FILE_NAME_IN_QUERY + " = ?";
            String[] whereArgs = {stickerPackIdentifier, fileName};

            db.update(TABLE_STICKER, contentValues, whereClause, whereArgs);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}
