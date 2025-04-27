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

package com.vinicius.sticker.domain.data.repository;

import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.FK_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.FK_STICKER_PACKS;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_FILE_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_ICON_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_PUBLISHER_IN_QUERY;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

public class InsertStickerPacks {
   private void insertStickerPack(SQLiteDatabase dbHelper, StickerPack pack) {
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
         long result = dbHelper.insert("sticker_pack", null, stickerPackValues);

         if ( result != -1 ) {
            for (Sticker sticker : pack.getStickers()) {
               ContentValues stickerValues = new ContentValues();
               stickerValues.put(STICKER_FILE_NAME_IN_QUERY, sticker.imageFileName);
               stickerValues.put(STICKER_FILE_EMOJI_IN_QUERY, String.valueOf(sticker.emojis));
               stickerValues.put(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY,
                                 sticker.accessibilityText);
               stickerValues.put(FK_STICKER_PACK, stickerPackId);
               dbHelper.insert("sticker", null, stickerValues);
            }
         }
      }
   }
}
