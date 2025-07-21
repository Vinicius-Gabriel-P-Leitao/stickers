/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.mapper;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.FK_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_IS_VALID;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import br.arch.sticker.domain.data.model.Sticker;

public class StickerMapper {
    @NonNull
    public static ContentValues writeStickerToContentValues(Sticker sticker) {
        ContentValues stickerValues = new ContentValues();
        stickerValues.put(STICKER_FILE_NAME_IN_QUERY, sticker.imageFileName);
        stickerValues.put(STICKER_FILE_EMOJI_IN_QUERY, String.valueOf(sticker.emojis));
        stickerValues.put(STICKER_IS_VALID, sticker.stickerIsValid);
        stickerValues.put(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY, sticker.accessibilityText);
        stickerValues.put(FK_STICKER_PACK, sticker.uuidPack);

        return stickerValues;
    }
}
