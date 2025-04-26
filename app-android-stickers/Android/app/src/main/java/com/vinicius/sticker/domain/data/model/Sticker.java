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
   public final List<String> emojis;
   public final String accessibilityText;
   long size;

   public Sticker(String imageFileName, List<String> emojis, String accessibilityText) {
      this.imageFileName = imageFileName;
      this.emojis = emojis;
      this.accessibilityText = accessibilityText;
   }

   public Sticker(Parcel in) {
      imageFileName = in.readString();
      emojis = in.createStringArrayList();
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
      dest.writeStringList(emojis);
      dest.writeString(accessibilityText);
      dest.writeLong(size);
   }
}
