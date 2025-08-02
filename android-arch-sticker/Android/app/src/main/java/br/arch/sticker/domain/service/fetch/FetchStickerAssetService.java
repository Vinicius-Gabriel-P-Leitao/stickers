/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.fetch;

import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;
import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class FetchStickerAssetService {
    private final static String TAG_LOG = FetchStickerAssetService.class.getSimpleName();

    private final ApplicationTranslate applicationTranslate;
    private final Context context;

    public FetchStickerAssetService(Context context) {
        this.context = context.getApplicationContext();
        this.applicationTranslate = new ApplicationTranslate(this.context.getResources());
    }

    public byte[] fetchStickerAsset(@NonNull final String stickerPackIdentifier, @NonNull final String fileName) throws FetchStickerException {
        File stickerFile = new File(new File(new File(context.getFilesDir(), STICKERS_ASSET), stickerPackIdentifier), fileName);

        if (stickerFile.exists()) {
            try (InputStream inputStream = new FileInputStream(stickerFile); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                int read;
                byte[] bytes = new byte[16384];

                while ((read = inputStream.read(bytes, 0, bytes.length)) != -1) {
                    buffer.write(bytes, 0, read);
                }

                return buffer.toByteArray();
            } catch (FileNotFoundException fileNotFoundException) {
                throw new FetchStickerException(
                        applicationTranslate.translate(R.string.error_read_sticker_file, stickerPackIdentifier + "/" + fileName)
                                .log(TAG_LOG, Level.ERROR).get(), fileNotFoundException, ErrorCode.ERROR_EMPTY_STICKERPACK
                );
            } catch (IOException exception) {
                throw new FetchStickerException(applicationTranslate.translate(R.string.error_io_sticker_file, stickerPackIdentifier + "/" + fileName)
                        .log(TAG_LOG, Level.ERROR).get(), exception, ErrorCode.ERROR_EMPTY_STICKERPACK
                );
            }
        } else {
            throw new FetchStickerException(applicationTranslate.translate(R.string.error_sticker_file_not_found_param, stickerFile.getAbsolutePath())
                    .log(TAG_LOG, Level.ERROR).get(), ErrorCode.ERROR_EMPTY_STICKERPACK
            );
        }
    }
}
