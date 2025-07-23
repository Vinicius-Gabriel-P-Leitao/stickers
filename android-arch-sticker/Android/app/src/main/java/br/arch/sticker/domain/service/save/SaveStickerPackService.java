/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.save;

import static br.arch.sticker.core.error.ErrorCode.ERROR_UNKNOWN;
import static br.arch.sticker.core.validation.StickerPackValidator.STICKER_SIZE_MIN;
import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;
import static br.arch.sticker.view.core.util.convert.ConvertThumbnail.THUMBNAIL_FILE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.content.InvalidWebsiteUrlException;
import br.arch.sticker.core.error.throwable.sticker.StickerFileException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackValidatorException;
import br.arch.sticker.core.error.throwable.sticker.StickerValidatorException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.core.validation.StickerPackValidator;
import br.arch.sticker.core.validation.StickerValidator;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.InsertStickerPackRepo;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;
import br.arch.sticker.domain.util.StickerPackDirectory;
import br.arch.sticker.domain.util.StickerPackPlaceholder;

public class SaveStickerPackService {
    private final static String TAG_LOG = SaveStickerPackService.class.getSimpleName();

    private final SaveStickerAssetService saveStickerAssetService;
    private final StickerPackPlaceholder stickerPackPlaceholder;
    private final InsertStickerPackRepo insertStickerPackRepo;
    private final StickerPackValidator stickerPackValidator;
    private final ApplicationTranslate applicationTranslate;
    private final StickerValidator stickerValidator;
    private final Context context;

    public SaveStickerPackService(Context context) {
        this.context = context.getApplicationContext();
        this.stickerValidator = new StickerValidator(this.context);
        this.stickerPackValidator = new StickerPackValidator(this.context);
        this.stickerPackPlaceholder = new StickerPackPlaceholder(this.context);
        this.saveStickerAssetService = new SaveStickerAssetService(this.context);
        this.applicationTranslate = new ApplicationTranslate(this.context.getResources());

        SQLiteDatabase database = StickerDatabaseHelper.getInstance(this.context).getWritableDatabase();
        this.insertStickerPackRepo = new InsertStickerPackRepo(database, this.context.getResources());
    }

    public CompletableFuture<CallbackResult<StickerPack>> saveStickerPackAsync(boolean isAnimated, @NonNull List<File> files, @NonNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String uuid = UUID.randomUUID().toString();
                String finalName = name.trim() + " - [" + uuid.substring(0, 8) + "]";

                StickerPack stickerPack = new StickerPack(uuid, finalName, "vinicius", THUMBNAIL_FILE, "", "", "", "", "1", false, isAnimated);

                List<Sticker> stickerList = new ArrayList<>();
                for (File file : files) {
                    boolean exists = stickerList.stream().anyMatch(sticker -> sticker.imageFileName.equals(file.getName()));
                    if (!exists) {
                        String accessibility = isAnimated ? "Pacote animado com nome " + finalName : "Pacote estático com nome " + finalName;

                        stickerList.add(new Sticker(file.getName().trim(), "\uD83D\uDDFF", "", accessibility, uuid));
                    }
                }

                stickerPack.setStickers(stickerList);
                return persistPackToStorage(context, stickerPack);
            } catch (Exception exception) {
                return CallbackResult.failure(new StickerPackSaveException(
                        applicationTranslate.translate(R.string.error_save_sticker_pack_general).log(TAG_LOG, Level.ERROR, exception).get(),
                        exception, ERROR_UNKNOWN
                ));
            }
        });
    }

    public CallbackResult<StickerPack> persistPackToStorage(@NonNull Context context, @NonNull StickerPack stickerPack)
            throws StickerPackSaveException {
        File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
        CallbackResult<Boolean> createdMainDirectory, copyStickerPack;

        createdMainDirectory = StickerPackDirectory.createMainDirectory(context, mainDirectory);
        if (!createdMainDirectory.isSuccess() && !createdMainDirectory.isDebug()) {
            if (createdMainDirectory.isWarning()) return CallbackResult.warning(createdMainDirectory.getWarningMessage());
            return CallbackResult.failure(createdMainDirectory.getError());
        }

        CallbackResult<File> createdStickerPackDirectory = StickerPackDirectory.createStickerPackDirectory(context, mainDirectory,
                stickerPack.identifier
        );
        if (!createdStickerPackDirectory.isSuccess() && !createdStickerPackDirectory.isWarning()) {
            if (createdStickerPackDirectory.isDebug()) {
                Log.d(TAG_LOG, createdStickerPackDirectory.getDebugMessage());
            } else {
                return CallbackResult.failure(createdStickerPackDirectory.getError());
            }
        }

        if (createdStickerPackDirectory.getData() == null) {
            return CallbackResult.failure(new StickerPackSaveException(
                    applicationTranslate.translate(R.string.error_save_sticker_pack_directory_null).log(TAG_LOG, Level.ERROR).get(),
                    ErrorCode.ERROR_PACK_SAVE_SERVICE
            ));
        }

        List<Sticker> stickerList = new ArrayList<>(stickerPack.getStickers());
        while (stickerList.size() < STICKER_SIZE_MIN) {
            Sticker placeholder = stickerPackPlaceholder.makeStickerPlaceholder(stickerPack, createdStickerPackDirectory.getData());
            stickerList.add(placeholder);
        }
        stickerPack.setStickers(stickerList);

        copyStickerPack = saveStickerAssetService.saveStickerFromCache(stickerPack, createdStickerPackDirectory.getData());
        if (!copyStickerPack.isSuccess()) {
            if (copyStickerPack.isDebug()) return CallbackResult.debug(copyStickerPack.getDebugMessage());
            if (copyStickerPack.isWarning()) return CallbackResult.warning(copyStickerPack.getWarningMessage());
            return CallbackResult.failure(copyStickerPack.getError());
        }

        try {
            stickerPackValidator.verifyStickerPackValidity(stickerPack);
        } catch (StickerPackValidatorException | StickerValidatorException | InvalidWebsiteUrlException exception) {
            Log.e(TAG_LOG,
                    exception.getMessage() != null ? exception.getMessage() : applicationTranslate.translate(R.string.error_validate_sticker_pack)
                            .get()
            );
        }

        for (Sticker sticker : stickerList) {
            try {
                stickerValidator.verifyStickerValidity(stickerPack.identifier, sticker, stickerPack.animatedStickerPack);
            } catch (StickerValidatorException | StickerFileException exception) {
                if (exception instanceof StickerFileException fileException) {
                    if (Objects.equals(sticker.imageFileName, fileException.getFileName())) {
                        sticker.setStickerIsInvalid(fileException.getErrorCodeName());
                    }
                }

                Log.e(TAG_LOG,
                        exception.getMessage() != null ? exception.getMessage() : applicationTranslate.translate(R.string.error_validate_sticker)
                                .get()
                );
            }
        }

        if (stickerPack.identifier == null) {
            return CallbackResult.failure(new StickerPackSaveException(
                    applicationTranslate.translate(R.string.error_save_sticker_pack_invalid_id).log(TAG_LOG, Level.ERROR).get(),
                    ErrorCode.ERROR_PACK_SAVE_DB
            ));
        }

        return insertStickerPackRepo.insertStickerPack(stickerPack);
    }
}
