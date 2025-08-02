/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.delete;

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.DeleteStickerException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.DeleteStickerRepo;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class DeleteStickerService {
    private final static String TAG_LOG = DeleteStickerService.class.getSimpleName();

    private final ApplicationTranslate applicationTranslate;
    private final DeleteStickerRepo deleteStickerPackRepo;

    public DeleteStickerService(Context paramContext) {
        Context context = paramContext.getApplicationContext();
        SQLiteDatabase database = StickerDatabaseHelper.getInstance(context).getWritableDatabase();
        this.applicationTranslate = new ApplicationTranslate(context.getResources());
        this.deleteStickerPackRepo = new DeleteStickerRepo(database);
    }

    public CallbackResult<Boolean> deleteStickerByPack(String stickerPackIdentifier, String fileName) {
        if (stickerPackIdentifier == null || fileName == null) {
            throw new DeleteStickerException(
                    applicationTranslate.translate(R.string.error_delete_sticker_pack_id)
                            .log(TAG_LOG, Level.ERROR).get(), ErrorCode.ERROR_PACK_DELETE_DB
            );
        }

        try {
            Integer deletedSticker = deleteStickerPackRepo.deleteSticker(stickerPackIdentifier,
                    fileName
            );

            if (deletedSticker > 0) {
                Log.i(TAG_LOG,
                        applicationTranslate.translate(R.string.information_sticker_deleted_success)
                                .get()
                );
                return CallbackResult.success(Boolean.TRUE);
            } else {
                return CallbackResult.warning(
                        applicationTranslate.translate(R.string.warn_no_sticker_deleted, fileName)
                                .log(TAG_LOG, Level.WARN).get());
            }
        } catch (IllegalArgumentException | SQLiteException exception) {
            return CallbackResult.failure(new DeleteStickerException(
                    applicationTranslate.translate(R.string.error_delete_sticker_db)
                            .log(TAG_LOG, Level.ERROR).get(), exception,
                    ErrorCode.ERROR_PACK_DELETE_DB
            ));
        }
    }
}
