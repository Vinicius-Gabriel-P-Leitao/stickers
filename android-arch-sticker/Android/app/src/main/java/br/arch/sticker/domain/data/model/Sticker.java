/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package br.arch.sticker.domain.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Sticker implements Parcelable {
    public final String imageFileName;
    public final String emojis;
    public String stickerIsValid;
    public final String accessibilityText;
    public final String uuidPack;
    long size;

    public String getUuidPack() {
        return uuidPack;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setStickerIsInvalid(String stickerIsValid) {
        this.stickerIsValid = stickerIsValid;
    }

    public Sticker(String imageFileName, String emojis, String stickerIsValid, String accessibilityText, String uuidPack) {
        this.imageFileName = imageFileName;
        this.emojis = emojis;
        this.stickerIsValid = stickerIsValid;
        this.accessibilityText = accessibilityText;
        this.uuidPack = uuidPack;
    }

    public Sticker(Parcel parcel) {
        imageFileName = parcel.readString();
        emojis = parcel.readString();
        stickerIsValid = parcel.readString();
        accessibilityText = parcel.readString();
        uuidPack = parcel.readString();
        size = parcel.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageFileName);
        dest.writeString(emojis);
        dest.writeString(stickerIsValid);
        dest.writeString(accessibilityText);
        dest.writeString(uuidPack);
        dest.writeLong(size);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Sticker> CREATOR = new Creator<>() {
        @Override
        public Sticker createFromParcel(Parcel parcel) {
            return new Sticker(parcel);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };
}
