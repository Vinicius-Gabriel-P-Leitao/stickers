/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.vinicius.sticker.domain.data.model;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_ICON_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class StickerPack implements Parcelable {
   public static final Creator<StickerPack> CREATOR = new Creator<StickerPack>() {
      @Override
      public StickerPack createFromParcel(Parcel in) {
         return new StickerPack(in);
      }

      @Override
      public StickerPack[] newArray(int size) {
         return new StickerPack[size];
      }
   };
   public String identifier;
   public final String name;
   public final String publisher;
   public final String trayImageFile;
   public final String publisherEmail;
   public final String publisherWebsite;
   public final String privacyPolicyWebsite;
   public final String licenseAgreementWebsite;
   public final String imageDataVersion;
   public final boolean avoidCache;
   public final boolean animatedStickerPack;
   public String iosAppStoreLink;
   public String androidPlayStoreLink;
   private List<Sticker> stickers;
   private long totalSize;
   private boolean isWhitelisted;

   public StickerPack(
       String identifier, String name, String publisher, String trayImageFile,
       String publisherEmail, String publisherWebsite, String privacyPolicyWebsite,
       String licenseAgreementWebsite, String imageDataVersion, boolean avoidCache,
       boolean animatedStickerPack
   ) {
      this.identifier = identifier;
      this.name = name;
      this.publisher = publisher;
      this.trayImageFile = trayImageFile;
      this.publisherEmail = publisherEmail;
      this.publisherWebsite = publisherWebsite;
      this.privacyPolicyWebsite = privacyPolicyWebsite;
      this.licenseAgreementWebsite = licenseAgreementWebsite;
      this.imageDataVersion = imageDataVersion;
      this.avoidCache = avoidCache;
      this.animatedStickerPack = animatedStickerPack;
   }

   private StickerPack(Parcel in) {
      identifier = in.readString();
      name = in.readString();
      publisher = in.readString();
      trayImageFile = in.readString();
      publisherEmail = in.readString();
      publisherWebsite = in.readString();
      privacyPolicyWebsite = in.readString();
      licenseAgreementWebsite = in.readString();
      iosAppStoreLink = in.readString();
      stickers = in.createTypedArrayList(Sticker.CREATOR);
      totalSize = in.readLong();
      androidPlayStoreLink = in.readString();
      isWhitelisted = in.readByte() != 0;
      imageDataVersion = in.readString();
      avoidCache = in.readByte() != 0;
      animatedStickerPack = in.readByte() != 0;
   }

   public boolean getIsWhitelisted() {
      return isWhitelisted;
   }

   public void setIsWhitelisted(boolean isWhitelisted) {
      this.isWhitelisted = isWhitelisted;
   }

   public void setAndroidPlayStoreLink(String androidPlayStoreLink) {
      this.androidPlayStoreLink = androidPlayStoreLink;
   }

   public void setIosAppStoreLink(String iosAppStoreLink) {
      this.iosAppStoreLink = iosAppStoreLink;
   }

   public List<Sticker> getStickers() {
      return stickers;
   }

   public void setStickers(List<Sticker> stickers) {
      this.stickers = stickers;
      totalSize = 0;
      for (Sticker sticker : stickers) {
         totalSize += sticker.size;
      }
   }

   public long getTotalSize() {
      return totalSize;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(identifier);
      dest.writeString(name);
      dest.writeString(publisher);
      dest.writeString(trayImageFile);
      dest.writeString(publisherEmail);
      dest.writeString(publisherWebsite);
      dest.writeString(privacyPolicyWebsite);
      dest.writeString(licenseAgreementWebsite);
      dest.writeString(iosAppStoreLink);
      dest.writeTypedList(stickers);
      dest.writeLong(totalSize);
      dest.writeString(androidPlayStoreLink);
      dest.writeByte((byte) (isWhitelisted ? 1 : 0));
      dest.writeString(imageDataVersion);
      dest.writeByte((byte) (avoidCache ? 1 : 0));
      dest.writeByte((byte) (animatedStickerPack ? 1 : 0));
   }

   public static StickerPack fromContentValues(ContentValues values) {
      String identifier = values.getAsString(STICKER_PACK_IDENTIFIER_IN_QUERY);
      String name = values.getAsString(STICKER_PACK_NAME_IN_QUERY);
      String publisher = values.getAsString(STICKER_PACK_PUBLISHER_IN_QUERY);
      String trayImageFile = values.getAsString(STICKER_PACK_ICON_IN_QUERY);
      String publisherEmail = values.getAsString(PUBLISHER_EMAIL);
      String publisherWebsite = values.getAsString(PUBLISHER_WEBSITE);
      String privacyPolicyWebsite = values.getAsString(PRIVACY_POLICY_WEBSITE);
      String licenseAgreementWebsite = values.getAsString(LICENSE_AGREEMENT_WEBSITE);
      String imageDataVersion = values.getAsString(IMAGE_DATA_VERSION);
      boolean avoidCache = values.getAsInteger(AVOID_CACHE) != 0;
      boolean animatedStickerPack = values.getAsInteger(ANIMATED_STICKER_PACK) != 0;

      List<Sticker> stickers = new ArrayList<>();

      StickerPack stickerPack = new StickerPack(
          identifier, name, publisher, trayImageFile, publisherEmail, publisherWebsite,
          privacyPolicyWebsite, licenseAgreementWebsite, imageDataVersion, avoidCache,
          animatedStickerPack
      );

      stickerPack.setStickers(stickers);

      return stickerPack;
   }
}
