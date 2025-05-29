/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.data.database.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper;

public class SelectStickerPacks {
    public static Cursor getPackForAllStickerPacks(StickerDatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query =
                "SELECT DISTINCT "
                        + StickerDatabaseHelper.TABLE_STICKER_PACKS
                        + ".*, "
                        + StickerDatabaseHelper.TABLE_STICKER_PACK
                        + ".*, "
                        + StickerDatabaseHelper.TABLE_STICKER
                        + ".* "
                        + "FROM "
                        + StickerDatabaseHelper.TABLE_STICKER_PACKS
                        + " "
                        + "INNER JOIN "
                        + StickerDatabaseHelper.TABLE_STICKER_PACK
                        + " ON "
                        + StickerDatabaseHelper.TABLE_STICKER_PACKS
                        + "."
                        + StickerDatabaseHelper.ID_STICKER_PACKS
                        + " = "
                        + StickerDatabaseHelper.TABLE_STICKER_PACK
                        + "."
                        + StickerDatabaseHelper.FK_STICKER_PACKS
                        + " "
                        + "INNER JOIN "
                        + StickerDatabaseHelper.TABLE_STICKER
                        + " ON "
                        + StickerDatabaseHelper.TABLE_STICKER_PACK
                        + "."
                        + StickerDatabaseHelper.ID_STICKER_PACK
                        + " = "
                        + StickerDatabaseHelper.TABLE_STICKER
                        + "."
                        + StickerDatabaseHelper.FK_STICKER_PACK;

        return db.rawQuery(query, null);
    }
}
