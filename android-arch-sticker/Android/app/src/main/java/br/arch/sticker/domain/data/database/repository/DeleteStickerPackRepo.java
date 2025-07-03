/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import br.arch.sticker.core.error.code.DeleteErrorCode;
import br.arch.sticker.core.error.throwable.sticker.DeleteStickerException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;

public class DeleteStickerPackRepo {
    private final static String TAG_LOG = DeleteStickerPackRepo.class.getSimpleName();

    private final SQLiteDatabase database;

    public DeleteStickerPackRepo(SQLiteDatabase database) {
        this.database = database;
    }

    public int deleteSticker(String stickerPackIdentifier, String fileName) throws DeleteStickerException {
        if (stickerPackIdentifier == null || fileName == null) {
            throw new DeleteStickerException(
                    "Erro ao encontrar o id do pacote para deletar figurinha.",
                    DeleteErrorCode.ERROR_PACK_DELETE_DB);
        }

        try {
            String query = StickerDatabaseHelper.FK_STICKER_PACK + " = ? AND " + StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY + " = ?";

            return database.delete(StickerDatabaseHelper.TABLE_STICKER, query,
                    new String[]{stickerPackIdentifier, fileName});
        } catch (IllegalArgumentException | SQLiteException exception) {
            Log.e(TAG_LOG, "Erro ao deletar sticker: " + exception.getMessage(), exception);

            throw new DeleteStickerException("Erro no banco ao tentar deletar sticker.", exception,
                    DeleteErrorCode.ERROR_PACK_DELETE_DB);
        }
    }

    public CallbackResult<Integer> deleteStickerPackFromDatabase(String stickerPackIdentifier) throws DeleteStickerException {
        if (stickerPackIdentifier == null) {
            return CallbackResult.failure(
                    new DeleteStickerException("Identificador do pacote está nulo.",
                            DeleteErrorCode.ERROR_PACK_DELETE_DB));
        }

        try {
            int deleted = database.delete(StickerDatabaseHelper.TABLE_STICKER_PACK,
                    StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?",
                    new String[]{stickerPackIdentifier});

            if (deleted == 0) {
                return CallbackResult.warning(
                        "Nenhum pacote foi deletado. Verifique se o ID está correto.");
            }

            return CallbackResult.success(deleted);
        } catch (IllegalArgumentException | SQLiteException runtimeException) {
            Log.e(TAG_LOG, "Erro ao deletar pacote do banco: " + runtimeException.getMessage(),
                    runtimeException);

            return CallbackResult.failure(
                    new DeleteStickerException("Erro no banco ao deletar pacote.", runtimeException,
                            DeleteErrorCode.ERROR_PACK_DELETE_DB));
        } catch (Exception exception) {
            Log.e(TAG_LOG, "Erro inesperado ao deletar pacote: " + exception.getMessage(),
                    exception);

            return CallbackResult.failure(
                    new DeleteStickerException("Erro inesperado ao deletar pacote.", exception,
                            DeleteErrorCode.ERROR_PACK_DELETE_DB));
        }
    }
}
