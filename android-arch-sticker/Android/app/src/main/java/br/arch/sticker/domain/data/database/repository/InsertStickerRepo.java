package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER;
import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.mapper.StickerMapper;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class InsertStickerRepo {
    private final static String TAG_LOG = InsertStickerRepo.class.getSimpleName();

    private final SQLiteDatabase database;
    private final ApplicationTranslate applicationTranslate;

    public InsertStickerRepo(SQLiteDatabase database, Resources resources) {
        this.database = database;
        this.applicationTranslate = new ApplicationTranslate(resources);
    }

    public CallbackResult<Sticker> insertSticker(Sticker sticker, String stickerPackIdentifier) {
        if (sticker == null || sticker.imageFileName == null || stickerPackIdentifier == null ||
                stickerPackIdentifier.isEmpty()) {
            return CallbackResult.failure(new StickerPackSaveException(
                    applicationTranslate.translate(R.string.error_insert_sticker_invalid_data)
                            .log(TAG_LOG, Level.ERROR).get(), ErrorCode.ERROR_PACK_SAVE_DB
            ));
        }

        try {
            ContentValues stickerValues = StickerMapper.writeStickerToContentValues(sticker);
            database.insert(TABLE_STICKER, null, stickerValues);

            return CallbackResult.success(sticker);
        } catch (SQLiteException sqLiteException) {
            return CallbackResult.failure(new StickerPackSaveException(
                    applicationTranslate.translate(R.string.error_insert_sticker_db)
                            .log(TAG_LOG, Level.ERROR, sqLiteException).get(), sqLiteException,
                    ErrorCode.ERROR_PACK_SAVE_DB
            ));
        } catch (Exception exception) {
            return CallbackResult.failure(new StickerPackSaveException(
                    applicationTranslate.translate(R.string.error_unexpected_insert_sticker_db)
                            .log(TAG_LOG, Level.ERROR).get(), exception,
                    ErrorCode.ERROR_PACK_SAVE_DB
            ));
        }
    }
}
