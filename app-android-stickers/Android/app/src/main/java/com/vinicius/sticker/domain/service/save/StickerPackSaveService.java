/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

package com.vinicius.sticker.domain.service.save;

import static com.vinicius.sticker.domain.data.content.provider.StickerContentProvider.STICKERS_ASSET;
import static com.vinicius.sticker.domain.data.database.repository.DeleteStickerPacks.deleteStickerPackFromDatabase;
import static com.vinicius.sticker.domain.data.database.repository.DeleteStickerPacks.deleteStickersOfPack;
import static com.vinicius.sticker.domain.service.delete.StickerDeleteService.deleteAllFilesInPack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import com.vinicius.sticker.core.exception.StickerPackSaveException;
import com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper;
import com.vinicius.sticker.domain.data.database.repository.InsertStickerPacks;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.pattern.CallbackResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StickerPackSaveService {

    @FunctionalInterface
    public interface SaveStickerPackCallback<T> {
        void onStickerPackSaveResult(CallbackResult<T> callbackResult);
    }

    public static void generateStructureForSavePack(Context context, StickerPack stickerPack, String previousIdentifier, SaveStickerPackCallback<String> callback) {

        File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
        if (!createMainDirectory(mainDirectory, callback)) {
            return;
        }

        if (shouldDeletePreviousPack(stickerPack.identifier, previousIdentifier)) {
            if (!deletePreviousPack(context, stickerPack.identifier, callback)) {
                return;
            }
        } else {
            callback.onStickerPackSaveResult(CallbackResult.warning("Identificador diferente, não houve deleção."));
        }

        File stickerPackDirectory = createStickerPackDirectory(mainDirectory, stickerPack.identifier, callback);
        if (stickerPackDirectory == null) {
            return;
        }

        if (!copyStickers(context, stickerPack, stickerPackDirectory, callback)) {
            return;
        }

        insertStickerPack(context, stickerPack, callback);
    }

    private static boolean createMainDirectory(File mainDirectory, SaveStickerPackCallback<String> callback) {
        if (!mainDirectory.exists()) {
            boolean created = mainDirectory.mkdirs();
            if (!created) {
                callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException(
                        "Falha ao criar o mainDirectory: " + mainDirectory.getPath())));
                return false;
            }
            callback.onStickerPackSaveResult(CallbackResult.success("mainDirectory criado com sucesso: " + mainDirectory.getPath()));
        } else {
            callback.onStickerPackSaveResult(CallbackResult.success("mainDirectory já existe: " + mainDirectory.getPath()));
        }
        return true;
    }

    private static boolean shouldDeletePreviousPack(String currentIdentifier, String previousIdentifier) {
        return currentIdentifier.equals(previousIdentifier) || previousIdentifier != null;
    }

    private static boolean deletePreviousPack(Context context, String identifier, SaveStickerPackCallback<String> callback) {

        CallbackResult<Void> stickerFilesDeleted = deleteAllFilesInPack(context, identifier);
        switch (stickerFilesDeleted.getStatus()) {
            case SUCCESS:
                callback.onStickerPackSaveResult(CallbackResult.success("Figurinhas deletadas com sucesso."));
                break;
            case WARNING:
                callback.onStickerPackSaveResult(CallbackResult.success(stickerFilesDeleted.getWarningMessage()));
                break;
            case FAILURE:
                callback.onStickerPackSaveResult(CallbackResult.failure(stickerFilesDeleted.getError()));
                return false;
        }

        CallbackResult<Integer> stickersDeletedInDb = deleteStickersOfPack(context, identifier);
        if (stickersDeletedInDb.isFailure()) {
            callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Falha ao limpar figurinhas do banco de dados.", stickersDeletedInDb.getError())));
            return false;
        }

        callback.onStickerPackSaveResult(CallbackResult.success("Figurinhas deletadas do banco com sucesso."));

        CallbackResult<Integer> stickerPackDeletedInDb = deleteStickerPackFromDatabase(context, identifier);
        return switch (stickerPackDeletedInDb.getStatus()) {
            case SUCCESS -> {
                callback.onStickerPackSaveResult(CallbackResult.success(
                        "Pacote de figurinhas deletado com sucesso. Status: " + stickerPackDeletedInDb.getData()));
                yield true;
            }
            case WARNING -> {
                callback.onStickerPackSaveResult(CallbackResult.success(stickerPackDeletedInDb.getWarningMessage()));
                yield true;
            }
            case FAILURE -> {
                callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Falha ao limpar pacote de figurinhas do banco.", stickerPackDeletedInDb.getError())));
                yield false;
            }
        };
    }

    private static File createStickerPackDirectory(File mainDirectory, String identifier, SaveStickerPackCallback<String> callback) {
        File stickerPackDirectory = new File(mainDirectory, identifier);
        if (!stickerPackDirectory.exists()) {
            boolean created = stickerPackDirectory.mkdirs();
            if (!created) {
                callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException(
                        "Falha ao criar a pasta: " + stickerPackDirectory.getPath())));
                return null;
            }
            callback.onStickerPackSaveResult(CallbackResult.success("Pasta criada com sucesso: " + stickerPackDirectory.getPath()));
        } else {
            callback.onStickerPackSaveResult(CallbackResult.warning("Pasta já existe: " + stickerPackDirectory.getPath()));
        }
        return stickerPackDirectory;
    }

    private static boolean copyStickers(Context context, StickerPack stickerPack, File stickerPackDirectory, SaveStickerPackCallback<String> callback) {

        List<Sticker> stickerList = stickerPack.getStickers();
        Sticker firstSticker = stickerList.get(0);
        File thumbnailSticker = new File(context.getCacheDir(), firstSticker.imageFileName);
        compressAndSaveThumbnail(thumbnailSticker, stickerPackDirectory, callback);

        for (Sticker sticker : stickerList) {
            String fileName = sticker.imageFileName;
            File sourceFile = new File(context.getCacheDir(), fileName);
            File destFile = new File(stickerPackDirectory, fileName);

            if (sourceFile.exists()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Path sourcePath = sourceFile.toPath();
                        Path destPath = destFile.toPath();
                        Files.copy(sourcePath, destPath);
                        callback.onStickerPackSaveResult(CallbackResult.success("Arquivo copiado para: " + destFile.getPath()));
                    }
                } catch (IOException exception) {
                    callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException(
                            "Arquivo não encontrado: " + fileName, exception)));
                    return false;
                }
            } else {
                callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Arquivo não encontrado: " + fileName)));
                return false;
            }
        }
        return true;
    }

    private static void insertStickerPack(Context context, StickerPack stickerPack, SaveStickerPackCallback<String> callback) {
        StickerDatabaseHelper dbHelper = StickerDatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        new InsertStickerPacks().insertStickerPack(db, stickerPack, callbackResult -> {
            switch (callbackResult.getStatus()) {
                case SUCCESS:
                    if (callback != null) {
                        callback.onStickerPackSaveResult(CallbackResult.success(callbackResult.getData().toString()));
                    } else {
                        Log.d("SaveStickerPack", "Callback é null!");
                    }
                    break;
                case WARNING:
                    Log.w("SaveStickerPack", callbackResult.getWarningMessage());
                    break;
                case FAILURE:
                    if (callbackResult.getError() instanceof StickerPackSaveException exception) {
                        callback.onStickerPackSaveResult(CallbackResult.failure(exception));
                    } else {
                        callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Erro interno desconhecido!")));
                    }
                    break;
            }
        });
    }

    private static void compressAndSaveThumbnail(File originalFile, File destinationDir, SaveStickerPackCallback<String> callback) {
        if (!originalFile.exists()) {
            callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException(
                    "Arquivo original não encontrado: " + originalFile.getAbsolutePath())));
            return;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());
            if (bitmap == null) {
                callback.onStickerPackSaveResult(CallbackResult.warning("Erro ao decodificar o bitmap."));
                return;
            }

            File thumbnailFile = new File(destinationDir, "thumbnail.jpg");
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            int quality = 100;
            byte[] compressedBytes;

            do {
                outStream.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
                compressedBytes = outStream.toByteArray();
                quality -= 5;
            } while (compressedBytes.length > 40 * 1024 && quality > 5);

            FileOutputStream fos = new FileOutputStream(thumbnailFile);
            fos.write(compressedBytes);
            fos.flush();
            fos.close();

            callback.onStickerPackSaveResult(CallbackResult.success("Thumbnail salva com sucesso: " + thumbnailFile.getAbsolutePath()));
        } catch (IOException exception) {
            callback.onStickerPackSaveResult(CallbackResult.failure(exception));
        }
    }
}
