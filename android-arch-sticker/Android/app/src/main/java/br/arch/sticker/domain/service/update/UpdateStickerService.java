/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
package br.arch.sticker.domain.service.update;

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.UpdateStickerException;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.UpdateStickerRepo;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class UpdateStickerService {
    private static final String TAG_LOG = UpdateStickerService.class.getSimpleName();

    private final ApplicationTranslate applicationTranslate;
    private final UpdateStickerRepo updateStickerRepo;

    public UpdateStickerService(Context paramContext) {
        Context context = paramContext.getApplicationContext();
        SQLiteDatabase database = StickerDatabaseHelper.getInstance(context).getWritableDatabase();

        Resources resources = context.getResources();
        this.applicationTranslate = new ApplicationTranslate(resources);
        this.updateStickerRepo = new UpdateStickerRepo(database, resources);
    }

    public boolean updateStickerFileName(String stickerPackIdentifier, String newFileName, String oldFileName) throws UpdateStickerException {
        if (stickerPackIdentifier.isEmpty() || newFileName.isEmpty() || oldFileName.isEmpty()) {
            Log.w(TAG_LOG, applicationTranslate.translate(R.string.warn_invalid_parameters_rename_sticker).get());
            return false;
        }

        Log.d(TAG_LOG, applicationTranslate.translate(R.string.debug_update_sticker_filename, stickerPackIdentifier, oldFileName, newFileName).get());

        if (updateStickerRepo.updateStickerFileName(stickerPackIdentifier, newFileName, oldFileName)) {
            return true;
        }

        throw new UpdateStickerException(applicationTranslate.translate(R.string.error_update_sticker_filename).get(),
                ErrorCode.ERROR_EMPTY_STICKERPACK
        );
    }

    public void updateInvalidSticker(String stickerPackIdentifier, String fileName, ErrorCode errorCode) throws UpdateStickerException {
        if (stickerPackIdentifier.isEmpty() || fileName.isEmpty() || errorCode == null) {
            Log.w(TAG_LOG, applicationTranslate.translate(R.string.warn_invalid_parameters_mark_invalid).get());
            return;
        }

        String errorName = errorCode.name();

        Log.d(TAG_LOG, applicationTranslate.translate(R.string.debug_mark_sticker_invalid, stickerPackIdentifier, fileName, errorName).get());

        boolean updated = updateStickerRepo.updateInvalidSticker(stickerPackIdentifier, fileName, errorName);

        if (!updated) {
            throw new UpdateStickerException(applicationTranslate.translate(R.string.error_update_sticker_filename).log(TAG_LOG, Level.ERROR).get(),
                    ErrorCode.ERROR_EMPTY_STICKERPACK
            );
        }

    }
}
