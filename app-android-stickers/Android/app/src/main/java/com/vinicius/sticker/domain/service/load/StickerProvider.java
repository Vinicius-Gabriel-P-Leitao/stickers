/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.load;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_FILE_NAME_IN_QUERY;

import android.database.Cursor;

import com.vinicius.sticker.domain.data.database.dao.StickerDatabase;
import com.vinicius.sticker.domain.data.database.repository.SelectStickerPacks;
import com.vinicius.sticker.domain.data.model.Sticker;

import java.util.ArrayList;
import java.util.List;

public class StickerProvider {
    public static List<Sticker> getStickerList(StickerDatabase dbHelper, String stickerPackIdentifier) {
        return getPackForJsonBuilder(dbHelper, stickerPackIdentifier);
    }

    public static List<Sticker> getPackForJsonBuilder(StickerDatabase dbHelper, String stickerPackIdentifier) {
        Cursor cursor = SelectStickerPacks.getStickerByStickerPackIdentifier(dbHelper, stickerPackIdentifier);
        List<Sticker> stickerList = new ArrayList<>();

        try {
            if (cursor.moveToFirst()) {
                do {
                    String imageFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                    String emojis = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                    String accessibilityText = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                    Sticker sticker = new Sticker(imageFile, emojis, accessibilityText);
                    stickerList.add(sticker);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return stickerList;
    }
}
