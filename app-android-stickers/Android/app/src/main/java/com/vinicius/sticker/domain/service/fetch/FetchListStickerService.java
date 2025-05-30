/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.fetch;

import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_NAME_IN_QUERY;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.domain.data.content.StickerContentProvider;
import com.vinicius.sticker.domain.data.model.Sticker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Busca lista com figurinhas tanto em banco de dados quanto em arquivo.
 */
public class FetchListStickerService {

    /**
     * <b>Descrição:</b>Busca os dados das figurinhas tanto arquivo quando metadados.
     *
     * @param context               Contexto da aplicação.
     * @param stickerPackIdentifier Sticker pack identifier.
     * @return Lista de arquivos e metadados em formato de objeto da figurinhas.
     */
    @NonNull
    public static List<Sticker> fetchListStickerForPack(Context context, String stickerPackIdentifier) {
        final List<Sticker> stickers = fetchFromContentProviderForStickers(stickerPackIdentifier, context.getContentResolver());

        for (Sticker sticker : stickers) {
            final byte[] bytes;
            try {
                bytes = FetchStickerFile.fetchStickerFile(stickerPackIdentifier, sticker.imageFileName, context.getContentResolver());

                if (bytes.length == 0) {
                    throw new IllegalStateException("O arquivo está vazio, pacote: " + stickerPackIdentifier + ", figurinha: " + sticker.imageFileName);
                }

                sticker.setSize(bytes.length);
            } catch (IOException | IllegalArgumentException exception) {
                throw new IllegalStateException(
                        "O arquivo não existe. pacote:" + stickerPackIdentifier + ", figurinha: " + sticker.imageFileName,
                        exception);
            }
        }

        return stickers;
    }

    /**
     * <b>Descrição:</b>Busca do content provider os métadados do sticker.
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
                String emojis = null;

                if (!TextUtils.isEmpty(emojisConcatenated)) {
                    emojis = emojisConcatenated;
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
     * <b>Descrição:</b>Busca a URI do pacote pelo identifier
     *
     * @param identifier Identificador da pasta.
     * @return Bytes do arquivo.
     */
    private static Uri getStickerListUri(String identifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
                .appendPath(StickerContentProvider.STICKERS).appendPath(identifier).build();
    }
}
