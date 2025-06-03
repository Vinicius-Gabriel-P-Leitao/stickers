/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.fetch;

import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_IS_VALID;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.domain.data.content.StickerContentProvider;
import com.vinicius.sticker.domain.data.model.Sticker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FetchStickerService {

    @NonNull
    public static List<Sticker> fetchListStickerForPack(Context context, String stickerPackIdentifier) {
        final List<Sticker> stickers = fetchListStickerFromContentProvider(stickerPackIdentifier, context.getContentResolver());

        for (Sticker sticker : stickers) {
            final byte[] bytes;
            try {
                bytes = FetchStickerAssetService.fetchStickerAsset(stickerPackIdentifier, sticker.imageFileName, context);

                if (bytes.length == 0) {
                    throw new IllegalStateException(
                            "O arquivo está vazio, pacote: " + stickerPackIdentifier + ", figurinha: " + sticker.imageFileName);
                }

                sticker.setSize(bytes.length);
            } catch (IOException | IllegalArgumentException exception) {
                throw new IllegalStateException(
                        "O arquivo não existe. pacote:" + stickerPackIdentifier + ", figurinha: " + sticker.imageFileName,
                        exception);
            }
        }

        return stickers;
    }

    @NonNull
    public static List<Sticker> fetchListStickerFromContentProvider(String stickerPackIdentifier, ContentResolver contentResolver) {
        Uri uri = buildStickerUri(stickerPackIdentifier);

        final String[] projection = {STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY, STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY};

        final Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        List<Sticker> stickers = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                final String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                final String emojisConcatenated = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                final String stickerIsValid = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_IS_VALID));
                final String accessibilityText = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                String emojis = null;
                if (!TextUtils.isEmpty(emojisConcatenated)) {
                    emojis = emojisConcatenated;
                }

                stickers.add(new Sticker(name, emojis, stickerIsValid, accessibilityText, stickerPackIdentifier));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return stickers;
    }

    private static Uri buildStickerUri(String stickerPackIdentifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
                .appendPath(StickerContentProvider.STICKERS).appendPath(stickerPackIdentifier).build();
    }
}
