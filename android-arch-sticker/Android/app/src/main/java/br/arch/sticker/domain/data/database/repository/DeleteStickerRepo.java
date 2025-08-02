package br.arch.sticker.domain.data.database.repository;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import java.util.List;

import br.arch.sticker.domain.data.database.StickerDatabaseHelper;

public class DeleteStickerRepo {
    private final static String TAG_LOG = DeleteStickerRepo.class.getSimpleName();

    private final SQLiteDatabase database;

    public DeleteStickerRepo(SQLiteDatabase database) {
        this.database = database;
    }

    public Integer deleteSticker(String stickerPackIdentifier, String fileName)
            throws IllegalArgumentException, SQLiteException {
        String query = StickerDatabaseHelper.FK_STICKER_PACK + " = ? AND " +
                StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY + " = ?";

        return database.delete(StickerDatabaseHelper.TABLE_STICKER, query,
                new String[]{stickerPackIdentifier, fileName}
        );
    }

    public Integer deleteListSticker(String stickerPackIdentifier, List<String> fileNames) {
        String query = getQuery(fileNames);

        String[] args = new String[fileNames.size() + 1];
        args[0] = stickerPackIdentifier;
        for (int counter = 0; counter < fileNames.size(); counter++) {
            args[counter + 1] = fileNames.get(counter);
        }

        return database.delete(StickerDatabaseHelper.TABLE_STICKER, query, args);
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
                StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY + " IN (" + placeholders + ")";
    }
}
