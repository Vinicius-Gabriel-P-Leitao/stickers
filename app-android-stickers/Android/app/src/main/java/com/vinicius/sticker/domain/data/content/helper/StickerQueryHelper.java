/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.data.content.helper;

import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_IS_VALID;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.vinicius.sticker.domain.data.database.StickerDatabase;
import com.vinicius.sticker.domain.data.database.repository.SelectStickerPackRepo;
import com.vinicius.sticker.domain.data.model.Sticker;

import java.util.ArrayList;
import java.util.List;

public class StickerQueryHelper {
    @NonNull
    public static Cursor fetchStickerData(@NonNull Context context, @NonNull Uri uri, @NonNull List<Sticker> stickerList) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY, STICKER_IS_VALID, STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY});

        for (Sticker sticker : stickerList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(sticker.imageFileName);
            builder.add(sticker.emojis);
            builder.add(sticker.stickerIsValid);
            builder.add(sticker.accessibilityText);
        }

        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    public static List<Sticker> fetchStickerListFromDatabase(StickerDatabase dbHelper, String stickerPackIdentifier) {
        Cursor cursor = SelectStickerPackRepo.getStickerByStickerPackIdentifier(dbHelper, stickerPackIdentifier);
        List<Sticker> stickerList = new ArrayList<>();

        try {
            if (cursor.moveToFirst()) {
                do {
                    String imageFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                    String emojis = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                    String stickerIsValid = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_IS_VALID));
                    String accessibilityText = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                    Sticker sticker = new Sticker(imageFile, emojis, stickerIsValid, accessibilityText, stickerPackIdentifier);
                    stickerList.add(sticker);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return stickerList;
    }
}
