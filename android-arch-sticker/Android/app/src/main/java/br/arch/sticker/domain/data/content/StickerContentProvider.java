/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.content;

import static br.arch.sticker.domain.data.database.StickerDatabase.isDatabaseEmpty;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;

import br.arch.sticker.BuildConfig;
import br.arch.sticker.core.error.throwable.content.ContentProviderException;
import br.arch.sticker.domain.data.content.provider.StickerAssetProvider;
import br.arch.sticker.domain.data.content.provider.StickerPackQueryProvider;
import br.arch.sticker.domain.data.content.provider.StickerQueryProvider;
import br.arch.sticker.domain.data.database.StickerDatabase;

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

    private static StickerDatabase dbHelper;

    public static final Uri AUTHORITY_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(
            BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METADATA).build();

    @Override
    public boolean onCreate()
        {
            final String authority = getAuthority();

            MATCHER.addURI(authority, METADATA, METADATA_CODE);
            MATCHER.addURI(authority, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK);
            MATCHER.addURI(authority, STICKERS + "/*", METADATA_CODE_ALL_STICKERS);
            MATCHER.addURI(authority, STICKERS_ASSET + "/*/*", STICKERS_FILES_CODE);

            dbHelper = new StickerDatabase(getContext());
            SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

            if (isDatabaseEmpty(sqLiteDatabase)) {
                dbHelper.onCreate(sqLiteDatabase);
            }

            dbHelper.close();
            return true;
        }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, String selection, String[] selectionArgs, String sortOrder)
        {
            final int code = MATCHER.match(uri);

            Context context = getContext();
            if (context == null) {
                throw new ContentProviderException("Contexto do content provider não disponível!");
            }

            String callingPackage = getCallingPackage();
            boolean isWhatsApp = "com.whatsapp".equals(callingPackage);

            StickerPackQueryProvider stickerPackQueryProvider = new StickerPackQueryProvider(context);
            StickerQueryProvider stickerQueryProvider = new StickerQueryProvider(context);

            if (code == METADATA_CODE) {
                return stickerPackQueryProvider.fetchAllStickerPack(uri, dbHelper);
            } else if (code == METADATA_CODE_FOR_SINGLE_PACK) {
                if (isWhatsApp) {
                    return stickerPackQueryProvider.fetchSingleStickerPack(uri, dbHelper, true);
                }

                return stickerPackQueryProvider.fetchSingleStickerPack(uri, dbHelper, false);
            } else if (code == METADATA_CODE_ALL_STICKERS) {
                return stickerQueryProvider.fetchStickerListForPack(uri, dbHelper);
            } else {
                throw new ContentProviderException("URI desconhecida: " + uri);
            }
        }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, String[] selectionArgs)
        {
            throw new UnsupportedOperationException("Operação não suportada!");
        }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values)
        {
            throw new UnsupportedOperationException("Operação não suportada!");
        }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs)
        {
            throw new UnsupportedOperationException("Operação não suportada!");
        }

    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode)
        {
            final int code = MATCHER.match(uri);

            Context context = getContext();
            if (context == null) {
                throw new ContentProviderException("Contexto do content provider não disponível!");
            }

            String callingPackage = getCallingPackage();
            boolean isWhatsApp = "com.whatsapp".equals(callingPackage);

            if (code == STICKERS_FILES_CODE || code == STICKER_PACK_TRAY_ICON_CODE) {
                try {
                    StickerAssetProvider stickerAssetProvider = new StickerAssetProvider(context);
                    return stickerAssetProvider.fetchStickerAsset(uri, dbHelper, isWhatsApp);
                } catch (ContentProviderException | FileNotFoundException exception) {
                    if (isWhatsApp) {
                        return null;
                    }

                    try {
                        return context.getAssets().openFd("sticker_3rdparty_warning.webp");
                    } catch (IOException ioException) {
                        Log.w(TAG_LOG, "Fallback não encontrado em assets", ioException);
                        try {
                            throw ioException;
                        } catch (IOException runtimeException) {
                            throw new RuntimeException(runtimeException);
                        }
                    }
                }
            }

            return null;
        }

    @Override
    public String getType(@NonNull Uri uri)
        {
            final int code = MATCHER.match(uri);

            return switch (code) {
                case METADATA_CODE -> "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
                case METADATA_CODE_FOR_SINGLE_PACK -> "vnd.android.cursor.item/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
                case METADATA_CODE_ALL_STICKERS -> "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + STICKERS;
                case STICKERS_FILES_CODE -> "image/webp";
                case STICKER_PACK_TRAY_ICON_CODE -> "image/jpg";

                default -> throw new ContentProviderException("URI desconhecida: " + uri);
            };
        }

    @NonNull
    private String getAuthority()
        {
            final String authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY;

            Context context = getContext();
            if (context == null) {
                throw new ContentProviderException("Contexto do content provider não disponível!");
            }

            String packageName = getContext().getPackageName();
            if (packageName == null) {
                throw new ContentProviderException("Nome do pacote do content provider não disponível!");
            }

            if (!authority.startsWith(packageName)) {
                throw new ContentProviderException(
                        "Sua autoridade (" + authority + ") para o provedor de conteúdo deve começar com o nome do seu pacote: " + getContext().getPackageName());
            }

            return authority;
        }
}
