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
import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.ID_STICKER;
import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.ID_STICKER_PACK;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_FILE_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_ICON_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_PUBLISHER_IN_QUERY;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.vinicius.sticker.domain.data.database.StickerDatabaseHelper;

public class SelectStickerPacks {
   public static Cursor getPackForAllStickerPacks(Uri uri, StickerDatabaseHelper dbHelper) {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      String[] projection = {ID_STICKER_PACK, STICKER_PACK_IDENTIFIER_IN_QUERY, STICKER_PACK_NAME_IN_QUERY, STICKER_PACK_PUBLISHER_IN_QUERY, STICKER_PACK_ICON_IN_QUERY};
      return db.query("sticker_pack", projection, null, null, null, null, null);
   }

   public static Cursor getCursorForSingleStickerPack(Uri uri, StickerDatabaseHelper dbHelper) {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      String[] projection = {ID_STICKER_PACK, STICKER_PACK_IDENTIFIER_IN_QUERY, STICKER_PACK_NAME_IN_QUERY, STICKER_PACK_PUBLISHER_IN_QUERY, STICKER_PACK_ICON_IN_QUERY, PUBLISHER_EMAIL, PUBLISHER_WEBSITE, PRIVACY_POLICY_WEBSITE, LICENSE_AGREEMENT_WEBSITE, ANIMATED_STICKER_PACK};
      String selection = ID_STICKER_PACK + " = ?";
      String[] selectionArgs = new String[]{uri.getLastPathSegment()};

      return db.query("sticker_pack", projection, selection, selectionArgs, null, null, null);
   }

   public static Cursor getStickersForAStickerPack(Uri uri, StickerDatabaseHelper dbHelper) {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      String[] projection = {ID_STICKER, STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY, STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY};
      String selection = FK_STICKER_PACK + " = ?";
      String[] selectionArgs = new String[]{uri.getLastPathSegment()};

      return db.query("sticker", projection, selection, selectionArgs, null, null, null);
   }
}
