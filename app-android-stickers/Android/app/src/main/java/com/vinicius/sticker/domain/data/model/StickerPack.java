/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.vinicius.sticker.domain.data.model;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.FK_STICKER_PACKS;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_ICON_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class StickerPack implements Parcelable {
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

  public static final Creator<StickerPack> CREATOR =
      new Creator<StickerPack>() {
        @Override
        public StickerPack createFromParcel(Parcel in) {
          return new StickerPack(in);
        }

        @Override
        public StickerPack[] newArray(int size) {
          return new StickerPack[size];
        }
      };

  public String getIdentifier() {
    return identifier;
  }

  public boolean getIsWhitelisted() {
    return isWhitelisted;
  }

  public List<Sticker> getStickers() {
    return stickers;
  }

  public long getTotalSize() {
    return totalSize;
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

  public void setStickers(List<Sticker> stickers) {
    this.stickers = (stickers != null) ? stickers : new ArrayList<>();
    totalSize = 0;

    for (Sticker sticker : this.stickers) {
      totalSize += sticker.size;
    }
  }

  public StickerPack(
      String identifier,
      String name,
      String publisher,
      String trayImageFile,
      String publisherEmail,
      String publisherWebsite,
      String privacyPolicyWebsite,
      String licenseAgreementWebsite,
      String imageDataVersion,
      boolean avoidCache,
      boolean animatedStickerPack) {
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

  private StickerPack(Parcel parcel) {
    identifier = parcel.readString();
    name = parcel.readString();
    publisher = parcel.readString();
    trayImageFile = parcel.readString();
    publisherEmail = parcel.readString();
    publisherWebsite = parcel.readString();
    privacyPolicyWebsite = parcel.readString();
    licenseAgreementWebsite = parcel.readString();
    iosAppStoreLink = parcel.readString();
    stickers = parcel.createTypedArrayList(Sticker.CREATOR);
    totalSize = parcel.readLong();
    androidPlayStoreLink = parcel.readString();
    isWhitelisted = parcel.readByte() != 0;
    imageDataVersion = parcel.readString();
    avoidCache = parcel.readByte() != 0;
    animatedStickerPack = parcel.readByte() != 0;
  }

  public StickerPack(StickerPack stickerPackClone) {
    this.isWhitelisted = stickerPackClone.isWhitelisted;
    this.totalSize = stickerPackClone.totalSize;
    this.stickers = stickerPackClone.stickers;
    this.androidPlayStoreLink = stickerPackClone.androidPlayStoreLink;
    this.iosAppStoreLink = stickerPackClone.iosAppStoreLink;
    this.animatedStickerPack = stickerPackClone.animatedStickerPack;
    this.avoidCache = stickerPackClone.avoidCache;
    this.imageDataVersion = stickerPackClone.imageDataVersion;
    this.licenseAgreementWebsite = stickerPackClone.licenseAgreementWebsite;
    this.privacyPolicyWebsite = stickerPackClone.privacyPolicyWebsite;
    this.publisherWebsite = stickerPackClone.publisherWebsite;
    this.publisherEmail = stickerPackClone.publisherEmail;
    this.trayImageFile = stickerPackClone.trayImageFile;
    this.publisher = stickerPackClone.publisher;
    this.name = stickerPackClone.name;
    this.identifier = stickerPackClone.identifier;
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

  @NonNull
  public static ContentValues toContentValues(StickerPack stickerPack, long stickerPackId) {
    ContentValues stickerPackValues = new ContentValues();
    stickerPackValues.put(STICKER_PACK_IDENTIFIER_IN_QUERY, stickerPack.identifier);
    stickerPackValues.put(STICKER_PACK_NAME_IN_QUERY, stickerPack.name);
    stickerPackValues.put(STICKER_PACK_PUBLISHER_IN_QUERY, stickerPack.publisher);
    stickerPackValues.put(STICKER_PACK_ICON_IN_QUERY, stickerPack.trayImageFile);
    stickerPackValues.put(PUBLISHER_EMAIL, stickerPack.publisherEmail);
    stickerPackValues.put(PUBLISHER_WEBSITE, stickerPack.publisherWebsite);
    stickerPackValues.put(PRIVACY_POLICY_WEBSITE, stickerPack.privacyPolicyWebsite);
    stickerPackValues.put(LICENSE_AGREEMENT_WEBSITE, stickerPack.licenseAgreementWebsite);
    stickerPackValues.put(ANIMATED_STICKER_PACK, stickerPack.animatedStickerPack ? 1 : 0);
    stickerPackValues.put(FK_STICKER_PACKS, stickerPackId);
    stickerPackValues.put(IMAGE_DATA_VERSION, stickerPack.imageDataVersion);
    stickerPackValues.put(AVOID_CACHE, stickerPack.avoidCache ? 1 : 0);

    return stickerPackValues;
  }
}
