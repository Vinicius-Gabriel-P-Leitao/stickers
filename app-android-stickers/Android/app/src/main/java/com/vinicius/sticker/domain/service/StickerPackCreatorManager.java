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

package com.vinicius.sticker.domain.service;

import static com.vinicius.sticker.domain.service.ContentFileParser.readStickerPack;
import static com.vinicius.sticker.view.feature.media.util.SaveStickerPackInCache.generateStructureForSavePack;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import com.vinicius.sticker.domain.builder.ContentJsonBuilder;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StickerPackCreatorManager {
   private static final List<Sticker> stickers = new ArrayList<>();
   private final static String uuidPack = UUID.randomUUID().toString();

   public static void generateJsonPack(
       Context context, boolean isAnimatedPack, List<File> fileList,
       String namePack
   ) {
      try {
         stickers.clear();
         ContentJsonBuilder builder = new ContentJsonBuilder();

         builder.setIdentifier(uuidPack)
             .setName(namePack)
             .setPublisher("vinicius")
             .setTrayImageFile(fileList.get(0).getName())
             .setImageDataVersion("1")
             .setAvoidCache(false)
             .setPublisherWebsite("")
             .setPublisherEmail("")
             .setPrivacyPolicyWebsite("")
             .setLicenseAgreementWebsite("")
             .setAnimatedStickerPack(isAnimatedPack);

         for (File file : fileList) {
            boolean exists = false;
            for (Sticker sticker : stickers) {
               if ( sticker.imageFileName.equals(file.getName()) ) {
                  exists = true;
                  break;
               }
            }

            if ( !exists ) {
               stickers.add(new Sticker(file.getName(), List.of("\uD83D\uDDFF"), "Sticker pack"));
            }
         }

         for (Sticker sticker : stickers) {
            builder.addSticker(sticker.imageFileName, sticker.emojis, sticker.accessibilityText);
         }

         String contentJson = builder.build();
         Log.d("Sticker Pack", contentJson);

         try (JsonReader jsonReader = new JsonReader(new StringReader(contentJson))) {
            StickerPack stickerPack = readStickerPack(jsonReader);
            generateStructureForSavePack(context, stickerPack, contentJson);
         }

      } catch (JSONException |
               IOException jsonException) {
         throw new RuntimeException(jsonException);
      }
   }


}
