package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.FK_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_IS_VALID;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import br.arch.sticker.R;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class UpdateStickerRepo {
    private final static String TAG_LOG = UpdateStickerRepo.class.getSimpleName();

    private final SQLiteDatabase database;
    private final ApplicationTranslate applicationTranslate;

    public UpdateStickerRepo(SQLiteDatabase database, Resources resources) {
        this.database = database;
        this.applicationTranslate = new ApplicationTranslate(resources);
    }

    public boolean updateStickerFileName(String stickerPackIdentifier, String newFileName, String oldFileName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(STICKER_FILE_NAME_IN_QUERY, newFileName);
        contentValues.put(STICKER_IS_VALID, "");

        String whereClause = FK_STICKER_PACK + " = ? AND " + STICKER_FILE_NAME_IN_QUERY + " = ?";
        String[] whereArgs = {stickerPackIdentifier, oldFileName};

        try {
            int rowsUpdated = database.update(TABLE_STICKER, contentValues, whereClause, whereArgs);

            if (rowsUpdated == 0) {
                Log.w(TAG_LOG, applicationTranslate.translate(R.string.warn_no_sticker_updated).get());
                return false;
            }

            return rowsUpdated > 0;
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, applicationTranslate.translate(R.string.error_update_sticker_filename, exception.getMessage()).get(), exception);
            return false;
        }
    }

    public boolean updateInvalidSticker(String stickerPackIdentifier, String fileName, String errorMessage) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(STICKER_IS_VALID, errorMessage);

        String whereClause = FK_STICKER_PACK + " = ? AND " + STICKER_FILE_NAME_IN_QUERY + " = ?";
        String[] whereArgs = {stickerPackIdentifier, fileName};

        try {
            int rowsUpdated = database.update(TABLE_STICKER, contentValues, whereClause, whereArgs);

            if (rowsUpdated == 0) {
                Log.w(TAG_LOG, applicationTranslate.translate(R.string.warn_no_sticker_marked_invalid).get());
                return false;
            }

            return rowsUpdated > 0;
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, applicationTranslate.translate(R.string.error_mark_sticker_invalid).get(), exception);
            return false;
        }
    }
}
