/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.fetch;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.domain.data.content.StickerContentProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FetchStickerFile {
    /**
     * <b>Descrição:</b>Busca o arquivo da figurinha baseado nos dados do pacote que está relacionado.
     *
     * @param identifier      Identificador do arquivo.
     * @param name            Nome do arquivo.
     * @param contentResolver Content resolver.
     * @return Bytes do arquivo.
     */
    public static byte[] fetchStickerFile(
            @NonNull final String identifier, @NonNull final String name, ContentResolver contentResolver) throws IOException {
        try (final InputStream inputStream = contentResolver.openInputStream(
                getStickerFileUri(identifier, name)); final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            if (inputStream == null) {
                throw new IOException("Não foi possível ler a figurinha :" + identifier + "/" + name);
            }

            int read;
            byte[] data = new byte[16384];

            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
            return buffer.toByteArray();
        }
    }

    /**
     * <b>Descrição:</b>Busca a URI do arquivo pelo identifier do pacote e o nome do arquivo
     *
     * @param identifier  Identificador do arquivo.
     * @param stickerName Nome do arquivo.
     * @return Bytes do arquivo.
     */
    public static Uri getStickerFileUri(String identifier, String stickerName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
                .appendPath(StickerContentProvider.STICKERS_ASSET).appendPath(identifier).appendPath(stickerName).build();
    }
}
