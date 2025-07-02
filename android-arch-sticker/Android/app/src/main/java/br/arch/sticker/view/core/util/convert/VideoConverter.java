/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.util.convert;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import br.arch.sticker.core.error.code.MediaConversionErrorCode;
import br.arch.sticker.core.error.throwable.media.MediaConversionException;
import br.arch.sticker.core.lib.NativeProcessWebp;

public class VideoConverter {
    private final Context context;

    public VideoConverter(Context context)
        {
            this.context = context.getApplicationContext();
        }

    public CompletableFuture<File> convertVideoToWebPAsyncFuture(
            @NonNull String inputPath, @NonNull String outputFileName) throws MediaConversionException
        {
            CompletableFuture<File> future = new CompletableFuture<>();

            String finalOutputFileName = ConvertMediaToStickerFormat.ensureWebpExtension(outputFileName);
            String outputFile = new File(context.getCacheDir(), finalOutputFileName).getAbsolutePath();

            NativeProcessWebp nativeProcessWebp = new NativeProcessWebp();
            nativeProcessWebp.processWebpAsync(inputPath, outputFile,

                    new NativeProcessWebp.ConversionCallback() {
                        @Override
                        public void onSuccess(File file)
                            {
                                future.complete(file);
                            }

                        @Override
                        public void onError(Exception exception)
                            {
                                future.completeExceptionally(
                                        new MediaConversionException(Objects.toString(exception.getMessage(), "Erro desconhecido ao converter mídia"),
                                                exception.getCause(), MediaConversionErrorCode.ERROR_PACK_CONVERSION_MEDIA));
                            }
                    });

            return future;
        }
}
