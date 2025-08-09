package br.arch.sticker.domain.service.update;

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.UpdateStickerException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.UpdateStickerPackRepo;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class UpdateStickerPackService {
    private static final String TAG_LOG = UpdateStickerPackService.class.getSimpleName();

    private final UpdateStickerPackRepo updateStickerPackRepo;
    private final ApplicationTranslate applicationTranslate;

    public UpdateStickerPackService(Context paramContext) {
        Context context = paramContext.getApplicationContext();
        SQLiteDatabase database = StickerDatabaseHelper.getInstance(context).getWritableDatabase();

        Resources resources = context.getResources();
        this.applicationTranslate = new ApplicationTranslate(resources);
        this.updateStickerPackRepo = new UpdateStickerPackRepo(database, resources);
    }

    public boolean updateStickerFileName(String stickerPackIdentifier, String newName) throws UpdateStickerException {
        if (stickerPackIdentifier.isEmpty() || newName.isEmpty()) {
            Log.w(TAG_LOG, applicationTranslate.translate(R.string.warn_invalid_parameters_rename_pack).get());
            return false;
        }

        Log.d(TAG_LOG,
                applicationTranslate.translate(R.string.debug_update_sticker_pack_name, stickerPackIdentifier, newName)
                        .get());

        if (updateStickerPackRepo.updateStickerPackName(stickerPackIdentifier, newName)) {
            return true;
        }

        throw new UpdateStickerException(
                applicationTranslate.translate(R.string.error_update_sticker_pack).log(TAG_LOG, Level.ERROR).get(),
                ErrorCode.ERROR_EMPTY_STICKERPACK);
    }

    public boolean cleanStickerPackUrl(String stickerPackIdentifier) throws UpdateStickerException {
        if (stickerPackIdentifier.isEmpty()) {
            Log.w(TAG_LOG, applicationTranslate.translate(R.string.warn_invalid_parameters_clean_urls).get());
            return false;
        }

        Log.d(TAG_LOG,
                applicationTranslate.translate(R.string.debug_update_sticker_pack_name, stickerPackIdentifier).get());

        if (updateStickerPackRepo.cleanStickerPackUrl(stickerPackIdentifier)) {
            return true;
        }

        throw new UpdateStickerException(
                applicationTranslate.translate(R.string.error_invalid_url).log(TAG_LOG, Level.WARN).get(),
                ErrorCode.ERROR_EMPTY_STICKERPACK);
    }
}
