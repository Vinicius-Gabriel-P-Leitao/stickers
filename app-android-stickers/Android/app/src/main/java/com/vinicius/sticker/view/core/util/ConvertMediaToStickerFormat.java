/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.core.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinicius.sticker.core.exception.media.MediaConversionException;
import com.vinicius.sticker.core.lib.NativeConvertToWebp;
import com.vinicius.sticker.view.core.usecase.definition.MimeTypesSupported;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConvertMediaToStickerFormat {
    private final static String TAG_LOG = ConvertMediaToStickerFormat.class.getSimpleName();

    private static MediaConversionCallback callback;

    public interface MediaConversionCallback {
        void onSuccess(File outputFile);

        void onError(Exception exception);
    }

    public static void convertMediaToWebP(Context context, @NonNull Uri inputUri, @NonNull String outputFile, MediaConversionCallback callback) {
        Map<String, String> mapDetailsFile = getFileDetailsFromUri(context, inputUri);
        if (mapDetailsFile.isEmpty()) {
            throw new MediaConversionException("Não foi possível determinar o tipo MIME do arquivo!");
        }

        mapDetailsFile.forEach((String fileName, String mimeType) -> {
            try {
                if (validateUniqueMimeType(mimeType, MimeTypesSupported.IMAGE.getMimeTypes())) {
                    File imageOutputFile = convertImageToWebP(context, fileName, outputFile);
                    callback.onSuccess(imageOutputFile);
                } else if (validateUniqueMimeType(mimeType, MimeTypesSupported.ANIMATED.getMimeTypes())) {
                    // NOTE: Callback já é dada no método
                    convertVideoToWebP(context, inputUri, outputFile, callback);
                } else {
                    throw new IllegalArgumentException("Tipo MIME não suportado para conversão: " + mimeType);
                }
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    public static File convertImageToWebP(Context context, String inputPath, String outputFileName) throws IOException {
        if (!outputFileName.toLowerCase().endsWith(".webp")) {
            outputFileName = outputFileName.replaceAll("\\.\\w+$", "") + ".webp";
        }

        String cleanedPath = inputPath.startsWith("file://") ? inputPath.substring(7) : inputPath;
        Bitmap bitmap = BitmapFactory.decodeFile(cleanedPath);

        if (bitmap != null) {
            Bitmap squareBitmap = cropAndResizeToSquare(bitmap);

            File outputFile = new File(context.getCacheDir(), outputFileName);
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    squareBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out);
                } else {
                    squareBitmap.compress(Bitmap.CompressFormat.WEBP, 80, out);
                }

                return outputFile.getAbsoluteFile();
            } catch (IOException exception) {
                callback.onError(new MediaConversionException(exception.getMessage(), exception.getCause()));
            }
        }

        return null;
    }

    public static void convertVideoToWebP(Context context, Uri inputPath, String outputFileName, MediaConversionCallback callback) {
        if (!outputFileName.toLowerCase().endsWith(".webp")) {
            outputFileName = outputFileName.replaceAll("\\.\\w+$", "") + ".webp";
        }
        File outputFile = new File(context.getCacheDir(), outputFileName);

        NativeConvertToWebp nativeConvertToWebp = new NativeConvertToWebp();
        nativeConvertToWebp.convertToWebpAsync(
                getAbsolutePath(context, inputPath), outputFile.getAbsolutePath(), new NativeConvertToWebp.ConversionCallback() {
                    @Override
                    public void onSuccess(File file) {
                        callback.onSuccess(file);
                    }

                    @Override
                    public void onError(Exception exception) {
                        callback.onError(new MediaConversionException(exception.getMessage(), exception.getCause()));
                    }
                });
    }

    private static Bitmap cropAndResizeToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newEdge = Math.min(width, height);

        int xOffset = (width - newEdge) / 2;
        int yOffset = (height - newEdge) / 2;

        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, newEdge, newEdge);

        return Bitmap.createScaledBitmap(squareBitmap, 512, 512, true);
    }

    public static boolean validateUniqueMimeType(String mimeType, String[] mimeTypesList) {
        for (String type : mimeTypesList) {
            Log.d(TAG_LOG, "Comparando MIME: " + mimeType + " com " + type);

            if (Objects.equals(mimeType, type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p><b>Descrição:</b>Captura o caminho absoluto da URI de um arquivo.</p>
     *
     * @param context Contexto da aplicação.
     * @param uri     Uri do arquivo.
     * @return Caminho do arquivo.
     */
    public static Map<String, String> getFileDetailsFromUri(Context context, Uri uri) {
        Map<String, String> fileDetails = new HashMap<>();
        fileDetails.put(getAbsolutePath(context, uri), context.getContentResolver().getType(uri));

        return fileDetails;
    }

    /**
     * <p><b>Descrição:</b>Captura o caminho absoluto da URI de um arquivo.</p>
     *
     * @param context Contexto da aplicação.
     * @param uri     Uri do arquivo.
     * @return Caminho do arquivo.
     */
    private static String getAbsolutePath(Context context, Uri uri) {
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return uri.getPath();
        } else {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            String path = cursor.getString(column_index);

            cursor.close();
            return path;
        }
    }
}
