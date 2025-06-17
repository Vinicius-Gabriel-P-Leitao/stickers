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

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import br.arch.sticker.core.error.code.MediaConversionErrorCode;
import br.arch.sticker.core.error.throwable.media.MediaConversionException;
import br.arch.sticker.core.lib.NativeConvertToWebp;
import br.arch.sticker.view.core.util.resolver.FileDetailsResolver;

// @formatter:off
public class VideoConverter {
    public static CompletableFuture<File> convertVideoToWebPAsyncFuture(@NonNull Context context, @NonNull Uri inputPath,
                                                                        @NonNull String outputFileName) throws MediaConversionException {
        CompletableFuture<File> future = new CompletableFuture<>();

        String finalOutputFileName = ensureWebpExtension(outputFileName);
        File outputFile = new File(context.getCacheDir(), finalOutputFileName);

        NativeConvertToWebp nativeConvertToWebp = new NativeConvertToWebp();
        nativeConvertToWebp.convertToWebpAsync(
                FileDetailsResolver.getAbsolutePath(context, inputPath),
                outputFile.getAbsolutePath(),
                new NativeConvertToWebp.ConversionCallback() {
                    @Override
                    public void onSuccess(File file) {
                        future.complete(file);
                    }

                    @Override
                    public void onError(Exception exception) {
                        future.completeExceptionally(new MediaConversionException(
                                Objects.toString(exception.getMessage(), "Erro desconhecido ao converter mídia"),
                                exception.getCause(),
                                MediaConversionErrorCode.ERROR_PACK_CONVERSION_MEDIA));
                    }
                });

        return future;
    }

    private static String ensureWebpExtension(String fileName) {
        if (!fileName.toLowerCase().endsWith(".webp")) {
            return fileName.replaceAll("\\.\\w+$", "") + ".webp";
        }

        return fileName;
    }
}
