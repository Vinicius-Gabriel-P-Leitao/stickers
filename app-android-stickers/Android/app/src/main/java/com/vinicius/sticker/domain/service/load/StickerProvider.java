/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.load;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_FILE_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_PACK_ICON_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_PACK_PUBLISHER_IN_QUERY;

import android.database.Cursor;
import android.util.JsonReader;

import com.vinicius.sticker.domain.builder.JsonParserStickerPackBuilder;
import com.vinicius.sticker.domain.builder.StickerPackParserJsonBuilder;
import com.vinicius.sticker.domain.data.database.dao.StickerDatabase;
import com.vinicius.sticker.domain.data.database.repository.SelectStickerPacks;
import com.vinicius.sticker.domain.data.model.Sticker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StickerProvider {
    private static List<Sticker> stickerList;

    public static List<Sticker> getStickerList(StickerDatabase dbHelper, String stickerPackIdentifier) {
        if (stickerList == null) {
            readContentFile(dbHelper, stickerPackIdentifier);
        }

        return stickerList;
    }

    public static synchronized void readContentFile(StickerDatabase dbHelper, String stickerPackIdentifier) {
        StringReader stringReaderStickerPack = new StringReader(getPackForJsonBuilder(dbHelper, stickerPackIdentifier));
        JsonReader jsonReaderStickerPack = new JsonReader(stringReaderStickerPack);

        try {
            stickerList = JsonParserStickerPackBuilder.readStickers(jsonReaderStickerPack);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static String getPackForJsonBuilder(StickerDatabase dbHelper, String stickerPackIdentifier) {
        Cursor cursor = SelectStickerPacks.getStickerByStickerPackIdentifier(dbHelper, stickerPackIdentifier);
        Map<String, StickerPackParserJsonBuilder> packMap = new LinkedHashMap<>();

        if (cursor.moveToFirst()) {
            do {
                String imageFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                String emojis = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                String accessibilityText = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                StickerPackParserJsonBuilder builder = packMap.get(stickerPackIdentifier);
                if (builder == null) {
                    builder = new StickerPackParserJsonBuilder();
                    packMap.put(stickerPackIdentifier, builder);
                }

                try {
                    builder.addSticker(imageFile, emojis, accessibilityText);
                } catch (JSONException exception) {
                    throw new RuntimeException("Failed to add sticker: " + exception.getMessage(), exception);
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        StickerPackParserJsonBuilder builder = packMap.get(stickerPackIdentifier);
        if (builder == null) {
            throw new RuntimeException("StickerPackBuilder not found for: " + stickerPackIdentifier);
        }

        try {
            String jsonString = builder.build();

            if (jsonString.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray stickerArray = jsonObject.getJSONArray("stickers");
                return stickerArray.toString(2);
            }
            return jsonString;
        } catch (JSONException exception) {
            throw new RuntimeException("Failed to process JSON: " + exception.getMessage(), exception);
        }
    }
}
