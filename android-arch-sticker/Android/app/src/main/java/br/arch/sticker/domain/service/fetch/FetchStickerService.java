/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.fetch;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_IS_VALID;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import br.arch.sticker.BuildConfig;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;
import br.arch.sticker.domain.data.content.StickerContentProvider;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.service.update.UpdateStickerService;

public class FetchStickerService {
    private final FetchStickerAssetService fetchStickerAssetService;
    private final UpdateStickerService updateStickerService;
    private final Context context;

    public FetchStickerService(Context context) {
        this.context = context.getApplicationContext();
        this.updateStickerService = new UpdateStickerService(this.context);
        this.fetchStickerAssetService = new FetchStickerAssetService(this.context);
    }

    @NonNull
    public List<Sticker> fetchListStickerForPack(String stickerPackIdentifier) {
        final List<Sticker> stickers = fetchListStickerFromContentProvider(stickerPackIdentifier);

        for (Sticker sticker : stickers) {
            try {
                byte[] bytes = fetchStickerAssetService.fetchStickerAsset(stickerPackIdentifier, sticker.imageFileName);

                if (bytes.length == 0) {
                    if (!TextUtils.equals(sticker.stickerIsValid, ErrorCode.STICKER_FILE_NOT_EXIST.name())) {
                        updateStickerService.updateInvalidSticker(stickerPackIdentifier, sticker.imageFileName, ErrorCode.STICKER_FILE_NOT_EXIST);
                    }

                    continue;
                }

                sticker.setSize(bytes.length);
            } catch (FetchStickerException exception) {
                if (!TextUtils.equals(sticker.stickerIsValid, ErrorCode.STICKER_FILE_NOT_EXIST.name())) {
                    updateStickerService.updateInvalidSticker(stickerPackIdentifier, sticker.imageFileName, ErrorCode.STICKER_FILE_NOT_EXIST);
                }
            }
        }

        return stickers;
    }

    @NonNull
    private List<Sticker> fetchListStickerFromContentProvider(String stickerPackIdentifier) {
        Uri uri = buildStickerUri(stickerPackIdentifier);
        final String[] projection = {STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY, STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY};

        List<Sticker> stickers = new ArrayList<>();

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
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
        }

        return stickers;
    }

    private static Uri buildStickerUri(String stickerPackIdentifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
                .appendPath(StickerContentProvider.STICKERS).appendPath(stickerPackIdentifier).build();
    }
}
