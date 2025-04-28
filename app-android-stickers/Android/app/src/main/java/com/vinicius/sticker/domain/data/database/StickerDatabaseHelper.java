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

package com.vinicius.sticker.domain.data.database;

import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StickerDatabaseHelper extends SQLiteOpenHelper {
   private static final String DATABASE_NAME = "stickers.db";
   private static final int DATABASE_VERSION = 1;
   public static final String ID_STICKER_PACKS = "id_sticker_packs";
   public static final String FK_STICKER_PACKS = "fk_sticker_packs";
   public static final String ID_STICKER_PACK = "id_sticker_pack";
   public static final String FK_STICKER_PACK = "fk_sticker_pack";
   public static final String ID_STICKER = "id_sticker";

   public StickerDatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   @Override
   public void onCreate(SQLiteDatabase sqLiteDatabase) {
      sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS  sticker_packs(" +
                                 ID_STICKER_PACKS +
                                 " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                 ANDROID_APP_DOWNLOAD_LINK_IN_QUERY +
                                 " TEXT," +
                                 IOS_APP_DOWNLOAD_LINK_IN_QUERY +
                                 " TEXT" +
                                 ")");

      String fkStickerPacks = String.format(
          "FOREIGN KEY( %s ) REFERENCES sticker_packs( %s ) ON DELETE CASCADE", FK_STICKER_PACKS,
          ID_STICKER_PACKS
      );
      sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS  sticker_pack(" +
                                 ID_STICKER_PACK +
                                 " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                 STICKER_PACK_IDENTIFIER_IN_QUERY +
                                 " TEXT UNIQUE," +
                                 STICKER_PACK_NAME_IN_QUERY +
                                 " TEXT," +
                                 STICKER_PACK_PUBLISHER_IN_QUERY +
                                 " TEXT," +
                                 STICKER_PACK_ICON_IN_QUERY +
                                 " TEXT," +
                                 PUBLISHER_EMAIL +
                                 " TEXT," +
                                 PUBLISHER_WEBSITE +
                                 " TEXT," +
                                 PRIVACY_POLICY_WEBSITE +
                                 " TEXT," +
                                 LICENSE_AGREEMENT_WEBSITE +
                                 " TEXT," +
                                 ANIMATED_STICKER_PACK +
                                 " TEXT," +
                                 FK_STICKER_PACKS +
                                 " INTEGER," +
                                 fkStickerPacks +
                                 ")");

      String fkSticker = String.format(
          "FOREIGN KEY( %s ) REFERENCES sticker_pack( %s ) ON DELETE CASCADE", FK_STICKER_PACK,
          ID_STICKER_PACK
      );
      sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS sticker(" +
                                 ID_STICKER +
                                 " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                 STICKER_FILE_NAME_IN_QUERY +
                                 " TEXT UNIQUE," +
                                 STICKER_FILE_EMOJI_IN_QUERY +
                                 " TEXT," +
                                 STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY +
                                 " TEXT," +
                                 FK_STICKER_PACK +
                                 " INTEGER," +
                                 fkSticker +
                                 ")");
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS sticker_packs");
      db.execSQL("DROP TABLE IF EXISTS sticker_pack");
      db.execSQL("DROP TABLE IF EXISTS sticker");
      onCreate(db);
   }

   public static boolean isDatabaseEmpty(SQLiteDatabase database) {
      Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM sticker_packs", null);
      boolean empty = true;

      if ( cursor.moveToFirst() ) {
         empty = cursor.getInt(0) == 0;
      }
      cursor.close();

      return empty;
   }
}
