package br.arch.sticker.domain.service.save;

import static br.arch.sticker.core.validation.StickerPackValidator.STICKER_SIZE_MIN;
import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.arch.sticker.R;
import br.arch.sticker.core.error.throwable.content.InvalidWebsiteUrlException;
import br.arch.sticker.core.error.throwable.sticker.StickerFileException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackValidatorException;
import br.arch.sticker.core.error.throwable.sticker.StickerValidatorException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.core.validation.StickerPackValidator;
import br.arch.sticker.core.validation.StickerValidator;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.InsertStickerRepo;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.StickerPackPlaceholder;

public class SaveStickerService {
    private final static String TAG_LOG = SaveStickerService.class.getSimpleName();

    private final SaveStickerAssetService saveStickerAssetService;
    private final StickerPackPlaceholder stickerPackPlaceholder;
    private final StickerPackValidator stickerPackValidator;
    private final ApplicationTranslate applicationTranslate;
    private final InsertStickerRepo insertStickerRepo;
    private final StickerValidator stickerValidator;
    private final Context context;

    public SaveStickerService(Context context) {
        this.context = context.getApplicationContext();
        this.stickerValidator = new StickerValidator(this.context);
        this.stickerPackValidator = new StickerPackValidator(this.context);
        this.stickerPackPlaceholder = new StickerPackPlaceholder(this.context);
        this.saveStickerAssetService = new SaveStickerAssetService(this.context);
        this.applicationTranslate = new ApplicationTranslate(this.context.getResources());

        SQLiteDatabase database = StickerDatabaseHelper.getInstance(this.context).getWritableDatabase();
        this.insertStickerRepo = new InsertStickerRepo(database, this.context.getResources());
    }

    public CallbackResult<StickerPack> addNewStickers(@NonNull StickerPack stickerPack, @NonNull List<File> files) {
        List<Sticker> currentStickers = new ArrayList<>(stickerPack.getStickers());
        List<Sticker> newStickers = new ArrayList<>();

        for (File file : files) {
            boolean exists = currentStickers.stream().anyMatch(sticker -> sticker.imageFileName.equals(file.getName()));

            if (!exists) {
                String accessibility = stickerPack.animatedStickerPack ? "Pacote animado com nome " + stickerPack.name : "Pacote estático com nome " + stickerPack.name;
                Sticker newSticker = new Sticker(file.getName().trim(), "\uD83D\uDDFF", "", accessibility,
                        stickerPack.identifier);

                currentStickers.add(newSticker);
                newStickers.add(newSticker);
            }
        }

        stickerPack.setStickers(currentStickers);
        File createdStickerPackDirectory = new File(new File(context.getFilesDir(), STICKERS_ASSET),
                stickerPack.identifier);

        while (currentStickers.size() < STICKER_SIZE_MIN) {
            Sticker placeholder = stickerPackPlaceholder.makeStickerPlaceholder(stickerPack,
                    createdStickerPackDirectory);
            currentStickers.add(placeholder);
        }

        CallbackResult<Boolean> copyStickerPack = saveStickerAssetService.saveStickerFromCache(newStickers,
                createdStickerPackDirectory);
        if (!copyStickerPack.isSuccess()) {
            if (copyStickerPack.isDebug()) return CallbackResult.debug(copyStickerPack.getDebugMessage());
            if (copyStickerPack.isWarning()) return CallbackResult.warning(copyStickerPack.getWarningMessage());
            return CallbackResult.failure(copyStickerPack.getError());
        }

        for (Sticker sticker : currentStickers) {
            try {
                stickerValidator.verifyStickerValidity(stickerPack.identifier, sticker,
                        stickerPack.animatedStickerPack);
            } catch (StickerValidatorException | StickerFileException exception) {
                if (exception instanceof StickerFileException fileException) {
                    if (Objects.equals(sticker.imageFileName, fileException.getFileName())) {
                        sticker.setStickerIsInvalid(fileException.getErrorCodeName());
                    }
                }

                Log.e(TAG_LOG, exception.getMessage() != null ? exception.getMessage() : applicationTranslate.translate(
                        R.string.error_validate_sticker).get());
            }
        }

        for (Sticker sticker : newStickers) {
            insertStickerRepo.insertSticker(sticker, stickerPack.identifier);
        }

        return CallbackResult.success(stickerPack);
    }
}
