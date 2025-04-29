/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

package com.vinicius.sticker.domain.data.database.repository;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.FK_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.FK_STICKER_PACKS;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ID_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ID_STICKER_PACKS;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper;

public class SelectStickerPacks {
   public static Cursor getPackForAllStickerPacks(StickerDatabaseHelper dbHelper) {
      SQLiteDatabase db = dbHelper.getReadableDatabase();

      String query = "SELECT DISTINCT sticker_packs.*, " +
          "sticker_pack.*, " +
          "sticker.* " +
          "FROM sticker_packs " +
          "INNER JOIN sticker_pack ON sticker_packs." +
          ID_STICKER_PACKS +
          " = sticker_pack." +
          FK_STICKER_PACKS +
          " " +
          "INNER JOIN sticker ON sticker_pack." +
          ID_STICKER_PACK +
          " = sticker." +
          FK_STICKER_PACK;

      return db.rawQuery(query, null);
   }
}
