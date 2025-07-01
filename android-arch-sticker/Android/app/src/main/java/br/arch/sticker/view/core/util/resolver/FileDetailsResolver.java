/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.util.resolver;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.HashMap;
import java.util.Map;

public class FileDetailsResolver {
    private final Context context;

    public FileDetailsResolver(Context context)
        {
            this.context = context.getApplicationContext();
        }

    public Map<String, String> getFileDetailsFromUri(Uri uri)
        {
            Map<String, String> fileDetails = new HashMap<>();
            String path = getAbsolutePath(uri);
            String mimeType = context.getContentResolver().getType(uri);
            if (path != null && mimeType != null) {
                fileDetails.put(path, mimeType);
            }
            return fileDetails;
        }

    public String getAbsolutePath(Uri uri)
        {
            String[] projection = {MediaStore.Files.FileColumns.DATA};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor == null || !cursor.moveToFirst()) {
                    return uri.getPath();
                }
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            } catch (Exception exception) {
                return uri.getPath();
            }
        }
}
