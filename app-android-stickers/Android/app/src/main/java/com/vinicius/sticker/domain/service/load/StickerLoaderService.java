/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.load;

import static com.vinicius.sticker.core.validation.StickerValidator.EMOJI_MAX_LIMIT;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.domain.data.content.provider.StickerContentProvider;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Busca lista com figurinhas tanto em banco de dados quanto em arquivo.</p>
 */
public class StickerLoaderService {

    /**
     * <p><b>Descrição:</b>Busca os dados das figurinhas tanto arquivo quando metadados.</p>
     *
     * @param context     Contexto da aplicação.
     * @param stickerPack Sticker pack.
     * @return Lista de arquivos e metadados em formato de objeto da figurinhas.
     */
    @NonNull
    public static List<Sticker> getStickersForPack(Context context, StickerPack stickerPack) {
        final List<Sticker> stickers = fetchFromContentProviderForStickers(stickerPack.identifier, context.getContentResolver());
        for (Sticker sticker : stickers) {
            final byte[] bytes;
            try {
                bytes = fetchStickerAsset(stickerPack.identifier, sticker.imageFileName, context.getContentResolver());
                if (bytes.length == 0) {
                    throw new IllegalStateException("Asset file is empty, pack: " + stickerPack.name + ", sticker: " + sticker.imageFileName);
                }
                sticker.setSize(bytes.length);
            } catch (IOException | IllegalArgumentException exception) {
                throw new IllegalStateException("Asset file doesn't exist. pack: " + stickerPack.name + ", sticker: " + sticker.imageFileName, exception);
            }
        }
        return stickers;
    }

    /**
     * <p><b>Descrição:</b>Busca do content provider os métadados do sticker.</p>
     *
     * @param identifier      Identificador do sticker.
     * @param contentResolver Content resolver.
     * @return Lista de dados das figurinhas.
     */
    @NonNull
    private static List<Sticker> fetchFromContentProviderForStickers(String identifier, ContentResolver contentResolver) {
        Uri uri = getStickerListUri(identifier);

        final String[] projection = {STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY, STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY};
        final Cursor cursor = contentResolver.query(uri, projection, null, null, null);

        List<Sticker> stickers = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                final String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                final String emojisConcatenated = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                final String accessibilityText = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));
                List<String> emojis = new ArrayList<>(EMOJI_MAX_LIMIT);

                if (!TextUtils.isEmpty(emojisConcatenated)) {
                    emojis = Arrays.asList(emojisConcatenated.split(","));
                }
                stickers.add(new Sticker(name, emojis, accessibilityText));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return stickers;
    }

    /**
     * <p><b>Descrição:</b>Busca o arquivo da figurinha baseado nos dados do pacote que está relacionado.</p>
     *
     * @param identifier      Identificador do arquivo.
     * @param name            Nome do arquivo.
     * @param contentResolver Content resolver.
     * @return Bytes do arquivo.
     */
    public static byte[] fetchStickerAsset(@NonNull final String identifier, @NonNull final String name, ContentResolver contentResolver) throws IOException {
        try (final InputStream inputStream = contentResolver.openInputStream(getStickerAssetUri(identifier, name)); final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("cannot read sticker asset:" + identifier + "/" + name);
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
     * <p><b>Descrição:</b>Busca a URI do pacote pelo identifier</p>
     *
     * @param identifier Identificador da pasta.
     * @return Bytes do arquivo.
     */
    private static Uri getStickerListUri(String identifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS).appendPath(identifier).build();
    }

    /**
     * <p><b>Descrição:</b>Busca a URI do arquivo pelo identifier do pacote e o nome do arquivo</p>
     *
     * @param identifier  Identificador do arquivo.
     * @param stickerName Nome do arquivo.
     * @return Bytes do arquivo.
     */
    public static Uri getStickerAssetUri(String identifier, String stickerName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
                .appendPath(StickerContentProvider.STICKERS_ASSET)
                .appendPath(identifier)
                .appendPath(stickerName).build();
    }
}
