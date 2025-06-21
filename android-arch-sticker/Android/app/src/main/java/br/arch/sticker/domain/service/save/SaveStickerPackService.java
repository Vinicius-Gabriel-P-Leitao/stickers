/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.save;

import static br.arch.sticker.core.validation.StickerPackValidator.STICKER_SIZE_MIN;
import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;
import static br.arch.sticker.view.core.util.convert.ConvertThumbnail.THUMBNAIL_FILE;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import br.arch.sticker.core.error.code.BaseErrorCode;
import br.arch.sticker.core.error.code.SaveErrorCode;
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
    private final static String TAG_LOG = SaveStickerPackService.class.getSimpleName();

    public final static String PLACEHOLDER_ANIMATED = "placeholder_animated.webp"; // FIXME: Trocar arquivo por um valido
    public final static String PLACEHOLDER_STATIC = "placeholder_static.webp";

    public static CompletableFuture<CallbackResult<StickerPack>> saveStickerPackAsync(
            @NonNull Context context, boolean isAnimated, @NonNull List<File> files, @NonNull String name)
        {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String uuid = UUID.randomUUID().toString();
                    String finalName = name.trim() + " - [" + uuid.substring(0, 8) + "]";

                    StickerPack stickerPack = new StickerPack(uuid, finalName, "vinicius", THUMBNAIL_FILE, "", "", "", "", "1", false, isAnimated);

                    List<Sticker> stickerList = new ArrayList<>();
                    for (File file : files) {
                        boolean exists = stickerList.stream().anyMatch(sticker -> sticker.imageFileName.equals(file.getName()));
                        if (!exists) {
                            String accessibility = isAnimated
                                                   ? "Pacote animado com nome " + finalName
                                                   : "Pacote estático com nome " + finalName;

                            stickerList.add(new Sticker(file.getName().trim(), "\uD83D\uDDFF", "", accessibility, uuid));
                        }
                    }

                    stickerPack.setStickers(stickerList);
                    return persistPackToStorage(context, stickerPack);
                } catch (Exception exception) {
                    Log.e(TAG_LOG, "ERRO: " + exception);
                    return CallbackResult.failure(
                            new StickerPackSaveException("Erro ao salvar pacote de figurinhas.", exception, BaseErrorCode.ERROR_UNKNOWN));
                }
            });
        }

    public static CallbackResult<StickerPack> persistPackToStorage(
            @NonNull Context context, @NonNull StickerPack stickerPack) throws StickerPackSaveException
        {
            List<Sticker> stickerList = new ArrayList<>(stickerPack.getStickers());

            while (stickerList.size() < STICKER_SIZE_MIN) {
                Sticker placeholder = makeStickerPlaceholder(context, stickerPack);
                stickerList.add(placeholder);
            }

            stickerPack.setStickers(stickerList);

            File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);

            CallbackResult<Boolean> mainDirectoryCallback = StickerPackDirectory.createMainDirectory(mainDirectory);
            if (!mainDirectoryCallback.isSuccess() && !mainDirectoryCallback.isDebug()) {
                if (mainDirectoryCallback.isWarning()) return CallbackResult.warning(mainDirectoryCallback.getWarningMessage());
                return CallbackResult.failure(mainDirectoryCallback.getError());
            }

            CallbackResult<File> stickerPackDirectoryCallback = StickerPackDirectory.createStickerPackDirectory(mainDirectory,
                    stickerPack.identifier);
            if (!stickerPackDirectoryCallback.isSuccess() && !stickerPackDirectoryCallback.isWarning()) {
                if (stickerPackDirectoryCallback.isDebug()) {
                    Log.d(TAG_LOG, stickerPackDirectoryCallback.getDebugMessage());
                } else {
                    return CallbackResult.failure(stickerPackDirectoryCallback.getError());
                }
            }

            if (stickerPackDirectoryCallback.getData() == null) {
                return CallbackResult.failure(
                        new StickerPackSaveException("O diretório para salvar os pacotes está como nulo.", SaveErrorCode.ERROR_PACK_SAVE_SERVICE));
            }

            // NOTE: Já chama aqui para não ter problemas na viewmodel
            CallbackResult<Boolean> copyStickerCallback = SaveStickerAssetService.saveStickerFromCache(context, stickerPack,
                    stickerPackDirectoryCallback.getData());
            if (!copyStickerCallback.isSuccess()) {
                if (copyStickerCallback.isDebug()) return CallbackResult.debug(copyStickerCallback.getDebugMessage());
                if (copyStickerCallback.isWarning()) return CallbackResult.warning(copyStickerCallback.getWarningMessage());
                return CallbackResult.failure(copyStickerCallback.getError());
            }

            try {
                StickerPackValidator.verifyStickerPackValidity(context, stickerPack);
            } catch (PackValidatorException | StickerValidatorException | InvalidWebsiteUrlException exception) {
                Log.e(TAG_LOG, exception.getMessage() != null
                               ? exception.getMessage()
                               : "Erro ao validar pacote!");
            }

            for (Sticker sticker : stickerList) {
                try {
                    StickerValidator.verifyStickerValidity(context, stickerPack.identifier, sticker, stickerPack.animatedStickerPack);
                } catch (StickerValidatorException | StickerFileException exception) {
                    if (exception instanceof StickerFileException fileException) {
                        if (Objects.equals(sticker.imageFileName, fileException.getFileName())) {
                            sticker.setStickerIsInvalid(fileException.getErrorCodeName());
                        }
                    }

                    Log.e(TAG_LOG, exception.getMessage() != null
                                   ? exception.getMessage()
                                   : "Erro ao validar sticker!");
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

    private static Sticker makeStickerPlaceholder(Context context, StickerPack stickerPack)
        {
            String fileName = stickerPack.animatedStickerPack
                              ? PLACEHOLDER_ANIMATED
                              : PLACEHOLDER_STATIC;

            String accessibility = stickerPack.animatedStickerPack
                                   ? "Pacote animado com nome " + stickerPack.name
                                   : "Pacote estático com nome " + stickerPack.name;

            File cacheFile = new File(context.getCacheDir(), fileName);
            try (AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd(fileName);
                 InputStream inputStream = assetFileDescriptor.createInputStream();
                 OutputStream outputStream = new FileOutputStream(cacheFile)) {

                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.flush();

                return new Sticker(fileName.trim(), "\uD83D\uDDFF", "", accessibility, stickerPack.identifier);
            } catch (IOException exception) {
                throw new StickerPackSaveException("Erro ao criar placeholder para o pacote de figurinhas!", exception,
                        SaveErrorCode.ERROR_PACK_SAVE_SERVICE);
            }
        }
}
