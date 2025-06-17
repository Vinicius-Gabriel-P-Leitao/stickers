/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.save;

import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;
import static br.arch.sticker.view.core.util.convert.ConvertThumbnail.THUMBNAIL_FILE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import br.arch.sticker.core.error.code.BaseErrorCode;
import br.arch.sticker.core.error.throwable.content.InvalidWebsiteUrlException;
import br.arch.sticker.core.error.throwable.sticker.PackValidatorException;
import br.arch.sticker.core.error.throwable.sticker.StickerFileException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.error.throwable.sticker.StickerValidatorException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.core.validation.StickerPackValidator;
import br.arch.sticker.core.validation.StickerValidator;
import br.arch.sticker.domain.data.database.StickerDatabase;
import br.arch.sticker.domain.data.database.repository.InsertStickerPackRepo;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.util.StickerPackDirectory;

public class SaveStickerPackService {
    public static CompletableFuture<CallbackResult<StickerPack>> saveStickerPackAsync(
            @NonNull Context context, boolean isAnimated, @NonNull List<File> files, @NonNull String name)
        {

            return CompletableFuture.supplyAsync(() -> {
                try
                {
                    String uuid = UUID.randomUUID().toString();
                    String finalName = name.trim() + " - [" + uuid.substring(0, 8) + "]";

                    StickerPack stickerPack = new StickerPack(uuid, finalName, "vinicius", THUMBNAIL_FILE, "", "", "", "", "1", false, isAnimated);

                    List<Sticker> stickerList = new ArrayList<>();
                    for (File file : files)
                    {
                        boolean exists = stickerList.stream().anyMatch(sticker -> sticker.imageFileName.equals(file.getName()));
                        if (!exists)
                        {
                            String accessibility = isAnimated
                                                   ? "Pacote animado com nome " + finalName
                                                   : "Pacote estático com nome " + finalName;

                            stickerList.add(new Sticker(file.getName().trim(), "\uD83D\uDDFF", "", accessibility, uuid));
                        }
                    }

                    stickerPack.setStickers(stickerList);
                    return persistPackToStorage(context, stickerPack);
                } catch (Exception exception)
                {
                    return CallbackResult.failure(
                            new StickerPackSaveException("Erro ao salvar pacote de figurinhas.", exception, BaseErrorCode.ERROR_UNKNOWN));
                }
            });
        }

    public static CallbackResult<StickerPack> persistPackToStorage(@NonNull Context context, @NonNull StickerPack stickerPack)
        {
            File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);

            CallbackResult<Boolean> mainDirectoryCallback = StickerPackDirectory.createMainDirectory(mainDirectory);
            if (!mainDirectoryCallback.isSuccess())
            {
                if (mainDirectoryCallback.isDebug()) return CallbackResult.debug(mainDirectoryCallback.getDebugMessage());
                if (mainDirectoryCallback.isWarning()) return CallbackResult.warning(mainDirectoryCallback.getWarningMessage());
                return CallbackResult.failure(mainDirectoryCallback.getError());
            }

            CallbackResult<File> stickerPackDirectoryCallback = StickerPackDirectory.createStickerPackDirectory(
                    mainDirectory,
                    stickerPack.identifier);
            if (!stickerPackDirectoryCallback.isSuccess() || stickerPackDirectoryCallback.getData() == null)
            {
                if (stickerPackDirectoryCallback.isDebug()) return CallbackResult.debug(stickerPackDirectoryCallback.getDebugMessage());
                if (stickerPackDirectoryCallback.isWarning()) return CallbackResult.warning(stickerPackDirectoryCallback.getWarningMessage());
                return CallbackResult.failure(stickerPackDirectoryCallback.getError());
            }

            CallbackResult<Boolean> copyStickerCallback = SaveStickerAssetService.copyStickerFromCache(
                    context, stickerPack,
                    stickerPackDirectoryCallback.getData());
            if (!copyStickerCallback.isSuccess())
            {
                if (copyStickerCallback.isDebug()) return CallbackResult.debug(copyStickerCallback.getDebugMessage());
                if (copyStickerCallback.isWarning()) return CallbackResult.warning(copyStickerCallback.getWarningMessage());
                return CallbackResult.failure(copyStickerCallback.getError());
            }

            try
            {
                StickerPackValidator.verifyStickerPackValidity(context, stickerPack);
            } catch (PackValidatorException | StickerValidatorException | InvalidWebsiteUrlException appCoreStateException)
            {
                return CallbackResult.failure(appCoreStateException);
            }

            for (Sticker sticker : stickerPack.getStickers())
            {
                try
                {
                    StickerValidator.verifyStickerValidity(context, stickerPack.identifier, sticker, stickerPack.animatedStickerPack);
                } catch (StickerFileException stickerFileException)
                {
                    if (Objects.equals(sticker.imageFileName, stickerFileException.getFileName()))
                    {
                        sticker.setStickerIsInvalid(stickerFileException.getErrorCodeName());
                        return CallbackResult.warning(
                                String.format("Alguns stickers foram marcados como inválidos: %s", stickerFileException.getFileName()));
                    }
                } catch (StickerValidatorException stickerValidatorException)
                {
                    return CallbackResult.failure(stickerValidatorException);
                }
            }

            return insertStickerPack(context, stickerPack);
        }

    private static CallbackResult<StickerPack> insertStickerPack(Context context, StickerPack stickerPack)
        {
            StickerDatabase instance = StickerDatabase.getInstance(context);
            SQLiteDatabase writableDatabase = instance.getWritableDatabase();

            InsertStickerPackRepo insertStickerPackRepo = new InsertStickerPackRepo(writableDatabase);
            return insertStickerPackRepo.insertStickerPack(stickerPack);
        }
}
