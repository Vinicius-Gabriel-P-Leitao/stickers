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

import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_ICON_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKER_PACK_PUBLISHER_IN_QUERY;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.data.provider.StickerContentProvider;

public class StickerPackSave {
   public static void savaStickerPack(
       Context context,
       StickerPack stickerPack
   ) throws IllegalStateException {
      ContentValues values = new ContentValues();
      values.put(STICKER_PACK_IDENTIFIER_IN_QUERY, stickerPack.identifier);
      values.put(STICKER_PACK_NAME_IN_QUERY, stickerPack.name);
      values.put(STICKER_PACK_PUBLISHER_IN_QUERY, stickerPack.publisher);
      values.put(STICKER_PACK_ICON_IN_QUERY, stickerPack.trayImageFile);
      values.put(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.androidPlayStoreLink);
      values.put(IOS_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.iosAppStoreLink);
      values.put(PUBLISHER_EMAIL, stickerPack.publisherEmail);
      values.put(PUBLISHER_WEBSITE, stickerPack.publisherWebsite);
      values.put(PRIVACY_POLICY_WEBSITE, stickerPack.privacyPolicyWebsite);
      values.put(LICENSE_AGREEMENT_WEBSITE, stickerPack.licenseAgreementWebsite);
      values.put(IMAGE_DATA_VERSION, stickerPack.imageDataVersion);
      values.put(AVOID_CACHE, stickerPack.avoidCache);
      values.put(ANIMATED_STICKER_PACK, stickerPack.animatedStickerPack);

      Uri resultUri = context.getContentResolver()
          .insert(StickerContentProvider.AUTHORITY_URI, values);

      if ( resultUri == null ) {
         throw new IllegalStateException("Falha ao salvar o StickerPack no ContentProvider");
      }
   }
}
