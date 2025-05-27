/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package com.vinicius.sticker.domain.data.content.provider;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.isDatabaseEmpty;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.domain.data.content.helpers.StickerQueryHelper;
import com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class StickerContentProvider extends ContentProvider {
    public static final Uri AUTHORITY_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
            .appendPath(StickerContentProvider.METADATA)
            .build();
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String STICKERS = "stickers";
    public static final String STICKERS_ASSET = "stickers_asset";
    private static final String METADATA = "metadata";
    private static final int METADATA_CODE = 1;
    private static final int METADATA_CODE_FOR_SINGLE_PACK = 2;
    private static final int STICKERS_CODE = 3;
    private static final int STICKERS_FILES_CODE = 4;
    private static final int STICKER_PACK_TRAY_ICON_CODE = 5;
    private static final int CREATE_STICKER_PACKS = 6;

    StickerDatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        final String authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY;
        if (!authority.startsWith(Objects.requireNonNull(getContext()).getPackageName())) {
            throw new IllegalStateException(
                    "your authority (" + authority + ") for the content provider should start with your package name: " + getContext().getPackageName());
        }

        MATCHER.addURI(authority, METADATA, METADATA_CODE);
        MATCHER.addURI(authority, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK);
        MATCHER.addURI(authority, STICKERS + "/*", STICKERS_CODE);
        MATCHER.addURI(authority, STICKERS_ASSET + "/*/*", STICKERS_FILES_CODE);
        MATCHER.addURI(authority, "create", CREATE_STICKER_PACKS);

        dbHelper = new StickerDatabaseHelper(getContext());
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        if (isDatabaseEmpty(sqLiteDatabase)) {
            dbHelper.onCreate(sqLiteDatabase);
        }

        dbHelper.close();
        return true;
    }

    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection, String selection, String[] selectionArgs, String sortOrder
    ) {
        final int code = MATCHER.match(uri);
        StickerQueryHelper stickerQueryHelper = new StickerQueryHelper(getContext());

        if (code == METADATA_CODE) {
            return stickerQueryHelper.getPackForAllStickerPacks(uri, dbHelper);
        } else if (code == METADATA_CODE_FOR_SINGLE_PACK) {
            return stickerQueryHelper.getCursorForSingleStickerPack(uri, dbHelper);
        } else if (code == STICKERS_CODE) {
            return stickerQueryHelper.getStickersForAStickerPack(uri, dbHelper);
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(
            @NonNull Uri uri,
            @Nullable String selection, String[] selectionArgs
    ) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Uri insert(
            @NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int update(
            @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public AssetFileDescriptor openAssetFile(
            @NonNull Uri uri,
            @NonNull String mode
    ) {
        final int matchCode = MATCHER.match(uri);
        if (matchCode == STICKERS_FILES_CODE || matchCode == STICKER_PACK_TRAY_ICON_CODE) {
            return getImageFiles(uri);
        }
        return null;
    }

    @Override
    public String getType(
            @NonNull Uri uri
    ) {
        final int matchCode = MATCHER.match(uri);
        return switch (matchCode) {
            case METADATA_CODE ->
                    "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case METADATA_CODE_FOR_SINGLE_PACK ->
                    "vnd.android.cursor.item/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case STICKERS_CODE ->
                    "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + STICKERS;
            case STICKERS_FILES_CODE -> "image/webp";
            case STICKER_PACK_TRAY_ICON_CODE -> "image/png";
            default -> throw new IllegalArgumentException("Unknown URI: " + uri);
        };
    }

    private AssetFileDescriptor getImageFiles(Uri uri) throws IllegalArgumentException {
        Context context = Objects.requireNonNull(getContext());

        File stickerPackDir = new File(context.getFilesDir(), STICKERS_ASSET);

        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 3) {
            throw new IllegalArgumentException("path segments should be 3, uri is: " + uri);
        }
        String fileName = pathSegments.get(pathSegments.size() - 1);
        final String identifier = pathSegments.get(pathSegments.size() - 2);

        if (TextUtils.isEmpty(identifier)) {
            throw new IllegalArgumentException("identifier is empty, uri: " + uri);
        }
        if (TextUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("file name is empty, uri: " + uri);
        }

        File stickerDirectory = new File(stickerPackDir, identifier);
        if (!stickerDirectory.exists() || !stickerDirectory.isDirectory()) {
            throw new IllegalArgumentException("Sticker directory not found: " + stickerDirectory.getPath());
        }

        File stickerFile = new File(stickerDirectory, fileName);
        if (stickerFile.exists() && stickerFile.isFile()) {
            try {
                ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(stickerFile, ParcelFileDescriptor.MODE_READ_ONLY);
                return new AssetFileDescriptor(fileDescriptor, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
            } catch (IOException exception) {
                Log.e(getContext().getPackageName(), "Erro ao abrir stickerFile: " + stickerFile.getAbsolutePath(), exception);
            }
        }

        return null;
    }
}
