package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.FK_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_IS_VALID;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import androidx.annotation.NonNull;

import br.arch.sticker.core.error.code.SaveErrorCode;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerPackException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;

public class InsertStickerRepo {
    private final static String TAG_LOG = InsertStickerRepo.class.getSimpleName();

    private final SQLiteDatabase database;

    public InsertStickerRepo(SQLiteDatabase database) {
        this.database = database;
    }

    @NonNull
    public CallbackResult<Sticker> insertSticker(Sticker sticker, String stickerPackIdentifier) throws FetchStickerPackException {
        if (sticker == null || sticker.imageFileName == null || stickerPackIdentifier == null ||
                stickerPackIdentifier.isEmpty()) {
            return CallbackResult.failure(new StickerPackSaveException("Dados da figurinha inválidos ou identificador de pacote ausente.", SaveErrorCode.ERROR_PACK_SAVE_DB));
        }

        try {
            ContentValues stickerValues = writeStickerToContentValues(sticker);
            database.insert(TABLE_STICKER, null, stickerValues);

            return CallbackResult.success(sticker);
        } catch (SQLiteException sqLiteException) {
            Log.e(TAG_LOG, "Erro de banco ao inserir figurinha: " +
                    sqLiteException.getMessage(), sqLiteException);

            return CallbackResult.failure(new StickerPackSaveException("Erro no banco ao inserir figurinha.", sqLiteException, SaveErrorCode.ERROR_PACK_SAVE_DB));
        } catch (Exception exception) {
            Log.e(TAG_LOG,
                    "Erro inesperado ao inserir figurinha: " + exception.getMessage(), exception);

            return CallbackResult.failure(new StickerPackSaveException("Erro inesperado ao salvar figurinha no banco de dados.", exception, SaveErrorCode.ERROR_PACK_SAVE_DB));
        }
    }

    @NonNull
    public static ContentValues writeStickerToContentValues(Sticker sticker) {
        ContentValues stickerValues = new ContentValues();
        stickerValues.put(STICKER_FILE_NAME_IN_QUERY, sticker.imageFileName);
        stickerValues.put(STICKER_FILE_EMOJI_IN_QUERY, String.valueOf(sticker.emojis));
        stickerValues.put(STICKER_IS_VALID, sticker.stickerIsValid);
        stickerValues.put(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY, sticker.accessibilityText);
        stickerValues.put(FK_STICKER_PACK, sticker.uuidPack);

        return stickerValues;
    }
}
