package com.vinicius.sticker.core.util;

public class ConvertMediaToStickerFormat {
//   public void convertImageToWebP(File inputFile, File outputFile) throws IOException {
//      Bitmap bitmap = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
//
//      try (FileOutputStream out = new FileOutputStream(outputFile)) {
//         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out);
//         }
//      }
//   }
//
//   public void convertVideoToWebP(String inputPath, String outputPath) {
//      String command = String.format(
//          "-i %s -vcodec libwebp -filter:v fps=15 -lossless 0 -compression_level 6 -q:v 70 -loop 0 %s", inputPath, outputPath);
//
//      FFmpegKit.executeAsync(command, session -> {
//         if (ReturnCode.isSuccess(session.getReturnCode())) {
//            Log.d("FFmpeg", "Conversão concluída!");
//         } else {
//            Log.e("FFmpeg", "Falhou: " + session.getFailStackTrace());
//         }
//      });
//   }
}
