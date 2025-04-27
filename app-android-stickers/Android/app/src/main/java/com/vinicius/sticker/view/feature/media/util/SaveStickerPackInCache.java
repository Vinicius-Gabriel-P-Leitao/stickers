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

package com.vinicius.sticker.view.feature.media.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SaveStickerPackInCache {
   public static final String STICKER_PACK_DIR = "sticker-pack";

   public static void generateStructureForSavePack(
       Context context, StickerPack stickerPack,
       String contentJson
   ) {
      File mainDirectory = new File(context.getCacheDir(), STICKER_PACK_DIR);

      String folderName = stickerPack.identifier;
      File stickerPackDirectory = new File(mainDirectory, folderName);
      if ( !stickerPackDirectory.exists() ) {
         boolean created = stickerPackDirectory.mkdirs();
         if ( !created ) {
            Log.e("StickerPackSaver", "Falha ao criar a pasta: " + stickerPackDirectory.getPath());
            return;
         }
         Log.i("StickerPackSaver", "Pasta criada com sucesso: " + stickerPackDirectory.getPath());
      } else {
         Log.i("StickerPackSaver", "Pasta já existe: " + stickerPackDirectory.getPath());
      }

      cleanDirectory(stickerPackDirectory);
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
                  Log.i("StickerPackSaver", "Arquivo copiado para: " + destFile.getPath());
               }
            } catch (IOException exception) {
               Log.e("StickerPackSaver", "Erro ao copiar o arquivo: " + fileName, exception);
            }
         } else {
            Log.e("StickerPackSaver", "Arquivo não encontrado no cache: " + fileName);
         }
      }

      try {
         saveContentJsonToCache(stickerPackDirectory, contentJson);
      } catch (IOException exception) {
         Log.e("StickerPackSaver", "Erro ao salvar o JSON no cache", exception);
      }
   }

   private static void cleanDirectory(File directory) {
      File[] files = directory.listFiles();
      if ( files != null ) {
         for (File file : files) {
            if ( file.isFile() ) {
               boolean deleted = file.delete();
               if ( deleted ) {
                  Log.i("StickerPackSaver", "Arquivo excluído: " + file.getName());
               } else {
                  Log.e("StickerPackSaver", "Erro ao excluir o arquivo: " + file.getName());
               }
            }
         }
      }
   }

   private static void saveContentJsonToCache(
       File stickerPackDirectory, String contentJson) throws IOException {
      File jsonFile = new File(stickerPackDirectory, "pack.json");
      try (FileOutputStream fos = new FileOutputStream(
          jsonFile); OutputStreamWriter writer = new OutputStreamWriter(fos)) {
         writer.write(contentJson);
         Log.i("StickerPackSaver", "JSON salvo em: " + jsonFile.getPath());
      }
   }
}
