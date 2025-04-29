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

package com.vinicius.sticker.domain.data.database.repository;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.FK_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.FK_STICKER_PACKS;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.vinicius.sticker.core.exception.StickerPackSaveException;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.pattern.CallbackResult;

public class InsertStickerPacks {

   public interface InsertStickerPackCallback {
      void onInsertSuccessful(CallbackResult result);
   }

   public void insertStickerPack(SQLiteDatabase dbHelper, StickerPack pack, InsertStickerPackCallback callback) {
      new Handler(Looper.getMainLooper()).postDelayed(
          () -> {
             if ( pack.getStickers().size() < 3 ) {
                throw new StickerPackSaveException("Sticker pack must contain at least 3 stickers.");
             }
             if ( pack.getStickers().size() > 30 ) {
                throw new StickerPackSaveException("Sticker pack must contain at most 30 stickers.");
             }
             if ( pack.trayImageFile == null ) {
                throw new StickerPackSaveException("Tray image file cannot be null.");
             }
             if ( pack.identifier == null || pack.identifier.isEmpty() ) {
                throw new StickerPackSaveException("Identifier cannot be null or empty.");
             } else {
                ContentValues stickerPacksValues = new ContentValues();
                stickerPacksValues.put(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, pack.androidPlayStoreLink);
                stickerPacksValues.put(IOS_APP_DOWNLOAD_LINK_IN_QUERY, pack.iosAppStoreLink);
                long stickerPackId = dbHelper.insert("sticker_packs", null, stickerPacksValues);

                if ( stickerPackId != -1 ) {
                   ContentValues stickerPackValues = new ContentValues();
                   stickerPackValues.put(STICKER_PACK_IDENTIFIER_IN_QUERY, pack.identifier);
                   stickerPackValues.put(STICKER_PACK_NAME_IN_QUERY, pack.name);
                   stickerPackValues.put(STICKER_PACK_PUBLISHER_IN_QUERY, pack.publisher);
                   stickerPackValues.put(STICKER_PACK_ICON_IN_QUERY, pack.trayImageFile);
                   stickerPackValues.put(PUBLISHER_EMAIL, pack.publisherEmail);
                   stickerPackValues.put(PUBLISHER_WEBSITE, pack.publisherWebsite);
                   stickerPackValues.put(PRIVACY_POLICY_WEBSITE, pack.privacyPolicyWebsite);
                   stickerPackValues.put(LICENSE_AGREEMENT_WEBSITE, pack.licenseAgreementWebsite);
                   stickerPackValues.put(ANIMATED_STICKER_PACK, pack.animatedStickerPack ? 1 : 0);
                   stickerPackValues.put(FK_STICKER_PACKS, stickerPackId);
                   stickerPackValues.put(IMAGE_DATA_VERSION, pack.imageDataVersion);
                   stickerPackValues.put(AVOID_CACHE, pack.avoidCache ? 1 : 0);
                   long result = dbHelper.insert("sticker_pack", null, stickerPackValues);

                   if ( result != -1 ) {
                      for (Sticker sticker : pack.getStickers()) {
                         ContentValues stickerValues = new ContentValues();
                         stickerValues.put(STICKER_FILE_NAME_IN_QUERY, sticker.imageFileName);
                         stickerValues.put(STICKER_FILE_EMOJI_IN_QUERY, String.valueOf(sticker.emojis));
                         stickerValues.put(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY, sticker.accessibilityText);
                         stickerValues.put(FK_STICKER_PACK, stickerPackId);
                         dbHelper.insert("sticker", null, stickerValues);
                      }

                      if ( callback != null ) {
                         callback.onInsertSuccessful(CallbackResult.success("Insert completado com sucesso!"));
                      }
                   } else {
                      if ( callback != null ) {
                         callback.onInsertSuccessful(CallbackResult.failure(new StickerPackSaveException("Failed to insert sticker pack.")));
                      }
                   }
                } else {
                   if ( callback != null ) {
                      callback.onInsertSuccessful(CallbackResult.failure(new StickerPackSaveException("Failed to insert sticker pack details.")));
                   }
                }
             }
          }, 1000
      );
   }
}
