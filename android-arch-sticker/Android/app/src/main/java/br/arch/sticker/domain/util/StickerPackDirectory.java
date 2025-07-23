/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.util;

import static br.arch.sticker.core.error.ErrorCode.ERROR_PACK_SAVE_UTIL;

import android.content.Context;

import java.io.File;

import br.arch.sticker.R;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

public class StickerPackDirectory {
    private static final String TAG_LOG = StickerPackDirectory.class.getSimpleName();

    public static CallbackResult<Boolean> createMainDirectory(Context context, File mainDirectory) throws StickerPackSaveException {
        if (!mainDirectory.exists()) {
            boolean created = mainDirectory.mkdirs();

            if (!created) {

                return CallbackResult.failure(new StickerPackSaveException(
                        ApplicationTranslate.translate(context, R.string.error_create_main_directory)
                                .log(TAG_LOG, Level.ERROR, mainDirectory.getPath()).get(), ERROR_PACK_SAVE_UTIL
                ));
            }

            return CallbackResult.success(true);
        } else {
            return CallbackResult.debug(ApplicationTranslate.translate(context, R.string.debug_main_directory_exists).log(TAG_LOG, Level.WARN).get());
        }
    }

    public static CallbackResult<File> createStickerPackDirectory(Context context, File mainDirectory, String stickerPackIdentifier)
            throws StickerPackSaveException {
        File stickerPackDirectory = new File(mainDirectory, stickerPackIdentifier);

        if (!stickerPackDirectory.exists()) {
            boolean created = stickerPackDirectory.mkdirs();

            if (!created) {
                return CallbackResult.failure(new StickerPackSaveException(
                        ApplicationTranslate.translate(context, R.string.error_create_sticker_pack_directory)
                                .log(TAG_LOG, Level.ERROR, stickerPackDirectory.getPath()).get(), ERROR_PACK_SAVE_UTIL
                ));

            }

            return CallbackResult.success(stickerPackDirectory);
        } else {
            return CallbackResult.debug(
                    ApplicationTranslate.translate(context, R.string.debug_sticker_pack_directory_exists).log(TAG_LOG, Level.WARN).get());
        }
    }
}
