/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.content.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.domain.data.content.helper.StickerQueryHelper;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class StickerQueryProvider {
    private final static String TAG_LOG = StickerQueryProvider.class.getSimpleName();

    private final ApplicationTranslate applicationTranslate;
    private final StickerQueryHelper stickerQueryHelper;

    public StickerQueryProvider(Context context) {
        this.applicationTranslate = new ApplicationTranslate(context.getResources());
        this.stickerQueryHelper = new StickerQueryHelper(context);
    }

    @NonNull
    public Cursor fetchStickerListForPack(@NonNull Uri uri) {
        final String stickerPackIdentifier = uri.getLastPathSegment();

        try {
            if (TextUtils.isEmpty(stickerPackIdentifier)) {
                Log.e(TAG_LOG,
                        applicationTranslate.translate(R.string.error_invalid_identifier).get() +
                                uri
                );
                return stickerQueryHelper.fetchStickerData(uri, new ArrayList<>());
            }

            List<Sticker> stickerPack = stickerQueryHelper.fetchStickerListFromDatabase(
                    stickerPackIdentifier);
            return stickerQueryHelper.fetchStickerData(uri, stickerPack);
        } catch (RuntimeException exception) {
            Log.e(TAG_LOG, applicationTranslate.translate(R.string.error_loading_sticker_pack,
                            stickerPackIdentifier
                    ).get(), exception
            );
            return stickerQueryHelper.fetchStickerData(uri, new ArrayList<>());
        }
    }
}
