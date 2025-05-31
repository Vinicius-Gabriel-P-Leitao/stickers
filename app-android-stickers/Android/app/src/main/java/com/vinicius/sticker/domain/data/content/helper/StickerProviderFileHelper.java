/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.data.content.helper;

import static com.vinicius.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinicius.sticker.core.exception.ContentProviderException;

import java.io.File;
import java.io.IOException;
import java.util.List;

// @formatter:off
public class StickerProviderFileHelper {
    private final static String TAG_LOG = StickerProviderQueryHelper.class.getSimpleName();

    @NonNull
    private final Context context;

    public StickerProviderFileHelper(@NonNull Context context) {
        this.context = context;
    }

    public AssetFileDescriptor getImageFiles(Uri uri) throws IllegalArgumentException {

        File stickerPackDir = new File(context.getFilesDir(), STICKERS_ASSET);

        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 3) {
            throw new ContentProviderException("Segmentos de caminho devem ser 3, uri é:" + uri);
        }
        String fileName = pathSegments.get(pathSegments.size() - 1);
        final String identifier = pathSegments.get(pathSegments.size() - 2);

        if (TextUtils.isEmpty(identifier)) {
            throw new ContentProviderException("Identificador está vazio, uri:" + uri);
        }

        if (TextUtils.isEmpty(fileName)) {
            throw new ContentProviderException("Nome do arquivo está vazio, uri:" + uri);
        }

        File stickerDirectory = new File(stickerPackDir, identifier);
        if (!stickerDirectory.exists() || !stickerDirectory.isDirectory()) {
            throw new ContentProviderException("Diretório de figurinhas não encontrado:" + stickerDirectory.getPath());
        }

        File stickerFile = new File(stickerDirectory, fileName);
        if (stickerFile.exists() && stickerFile.isFile()) {
            try {
                ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(stickerFile, ParcelFileDescriptor.MODE_READ_ONLY);
                return new AssetFileDescriptor(fileDescriptor, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
            } catch (IOException exception) {
                Log.e(TAG_LOG, "Erro ao abrir stickerFile: " + stickerFile.getAbsolutePath(), exception);
            }
        }

        return null;
    }
}
