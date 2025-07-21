/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.content.provider;

import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static br.arch.sticker.view.core.util.convert.ConvertThumbnail.THUMBNAIL_FILE;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import br.arch.sticker.R;
import br.arch.sticker.core.error.throwable.base.InternalAppException;
import br.arch.sticker.core.error.throwable.content.ContentProviderException;
import br.arch.sticker.core.error.throwable.sticker.StickerFileException;
import br.arch.sticker.core.validation.StickerValidator;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.SelectStickerPackRepo;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

public class StickerAssetProvider {
    private final static String TAG_LOG = StickerAssetProvider.class.getSimpleName();

    private final SelectStickerPackRepo selectStickerPackRepo;
    private final ApplicationTranslate applicationTranslate;
    private final StickerValidator stickerValidator;
    private final Context context;

    public StickerAssetProvider(Context context) {
        this.context = context.getApplicationContext();
        this.stickerValidator = new StickerValidator(this.context);
        this.applicationTranslate = new ApplicationTranslate(this.context.getResources());
        this.selectStickerPackRepo = new SelectStickerPackRepo(
                StickerDatabaseHelper.getInstance(this.context).getReadableDatabase());
    }

    public AssetFileDescriptor fetchStickerAsset(Uri uri, boolean isWhatsApp)
            throws ContentProviderException, FileNotFoundException {
        final File stickerPackDir = new File(context.getFilesDir(), STICKERS_ASSET);
        final List<String> pathSegments = uri.getPathSegments();

        if (pathSegments.size() != 3) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.error_invalid_path_segments, uri)
                            .log(TAG_LOG, Level.ERROR).get());
        }

        final String fileName = pathSegments.get(2);
        final String stickerPackIdentifier = pathSegments.get(1);

        if (TextUtils.isEmpty(stickerPackIdentifier)) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.error_invalid_identifier, uri)
                            .log(TAG_LOG, Level.ERROR).get());
        }

        if (TextUtils.isEmpty(fileName)) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.error_invalid_sticker_filename, uri)
                            .log(TAG_LOG, Level.ERROR).get());
        }

        final File stickerDirectory = new File(stickerPackDir, stickerPackIdentifier);
        final File stickerFile = new File(stickerDirectory, fileName);

        if (!stickerDirectory.exists() || !stickerDirectory.isDirectory()) {
            throw new FileNotFoundException(
                    applicationTranslate.translate(R.string.error_could_not_extract_path,
                            stickerDirectory.getPath()
                    ).log(TAG_LOG, Level.ERROR).get());
        }

        if (!stickerFile.exists() || !stickerFile.isFile()) {
            throw new FileNotFoundException(
                    applicationTranslate.translate(R.string.error_file_not_found,
                            stickerFile.getAbsolutePath()
                    ).log(TAG_LOG, Level.ERROR).get());
        }

        if (THUMBNAIL_FILE.equalsIgnoreCase(fileName)) {
            return openAssetFileSafely(stickerFile, "thumbnail");
        }

        try (Cursor cursor = selectStickerPackRepo.selectStickerPackIsAnimated(
                stickerPackIdentifier)) {
            if (cursor == null) {
                throw new ContentProviderException(
                        applicationTranslate.translate(R.string.error_null_cursor)
                                .log(TAG_LOG, Level.ERROR).get());
            }

            if (!cursor.moveToFirst()) {
                throw new ContentProviderException(
                        applicationTranslate.translate(R.string.error_sticker_pack_not_found,
                                stickerPackIdentifier
                        ).log(TAG_LOG, Level.ERROR).get());
            }

            final boolean animatedStickerPack =
                    cursor.getInt(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) != 0;
            if (isWhatsApp) {
                if (!fileName.toLowerCase(Locale.ROOT).endsWith(".webp")) {
                    Log.w(TAG_LOG,
                            applicationTranslate.translate(R.string.warn_non_webp_file, fileName)
                                    .get()
                    );
                    return null;
                }

                try {
                    stickerValidator.validateStickerFile(stickerPackIdentifier, fileName,
                            animatedStickerPack
                    );
                } catch (StickerFileException | InternalAppException exception) {
                    throw new ContentProviderException(
                            applicationTranslate.translate(R.string.error_invalid_sticker)
                                    .log(TAG_LOG, Level.WARN, stickerFile.getAbsolutePath(),
                                            fileName, exception.getMessage()
                                    ).get(), exception
                    );
                }
            } else {
                Log.d(TAG_LOG, (applicationTranslate.translate(R.string.debug_validation_skipped,
                                stickerFile.getAbsolutePath()
                        ).get())
                );
            }

            return this.openAssetFileSafely(stickerFile, "sticker");
        } catch (SQLException sqlException) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.error_sticker_pack_not_found,
                            stickerPackIdentifier
                    ).log(TAG_LOG, Level.ERROR, sqlException).get(), sqlException
            );
        } catch (RuntimeException exception) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.error_unknown, stickerPackIdentifier)
                            .log(TAG_LOG, Level.ERROR, exception).get(), exception
            );
        }
    }

    private AssetFileDescriptor openAssetFileSafely(File file, String type) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file,
                    ParcelFileDescriptor.MODE_READ_ONLY
            );
            return new AssetFileDescriptor(parcelFileDescriptor, 0,
                    AssetFileDescriptor.UNKNOWN_LENGTH
            );
        } catch (IOException exception) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.error_open_file_error, type,
                            file.getAbsolutePath()
                    ).log(TAG_LOG, Level.ERROR).get(), exception
            );
        }
    }
}
