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

import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKERS_ASSET;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.vinicius.sticker.core.exception.StickerPackSaveException;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.pattern.CallbackResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SaveStickerPack {

   @FunctionalInterface
   public interface SaveStickerPackCallback {
      void onStickerPackSaveCompleted(CallbackResult callbackResult);
   }

   public static void generateStructureForSavePack(
       Context context, StickerPack stickerPack, SaveStickerPackCallback callback) {

      File mainDirectory =
          new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), STICKERS_ASSET);

      if ( !mainDirectory.exists() ) {
         boolean created = mainDirectory.mkdirs();
         if ( !created ) {
            callback.onStickerPackSaveCompleted(CallbackResult.failure(new StickerPackSaveException(
                "Falha ao criar o mainDirectory: " + mainDirectory.getPath())));
            return;
         } else {
            callback.onStickerPackSaveCompleted(CallbackResult.success(
                "mainDirectory criado com sucesso: " + mainDirectory.getPath()));
         }
      } else {
         callback.onStickerPackSaveCompleted(
             CallbackResult.success("mainDirectory já existe: " + mainDirectory.getPath()));
      }

      String folderName = stickerPack.identifier;
      File stickerPackDirectory = new File(mainDirectory, folderName);
      if ( !stickerPackDirectory.exists() ) {
         boolean created = stickerPackDirectory.mkdirs();
         if ( !created ) {
            callback.onStickerPackSaveCompleted(CallbackResult.failure(new StickerPackSaveException(
                "Falha ao criar a pasta: " + stickerPackDirectory.getPath())));
            return;
         }
         callback.onStickerPackSaveCompleted(
             CallbackResult.success("Pasta criada com sucesso: " + stickerPackDirectory.getPath()));
      } else {
         callback.onStickerPackSaveCompleted(
             CallbackResult.warning("Pasta já existe: " + stickerPackDirectory.getPath()));
      }

      cleanDirectory(stickerPackDirectory, callback);
      List<Sticker> stickerList = stickerPack.getStickers();
      for (Sticker sticker : stickerList) {
         String fileName = sticker.imageFileName;
         File sourceFile = new File(context.getCacheDir(), fileName);
         File destFile = new File(stickerPackDirectory, fileName);

         if ( sourceFile.exists() ) {
            try {
               if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
                  Path sourcePath = sourceFile.toPath();
                  Path destPath = destFile.toPath();
                  Files.copy(sourcePath, destPath);

                  callback.onStickerPackSaveCompleted(
                      CallbackResult.success("Arquivo copiado para: " + destFile.getPath()));
               }
            } catch (IOException exception) {
               callback.onStickerPackSaveCompleted(CallbackResult.failure(
                   new StickerPackSaveException("Arquivo não encontrado: " + fileName, exception)));
            }
         } else {
            callback.onStickerPackSaveCompleted(CallbackResult.failure(
                new StickerPackSaveException("Arquivo não encontrado: " + fileName)));
         }
      }
      // Todo: Usar repositorio para salvar o stickerPack metadata dentro do Sqlite}
   }

   private static void cleanDirectory(File directory, SaveStickerPackCallback callback) {
      File[] files = directory.listFiles();
      if ( files != null ) {
         for (File file : files) {
            if ( file.isFile() ) {
               boolean deleted = file.delete();
               if ( deleted ) {
                  callback.onStickerPackSaveCompleted(
                      CallbackResult.success("Arquivo excluído: " + file.getName()));
               } else {
                  callback.onStickerPackSaveCompleted(CallbackResult.failure(
                      new StickerPackSaveException(
                          "Erro ao excluir o arquivo: " + file.getName())));
               }
            }
         }
      }
   }

}
