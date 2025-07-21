/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.content.helper;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_IS_VALID;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.core.error.throwable.content.ContentProviderException;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.SelectStickerPackRepo;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class StickerQueryHelper {
    private final static String TAG_LOG = StickerQueryHelper.class.getSimpleName();

    private final Context context;
    private final ApplicationTranslate applicationTranslate;
    private final SelectStickerPackRepo selectStickerPackRepo;

    public StickerQueryHelper(Context context) {
        this.context = context.getApplicationContext();
        SQLiteDatabase database = StickerDatabaseHelper.getInstance(this.context)
                .getReadableDatabase();
        this.selectStickerPackRepo = new SelectStickerPackRepo(database);
        this.applicationTranslate = new ApplicationTranslate(this.context.getResources());
    }

    @NonNull
    public Cursor fetchStickerData(@NonNull Uri uri, @NonNull List<Sticker> stickerList) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY,
                        STICKER_IS_VALID, STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY});

        for (Sticker sticker : stickerList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(sticker.imageFileName);
            builder.add(sticker.emojis);
            builder.add(sticker.stickerIsValid);
            builder.add(sticker.accessibilityText);
        }

        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    public List<Sticker> fetchStickerListFromDatabase(String stickerPackIdentifier) {
        Cursor cursor = selectStickerPackRepo.getStickerByStickerPackIdentifier(
                stickerPackIdentifier);
        if (cursor == null) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.error_null_cursor)
                            .log(TAG_LOG, ApplicationTranslate.LoggableString.Level.ERROR).get());
        }

        List<Sticker> stickerList = new ArrayList<>();

        try {
            if (cursor.moveToFirst()) {
                do {
                    String imageFile = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                    String emojis = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                    String stickerIsValid = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_IS_VALID));
                    String accessibilityText = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                    Sticker sticker = new Sticker(imageFile, emojis, stickerIsValid,
                            accessibilityText, stickerPackIdentifier
                    );
                    stickerList.add(sticker);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return stickerList;
    }
}
