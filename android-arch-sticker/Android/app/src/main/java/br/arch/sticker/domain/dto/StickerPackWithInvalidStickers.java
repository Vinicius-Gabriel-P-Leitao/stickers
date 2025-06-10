/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.dto;

import android.os.Parcel;
import android.os.Parcelable;

import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;

import java.util.ArrayList;

public class StickerPackWithInvalidStickers implements Parcelable {
    public StickerPack stickerPack;
    public ArrayList<Sticker> invalidStickers;

    public StickerPack getStickerPack() {
        return stickerPack;
    }

    public ArrayList<Sticker> getInvalidStickers() {
        return invalidStickers;
    }

    public StickerPackWithInvalidStickers(StickerPack stickerPack, ArrayList<Sticker> invalidStickers) {
        this.stickerPack = stickerPack;
        this.invalidStickers = invalidStickers;
    }

    public StickerPackWithInvalidStickers(Parcel parcel) {
        stickerPack = parcel.readParcelable(StickerPack.class.getClassLoader());
        invalidStickers = parcel.createTypedArrayList(Sticker.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(stickerPack, flags);
        dest.writeTypedList(invalidStickers);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StickerPackWithInvalidStickers> CREATOR = new Creator<StickerPackWithInvalidStickers>() {
        @Override
        public StickerPackWithInvalidStickers createFromParcel(Parcel parcel) {
            return new StickerPackWithInvalidStickers(parcel);
        }

        @Override
        public StickerPackWithInvalidStickers[] newArray(int size) {
            return new StickerPackWithInvalidStickers[size];
        }
    };
}
