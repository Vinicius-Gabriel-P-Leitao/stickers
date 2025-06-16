/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.util.convert;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.Objects;

import br.arch.sticker.core.exception.throwable.media.MediaConversionException;
import br.arch.sticker.core.lib.NativeConvertToWebp;
import br.arch.sticker.view.core.util.resolver.FileDetailsResolver;

public class VideoConverter {

    public static void convertVideoToWebP(
            Context context, Uri inputPath, String outputFileName, ConvertMediaToStickerFormat.MediaConversionCallback callback) {
        String finalOutputFileName = ensureWebpExtension(outputFileName);
        File outputFile = new File(context.getCacheDir(), finalOutputFileName);

        NativeConvertToWebp nativeConvertToWebp = new NativeConvertToWebp();
        nativeConvertToWebp.convertToWebpAsync(
                FileDetailsResolver.getAbsolutePath(context, inputPath), outputFile.getAbsolutePath(), new NativeConvertToWebp.ConversionCallback() {
                    @Override
                    public void onSuccess(File file) {
                        callback.onSuccess(file);
                    }

                    @Override
                    public void onError(Exception exception) {
                        callback.onError(
                                new MediaConversionException(
                                        Objects.toString(exception.getMessage(), "Erro desconhecido ao converter mídia"),
                                        exception.getCause()));
                    }
                });
    }

    private static String ensureWebpExtension(String fileName) {
        if (!fileName.toLowerCase().endsWith(".webp")) {
            return fileName.replaceAll("\\.\\w+$", "") + ".webp";
        }
        return fileName;
    }
}
