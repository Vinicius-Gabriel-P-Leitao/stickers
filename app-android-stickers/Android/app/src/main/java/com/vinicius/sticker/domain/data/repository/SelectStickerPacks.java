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

package com.vinicius.sticker.domain.data.repository;

import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.FK_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.FK_STICKER_PACKS;
import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.ID_STICKER;
import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.ID_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.ID_STICKER_PACKS;
import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.vinicius.sticker.domain.data.database.StickerDatabaseHelper;

public class SelectStickerPacks {
   public static Cursor getPackForAllStickerPacks(Uri uri, StickerDatabaseHelper dbHelper) {
      SQLiteDatabase db = dbHelper.getReadableDatabase();

      String query = "SELECT * FROM sticker_packs " +
          "INNER JOIN sticker_pack ON sticker_packs." +
          ID_STICKER_PACKS +
          " = sticker_pack." +
          FK_STICKER_PACKS +
          " " +
          "INNER JOIN sticker  ON sticker_pack." +
          ID_STICKER_PACK +
          " = sticker." +
          FK_STICKER_PACK;

      return db.rawQuery(query, null);
   }

   public static Cursor getCursorForSingleStickerPack(Uri uri, StickerDatabaseHelper dbHelper) {
      SQLiteDatabase db = dbHelper.getReadableDatabase();

      String query = "SELECT * FROM sticker_packs " +
          "INNER JOIN sticker_pack ON sticker_packs." +
          ID_STICKER_PACKS +
          " = sticker_pack." +
          FK_STICKER_PACKS +
          " " +
          "INNER JOIN sticker  ON sticker_pack." +
          ID_STICKER_PACK +
          " = STICKER_TABLE." +
          FK_STICKER_PACK;

      return db.rawQuery(query, null);
   }

   public static Cursor getStickersForAStickerPack(Uri uri, StickerDatabaseHelper dbHelper) {
      SQLiteDatabase db = dbHelper.getReadableDatabase();

      String[] projectionStickerPack =
          {ID_STICKER, STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY, STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY};
      String selectionStickerPack = FK_STICKER_PACK + " = ?";
      String[] selectionArgsStickerPack = new String[]{uri.getLastPathSegment()};

      return db.query("sticker", projectionStickerPack, selectionStickerPack, selectionArgsStickerPack, null, null, null);
   }
}
