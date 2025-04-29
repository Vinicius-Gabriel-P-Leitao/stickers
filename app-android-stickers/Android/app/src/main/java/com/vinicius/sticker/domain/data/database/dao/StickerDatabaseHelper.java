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

package com.vinicius.sticker.domain.data.database.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StickerDatabaseHelper extends SQLiteOpenHelper {
   private static StickerDatabaseHelper instance;

   private static final String DATABASE_NAME = "stickers.db";
   private static final int DATABASE_VERSION = 1;

   public static final String STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier";
   public static final String STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name";
   public static final String STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher";
   public static final String STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon";
   public static final String ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link";
   public static final String IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link";
   public static final String PUBLISHER_EMAIL = "sticker_pack_publisher_email";
   public static final String PUBLISHER_WEBSITE = "sticker_pack_publisher_website";
   public static final String PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website";
   public static final String IMAGE_DATA_VERSION = "image_data_version";
   public static final String AVOID_CACHE = "whatsapp_will_not_cache_stickers";
   public static final String LICENSE_AGREEMENT_WEBSITE = "sticker_pack_license_agreement_website";
   public static final String ANIMATED_STICKER_PACK = "animated_sticker_pack";
   public static final String STICKER_FILE_NAME_IN_QUERY = "sticker_file_name";
   public static final String STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji";
   public static final String STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY = "sticker_accessibility_text";
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
                                 " VARCHAR(100)," +
                                 IOS_APP_DOWNLOAD_LINK_IN_QUERY +
                                 " VARCHAR(100)" +
                                 ")");

      String fkStickerPacks = String.format("FOREIGN KEY( %s ) REFERENCES sticker_packs( %s ) ON DELETE CASCADE", FK_STICKER_PACKS, ID_STICKER_PACKS);
      sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS  sticker_pack(" +
                                 ID_STICKER_PACK +
                                 " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                 STICKER_PACK_IDENTIFIER_IN_QUERY +
                                 " UUID TEXT NOT NULL UNIQUE," +
                                 STICKER_PACK_NAME_IN_QUERY +
                                 " VARCHAR(20) NOT NULL," +
                                 STICKER_PACK_PUBLISHER_IN_QUERY +
                                 " VARCHAR(20) NOT NULL," +
                                 STICKER_PACK_ICON_IN_QUERY +
                                 " CHAR(3) NOT NULL," +
                                 PUBLISHER_EMAIL +
                                 " VARCHAR(60)," +
                                 PUBLISHER_WEBSITE +
                                 " VARCHAR(40)," +
                                 PRIVACY_POLICY_WEBSITE +
                                 " VARCHAR(100)," +
                                 LICENSE_AGREEMENT_WEBSITE +
                                 " VARCHAR(100)," +
                                 ANIMATED_STICKER_PACK +
                                 " CHAR(1) NOT NULL," +
                                 IMAGE_DATA_VERSION +
                                 " CHAR(4)," +
                                 AVOID_CACHE +
                                 " CHAR(5)," +
                                 FK_STICKER_PACKS +
                                 " INTEGER," +
                                 fkStickerPacks +
                                 ")");

      String fkSticker = String.format("FOREIGN KEY( %s ) REFERENCES sticker_pack( %s ) ON DELETE CASCADE", FK_STICKER_PACK, ID_STICKER_PACK);
      sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS sticker(" +
                                 ID_STICKER +
                                 " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                 STICKER_FILE_NAME_IN_QUERY +
                                 " VARCHAR(255) NOT NULL," +
                                 STICKER_FILE_EMOJI_IN_QUERY +
                                 " TEXT NOT NULL," +
                                 STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY +
                                 " TEXT NOT NULL," +
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

   public static synchronized StickerDatabaseHelper getInstance(Context context) {
      if ( instance == null ) {
         instance = new StickerDatabaseHelper(context.getApplicationContext());
      }
      return instance;
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
