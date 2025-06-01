/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.data.content.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinicius.sticker.domain.data.content.helper.StickerPackQueryHelper;
import com.vinicius.sticker.domain.data.database.StickerDatabase;
import com.vinicius.sticker.domain.data.model.StickerPack;

import java.util.ArrayList;

// @formatter:off
public class StickerPackQueryProvider {
    private final static String TAG_LOG = StickerPackQueryHelper.class.getSimpleName();

    @NonNull
    private final Context context;

    public StickerPackQueryProvider(@NonNull Context context) {
        this.context = context;
    }

    public Cursor fetchAllStickerPack(@NonNull Uri uri, StickerDatabase dbHelper) {
        return StickerPackQueryHelper.fetchListStickerPackData(context, uri, StickerPackQueryHelper.fetchListStickerPackFromDatabase(dbHelper));
    }

    public Cursor fetchSingleStickerPack(@NonNull Uri uri, StickerDatabase dbHelper) {
        final String stickerPackIdentifier = uri.getLastPathSegment();

        if (TextUtils.isEmpty(stickerPackIdentifier)) {
            Log.e(TAG_LOG, "Identificador de pacote de adesivos inválido na Uri: " + uri);
            return StickerPackQueryHelper.fetchListStickerPackData(context, uri, new ArrayList<>());
        }

        try {
            StickerPack stickerPack = StickerPackQueryHelper.fetchStickerPackFromDatabase(dbHelper, stickerPackIdentifier);
            if (stickerPack == null) {
                Log.w(TAG_LOG, "Nenhum pacote de figurinhas encontrado para o identificador: " + stickerPackIdentifier);
                return StickerPackQueryHelper.fetchListStickerPackData(context, uri, new ArrayList<>());
            }

            return StickerPackQueryHelper.fetchStickerPackData(context, uri, stickerPack);
        } catch (RuntimeException exception) {
            Log.e(TAG_LOG, "Error retrieving sticker pack: " + stickerPackIdentifier, exception);
            return StickerPackQueryHelper.fetchListStickerPackData(context, uri, new ArrayList<>());
        }
    }
}
