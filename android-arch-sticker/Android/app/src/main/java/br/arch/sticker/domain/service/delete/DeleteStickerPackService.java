/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.delete;

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.core.error.code.DeleteErrorCode;
import br.arch.sticker.core.error.throwable.sticker.DeleteStickerException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.DeleteStickerPackRepo;
import br.arch.sticker.domain.data.database.repository.DeleteStickerRepo;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class DeleteStickerPackService {
    private final static String TAG_LOG = DeleteStickerPackService.class.getSimpleName();

    private final DeleteStickerPackRepo deleteStickerPackRepo;
    private final ApplicationTranslate applicationTranslate;
    private final DeleteStickerRepo deleteStickerRepo;

    public DeleteStickerPackService(Context context) {
        SQLiteDatabase database = StickerDatabaseHelper.getInstance(context).getWritableDatabase();
        this.applicationTranslate = new ApplicationTranslate(context.getResources());
        this.deleteStickerPackRepo = new DeleteStickerPackRepo(database);
        this.deleteStickerRepo = new DeleteStickerRepo(database);
    }

    public CallbackResult<Boolean> deleteStickerPack(String stickerPackIdentifier) {
        if (stickerPackIdentifier == null) {
            return CallbackResult.failure(new DeleteStickerException(
                    applicationTranslate.translate(R.string.error_invalid_identifier)
                            .log(TAG_LOG, Level.ERROR).get(), DeleteErrorCode.ERROR_PACK_DELETE_DB
            ));
        }

        try {
            Integer stickerPackDeletedInDb = deleteStickerPackRepo.deleteStickerPackFromDatabase(
                    stickerPackIdentifier);

            if (stickerPackDeletedInDb == 0) {
                return CallbackResult.warning(
                        applicationTranslate.translate(R.string.warn_no_pack_deleted)
                                .log(TAG_LOG, Level.WARN).get());
            }

            return CallbackResult.success(Boolean.TRUE);
        } catch (IllegalArgumentException | SQLiteException runtimeException) {
            return CallbackResult.failure(new DeleteStickerException(
                    applicationTranslate.translate(R.string.error_delete_sticker_db)
                            .log(TAG_LOG, Level.ERROR, runtimeException).get(), runtimeException,
                    DeleteErrorCode.ERROR_PACK_DELETE_DB
            ));
        } catch (Exception exception) {
            return CallbackResult.failure(new DeleteStickerException(
                    applicationTranslate.translate(R.string.error_unknown)
                            .log(TAG_LOG, Level.ERROR, exception).get(), exception,
                    DeleteErrorCode.ERROR_PACK_DELETE_DB
            ));
        }
    }

    public CallbackResult<Boolean> deleteSpareStickerPack(String stickerPackIdentifier, List<String> stickersFileNameToDelete) {
        if (stickerPackIdentifier == null) {
            return CallbackResult.failure(new DeleteStickerException(
                    applicationTranslate.translate(R.string.error_invalid_identifier)
                            .log(TAG_LOG, Level.ERROR).get(), DeleteErrorCode.ERROR_PACK_DELETE_DB
            ));
        }

        if (stickersFileNameToDelete == null || stickersFileNameToDelete.isEmpty()) {
            throw new DeleteStickerException(
                    applicationTranslate.translate(R.string.error_sticker_file_not_found)
                            .log(TAG_LOG, Level.ERROR).get(), DeleteErrorCode.ERROR_PACK_DELETE_DB
            );
        }

        try {

            Integer listStickerDeletedInDb = deleteStickerRepo.deleteListSticker(
                    stickerPackIdentifier, stickersFileNameToDelete);

            if (listStickerDeletedInDb == 0) {
                return CallbackResult.warning(
                        applicationTranslate.translate(R.string.warn_no_pack_deleted)
                                .log(TAG_LOG, Level.WARN).get());
            }

            return CallbackResult.success(Boolean.TRUE);
        } catch (Exception exception) {
            return CallbackResult.failure(new DeleteStickerException(
                    applicationTranslate.translate(R.string.error_unknown)
                            .log(TAG_LOG, Level.ERROR, exception).get(), exception,
                    DeleteErrorCode.ERROR_STICKER_DELETE_DB
            ));
        }
    }
}
