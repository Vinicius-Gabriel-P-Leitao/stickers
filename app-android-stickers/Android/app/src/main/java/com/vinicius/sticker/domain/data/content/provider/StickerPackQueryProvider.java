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
import android.database.MatrixCursor;
import android.database.SQLException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinicius.sticker.domain.data.content.helper.StickerPackQueryHelper;
import com.vinicius.sticker.domain.data.database.StickerDatabase;
import com.vinicius.sticker.domain.data.model.StickerPack;

import java.util.List;

// @formatter:off
public class StickerPackQueryProvider {
    private final static String TAG_LOG = StickerPackQueryHelper.class.getSimpleName();

    @NonNull
    private final Context context;

    public StickerPackQueryProvider(@NonNull Context context) {
        this.context = context;
    }

    public Cursor fetchAllStickerPack(@NonNull Uri uri, @NonNull StickerDatabase dbHelper) {
        try{
            List<StickerPack> stickerPackList = StickerPackQueryHelper.fetchListStickerPackFromDatabase(dbHelper);
            if (stickerPackList.isEmpty())  {
                Log.w(TAG_LOG, "Nenhum pacote de figurinhas encontrado!");
                return new MatrixCursor(new String[]{"Nenhum pacote de figurinhas encontrado!"});
            }

            return StickerPackQueryHelper.fetchListStickerPackData(context, uri, stickerPackList);
        } catch (SQLException sqlException) {
            Log.e(TAG_LOG, "Erro no banco de dados ao buscar pacotes de figurinhas!", sqlException);
            throw sqlException;
        } catch (RuntimeException exception) {
            Log.e(TAG_LOG, "Error retrieving sticker pack!", exception);
            throw new RuntimeException("Erro inesperado ao buscar sticker pack", exception);
        }
    }

    public Cursor fetchSingleStickerPack(@NonNull Uri uri, @NonNull StickerDatabase dbHelper, boolean isFiltered) {
        final String stickerPackIdentifier = uri.getLastPathSegment();
        if (TextUtils.isEmpty(stickerPackIdentifier)) {
            Log.e(TAG_LOG, "Identificador de pacote de figurinhas inválido na Uri: " + uri);
            return new MatrixCursor(new String[]{"O identifer do pacote está nulo!"});
        }

        try {
            StickerPack stickerPack = StickerPackQueryHelper.fetchStickerPackFromDatabase(dbHelper, stickerPackIdentifier, isFiltered);
            if (stickerPack == null) {
                Log.w(TAG_LOG, "Nenhum pacote de figurinhas encontrado para o identificador: " + stickerPackIdentifier);
                return new MatrixCursor(new String[]{"Erro ao buscar pacote, ele é nulo!"});
            }

            return StickerPackQueryHelper.fetchStickerPackData(context, uri, stickerPack);
        } catch (SQLException sqlException) {
            Log.e(TAG_LOG, "Erro no banco de dados ao buscar pacote de figurinhas: " + stickerPackIdentifier, sqlException);
            throw sqlException;
        } catch (RuntimeException exception) {
            Log.e(TAG_LOG, "Error retrieving sticker pack: " + stickerPackIdentifier, exception);
            throw new RuntimeException("Erro inesperado ao buscar sticker pack", exception);
        }
    }
}
