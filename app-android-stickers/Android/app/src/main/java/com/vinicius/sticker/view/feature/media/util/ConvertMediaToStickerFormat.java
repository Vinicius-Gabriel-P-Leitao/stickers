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

import static com.vinicius.sticker.view.feature.media.util.CursorFindUriMedia.getFileDetailsFromUri;
import static com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher.ANIMATED_MIME_TYPES;
import static com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher.IMAGE_MIME_TYPES;
import static com.vinicius.sticker.core.validation.MimeTypesValidator.validateUniqueMimeType;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class ConvertMediaToStickerFormat {
   public static void convertMediaToWebP(Context context, Uri inputUri, String outputFile) {
      Map<String, String> mapDetailsFile = getFileDetailsFromUri(context,
          inputUri);
      if (mapDetailsFile.isEmpty()) {
         throw new IllegalArgumentException("Não foi possível determinar o tipo MIME do arquivo");
      }

      mapDetailsFile.forEach((String fileName, String mimeType) -> {
         try {
            if (validateUniqueMimeType(mimeType,
                IMAGE_MIME_TYPES)) {
               convertImageToWebP(context,
                   fileName,
                   outputFile);
            } else if (validateUniqueMimeType(mimeType,
                ANIMATED_MIME_TYPES)) {
               convertVideoToWebP(context,
                   fileName,
                   outputFile);
            } else {
               throw new IllegalArgumentException("Tipo MIME não suportado para conversão: " + mimeType);
            }
         } catch (IOException exception) {
            throw new RuntimeException(exception);
         }
      });
   }

   public static void convertImageToWebP(Context context, String inputPath, String outputFileName) throws IOException {
      if (!outputFileName.toLowerCase().endsWith(".webp")) {
         outputFileName = outputFileName.replaceAll("\\.\\w+$",
             "") + ".webp";
      }

      String cleanedPath = inputPath.startsWith("file://") ? inputPath.substring(7) : inputPath;
      Bitmap bitmap = BitmapFactory.decodeFile(cleanedPath);

      File outputFile = new File(context.getCacheDir(),
          outputFileName);
      try (FileOutputStream out = new FileOutputStream(outputFile)) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY,
                80,
                out);
         }
      }

      Log.d("ConvertImage",
          "Imagem convertida salva em cache: " + outputFile.getAbsolutePath());
   }

   public static void convertVideoToWebP(Context context, String inputPath, String outputFileName) {
      File outputFile = new File(context.getCacheDir(),
          outputFileName);

      if (!outputFileName.toLowerCase().endsWith(".webp")) {
         outputFileName = outputFileName.replaceAll("\\.\\w+$",
             "") + ".webp";
         outputFile = new File(context.getCacheDir(),
             outputFileName);
      }

      String command = String.format("-y -i \"%s\" -vcodec libwebp -filter:v fps=15 -lossless 0 -compression_level 6 -q:v 70 -loop 0 \"%s\"",
          inputPath,
          outputFile.getAbsolutePath());

      File finalFile = outputFile;
      FFmpegKit.executeAsync(command,
          session -> {
             if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.d("FFmpeg",
                    "Vídeo convertido com sucesso: " + finalFile.getAbsolutePath());
             } else {
                Log.e("FFmpeg",
                    "Falhou: " + session);
             }
          });
   }
}
