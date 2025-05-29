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

package com.vinicius.sticker.domain.data.model;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.FK_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Sticker implements Parcelable {
    public static final Creator<Sticker> CREATOR = new Creator<Sticker>() {
        @Override
        public Sticker createFromParcel(Parcel in) {
            return new Sticker(in);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };
    public final String imageFileName;
    public final String emojis;
    public final String accessibilityText;
    long size;

    public Sticker(String imageFileName, String emojis, String accessibilityText) {
        this.imageFileName = imageFileName;
        this.emojis = emojis;
        this.accessibilityText = accessibilityText;
    }

    public Sticker(Parcel in) {
        imageFileName = in.readString();
        emojis = in.readString();
        accessibilityText = in.readString();
        size = in.readLong();
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageFileName);
        dest.writeString(emojis);
        dest.writeString(accessibilityText);
        dest.writeLong(size);
    }

    public static ContentValues toContentValues(Sticker sticker, long stickerPackId) {
        ContentValues stickerValues = new ContentValues();
        stickerValues.put(STICKER_FILE_NAME_IN_QUERY, sticker.imageFileName);
        stickerValues.put(STICKER_FILE_EMOJI_IN_QUERY, String.valueOf(sticker.emojis));
        stickerValues.put(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY, sticker.accessibilityText);
        stickerValues.put(FK_STICKER_PACK, stickerPackId);

        return stickerValues;
    }
}
