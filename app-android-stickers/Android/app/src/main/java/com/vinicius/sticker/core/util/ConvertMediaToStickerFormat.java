package com.vinicius.sticker.core.util;

import static com.vinicius.sticker.core.util.OpenOwnGalleryActivity.ANIMATED_MIME_TYPES;
import static com.vinicius.sticker.core.util.OpenOwnGalleryActivity.IMAGE_MIME_TYPES;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Objects;

public class ConvertMediaToStickerFormat {
   public static void convertMediaToWebP(Context context, String inputPath, String outputFile) {
      try {
         String mimeType = URLConnection.guessContentTypeFromName(inputPath);
         if (mimeType == null) {
            throw new IllegalArgumentException("Não foi possível determinar o tipo MIME do arquivo");
         }

         if (isMimeTypeInList(mimeType, IMAGE_MIME_TYPES)) {
            convertImageToWebP(context, inputPath, outputFile);
         } else if (isMimeTypeInList(mimeType, ANIMATED_MIME_TYPES)) {
            convertVideoToWebP(context, inputPath, outputFile);
         } else {
            throw new IllegalArgumentException("Tipo MIME não suportado para conversão: " + mimeType);
         }
      } catch (IOException exception) {
         throw new RuntimeException(exception);
      }
   }

   private static boolean isMimeTypeInList(String mimeType, String[] mimeTypesList) {
      for (String type : mimeTypesList) {
         if (Objects.equals(mimeType, type)) {
            return true;
         }
      }
      return false;
   }

   public static void convertImageToWebP(Context context, String inputPath, String outputFileName) throws IOException {
      if (!outputFileName.toLowerCase().endsWith(".webp")) {
         outputFileName = outputFileName.replaceAll("\\.\\w+$", "") + ".webp";
      }

      String cleanedPath = inputPath.startsWith("file://") ? inputPath.substring(7) : inputPath;
      Bitmap bitmap = BitmapFactory.decodeFile(cleanedPath);

      File outputFile = new File(context.getCacheDir(), outputFileName);
      try (FileOutputStream out = new FileOutputStream(outputFile)) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out);
         }
      }

      Log.d("ConvertImage", "Imagem convertida salva em cache: " + outputFile.getAbsolutePath());
   }

   public static void convertVideoToWebP(Context context, String inputPath, String outputFileName) {
      File outputFile = new File(context.getCacheDir(), outputFileName);

      if (!outputFileName.toLowerCase().endsWith(".webp")) {
         outputFileName = outputFileName.replaceAll("\\.\\w+$", "") + ".webp";
         outputFile = new File(context.getCacheDir(), outputFileName);
      }

      String command = String.format("-y -i \"%s\" -vcodec libwebp -filter:v fps=15 -lossless 0 -compression_level 6 -q:v 70 -loop 0 \"%s\"", inputPath, outputFile.getAbsolutePath());

      File finalFile = outputFile;
      FFmpegKit.executeAsync(command, session -> {
         if (ReturnCode.isSuccess(session.getReturnCode())) {
            Log.d("FFmpeg", "Vídeo convertido com sucesso: " + finalFile.getAbsolutePath());
         } else {
            Log.e("FFmpeg", "Falhou: " + session);
         }
      });
   }
}
