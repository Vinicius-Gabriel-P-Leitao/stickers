/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.content.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import br.arch.sticker.domain.data.content.helper.StickerPackQueryHelper;
import br.arch.sticker.domain.data.model.StickerPack;

public class StickerPackQueryProvider {
    private final static String TAG_LOG = StickerPackQueryProvider.class.getSimpleName();

    private final StickerPackQueryHelper stickerPackQueryHelper;

    public StickerPackQueryProvider(Context context) {
        this.stickerPackQueryHelper = new StickerPackQueryHelper(context);
    }

    public Cursor fetchAllStickerPack(@NonNull Uri uri) {
        try {
            List<StickerPack> stickerPackList = stickerPackQueryHelper.fetchListStickerPackFromDatabase();
            if (stickerPackList.isEmpty()) {
                Log.w(TAG_LOG, "Nenhum pacote de figurinhas encontrado!");
                return new MatrixCursor(new String[]{"Nenhum pacote de figurinhas encontrado!"});
            }

            return stickerPackQueryHelper.fetchListStickerPackData(uri, stickerPackList);
        } catch (SQLException sqlException) {
            Log.e(TAG_LOG, "Erro no banco de dados ao buscar pacotes de figurinhas!", sqlException);
            throw sqlException;
        } catch (RuntimeException exception) {
            Log.e(TAG_LOG, "Error buscar pacote de figurinhas!", exception);
            throw new RuntimeException("Erro inesperado ao buscar pacotes de figuinhas", exception);
        }
    }

    public Cursor fetchSingleStickerPack(@NonNull Uri uri, boolean isFiltered) {
        final String stickerPackIdentifier = uri.getLastPathSegment();
        if (TextUtils.isEmpty(stickerPackIdentifier)) {
            Log.e(TAG_LOG, "Identificador de pacote de figurinhas inválido na Uri: " + uri);
            return new MatrixCursor(new String[]{"O identifer do pacote está nulo!"});
        }

        try {
            StickerPack stickerPack = stickerPackQueryHelper.fetchStickerPackFromDatabase(stickerPackIdentifier, isFiltered);
            if (stickerPack == null) {
                Log.w(TAG_LOG, "Nenhum pacote de figurinhas encontrado para o identificador: " +
                        stickerPackIdentifier);
                return new MatrixCursor(new String[]{"Erro ao buscar pacote, ele é nulo!"});
            }

            return stickerPackQueryHelper.fetchStickerPackData(uri, stickerPack);
        } catch (SQLException sqlException) {
            Log.e(TAG_LOG, "Erro no banco de dados ao buscar pacote de figurinhas: " +
                    stickerPackIdentifier, sqlException);
            throw sqlException;
        } catch (RuntimeException exception) {
            Log.e(TAG_LOG, "Error buscar pacote de figurinha: " + stickerPackIdentifier, exception);
            throw new RuntimeException("Erro inesperado ao buscar pacote de figuinha", exception);
        }
    }
}
