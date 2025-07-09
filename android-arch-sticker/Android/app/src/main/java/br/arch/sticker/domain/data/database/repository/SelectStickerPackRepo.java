/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_IS_VALID;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER;

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

    public Cursor getAllStickerPacks() {
        try{
            String query =
                    "SELECT DISTINCT " +
                            StickerDatabaseHelper.TABLE_STICKER_PACKS + ".*, " + StickerDatabaseHelper.TABLE_STICKER_PACK + ".*, " + TABLE_STICKER + ".* " +
                    "FROM " +
                            StickerDatabaseHelper.TABLE_STICKER_PACKS + " " +
                    "INNER JOIN " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK +
                    " ON " +
                            StickerDatabaseHelper.TABLE_STICKER_PACKS + "." + StickerDatabaseHelper.ID_STICKER_PACKS + " = " + StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.FK_STICKER_PACKS + " " +
                    "INNER JOIN " +
                            TABLE_STICKER +
                    " ON " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = " + TABLE_STICKER + "." + StickerDatabaseHelper.FK_STICKER_PACK;

            return database.rawQuery(query, null);
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao executar getAllStickerPacks: " + exception.getMessage(), exception);
            return null;
        }
    }

    public  Cursor getStickerPackByIdentifier( String stickerPackIdentifier) {
        try {
            String query =
                "SELECT DISTINCT " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + ".*, " + TABLE_STICKER + ".* " +
                "FROM " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + " " +
                "INNER JOIN " +
                        TABLE_STICKER +
                " ON " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = " + TABLE_STICKER + "." + StickerDatabaseHelper.FK_STICKER_PACK + " " +
                "WHERE " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?";

             return database.rawQuery(query, new String[]{stickerPackIdentifier});
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao executar getFilteredStickerPackByIdentifier: " + exception.getMessage(), exception);
            return null;
        }
    }

    public  Cursor getFilteredStickerPackByIdentifier( String stickerPackIdentifier) {
        try {
            String query =
                    "SELECT DISTINCT " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + ".*, " + TABLE_STICKER + ".*" +
                        " FROM " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK +
                        " INNER JOIN " +
                            TABLE_STICKER +
                        " ON " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = " + TABLE_STICKER + "." + StickerDatabaseHelper.FK_STICKER_PACK  +
                        " WHERE " +
                            StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?" +
                        " AND (" +
                                TABLE_STICKER + "." + STICKER_IS_VALID + " IS NULL OR " +
                                TABLE_STICKER + "." + STICKER_IS_VALID + " = ''" +
                            ");";


            return database.rawQuery(query, new String[]{stickerPackIdentifier});
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao executar getStickerPackIsAnimated: " + exception.getMessage(), exception);
            return null;
        }
    }

    public  Cursor getStickerPackIsAnimated( String stickerPackIdentifier) {
        String query =
                "SELECT DISTINCT " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + ANIMATED_STICKER_PACK +
                    " FROM " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK +
                    " WHERE " +
                        StickerDatabaseHelper.TABLE_STICKER_PACK + "." + StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ? " + ";";


        return database.rawQuery(query, new String[]{stickerPackIdentifier});
    }

    public  Cursor getStickerByStickerPackIdentifier( String stickerPackIdentifier) {
        try {
            String query =
                    "SELECT * FROM " +
                            TABLE_STICKER +
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
        } catch (SQLException | IllegalStateException e) {
            Log.e(TAG_LOG, "Erro ao executar getStickerByStickerPackIdentifier: " + e.getMessage(), e);
            return null;
        }
    }
}
