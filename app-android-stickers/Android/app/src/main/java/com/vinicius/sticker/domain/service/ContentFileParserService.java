/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package com.vinicius.sticker.domain.service;

import static com.vinicius.sticker.core.validation.StickerValidator.EMOJI_MAX_LIMIT;

import android.text.TextUtils;
import android.util.JsonReader;

import androidx.annotation.NonNull;

import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContentFileParserService {
    private static final String FIELD_STICKER_IMAGE_FILE = "image_file";
    private static final String FIELD_STICKER_EMOJIS = "emojis";
    private static final String FIELD_STICKER_ACCESSIBILITY_TEXT = "accessibility_text";

    @NonNull
    public static List<StickerPack> readStickerPacks(@NonNull JsonReader reader) throws IOException, IllegalStateException {
        List<StickerPack> stickerPackList = new ArrayList<>();
        String androidPlayStoreLink = null;
        String iosAppStoreLink = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if ("android_play_store_link".equals(key)) {
                androidPlayStoreLink = reader.nextString();
            } else if ("ios_app_store_link".equals(key)) {
                iosAppStoreLink = reader.nextString();
            } else if ("sticker_packs".equals(key)) {
                reader.beginArray();
                while (reader.hasNext()) {
                    StickerPack stickerPack = readStickerPack(reader);
                    stickerPackList.add(stickerPack);
                }
                reader.endArray();
            } else {
                throw new IllegalStateException("unknown field in json: " + key);
            }
        }
        reader.endObject();
        if (stickerPackList.isEmpty()) {
            throw new IllegalStateException("sticker pack list cannot be empty");
        }
        for (StickerPack stickerPack : stickerPackList) {
            stickerPack.setAndroidPlayStoreLink(androidPlayStoreLink);
            stickerPack.setIosAppStoreLink(iosAppStoreLink);
        }
        return stickerPackList;
    }

    @NonNull
    public static StickerPack readStickerPack(@NonNull JsonReader reader) throws IOException, IllegalStateException {
        reader.beginObject();
        String identifier = null;
        String name = null;
        String publisher = null;
        String trayImageFile = null;
        String publisherEmail = null;
        String publisherWebsite = null;
        String privacyPolicyWebsite = null;
        String licenseAgreementWebsite = null;
        String imageDataVersion = "";
        boolean avoidCache = false;
        boolean animatedStickerPack = false;
        List<Sticker> stickerList = null;
        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case "identifier":
                    identifier = reader.nextString();
                    break;
                case "name":
                    name = reader.nextString();
                    break;
                case "publisher":
                    publisher = reader.nextString();
                    break;
                case "tray_image_file":
                    trayImageFile = reader.nextString();
                    break;
                case "publisher_email":
                    publisherEmail = reader.nextString();
                    break;
                case "publisher_website":
                    publisherWebsite = reader.nextString();
                    break;
                case "privacy_policy_website":
                    privacyPolicyWebsite = reader.nextString();
                    break;
                case "license_agreement_website":
                    licenseAgreementWebsite = reader.nextString();
                    break;
                case "stickers":
                    stickerList = readStickers(reader);
                    break;
                case "image_data_version":
                    imageDataVersion = reader.nextString();
                    break;
                case "avoid_cache":
                    avoidCache = reader.nextBoolean();
                    break;
                case "animated_sticker_pack":
                    animatedStickerPack = reader.nextBoolean();
                    break;
                default:
                    reader.skipValue();
            }
        }
        if (TextUtils.isEmpty(identifier)) {
            throw new IllegalStateException("identifier cannot be empty");
        }
        if (TextUtils.isEmpty(name)) {
            throw new IllegalStateException("name cannot be empty");
        }
        if (TextUtils.isEmpty(publisher)) {
            throw new IllegalStateException("publisher cannot be empty");
        }
        if (TextUtils.isEmpty(trayImageFile)) {
            throw new IllegalStateException("tray_image_file cannot be empty");
        }
        if (stickerList == null || stickerList.isEmpty()) {
            throw new IllegalStateException("sticker list is empty");
        }
        if (identifier == null || identifier.contains("..") || identifier.contains("/")) {
            throw new IllegalStateException("identifier should not contain .. or / to prevent directory traversal");
        }
        if (TextUtils.isEmpty(imageDataVersion)) {
            throw new IllegalStateException("image_data_version should not be empty");
        }
        reader.endObject();
        final StickerPack stickerPack =
                new StickerPack(identifier, name, publisher, trayImageFile, publisherEmail, publisherWebsite, privacyPolicyWebsite, licenseAgreementWebsite, imageDataVersion, avoidCache, animatedStickerPack);
        stickerPack.setStickers(stickerList);
        return stickerPack;
    }

    @NonNull
    private static List<Sticker> readStickers(@NonNull JsonReader reader) throws IOException, IllegalStateException {
        reader.beginArray();
        List<Sticker> stickerList = new ArrayList<>();

        while (reader.hasNext()) {
            reader.beginObject();
            String imageFile = null;
            String accessibilityText = null;
            List<String> emojis = new ArrayList<>(EMOJI_MAX_LIMIT);
            while (reader.hasNext()) {
                final String key = reader.nextName();
                if (FIELD_STICKER_IMAGE_FILE.equals(key)) {
                    imageFile = reader.nextString();
                } else if (FIELD_STICKER_EMOJIS.equals(key)) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        String emoji = reader.nextString();
                        if (!TextUtils.isEmpty(emoji)) {
                            emojis.add(emoji);
                        }
                    }
                    reader.endArray();
                } else if (FIELD_STICKER_ACCESSIBILITY_TEXT.equals(key)) {
                    accessibilityText = reader.nextString();
                } else {
                    throw new IllegalStateException("unknown field in json: " + key);
                }
            }
            reader.endObject();
            if (imageFile == null || TextUtils.isEmpty(imageFile)) {
                throw new IllegalStateException("sticker image_file cannot be empty");
            }
            if (!imageFile.endsWith(".webp")) {
                throw new IllegalStateException("image file for stickers should be webp files, image file is: " + imageFile);
            }
            if (imageFile.contains("..") || imageFile.contains("/")) {
                throw new IllegalStateException(
                        "the file name should not contain .. or / to prevent directory traversal, image file is:" + imageFile);
            }
            stickerList.add(new Sticker(imageFile, emojis, accessibilityText));
        }
        reader.endArray();
        return stickerList;
    }

}
