/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.core.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.vinicius.sticker.core.exception.media.MediaConversionException;
import com.vinicius.sticker.view.core.usecase.definition.MimeTypesSupported;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CursorSearchUriMedia {

    /**
     * <p><b>Descrição:</b>Busca as URI dos arquivos baseado nos mimetypes e o contexto.</p>
     *
     * @param context   Contexto da aplicação.
     * @param mimeTypes Mimetype dos arquivos a serem buscados no aparelho do usuário.
     * @return Lista com as URIs.
     */
    public static List<Uri> fetchMediaUri(Context context, String[] mimeTypes) {
        List<Uri> mediaUris;

        if (Arrays.equals(mimeTypes, MimeTypesSupported.IMAGE.getMimeTypes())) {
            mediaUris = fetchListUri(context, MimeTypesSupported.IMAGE);
        } else if (Arrays.equals(mimeTypes, MimeTypesSupported.ANIMATED.getMimeTypes())) {
            mediaUris = fetchListUri(context, MimeTypesSupported.ANIMATED);
        } else {
            throw new MediaConversionException("Tipo MIME não suportado para conversão: " + Arrays.toString(mimeTypes));
        }

        return mediaUris;
    }

    /**
     * <p><b>Descrição:</b>Busca uma lista de URI de imagens ou arquivos animados, baseado no enum MediaType.</p>
     *
     * @param context        Contexto da aplicação.
     * @param mediaTypeParam mimeType recebido para buscar as URI.
     * @return Lista com as URIs.
     */
    public static List<Uri> fetchListUri(Context context, MimeTypesSupported mediaTypeParam) {
        List<Uri> mediaUris = new ArrayList<>();

        Uri collection;
        String[] projection;
        String selection;
        String[] mimeTypes;
        String LOG_TAG;

        if (mediaTypeParam == MimeTypesSupported.IMAGE) {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE};
            selection = MediaStore.Images.Media.MIME_TYPE + "=? OR " + MediaStore.Images.Media.MIME_TYPE + "=?";
            mimeTypes = MimeTypesSupported.IMAGE.getMimeTypes();
            LOG_TAG = "ImageUri";
        } else {
            collection = MediaStore.Files.getContentUri("external");
            projection = new String[]{MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.MIME_TYPE};
            selection = MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?";
            mimeTypes = MimeTypesSupported.ANIMATED.getMimeTypes();
            LOG_TAG = "AnimatedUri";
        }

        String sortOrder = (MediaStore.Images.Media.DATE_ADDED) + " DESC";
        Cursor cursor = context.getContentResolver().query(collection, projection, selection, mimeTypes, sortOrder);
        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

            int dataColumn = cursor.getColumnIndexOrThrow(
                    mediaTypeParam == MimeTypesSupported.IMAGE ? MediaStore.Images.Media.DATA : MediaStore.Files.FileColumns.MEDIA_TYPE);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);

                Uri mediaUri = ContentUris.withAppendedId(collection, id);
                mediaUris.add(mediaUri);

                Log.i(LOG_TAG, "Uri: " + cursor.getString(dataColumn));
            }
            cursor.close();
        }

        return mediaUris;
    }
}
