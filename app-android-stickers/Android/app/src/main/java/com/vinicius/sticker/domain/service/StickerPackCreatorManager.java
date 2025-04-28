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

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import com.vinicius.sticker.domain.builder.ContentJsonBuilder;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.pattern.CallbackResult;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StickerPackCreatorManager {
   private static final List<Sticker> stickers = new ArrayList<>();
   private final static String uuidPack = UUID.randomUUID().toString();

   @FunctionalInterface
   public interface JsonValidateCallback {
      void onJsonValidateDataComplete(String contentJson);
   }

   @FunctionalInterface
   public interface SavedStickerPackCallback {
      void onSavedStickerPack(CallbackResult callbackResult);
   }

   public static void generateJsonPack(
       Context context, boolean isAnimatedPack, List<File> fileList, String namePack,
       JsonValidateCallback jsonValidateCallback, SavedStickerPackCallback savedStickerPackCallback
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
         try (JsonReader jsonReader = new JsonReader(new StringReader(contentJson))) {
            StickerPack stickerPack = readStickerPack(jsonReader);

            if ( jsonValidateCallback != null ) {
               jsonValidateCallback.onJsonValidateDataComplete(contentJson);
            }

            SaveStickerPack.generateStructureForSavePack(
                context, stickerPack, callbackResult -> {
                   switch (callbackResult.getStatus()) {
                      case SUCCESS:
                         if ( savedStickerPackCallback != null ) {
                            savedStickerPackCallback.onSavedStickerPack(
                                CallbackResult.success(callbackResult.getData()));
                         } else {
                            Log.d("SaveStickerPack", "Callback não foi retornada corretamente!");
                         }
                         break;
                      case WARNING:
                         Log.w("SaveStickerPack", callbackResult.getWarningMessage());
                         break;
                      case FAILURE:
                         Log.e(
                             "SaveStickerPack",
                             Objects.requireNonNull(callbackResult.getError().getMessage())
                         );
                         break;
                   }
                }
            );
         }
      } catch (JSONException |
               IOException exception) {
         throw new RuntimeException(exception);
      }
   }
}
