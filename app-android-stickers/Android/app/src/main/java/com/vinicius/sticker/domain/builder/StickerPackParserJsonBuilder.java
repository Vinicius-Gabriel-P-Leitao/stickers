/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.builder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StickerPackParserJsonBuilder {
    private final JSONObject stickerPackJson;
    private final JSONArray stickersArray;

    public StickerPackParserJsonBuilder() {
        stickerPackJson = new JSONObject();
        stickersArray = new JSONArray();
    }

    public StickerPackParserJsonBuilder setIdentifier(String identifier) throws JSONException {
        stickerPackJson.put("identifier", identifier);
        return this;
    }

    public StickerPackParserJsonBuilder setName(String name) throws JSONException {
        stickerPackJson.put("name", name);
        return this;
    }

    public StickerPackParserJsonBuilder setPublisher(String publisher) throws JSONException {
        stickerPackJson.put("publisher", publisher);
        return this;
    }

    public StickerPackParserJsonBuilder setTrayImageFile(String trayImageFile) throws JSONException {
        stickerPackJson.put("tray_image_file", trayImageFile);
        return this;
    }

    public StickerPackParserJsonBuilder setImageDataVersion(String version) throws JSONException {
        stickerPackJson.put("image_data_version", version);
        return this;
    }

    public StickerPackParserJsonBuilder setAvoidCache(boolean avoidCache) throws JSONException {
        stickerPackJson.put("avoid_cache", avoidCache);
        return this;
    }

    public StickerPackParserJsonBuilder setPublisherEmail(String email) throws JSONException {
        stickerPackJson.put("publisher_email", email);
        return this;
    }

    public StickerPackParserJsonBuilder setPublisherWebsite(String website) throws JSONException {
        stickerPackJson.put("publisher_website", website);
        return this;
    }

    public StickerPackParserJsonBuilder setPrivacyPolicyWebsite(String url) throws JSONException {
        stickerPackJson.put("privacy_policy_website", url);
        return this;
    }

    public StickerPackParserJsonBuilder setLicenseAgreementWebsite(String url) throws JSONException {
        stickerPackJson.put("license_agreement_website", url);
        return this;
    }

    public void setAnimatedStickerPack(boolean animated) throws JSONException {
        stickerPackJson.put("animated_sticker_pack", animated);
    }

    public void addSticker(String imageFile, String emojis, String accessibilityText) throws JSONException {
        JSONObject stickerJson = new JSONObject();
        stickerJson.put("image_file", imageFile);
        stickerJson.put("emojis", emojis);

        if (accessibilityText != null) {
            stickerJson.put("accessibility_text", accessibilityText);
        }

        stickersArray.put(stickerJson);
    }

    public String build() throws JSONException {
        stickerPackJson.put("stickers", stickersArray);
        return stickerPackJson.toString(2);
    }
}
