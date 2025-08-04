/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.lib;

import android.content.res.Resources;

import java.io.File;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.media.MediaConversionException;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

public class NativeProcessWebp {
    public interface ConversionCallback {
        void onSuccess(File file);

        void onError(Exception exception);
    }

    private final static String TAG_LOG = NativeProcessWebp.class.getSimpleName();

    static {
        System.loadLibrary("ConvertSticker");
    }

    private final ApplicationTranslate applicationTranslate;

    public NativeProcessWebp(Resources resources) {
        this.applicationTranslate = new ApplicationTranslate(resources);
    }

    public native boolean convertToWebp(String inputPath, String outputPath, float quality, boolean lossless);

    public void processWebpAsync(String inputPath, String outputPath, float quality, boolean lossless, ConversionCallback callback) throws MediaConversionException {

        try {
            boolean success = convertToWebp(inputPath, outputPath, quality, lossless);
            File outputFile = new File(outputPath);

            if (success && outputFile.exists()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException exception) {
                    throw new MediaConversionException(
                            exception.getMessage() != null ? exception.getMessage() : applicationTranslate.translate(
                                    R.string.error_pausing_thread).log(TAG_LOG, Level.ERROR, exception).get(),
                            exception.getCause(), ErrorCode.ERROR_NATIVE_CONVERSION);
                }

                callback.onSuccess(outputFile);
            } else {
                callback.onError(new MediaConversionException(
                        applicationTranslate.translate(R.string.error_conversion_failed).log(TAG_LOG, Level.ERROR)
                                .get(), ErrorCode.ERROR_NATIVE_CONVERSION));
            }
        } catch (Exception exception) {
            callback.onError(new MediaConversionException(
                    applicationTranslate.translate(R.string.error_native_conversion)
                            .log(TAG_LOG, Level.ERROR, exception).get(), ErrorCode.ERROR_NATIVE_CONVERSION));
        }
    }
}