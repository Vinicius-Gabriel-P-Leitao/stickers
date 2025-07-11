/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// @formatter:off
public class StickerDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "stickers.db";
    private static final int DATABASE_VERSION = 1;

    // Tabelas
    public static final String TABLE_STICKER_PACK = "sticker_pack";
    public static final String TABLE_STICKER = "sticker";

    // Colunas sticker_pack
    public static final String STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier";
    public static final String STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name";
    public static final String STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher";
    public static final String STICKER_PACK_TRAY_IMAGE_IN_QUERY = "sticker_pack_icon";
    public static final String PUBLISHER_EMAIL = "sticker_pack_publisher_email";
    public static final String PUBLISHER_WEBSITE = "sticker_pack_publisher_website";
    public static final String PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website";
    public static final String LICENSE_AGREEMENT_WEBSITE = "sticker_pack_license_agreement_website";
    public static final String ANIMATED_STICKER_PACK = "animated_sticker_pack";
    public static final String IMAGE_DATA_VERSION = "image_data_version";
    public static final String AVOID_CACHE = "whatsapp_will_not_cache_stickers";
    public static final String ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link";
    public static final String IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link";

    // Colunas sticker
    public static final String ID_STICKER = "id_sticker";
    public static final String STICKER_FILE_NAME_IN_QUERY = "sticker_file_name";
    public static final String STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji";
    public static final String STICKER_IS_VALID = "sticker_is_valid";
    public static final String STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY = "sticker_accessibility_text";
    public static final String FK_STICKER_PACK = "fk_sticker_pack";

    // Valores de campos que tem são constantes no app todo
    public static final int CHAR_IDENTIFIER_COUNT_MAX = 36;
    public static final int CHAR_NAME_COUNT_MAX = 35;
    public static final int CHAR_PUBLISHER_COUNT_MAX = 40;

    private static volatile StickerDatabaseHelper  instance;


    public StickerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_STICKER_PACK +
                        " (" +
                            STICKER_PACK_IDENTIFIER_IN_QUERY + " TEXT PRIMARY KEY " +
                                        "CHECK(length(" + STICKER_PACK_IDENTIFIER_IN_QUERY +") <= "+ CHAR_IDENTIFIER_COUNT_MAX +"), " +
                            STICKER_PACK_NAME_IN_QUERY + " VARCHAR NOT NULL " +
                                        "CHECK(length(" + STICKER_PACK_NAME_IN_QUERY +") <= "+ CHAR_NAME_COUNT_MAX + "), " +
                            STICKER_PACK_PUBLISHER_IN_QUERY + " VARCHAR(20) NOT NULL, " +
                            STICKER_PACK_TRAY_IMAGE_IN_QUERY + " CHAR(3) NOT NULL, " +
                            PUBLISHER_EMAIL + " VARCHAR(60), " +
                            PUBLISHER_WEBSITE + " VARCHAR " +
                                        "CHECK(length("+ PUBLISHER_WEBSITE +") <= " +CHAR_PUBLISHER_COUNT_MAX +"), " +
                            PRIVACY_POLICY_WEBSITE + " VARCHAR(100), " +
                            LICENSE_AGREEMENT_WEBSITE + " VARCHAR(100), " +
                            ANIMATED_STICKER_PACK + " CHAR(1) NOT NULL, " +
                            IMAGE_DATA_VERSION + " CHAR(4), " +
                            AVOID_CACHE + " CHAR(5), " +
                            ANDROID_APP_DOWNLOAD_LINK_IN_QUERY + " VARCHAR(100), " +
                            IOS_APP_DOWNLOAD_LINK_IN_QUERY + " VARCHAR(100)" +
                        ")"
        );

        String fkSticker = String.format(
                "FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE",
                FK_STICKER_PACK,
                TABLE_STICKER_PACK,
                STICKER_PACK_IDENTIFIER_IN_QUERY
        );

        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_STICKER +
                        " (" +
                            ID_STICKER + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            STICKER_FILE_NAME_IN_QUERY + " VARCHAR(255) NOT NULL, " +
                            STICKER_FILE_EMOJI_IN_QUERY + " TEXT NOT NULL, " +
                            STICKER_IS_VALID + " VARCHAR(255), " +
                            STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY + " TEXT NOT NULL, " +
                            FK_STICKER_PACK + " TEXT, " + fkSticker +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STICKER_PACK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STICKER);

        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    public static synchronized StickerDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new StickerDatabaseHelper(context.getApplicationContext());
        }

        return instance;
    }
}
