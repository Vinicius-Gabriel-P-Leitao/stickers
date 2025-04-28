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

package com.vinicius.sticker.presentation.feature.media.util;

import static com.vinicius.sticker.domain.data.provider.StickerContentProvider.STICKERS_ASSET;
import static com.vinicius.sticker.domain.service.ContentFileParser.readStickerPack;

import android.content.Context;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;

import com.vinicius.sticker.domain.data.model.StickerPack;

import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LoadStickerPackInPicture {
   public static StickerPack processStickerPacks(String uuidPack, Context context) {
      List<StickerPack> stickerPacks = new ArrayList<>();
      File stickerPackDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), STICKERS_ASSET);

      if ( stickerPackDir.exists() && stickerPackDir.isDirectory() ) {
         File[] packDirs = stickerPackDir.listFiles();

         if ( packDirs != null ) {
            for (File packDir : packDirs) {
               if ( packDir.isDirectory() ) {
                  Log.d("StickerPack", "Processing sticker pack with UUID: " + uuidPack);

                  File jsonFile = new File(packDir, "pack.json");
                  if ( jsonFile.exists() ) {
                     try {
                        InputStream stickerPackJson = readJsonFile(jsonFile);
                        List<String> webpFiles = getWebPFiles(packDir);

                        Log.d("StickerPack", "Found JSON: " + stickerPackJson.toString());
                        Log.d("StickerPack", "Found WebP files: " + webpFiles);

                        JsonReader reader = new JsonReader(new InputStreamReader(stickerPackJson));

                        return readStickerPack(reader);
                     } catch (IOException |
                              JSONException exception) {
                        Log.e("StickerPack", "Error processing pack: " + exception.getMessage());
                     }
                  } else {
                     Log.e("StickerPack", "No JSON file found in: " + packDir.getName());
                  }
               }
            }
         }
      } else {
         Log.e("StickerPack", "Sticker pack directory not found.");
      }

      return null;
   }

   private static ByteArrayInputStream readJsonFile(
       File jsonFile
   ) throws IOException, JSONException {
      FileReader fileReader = new FileReader(jsonFile);
      StringBuilder jsonContent = new StringBuilder();
      int counter;
      while ((counter = fileReader.read()) != -1) {
         jsonContent.append((char) counter);
      }
      fileReader.close();
      return new ByteArrayInputStream(jsonContent.toString().getBytes(StandardCharsets.UTF_8));
   }

   private static List<String> getWebPFiles(File packDir) {
      List<String> webpFiles = new ArrayList<>();
      File[] files = packDir.listFiles((dir, name) -> name.endsWith(".webp"));
      if ( files != null ) {
         for (File file : files) {
            webpFiles.add(file.getName());
         }
      }
      return webpFiles;
   }
}
