/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

package com.vinicius.sticker.domain.builder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class StickerPackContentJsonBuilder {
   private final JSONObject stickerPackJson;
   private final JSONArray stickersArray;

   public StickerPackContentJsonBuilder() {
      stickerPackJson = new JSONObject();
      stickersArray = new JSONArray();
   }

   public StickerPackContentJsonBuilder setIdentifier(String identifier) throws JSONException {
      stickerPackJson.put("identifier", identifier);
      return this;
   }

   public StickerPackContentJsonBuilder setName(String name) throws JSONException {
      stickerPackJson.put("name", name);
      return this;
   }

   public StickerPackContentJsonBuilder setPublisher(String publisher) throws JSONException {
      stickerPackJson.put("publisher", publisher);
      return this;
   }

   public StickerPackContentJsonBuilder setTrayImageFile(String trayImageFile) throws JSONException {
      stickerPackJson.put("tray_image_file", trayImageFile);
      return this;
   }

   public StickerPackContentJsonBuilder setImageDataVersion(String version) throws JSONException {
      stickerPackJson.put("image_data_version", version);
      return this;
   }

   public StickerPackContentJsonBuilder setAvoidCache(boolean avoidCache) throws JSONException {
      stickerPackJson.put("avoid_cache", avoidCache);
      return this;
   }

   public StickerPackContentJsonBuilder setPublisherEmail(String email) throws JSONException {
      stickerPackJson.put("publisher_email", email);
      return this;
   }

   public StickerPackContentJsonBuilder setPublisherWebsite(String website) throws JSONException {
      stickerPackJson.put("publisher_website", website);
      return this;
   }

   public StickerPackContentJsonBuilder setPrivacyPolicyWebsite(String url) throws JSONException {
      stickerPackJson.put("privacy_policy_website", url);
      return this;
   }

   public StickerPackContentJsonBuilder setLicenseAgreementWebsite(String url) throws JSONException {
      stickerPackJson.put("license_agreement_website", url);
      return this;
   }

   public void setAnimatedStickerPack(boolean animated) throws JSONException {
      stickerPackJson.put("animated_sticker_pack", animated);
   }

   public void addSticker(
       String imageFile, List<String> emojis, String accessibilityText) throws JSONException {
      JSONObject stickerJson = new JSONObject();
      stickerJson.put("image_file", imageFile);

      JSONArray emojisArray = new JSONArray();
      for (String emoji : emojis) {
         emojisArray.put(emoji);
      }
      stickerJson.put("emojis", emojisArray);

      if ( accessibilityText != null ) {
         stickerJson.put("accessibility_text", accessibilityText);
      }

      stickersArray.put(stickerJson);
   }

   public String build() throws JSONException {
      stickerPackJson.put("stickers", stickersArray);
      return stickerPackJson.toString(2);
   }
}
