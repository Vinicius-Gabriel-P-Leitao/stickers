/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.util.resolver;

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.media.MediaConversionException;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;

public class UriDetailsResolver {
    private final static String TAG_LOG = UriDetailsResolver.class.getSimpleName();

    public static List<Uri> fetchMediaUri(Context context, String[] mimeTypes) {
        List<Uri> mediaUris;

        if (Arrays.equals(mimeTypes, MimeTypesSupported.IMAGE.getMimeTypes())) {
            mediaUris = fetchListUri(context, MimeTypesSupported.IMAGE);
        } else if (Arrays.equals(mimeTypes, MimeTypesSupported.ANIMATED.getMimeTypes())) {
            mediaUris = fetchListUri(context, MimeTypesSupported.ANIMATED);
        } else {
            throw new MediaConversionException(
                    ApplicationTranslate.translate(context, R.string.error_unsupported_file_type).log(TAG_LOG, Level.ERROR).get(),
                    ErrorCode.ERROR_PACK_CONVERSION_MEDIA
            );
        }

        return mediaUris;
    }

    public static List<Uri> fetchListUri(Context context, MimeTypesSupported mediaTypeParam) {
        List<Uri> mediaUris = new ArrayList<>();

        Uri collection;
        String[] projection;
        String selectionColumn;
        String[] mimeTypes;
        String LOG_TAG;

        if (mediaTypeParam == MimeTypesSupported.IMAGE) {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE};
            mimeTypes = MimeTypesSupported.IMAGE.getMimeTypes();
            selectionColumn = MediaStore.Images.Media.MIME_TYPE;
            LOG_TAG = "ImageUri";
        } else {
            collection = MediaStore.Files.getContentUri("external");
            projection = new String[]{MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE};
            mimeTypes = MimeTypesSupported.ANIMATED.getMimeTypes();
            selectionColumn = MediaStore.Files.FileColumns.MIME_TYPE;
            LOG_TAG = "AnimatedUri";
        }

        String placeholders = String.join(",", Collections.nCopies(mimeTypes.length, "?"));
        String selection = selectionColumn + " IN (" + placeholders + ")";

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
