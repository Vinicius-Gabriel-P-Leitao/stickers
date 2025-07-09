/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package br.arch.sticker.domain.data.model;

import android.os.Parcel;
import android.os.Parcelable;

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
            String identifier, String name, String publisher, String trayImageFile, String publisherEmail, String publisherWebsite,
            String privacyPolicyWebsite, String licenseAgreementWebsite, String imageDataVersion, boolean avoidCache, boolean animatedStickerPack
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(String.valueOf(identifier));
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

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StickerPack> CREATOR = new Creator<>() {
        @Override
        public StickerPack createFromParcel(Parcel parcel) {
            return new StickerPack(parcel);
        }

        @Override
        public StickerPack[] newArray(int size) {
            return new StickerPack[size];
        }
    };
}
