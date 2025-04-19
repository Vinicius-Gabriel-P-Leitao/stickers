package com.whatsapp.sticker.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CursorFindMediaActivity {
    public static final String[] IMAGE_MIME_TYPES = {"image/jpeg", "image/png", "image/webp"};
    public static final String[] ANIMATED_MIME_TYPE = {"video/mp4", "image/gif"};

    public static List<String> getMediaPaths(Context context, String[] mimeTypes) {
        List<String> mediaPaths = new ArrayList<>();
        String[] projection = {
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATA
        };

        StringBuilder selectionBuilder = new StringBuilder();
        for (int i = 0; i < mimeTypes.length; i++) {
            selectionBuilder.append(MediaStore.MediaColumns.MIME_TYPE).append("=?");
            if (i < mimeTypes.length - 1) selectionBuilder.append(" OR ");
        }
        String selection = selectionBuilder.toString();

        Uri collectionUri;
        boolean containsOnlyVideos = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            containsOnlyVideos = Arrays.stream(mimeTypes).allMatch(type -> type.startsWith("video/mp4"));
        }
        collectionUri = containsOnlyVideos
                ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


        Cursor cursor = context.getContentResolver().query(
                collectionUri,
                projection,
                selection,
                mimeTypes,
                MediaStore.Images.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                mediaPaths.add(imagePath);
            }
            cursor.close();
        }

        return mediaPaths;
    }
}
