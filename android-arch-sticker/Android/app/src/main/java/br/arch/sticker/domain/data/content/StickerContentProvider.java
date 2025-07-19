/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.content;

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;

import br.arch.sticker.BuildConfig;
import br.arch.sticker.R;
import br.arch.sticker.core.error.throwable.content.ContentProviderException;
import br.arch.sticker.domain.data.content.provider.StickerAssetProvider;
import br.arch.sticker.domain.data.content.provider.StickerPackQueryProvider;
import br.arch.sticker.domain.data.content.provider.StickerQueryProvider;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class StickerContentProvider extends ContentProvider {
    private final static String TAG_LOG = StickerContentProvider.class.getSimpleName();

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String STICKERS_ASSET = "stickers_asset";
    private static final String METADATA = "metadata";
    public static final String STICKERS = "stickers";

    private static final int METADATA_CODE = 1;
    private static final int METADATA_CODE_FOR_SINGLE_PACK = 2;
    private static final int METADATA_CODE_ALL_STICKERS = 3;
    private static final int STICKERS_FILES_CODE = 4;
    private static final int STICKER_PACK_TRAY_ICON_CODE = 5;

    public static final Uri AUTHORITY_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
            .appendPath(StickerContentProvider.METADATA).build();

    private StickerPackQueryProvider stickerPackQueryProvider;
    private ApplicationTranslate applicationTranslate;
    private StickerQueryProvider stickerQueryProvider;
    private StickerAssetProvider stickerAssetProvider;
    private Resources resources;
    private Context context;

    @Override
    public boolean onCreate() {
        final String authority = getAuthority();

        MATCHER.addURI(authority, METADATA, METADATA_CODE);
        MATCHER.addURI(authority, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK);
        MATCHER.addURI(authority, STICKERS + "/*", METADATA_CODE_ALL_STICKERS);
        MATCHER.addURI(authority, STICKERS_ASSET + "/*/*", STICKERS_FILES_CODE);

        context = getContext();
        if (context == null) {
            Log.e(TAG_LOG, "Context is null!");
            return false;
        }

        resources = context.getResources();

        applicationTranslate = new ApplicationTranslate(resources);

        stickerPackQueryProvider = new StickerPackQueryProvider(context);
        stickerQueryProvider = new StickerQueryProvider(context);
        stickerAssetProvider = new StickerAssetProvider(context);

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int code = MATCHER.match(uri);

        String callingPackage = getCallingPackage();
        boolean isWhatsApp = "com.whatsapp".equals(callingPackage);

        if (code == METADATA_CODE) {
            return stickerPackQueryProvider.fetchAllStickerPack(uri);
        } else if (code == METADATA_CODE_FOR_SINGLE_PACK) {
            if (isWhatsApp) {
                return stickerPackQueryProvider.fetchSingleStickerPack(uri, true);
            }

            return stickerPackQueryProvider.fetchSingleStickerPack(uri, false);
        } else if (code == METADATA_CODE_ALL_STICKERS) {
            return stickerQueryProvider.fetchStickerListForPack(uri);
        } else {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.throw_unknown_uri, uri)
                            .log(TAG_LOG, Level.ERROR).get());
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException(
                applicationTranslate.translate(R.string.throw_unsupported_operation)
                        .log(TAG_LOG, Level.ERROR).get());
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException(
                applicationTranslate.translate(R.string.throw_unsupported_operation)
                        .log(TAG_LOG, Level.ERROR).get());
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException(
                applicationTranslate.translate(R.string.throw_unsupported_operation)
                        .log(TAG_LOG, Level.ERROR).get());
    }

    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode) {
        final int code = MATCHER.match(uri);

        String callingPackage = getCallingPackage();
        boolean isWhatsApp = "com.whatsapp".equals(callingPackage);

        if (code == STICKERS_FILES_CODE || code == STICKER_PACK_TRAY_ICON_CODE) {
            try {
                return stickerAssetProvider.fetchStickerAsset(uri, isWhatsApp);
            } catch (ContentProviderException | FileNotFoundException exception) {
                if (isWhatsApp) {
                    return null;
                }

                try {
                    return context.getAssets().openFd("sticker_warning.webp");
                } catch (IOException ioException) {
                    Log.w(TAG_LOG, resources.getString(R.string.warn_log_fallback_not_found),
                            ioException
                    );
                    throw new RuntimeException(ioException);
                }
            }
        }

        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int code = MATCHER.match(uri);

        return switch (code) {
            case METADATA_CODE ->
                    "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." +
                            METADATA;
            case METADATA_CODE_FOR_SINGLE_PACK ->
                    "vnd.android.cursor.item/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." +
                            METADATA;
            case METADATA_CODE_ALL_STICKERS ->
                    "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." +
                            STICKERS;
            case STICKERS_FILES_CODE -> "image/webp";
            case STICKER_PACK_TRAY_ICON_CODE -> "image/jpg";

            default -> throw new ContentProviderException(
                    applicationTranslate.translate(R.string.throw_unknown_uri, uri)
                            .log(TAG_LOG, Level.ERROR).get());
        };
    }

    @NonNull
    private String getAuthority() {
        final String authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY;

        String packageName = context.getPackageName();
        if (packageName == null) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.throw_package_name_unavailable)
                            .log(TAG_LOG, Level.ERROR).get());
        }

        if (!authority.startsWith(packageName)) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.throw_invalid_authority, authority,
                            context.getPackageName()
                    ).log(TAG_LOG, Level.ERROR).get());
        }

        return authority;
    }
}
