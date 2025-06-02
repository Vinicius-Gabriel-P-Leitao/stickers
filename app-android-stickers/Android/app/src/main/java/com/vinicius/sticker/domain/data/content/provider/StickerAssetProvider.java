/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.data.content.provider;

import static com.vinicius.sticker.core.validation.StickerValidator.ANIMATED_STICKER_FILE_LIMIT_KB;
import static com.vinicius.sticker.core.validation.StickerValidator.KB_IN_BYTES;
import static com.vinicius.sticker.core.validation.StickerValidator.STATIC_STICKER_FILE_LIMIT_KB;
import static com.vinicius.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.ANIMATED_STICKER_PACK;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinicius.sticker.core.exception.content.ContentProviderException;
import com.vinicius.sticker.domain.data.database.StickerDatabase;
import com.vinicius.sticker.domain.data.database.repository.SelectStickerPackRepo;

import java.io.File;
import java.io.IOException;
import java.util.List;

// @formatter:off
public class StickerAssetProvider {
    private final static String TAG_LOG = StickerQueryProvider.class.getSimpleName();

    @NonNull
    private final Context context;

    public StickerAssetProvider(@NonNull Context context) {
        this.context = context;
    }

    public AssetFileDescriptor fetchStickerAsset(@NonNull Uri uri, StickerDatabase dbHelper,boolean isWhatsApp) throws IllegalArgumentException {
        final File stickerPackDir = new File(context.getFilesDir(), STICKERS_ASSET);

        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 3) {
            throw new ContentProviderException("Segmentos de caminho devem ser 3, uri é:" + uri);
        }
        String fileName = pathSegments.get(pathSegments.size() - 1);
        final String stickerPackIdentifier = pathSegments.get(pathSegments.size() - 2);

        if (TextUtils.isEmpty(stickerPackIdentifier)) {
            throw new ContentProviderException("Identificador está vazio, uri:" + uri);
        }

        if (TextUtils.isEmpty(fileName)) {
            throw new ContentProviderException("Nome do arquivo está vazio, uri:" + uri);
        }

        final File stickerDirectory = new File(stickerPackDir, stickerPackIdentifier);
        if (!stickerDirectory.exists() || !stickerDirectory.isDirectory()) {
            throw new ContentProviderException("Diretório de figurinhas não encontrado:" + stickerDirectory.getPath());
        }

        File stickerFile = new File(stickerDirectory, fileName);
        if (stickerFile.exists() && stickerFile.isFile()) {
            Cursor cursor;

            try{
                cursor = SelectStickerPackRepo.getStickerPackIsAnimated(dbHelper, stickerPackIdentifier);
            } catch (SQLException sqlException) {
                Log.e(TAG_LOG, "Erro no banco de dados ao buscar se o pacote  de figurinhas é animado: " + stickerPackIdentifier, sqlException);
                throw sqlException;
            } catch (RuntimeException exception) {
                Log.e(TAG_LOG, "Error retrieving sticker pack: " + stickerPackIdentifier, exception);
                throw new RuntimeException("Erro inesperado ao buscar sticker pack", exception);
            }

            if (cursor.moveToFirst()) {
                boolean animatedStickerPack = cursor.getInt(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) != 0;

                if (animatedStickerPack && isWhatsApp && stickerFile.length() > ANIMATED_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                    Log.w(TAG_LOG, "Arquivo " + stickerFile.getAbsolutePath() + " passa de 500KB, ignorado.");
                    return null;
                }

                if (!animatedStickerPack && isWhatsApp && stickerFile.length() > STATIC_STICKER_FILE_LIMIT_KB * KB_IN_BYTES) {
                    Log.w(TAG_LOG, "Arquivo " + stickerFile.getAbsolutePath() + " passa de 100KB, ignorado.");
                    return null;
                }

                try {
                    ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(stickerFile, ParcelFileDescriptor.MODE_READ_ONLY);
                    return new AssetFileDescriptor(fileDescriptor, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
                } catch (IOException exception) {
                    Log.e(TAG_LOG, "Erro ao abrir stickerFile: " + stickerFile.getAbsolutePath(), exception);
                } finally {
                    cursor.close();
                }
            } else {
                Log.e(TAG_LOG, "Sticker pack não encontrado: " + stickerPackIdentifier);
                return null;
            }
        }

        return null;
    }
}
