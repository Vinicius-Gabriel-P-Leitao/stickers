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
package com.vinicius.sticker.domain.data.provider;

import static com.vinicius.sticker.domain.data.database.StickerDatabaseHelper.isDatabaseEmpty;
import static com.vinicius.sticker.domain.data.repository.SelectStickerPacks.getCursorForSingleStickerPack;
import static com.vinicius.sticker.domain.data.repository.SelectStickerPacks.getPackForAllStickerPacks;
import static com.vinicius.sticker.domain.data.repository.SelectStickerPacks.getStickersForAStickerPack;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.domain.data.database.StickerDatabaseHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class StickerContentProvider extends ContentProvider {

   /**
    * Do not change the strings listed below, as these are used by WhatsApp. And changing these will break the interface between sticker app and WhatsApp.
    */
   public static final String STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier";
   public static final String STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name";
   public static final String STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher";
   public static final String STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon";
   public static final String ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link";
   public static final String IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link";
   public static final String PUBLISHER_EMAIL = "sticker_pack_publisher_email";
   public static final String PUBLISHER_WEBSITE = "sticker_pack_publisher_website";
   public static final String PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website";
   public static final String LICENSE_AGREEMENT_WEBSITE = "sticker_pack_license_agreement_website";
   public static final String IMAGE_DATA_VERSION = "image_data_version";
   public static final String AVOID_CACHE = "whatsapp_will_not_cache_stickers";
   public static final String ANIMATED_STICKER_PACK = "animated_sticker_pack";

   public static final String STICKER_FILE_NAME_IN_QUERY = "sticker_file_name";
   public static final String STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji";
   public static final String STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY = "sticker_accessibility_text";
   public static final String STICKERS = "stickers";
   public static final String STICKERS_ASSET = "stickers_asset";
   private static final String CONTENT_FILE_NAME = "contents.json";
   /**
    * Do not change the values in the UriMatcher because otherwise, WhatsApp will not be able to fetch the stickers from the ContentProvider.
    */
   private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
   private static final String METADATA = "metadata";
   public static final Uri AUTHORITY_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
       .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
       .appendPath(StickerContentProvider.METADATA)
       .build();
   private static final int METADATA_CODE = 1;
   private static final int METADATA_CODE_FOR_SINGLE_PACK = 2;
   private static final int STICKERS_CODE = 3;
   private static final int STICKERS_ASSET_CODE = 4;
   private static final int STICKER_PACK_TRAY_ICON_CODE = 5;
   StickerDatabaseHelper dbHelper;

   @Override
   public boolean onCreate() {
      final String authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY;
      if ( !authority.startsWith(Objects.requireNonNull(getContext()).getPackageName()) ) {
         throw new IllegalStateException("your authority (" +
                                             authority +
                                             ") for the content provider should start with your package name: " +
                                             getContext().getPackageName());
      }

      MATCHER.addURI(authority, METADATA, METADATA_CODE);
      MATCHER.addURI(authority, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK);
      MATCHER.addURI(authority, STICKERS + "/*", STICKERS_CODE);

      dbHelper = new StickerDatabaseHelper(getContext());
      SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

      if ( isDatabaseEmpty(sqLiteDatabase) ) {
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
      if ( code == METADATA_CODE ) {
         return getPackForAllStickerPacks(uri, dbHelper);
      } else if ( code == METADATA_CODE_FOR_SINGLE_PACK ) {
         return getCursorForSingleStickerPack(uri, dbHelper);
      } else if ( code == STICKERS_CODE ) {
         return getStickersForAStickerPack(uri, dbHelper);
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
      if ( matchCode == STICKERS_ASSET_CODE || matchCode == STICKER_PACK_TRAY_ICON_CODE ) {
         return getImageAsset(uri);
      }
      return null;
   }

   @Override
   public String getType(
       @NonNull Uri uri
   ) {
      final int matchCode = MATCHER.match(uri);
      return switch (matchCode) {
         case METADATA_CODE -> "vnd.android.cursor.dir/vnd." +
             BuildConfig.CONTENT_PROVIDER_AUTHORITY +
             "." +
             METADATA;
         case METADATA_CODE_FOR_SINGLE_PACK -> "vnd.android.cursor.item/vnd." +
             BuildConfig.CONTENT_PROVIDER_AUTHORITY +
             "." +
             METADATA;
         case STICKERS_CODE -> "vnd.android.cursor.dir/vnd." +
             BuildConfig.CONTENT_PROVIDER_AUTHORITY +
             "." +
             STICKERS;
         case STICKERS_ASSET_CODE -> "image/webp";
         case STICKER_PACK_TRAY_ICON_CODE -> "image/png";
         default -> throw new IllegalArgumentException("Unknown URI: " + uri);
      };
   }

   private AssetFileDescriptor getImageAsset(Uri uri) throws IllegalArgumentException {
      Context context = Objects.requireNonNull(getContext());

      File customFolder = new File(context.getFilesDir(), "custom_stickers");

      final List<String> pathSegments = uri.getPathSegments();
      if ( pathSegments.size() != 3 ) {
         throw new IllegalArgumentException("path segments should be 3, uri is: " + uri);
      }
      String fileName = pathSegments.get(pathSegments.size() - 1);
      final String identifier = pathSegments.get(pathSegments.size() - 2);

      if ( TextUtils.isEmpty(identifier) ) {
         throw new IllegalArgumentException("identifier is empty, uri: " + uri);
      }
      if ( TextUtils.isEmpty(fileName) ) {
         throw new IllegalArgumentException("file name is empty, uri: " + uri);
      }

      File stickerDirectory = new File(customFolder, identifier);
      if ( !stickerDirectory.exists() || !stickerDirectory.isDirectory() ) {
         throw new IllegalArgumentException(
             "Sticker directory not found: " + stickerDirectory.getPath());
      }

      File stickerFile = new File(stickerDirectory, fileName);
      if ( stickerFile.exists() && stickerFile.isFile() ) {
         try {
            return context.getContentResolver()
                .openAssetFileDescriptor(Uri.fromFile(stickerFile), "r");
         } catch (IOException exception) {
            Log.e(getContext().getPackageName(), "IOException when getting asset file, uri:" + uri,
                  exception
            );
         }
      }

      return null;
   }
}
