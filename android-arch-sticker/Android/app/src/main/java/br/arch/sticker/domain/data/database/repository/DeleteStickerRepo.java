package br.arch.sticker.domain.data.database.repository;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import br.arch.sticker.core.error.code.DeleteErrorCode;
import br.arch.sticker.core.error.throwable.sticker.DeleteStickerException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;

public class DeleteStickerRepo {
    private final static String TAG_LOG = DeleteStickerRepo.class.getSimpleName();

    private final SQLiteDatabase database;

    public DeleteStickerRepo(SQLiteDatabase database) {
        this.database = database;
    }

    public int deleteSticker(String stickerPackIdentifier, String fileName) throws DeleteStickerException {
        if (stickerPackIdentifier == null || fileName == null) {
            throw new DeleteStickerException("Erro ao encontrar o id do pacote para deletar figurinha.", DeleteErrorCode.ERROR_PACK_DELETE_DB);
        }

        try {
            String query = StickerDatabaseHelper.FK_STICKER_PACK + " = ? AND " +
                    StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY + " = ?";

            return database.delete(StickerDatabaseHelper.TABLE_STICKER, query, new String[]{stickerPackIdentifier, fileName});
        } catch (IllegalArgumentException | SQLiteException exception) {
            Log.e(TAG_LOG, "Erro ao deletar sticker: " + exception.getMessage(), exception);

            throw new DeleteStickerException("Erro no banco ao tentar deletar sticker.", exception, DeleteErrorCode.ERROR_PACK_DELETE_DB);
        }
    }

    public CallbackResult<Integer> deleteListSticker(String stickerPackIdentifier, List<String> fileNames) {
        if (stickerPackIdentifier == null || fileNames == null || fileNames.isEmpty()) {
            throw new DeleteStickerException("Parâmetros inválidos para deletar stickers.", DeleteErrorCode.ERROR_PACK_DELETE_DB);
        }

        try {
            String query = getQuery(fileNames);

            String[] args = new String[fileNames.size() + 1];
            args[0] = stickerPackIdentifier;
            for (int counter = 0; counter < fileNames.size(); counter++) {
                args[counter + 1] = fileNames.get(counter);
            }

            int deleted = database.delete(StickerDatabaseHelper.TABLE_STICKER, query, args);
            if (deleted == 0) {
                return CallbackResult.warning("Nenhum pacote foi deletado. Verifique se o ID está correto.");
            }

            return CallbackResult.success(deleted);
        } catch (Exception exception) {
            Log.e(TAG_LOG,
                    "Erro inesperado ao deletar os stickers: " + exception.getMessage(), exception);

            return CallbackResult.failure(new DeleteStickerException("Erro inesperado ao deletar os stickers.", exception, DeleteErrorCode.ERROR_STICKER_DELETE_DB));
        }
    }

    @NonNull
    private static String getQuery(List<String> fileNames) {
        StringBuilder placeholders = new StringBuilder();
        for (int counter = 0; counter < fileNames.size(); counter++) {
            placeholders.append("?");
            if (counter < fileNames.size() - 1) {
                placeholders.append(", ");
            }
        }

        return StickerDatabaseHelper.FK_STICKER_PACK + " = ? AND " +
                StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY + " IN (" +
                placeholders.toString() + ")";
    }
}
