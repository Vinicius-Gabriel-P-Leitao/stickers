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

package com.vinicius.sticker.domain.service;

import static com.vinicius.sticker.domain.data.content.provider.StickerContentProvider.STICKERS_ASSET;
import static com.vinicius.sticker.domain.data.database.repository.DeleteStickerPacks.deleteStickerPackFromDatabase;
import static com.vinicius.sticker.domain.data.database.repository.DeleteStickerPacks.deleteStickersOfPack;
import static com.vinicius.sticker.domain.service.StickerDeleteService.deleteOldFilesInPack;

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

        if (!mainDirectory.exists()) {
            boolean created = mainDirectory.mkdirs();
            if (!created) {
                callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Falha ao criar o mainDirectory: " + mainDirectory.getPath())));
                return;
            } else {
                callback.onStickerPackSaveResult(CallbackResult.success("mainDirectory criado com sucesso: " + mainDirectory.getPath()));
            }
        } else {
            callback.onStickerPackSaveResult(CallbackResult.success("mainDirectory já existe: " + mainDirectory.getPath()));
        }

        if (stickerPack.identifier.equals(previousIdentifier) || previousIdentifier != null) {
            CallbackResult<Void> stickerFilesDeleted = deleteOldFilesInPack(context, stickerPack.identifier);
            switch (stickerFilesDeleted.getStatus()) {
                case SUCCESS:
                    callback.onStickerPackSaveResult(CallbackResult.warning("Figurinhas deletadas com sucesso."));
                    break;
                case WARNING:
                    callback.onStickerPackSaveResult(CallbackResult.warning(stickerFilesDeleted.getWarningMessage()));
                    break;
                case FAILURE:
                    callback.onStickerPackSaveResult(CallbackResult.failure(stickerFilesDeleted.getError()));
            }

            CallbackResult<Integer> stickersDeletedInDb = deleteStickersOfPack(context, stickerPack.identifier);
            if (stickersDeletedInDb.isSuccess()) {
                callback.onStickerPackSaveResult(CallbackResult.warning("Figurinhas deletadas do banco com sucesso."));
                CallbackResult<Integer> stickerPackDeletedInDb = deleteStickerPackFromDatabase(context, stickerPack.identifier);

                switch (stickerPackDeletedInDb.getStatus()) {
                    case SUCCESS -> {
                        callback.onStickerPackSaveResult(CallbackResult.warning("Pacote de figurinhas deletado com sucesso. Status: " + stickerPackDeletedInDb.getData()));
                        return;
                    }
                    case WARNING -> {
                        callback.onStickerPackSaveResult(CallbackResult.warning(stickerPackDeletedInDb.getWarningMessage()));
                        return;
                    }
                    case FAILURE -> {
                        callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Falha ao limpar pacote de figurinhas do banco.", stickerPackDeletedInDb.getError())));
                        return;
                    }
                }
            } else if (stickersDeletedInDb.isFailure()) {
                callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Falha ao limpar figurinhas do banco de dados.", stickersDeletedInDb.getError())));
            }
        } else {
            callback.onStickerPackSaveResult(CallbackResult.warning("Identificador diferente, não houve deleção."));
        }

        String folderName = stickerPack.identifier;
        File stickerPackDirectory = new File(mainDirectory, folderName);

        if (!stickerPackDirectory.exists()) {
            boolean created = stickerPackDirectory.mkdirs();
            if (!created) {
                callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Falha ao criar a pasta: " + stickerPackDirectory.getPath())));
                return;
            }
            callback.onStickerPackSaveResult(CallbackResult.success("Pasta criada com sucesso: " + stickerPackDirectory.getPath()));
        } else {
            callback.onStickerPackSaveResult(CallbackResult.warning("Pasta já existe: " + stickerPackDirectory.getPath()));
        }

        List<Sticker> stickerList = stickerPack.getStickers();

        Sticker firstSticker = stickerList.get(0);

        File thumbnailSticker = new File(context.getCacheDir(), firstSticker.imageFileName);
        compressAndSaveThumbnail(thumbnailSticker, stickerPackDirectory);

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
                    callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Arquivo não encontrado: " + fileName, exception)));
                }
            } else {
                callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Arquivo não encontrado: " + fileName)));
            }
        }

        insertStickerPack(context, stickerPack, callback);
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

    public static void compressAndSaveThumbnail(File originalFile, File destinationDir) {
        if (!originalFile.exists()) {
            Log.e("Thumbnail", "Arquivo original não encontrado: " + originalFile.getAbsolutePath());
            return;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());
            if (bitmap == null) {
                Log.e("Thumbnail", "Erro ao decodificar o bitmap.");
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

            Log.d("Thumbnail", "Thumbnail salva com sucesso: " + thumbnailFile.getAbsolutePath());
        } catch (IOException exception) {
            Log.e("Thumbnail", "Erro ao salvar thumbnail: " + exception.getMessage());
        }
    }
}
