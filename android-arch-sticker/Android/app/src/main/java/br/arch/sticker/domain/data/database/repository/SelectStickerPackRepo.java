/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.ANIMATED_STICKER_PACK;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import br.arch.sticker.domain.data.database.StickerDatabaseHelper;

// @formatter:off
public class SelectStickerPackRepo {
    private final static String TAG_LOG = SelectStickerPackRepo.class.getSimpleName();

    private final SQLiteDatabase database;

    public SelectStickerPackRepo(SQLiteDatabase database)
        {
            this.database = database;
        }

    public Cursor selectAllStickerPacks() {
        try{
            String query =
                    "SELECT " +
                            // STICKER PACK
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_TRAY_IMAGE_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.PUBLISHER_EMAIL + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.PUBLISHER_WEBSITE + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.IMAGE_DATA_VERSION + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.AVOID_CACHE + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.ANIMATED_STICKER_PACK + ", " +

                            // STICKER
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.ID_STICKER + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_IS_VALID + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY +
                    " FROM " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK +
                    " INNER JOIN " +
                            StickerDatabaseHelper.TABLE_STICKER +
                    " ON " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.FK_STICKER_PACK +
                    " ORDER BY " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + ";";


            return database.rawQuery(query, null);
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao executar getAllStickerPacks: " + exception.getMessage(), exception);
            return null;
        }
    }

    public Cursor selectStickerPackByIdentifier(String stickerPackIdentifier) {
        try {
            String query =
                "SELECT " +
                        // STICKER PACK
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_TRAY_IMAGE_IN_QUERY + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.PUBLISHER_EMAIL + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.PUBLISHER_WEBSITE + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.IMAGE_DATA_VERSION + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.AVOID_CACHE + ", " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.ANIMATED_STICKER_PACK + ", " +

                        // STICKER
                        StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.ID_STICKER + ", " +
                        StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY + ", " +
                        StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY + ", " +
                        StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_IS_VALID + ", " +
                        StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY +
                " FROM " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + " " +
                "INNER JOIN " +
                        StickerDatabaseHelper.TABLE_STICKER +
                " ON " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = " +
                        StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.FK_STICKER_PACK + " " +
                "WHERE " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?";

             return database.rawQuery(query, new String[]{stickerPackIdentifier});
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao executar getFilteredStickerPackByIdentifier: " + exception.getMessage(), exception);
            return null;
        }
    }

    public Cursor selectFilteredStickerPackByIdentifier(String stickerPackIdentifier) {
        try {
            String query =
                    "SELECT " +
                            // STICKER PACK
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_TRAY_IMAGE_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.PUBLISHER_EMAIL + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.PUBLISHER_WEBSITE + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.IMAGE_DATA_VERSION + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.AVOID_CACHE + ", " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.ANIMATED_STICKER_PACK + ", " +

                            // STICKER
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.ID_STICKER + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_IS_VALID + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY +
                    " FROM " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK +
                    " INNER JOIN " +
                            StickerDatabaseHelper.TABLE_STICKER +
                    " ON " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.FK_STICKER_PACK  +
                    " WHERE " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?" +
                    " AND (" +
                             StickerDatabaseHelper.TABLE_STICKER + "." +  StickerDatabaseHelper.STICKER_IS_VALID + " IS NULL OR " +
                             StickerDatabaseHelper.TABLE_STICKER + "." +  StickerDatabaseHelper.STICKER_IS_VALID + " = ''" +
                        ");";


            return database.rawQuery(query, new String[]{stickerPackIdentifier});
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao executar getStickerPackIsAnimated: " + exception.getMessage(), exception);
            return null;
        }
    }

    public Cursor selectStickerPackIsAnimated(String stickerPackIdentifier) {
        String query =
                "SELECT DISTINCT " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + ANIMATED_STICKER_PACK +
                " FROM " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK +
                " WHERE " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ? " + ";";


        return database.rawQuery(query, new String[]{stickerPackIdentifier});
    }

    public Cursor selectStickerByStickerPackIdentifier(String stickerPackIdentifier) {
        try {
            String query =
                    "SELECT " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.ID_STICKER + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_IS_VALID + ", " +
                            StickerDatabaseHelper.TABLE_STICKER + "." + StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY +
                    " FROM " +
                            StickerDatabaseHelper.TABLE_STICKER +
                    " WHERE " +
                            StickerDatabaseHelper.FK_STICKER_PACK + " = " +
                            "(" +
                                "SELECT " +
                                    StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY +
                                " FROM " +
                                    StickerDatabaseHelper.TABLE_STICKER_PACK +
                                " WHERE " +
                                    StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?" +
                            ")";

            return database.rawQuery(query, new String[]{stickerPackIdentifier});
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao executar getStickerByStickerPackIdentifier: " + exception.getMessage(), exception);
            return null;
        }
    }
}
