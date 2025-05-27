/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


package com.vinicius.sticker.presentation.feature.media.util;

import static com.vinicius.sticker.presentation.feature.media.launcher.GalleryMediaPickerLauncher.ANIMATED_MIME_TYPES;
import static com.vinicius.sticker.presentation.feature.media.launcher.GalleryMediaPickerLauncher.IMAGE_MIME_TYPES;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.vinicius.sticker.presentation.feature.media.launcher.GalleryMediaPickerLauncher;

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
    public static List<Uri> getMediaUris(Context context, String[] mimeTypes) {
        List<Uri> mediaUris;

        if (validateArraysMimeTypes(mimeTypes, IMAGE_MIME_TYPES)) {
            mediaUris = getMediaUris(context, GalleryMediaPickerLauncher.MediaType.IMAGE_MIME_TYPES);
        } else if (validateArraysMimeTypes(mimeTypes, ANIMATED_MIME_TYPES)) {
            mediaUris = getMediaUris(context, GalleryMediaPickerLauncher.MediaType.ANIMATED_MIME_TYPES);
        } else {
            throw new IllegalArgumentException("Tipo MIME não suportado para conversão: " + mimeTypes);
        }

        return mediaUris;
    }

    /**
     * <p><b>Descrição:</b>Captura o caminho absoluto da URI de um arquivo.</p>
     *
     * @param context Contexto da aplicação.
     * @param uri     Uri do arquivo.
     * @return Caminho do arquivo.
     */
    public static String getAbsolutePath(Context context, Uri uri) {
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return uri.getPath();
        } else {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            String path = cursor.getString(column_index);

            cursor.close();
            return path;
        }
    }

    /**
     * <p><b>Descrição:</b>Busca uma lista de URI de imagens ou arquivos animados, baseado no enum MediaType.</p>
     *
     * @param context   Contexto da aplicação.
     * @param mediaType mimeType recebido para buscar as URI.
     * @return Lista com as URIs.
     */
    public static List<Uri> getMediaUris(Context context, GalleryMediaPickerLauncher.MediaType mediaType) {
        List<Uri> mediaUris = new ArrayList<>();

        Uri collection;
        String[] projection;
        String selection;
        String[] mimeTypes;
        String logTag;

        if (mediaType == GalleryMediaPickerLauncher.MediaType.IMAGE_MIME_TYPES) {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE};
            selection = MediaStore.Images.Media.MIME_TYPE + "=? OR " + MediaStore.Images.Media.MIME_TYPE + "=?";
            mimeTypes = IMAGE_MIME_TYPES;
            logTag = "imageUri";
        } else {
            collection = MediaStore.Files.getContentUri("external");
            projection =
                    new String[]{MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.MIME_TYPE};
            selection = MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?";
            mimeTypes = ANIMATED_MIME_TYPES;
            logTag = "animatedUri";
        }

        String sortOrder = (MediaStore.Images.Media.DATE_ADDED) + " DESC";

        Cursor cursor = context.getContentResolver().query(collection, projection, selection, mimeTypes, sortOrder);

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

            int dataColumn = cursor.getColumnIndexOrThrow(mediaType == GalleryMediaPickerLauncher.MediaType.IMAGE_MIME_TYPES ?
                                                          MediaStore.Images.Media.DATA :
                                                          MediaStore.Files.FileColumns.MEDIA_TYPE);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);

                Uri mediaUri = ContentUris.withAppendedId(collection, id);
                mediaUris.add(mediaUri);

                Log.i(logTag, "Uri: " + cursor.getString(dataColumn));
            }
            cursor.close();
        }

        return mediaUris;
    }

    /**
     * <p><b>Descrição:</b>Valida se uma array de string com dados referente a mimetypes são iguais.</p>
     *
     * @param mimeTypes       mimeType a ser usado como referencia.
     * @param staticMimeTypes mimeType recebido.
     * @return Resultado com booleano para retorno.
     */
    public static boolean validateArraysMimeTypes(String[] mimeTypes, String[] staticMimeTypes) {
        for (String type : staticMimeTypes) {
            Log.d("MimeTypeCheck", "Comparando MIME: " + Arrays.toString(mimeTypes) + " com " + type);
            if (Arrays.equals(mimeTypes, staticMimeTypes)) {
                return true;
            }
        }
        return false;
    }
}
