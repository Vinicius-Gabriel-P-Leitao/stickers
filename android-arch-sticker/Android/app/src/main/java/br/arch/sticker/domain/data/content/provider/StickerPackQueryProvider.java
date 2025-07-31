/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.content.provider;

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.domain.data.content.helper.StickerPackQueryHelper;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class StickerPackQueryProvider {
    private final static String TAG_LOG = StickerPackQueryProvider.class.getSimpleName();

    private final StickerPackQueryHelper stickerPackQueryHelper;
    private final ApplicationTranslate applicationTranslate;
    private final Resources resources;

    public StickerPackQueryProvider(Context context) {
        this.resources = context.getResources();
        this.stickerPackQueryHelper = new StickerPackQueryHelper(context);
        this.applicationTranslate = new ApplicationTranslate(this.resources);
    }

    public Cursor fetchAllStickerPack(@NonNull Uri uri) {
        try {
            List<StickerPack> stickerPackList = stickerPackQueryHelper.fetchListStickerPackFromDatabase();
            if (stickerPackList.isEmpty()) {
                return new MatrixCursor(new String[]{
                        applicationTranslate.translate(R.string.error_sticker_pack_not_found)
                                .log(TAG_LOG, Level.WARN).get()});
            }

            return stickerPackQueryHelper.fetchListStickerPackData(uri, stickerPackList);
        } catch (SQLException sqlException) {
            Log.e(TAG_LOG, applicationTranslate.translate(R.string.error_sticker_pack_not_found)
                    .log(TAG_LOG, Level.ERROR, sqlException).get(), sqlException
            );
            throw sqlException;
        } catch (RuntimeException exception) {
            throw new RuntimeException(
                    applicationTranslate.translate(R.string.error_unknown)
                            .log(TAG_LOG, Level.ERROR, exception).get(), exception
            );
        }
    }

    public Cursor fetchSingleStickerPack(@NonNull Uri uri, boolean isFiltered) {
        final String stickerPackIdentifier = uri.getLastPathSegment();
        if (TextUtils.isEmpty(stickerPackIdentifier)) {
            return new MatrixCursor(new String[]{
                    applicationTranslate.translate(R.string.error_invalid_identifier)
                            .log(TAG_LOG, Level.ERROR, uri).get()});
        }

        try {
            StickerPack stickerPack = stickerPackQueryHelper.fetchStickerPackFromDatabase(
                    stickerPackIdentifier, isFiltered);
            if (stickerPack == null) {
                return new MatrixCursor(new String[]{
                        applicationTranslate.translate(R.string.error_sticker_pack_not_found_param,
                                stickerPackIdentifier
                        ).log(TAG_LOG, Level.WARN).get()});
            }

            return stickerPackQueryHelper.fetchStickerPackData(uri, stickerPack);
        } catch (SQLException sqlException) {
            Log.e(TAG_LOG, resources.getString(R.string.error_sticker_pack_not_found_param,
                            stickerPackIdentifier
                    ), sqlException
            );
            throw sqlException;
        } catch (RuntimeException exception) {
            throw new RuntimeException(applicationTranslate.translate(R.string.error_unknown)
                    .log(TAG_LOG, Level.ERROR, exception).get(), exception
            );
        }
    }
}
