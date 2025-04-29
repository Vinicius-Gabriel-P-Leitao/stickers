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
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper;

public class DeleteStickerPacks {

   public static int deleteStickersOfPack(Context context, String stickerPackIdentifier) {
      StickerDatabaseHelper dbHelper = StickerDatabaseHelper.getInstance(context);
      SQLiteDatabase db = dbHelper.getWritableDatabase();

      Integer stickerPackId = SelectStickerPacks.getStickerPackId(db, stickerPackIdentifier);

      return db.delete("sticker", FK_STICKER_PACK + " = ?", new String[]{String.valueOf(stickerPackId)});
   }

   public static int deleteStickerPackFromDatabase(Context context, String identifier) {
      StickerDatabaseHelper dbHelper = StickerDatabaseHelper.getInstance(context);
      SQLiteDatabase db = dbHelper.getWritableDatabase();

      return db.delete("sticker_pack", STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?", new String[]{identifier});
   }
}
