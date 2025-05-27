/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.data.database.repository;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.FK_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.FK_STICKER_PACKS;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ID_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ID_STICKER_PACKS;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper;

public class SelectStickerPacks {
   public static Cursor getPackForAllStickerPacks(StickerDatabaseHelper dbHelper) {
      SQLiteDatabase db = dbHelper.getReadableDatabase();

      String query = "SELECT DISTINCT sticker_packs.*, "
                     + "sticker_pack.*, "
                     + "sticker.* "
                     + "FROM sticker_packs "
                     + "INNER JOIN sticker_pack ON sticker_packs."
                     + ID_STICKER_PACKS
                     + " = sticker_pack."
                     + FK_STICKER_PACKS
                     + " "
                     + "INNER JOIN sticker ON sticker_pack."
                     + ID_STICKER_PACK
                     + " = sticker."
                     + FK_STICKER_PACK;

      return db.rawQuery(query, null);
   }

   public static Integer getStickerPackId(SQLiteDatabase db, String identifier) {
      String query = "SELECT " + ID_STICKER_PACK + " FROM sticker_pack WHERE " + STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?";
      Cursor cursor = db.rawQuery(query, new String[]{identifier});
      Integer id = null;
      if ( cursor.moveToFirst() ) {
         id = cursor.getInt(0);
      }
      cursor.close();
      return id;
   }

   public static String getStickerPackIdentifier(SQLiteDatabase db, String identifier) {
      String query = "SELECT " + STICKER_PACK_IDENTIFIER_IN_QUERY + " FROM sticker_pack WHERE " + STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?";
      Cursor cursor = db.rawQuery(query, new String[]{identifier});
      String id = null;
      if ( cursor.moveToFirst() ) {
         id = cursor.getString(0);
      }
      cursor.close();
      return id;
   }

   public static boolean namePackIsPresent(SQLiteDatabase db, String name) {
      String query = "SELECT 1 FROM sticker_pack WHERE " + STICKER_PACK_NAME_IN_QUERY + " = ?";
      Cursor cursor = db.rawQuery(query, new String[]{name});
      boolean exists = cursor.moveToFirst();
      cursor.close();
      return exists;
   }

   public static boolean identifierPackIsPresent(SQLiteDatabase db, String identifier) {
      String query = "SELECT 1 FROM sticker_pack WHERE " + STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?";
      Cursor cursor = db.rawQuery(query, new String[]{identifier});
      boolean exists = cursor.moveToFirst();
      cursor.close();
      return exists;
   }
}
