/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.load;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.ID_STICKER_PACKS;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
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
import android.util.Log;

import com.vinicius.sticker.domain.builder.JsonParserStickerPackBuilder;
import com.vinicius.sticker.domain.builder.StickerPackParserJsonBuilder;
import com.vinicius.sticker.domain.data.database.dao.StickerDatabase;
import com.vinicius.sticker.domain.data.database.repository.SelectStickerPacks;
import com.vinicius.sticker.domain.data.model.StickerPack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class StickerPackProvider {
    private static StickerPack stickerPack;

    public static StickerPack getStickerPack(StickerDatabase dbHelper, String stickerPackIdentifier) {
        if (stickerPack == null) {
            readContentFile(dbHelper, stickerPackIdentifier);
        }

        return stickerPack;
    }

    public static synchronized void readContentFile(StickerDatabase dbHelper, String stickerPackIdentifier) {
        String packForJsonBuilder = getPackForJsonBuilder(dbHelper, stickerPackIdentifier);
        StringReader stringReaderStickerPack = new StringReader(packForJsonBuilder);
        Log.d("JsonReader", packForJsonBuilder);
        JsonReader jsonReaderStickerPack = new JsonReader(stringReaderStickerPack);

        try {
            stickerPack = JsonParserStickerPackBuilder.readStickerPack(jsonReaderStickerPack);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static String getPackForJsonBuilder(StickerDatabase dbHelper, String stickerPackIdentifier) {
        Cursor cursor = SelectStickerPacks.getStickerPackByIdentifier(dbHelper, stickerPackIdentifier);
        Map<String, StickerPackParserJsonBuilder> packMap = new LinkedHashMap<>();

        if (cursor.moveToFirst()) {
            do {
                String identifier = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY));
                String publisher = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY));
                String trayImageFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_ICON_IN_QUERY));
                String imageDataVersion = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION));
                boolean avoidCache = cursor.getInt(cursor.getColumnIndexOrThrow(AVOID_CACHE)) != 0;
                String publisherEmail = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
                String publisherWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
                String privacyPolicyWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
                String licenseAgreementWebsite = cursor.getString(cursor.getColumnIndexOrThrow(LICENSE_AGREEMENT_WEBSITE));
                boolean animatedStickerPack = cursor.getInt(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) != 0;

                String imageFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                String emojis = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                String accessibilityText = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                StickerPackParserJsonBuilder builder = packMap.get(identifier);
                if (builder == null) {
                    try {
                        builder = new StickerPackParserJsonBuilder().setIdentifier(identifier).setName(name).setPublisher(publisher)
                                .setTrayImageFile(trayImageFile).setImageDataVersion(imageDataVersion).setAvoidCache(avoidCache)
                                .setPublisherEmail(publisherEmail).setPublisherWebsite(publisherWebsite).setPrivacyPolicyWebsite(privacyPolicyWebsite)
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

        StickerPackParserJsonBuilder builder = packMap.get(stickerPackIdentifier);

        if (builder == null) {
            throw new RuntimeException("StickerPackBuilder não encontrado para: " + stickerPackIdentifier);
        }

        JSONObject finalJson = null;
        try {
            finalJson = new JSONObject(builder.build());
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

