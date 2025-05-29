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

import com.vinicius.sticker.domain.data.database.dao.StickerDatabase;

public class SelectStickerPacks {
    public static Cursor getAllStickerPacks(StickerDatabase dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT " + StickerDatabase.TABLE_STICKER_PACKS + ".*, " + StickerDatabase.TABLE_STICKER_PACK + ".*, " +
                StickerDatabase.TABLE_STICKER + ".* " + "FROM " + StickerDatabase.TABLE_STICKER_PACKS + " " + "INNER JOIN " +
                StickerDatabase.TABLE_STICKER_PACK + " ON " + StickerDatabase.TABLE_STICKER_PACKS + "." + StickerDatabase.ID_STICKER_PACKS + " = " +
                StickerDatabase.TABLE_STICKER_PACK + "." + StickerDatabase.FK_STICKER_PACKS + " " + "INNER JOIN " + StickerDatabase.TABLE_STICKER +
                " ON " + StickerDatabase.TABLE_STICKER_PACK + "." + StickerDatabase.ID_STICKER_PACK + " = " + StickerDatabase.TABLE_STICKER + "." +
                StickerDatabase.FK_STICKER_PACK;

        return db.rawQuery(query, null);
    }

    public static Cursor getStickerPackByIdentifier(StickerDatabase dbHelper, String stickerPackIdentifier) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT " + StickerDatabase.TABLE_STICKER_PACK + ".*, " + StickerDatabase.TABLE_STICKER + ".* " + "FROM " +
                StickerDatabase.TABLE_STICKER_PACK + " " + "INNER JOIN " + StickerDatabase.TABLE_STICKER + " ON " +
                StickerDatabase.TABLE_STICKER_PACK + "." + StickerDatabase.ID_STICKER_PACK + " = " + StickerDatabase.TABLE_STICKER + "." +
                StickerDatabase.FK_STICKER_PACK + " WHERE " + StickerDatabase.TABLE_STICKER_PACK + "." +
                StickerDatabase.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?";

        return db.rawQuery(query, new String[]{stickerPackIdentifier});
    }

    public static Cursor getStickerByStickerPackIdentifier(StickerDatabase dbHelper, String stickerPackIdentifier) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + StickerDatabase.TABLE_STICKER + " WHERE " + StickerDatabase.FK_STICKER_PACK + " = " + "(SELECT " +
                StickerDatabase.ID_STICKER_PACK + " FROM " + StickerDatabase.TABLE_STICKER_PACK + " WHERE " +
                StickerDatabase.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?)";

        return db.rawQuery(query, new String[]{stickerPackIdentifier});
    }
}
