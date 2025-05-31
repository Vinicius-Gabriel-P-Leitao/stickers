/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.data.content.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinicius.sticker.domain.data.database.StickerDatabase;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.content.provider.StickerQueryProvider;

import java.util.ArrayList;
import java.util.List;

public class StickerProviderQueryHelper {
    private final static String TAG_LOG = StickerProviderQueryHelper.class.getSimpleName();

    @NonNull
    private final Context context;

    public StickerProviderQueryHelper(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    public Cursor getCursorForStickerListForPack(@NonNull Uri uri, @NonNull StickerDatabase dbHelper) {
        final String stickerPackIdentifier = uri.getLastPathSegment();

        if (TextUtils.isEmpty(stickerPackIdentifier)) {
            Log.e(TAG_LOG, "Identificador de pacote de adesivos inválido na Uri: " + uri);
            return StickerQueryProvider.getStickerInfo(context, uri, new ArrayList<>());
        }

        try {
            List<Sticker> stickerPack = StickerQueryProvider.getStickerListFromDatabase(dbHelper, stickerPackIdentifier);
            return StickerQueryProvider.getStickerInfo(context, uri, stickerPack);
        } catch (RuntimeException exception) {
            Log.e(TAG_LOG, "Erro ao buscar pacote de figurinhas: " + stickerPackIdentifier, exception);
            return StickerQueryProvider.getStickerInfo(context, uri, new ArrayList<>());
        }
    }
}
