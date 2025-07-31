/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.lib;

import android.content.res.Resources;
import android.util.Log;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.media.MediaConversionException;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class NativeCropMedia {
    public interface CropCallback {
        void onSuccess(File file);

        void onError(Exception exception);
    }

    private final static String TAG_LOG = NativeCropMedia.class.getSimpleName();

    static {
        System.loadLibrary("CropMedia");
    }

    private final ApplicationTranslate applicationTranslate;

    private static final ExecutorService nativeExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public NativeCropMedia(Resources resources) {
        this.applicationTranslate = new ApplicationTranslate(resources);
    }

    public native boolean cropMedia(String inputPath, String outputPath, int x, int y, int width, int height, float startSeconds, float endSeconds);

    public void processWebpAsync(String inputPath, String outputPath, int x, int y, int width, int height, float startSeconds, float endSeconds, CropCallback callback) throws MediaConversionException {
        nativeExecutor.submit(() -> {
            try {
                Log.i(TAG_LOG, String.format("X: %s Y: %s Width: %s Height: %s Start: %s End: %s \nInput: %s Output: %s", x, y, width, height, startSeconds, endSeconds, inputPath, outputPath));
                boolean success = cropMedia(inputPath, outputPath, x, y, width, height, startSeconds, endSeconds);
                File outputFile = new File(outputPath);

                if (success && outputFile.exists()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException exception) {
                        throw new MediaConversionException(exception.getMessage() != null ? exception.getMessage() : applicationTranslate.translate(R.string.error_pausing_thread).log(TAG_LOG, ApplicationTranslate.LoggableString.Level.ERROR, exception).get(), exception.getCause(), ErrorCode.ERROR_NATIVE_CONVERSION);
                    }

                    callback.onSuccess(outputFile);
                } else {
                    callback.onError(new MediaConversionException(applicationTranslate.translate(R.string.error_conversion_failed).log(TAG_LOG, ApplicationTranslate.LoggableString.Level.ERROR).get(), ErrorCode.ERROR_NATIVE_CONVERSION));
                }
            } catch (Exception exception) {
                callback.onError(new MediaConversionException(applicationTranslate.translate(R.string.error_native_conversion).log(TAG_LOG, ApplicationTranslate.LoggableString.Level.ERROR, exception).get(), ErrorCode.ERROR_NATIVE_CONVERSION));
            }
        });
    }
}
