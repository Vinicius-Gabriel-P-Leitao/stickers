/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.manager;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_ICON_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY;

import android.database.Cursor;
import android.util.JsonReader;

import com.vinicius.sticker.domain.builder.JsonParserStickerPackBuilder;
import com.vinicius.sticker.domain.builder.StickerPackParserJsonBuilder;
import com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper;
import com.vinicius.sticker.domain.data.database.repository.SelectStickerPacks;
import com.vinicius.sticker.domain.data.model.StickerPack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StickerPackLoaderManager {
    private static List<StickerPack> stickerPackList;

    public static List<StickerPack> getStickerPackList(StickerDatabaseHelper dbHelper) {
        if (stickerPackList == null) {
            readContentFile(Objects.requireNonNull(dbHelper));
        }

        return stickerPackList;
    }

    public static synchronized void readContentFile(StickerDatabaseHelper dbHelper) {
        StringReader stringReaderStickerPack =
                new StringReader(getPackForAllContentJsonBuilder(dbHelper));
        JsonReader jsonReaderStickerPack = new JsonReader(stringReaderStickerPack);

        try {
            stickerPackList = JsonParserStickerPackBuilder.readStickerPacks(jsonReaderStickerPack);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static String getPackForAllContentJsonBuilder(StickerDatabaseHelper dbHelper) {
        Cursor cursor = SelectStickerPacks.getPackForAllStickerPacks(dbHelper);
        Map<String, StickerPackParserJsonBuilder> packMap = new LinkedHashMap<>();

        if (cursor.moveToFirst()) {
            do {
                String identifier =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY));
                String name =
                        cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY));
                String publisher =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY));
                String trayImageFile =
                        cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_ICON_IN_QUERY));
                String imageDataVersion =
                        cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION));
                boolean avoidCache = cursor.getInt(cursor.getColumnIndexOrThrow(AVOID_CACHE)) != 0;
                String publisherEmail =
                        cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
                String publisherWebsite =
                        cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
                String privacyPolicyWebsite =
                        cursor.getString(cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
                String licenseAgreementWebsite =
                        cursor.getString(cursor.getColumnIndexOrThrow(LICENSE_AGREEMENT_WEBSITE));
                boolean animatedStickerPack =
                        cursor.getInt(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) != 0;

                String imageFile =
                        cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                String emojis =
                        cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                String accessibilityText =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                StickerPackParserJsonBuilder builder = packMap.get(identifier);
                if (builder == null) {
                    try {
                        builder =
                                new StickerPackParserJsonBuilder()
                                        .setIdentifier(identifier)
                                        .setName(name)
                                        .setPublisher(publisher)
                                        .setTrayImageFile(trayImageFile)
                                        .setImageDataVersion(imageDataVersion)
                                        .setAvoidCache(avoidCache)
                                        .setPublisherEmail(publisherEmail)
                                        .setPublisherWebsite(publisherWebsite)
                                        .setPrivacyPolicyWebsite(privacyPolicyWebsite)
                                        .setLicenseAgreementWebsite(licenseAgreementWebsite);
                    } catch (JSONException exception) {
                        throw new RuntimeException(exception);
                    }

                    try {
                        builder.setAnimatedStickerPack(animatedStickerPack);
                    } catch (JSONException exception) {
                        throw new RuntimeException(exception);
                    }
                    packMap.put(identifier, builder);
                }
                try {
                    builder.addSticker(imageFile, emojis, accessibilityText);

                } catch (JSONException exception) {
                    throw new RuntimeException(exception);
                }

            } while (cursor.moveToNext());

            cursor.close();
        }

        JSONArray stickerPacksArray = new JSONArray();
        for (StickerPackParserJsonBuilder builder : packMap.values()) {
            JSONObject packJson = null;

            try {
                packJson = new JSONObject(builder.build());
            } catch (JSONException exception) {
                throw new RuntimeException(exception);
            }

            stickerPacksArray.put(packJson);
        }

        JSONObject finalJson = new JSONObject();
        try {
            finalJson.put("android_play_store_link", "");
            finalJson.put("ios_app_store_link", "");
            finalJson.put("sticker_packs", stickerPacksArray);
        } catch (JSONException exception) {
            throw new RuntimeException(exception);
        }

        try {
            return finalJson.toString(2);
        } catch (JSONException exception) {
            throw new RuntimeException(exception);
        }
    }
}
