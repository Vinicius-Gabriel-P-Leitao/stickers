/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.util;

import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.InsertStickerRepo;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

public class StickerPackPlaceholder {
    private final static String TAG_LOG = StickerPackPlaceholder.class.getSimpleName();

    public final static String PLACEHOLDER_ANIMATED = "placeholder_animated.webp";
    public final static String PLACEHOLDER_STATIC = "placeholder_static.webp";

    private static boolean isCreatingPlaceholder = false;

    private final ApplicationTranslate applicationTranslate;
    private final InsertStickerRepo insertStickerPackRepo;
    private final Context context;

    public StickerPackPlaceholder(Context context) {
        this.context = context;

        Resources resources = this.context.getResources();
        this.applicationTranslate = new ApplicationTranslate(resources);
        this.insertStickerPackRepo = new InsertStickerRepo(
                StickerDatabaseHelper.getInstance(this.context).getWritableDatabase(), resources);
    }

    public Sticker makeAndSaveStickerPlaceholder(StickerPack stickerPack) {
        if (isCreatingPlaceholder) {
            return null;
        }

        isCreatingPlaceholder = true;

        try {
            File stickerDir = new File(context.getFilesDir(),
                    new File(STICKERS_ASSET, stickerPack.identifier).toString()
            );

            if (!stickerDir.exists()) {
                stickerDir.mkdirs();
            }

            Sticker stickerPlaceholder = this.makeStickerPlaceholder(stickerPack, stickerDir);
            CallbackResult<Sticker> insertedSticker = insertStickerPackRepo.insertSticker(
                    stickerPlaceholder, stickerPack.identifier);

            if (insertedSticker.isFailure()) {
                isCreatingPlaceholder = false;
            }

            return insertedSticker.getData();
        } finally {
            isCreatingPlaceholder = false;
        }
    }

    public Sticker makeStickerPlaceholder(StickerPack stickerPack, File outputFile) {
        String fileName = stickerPack.animatedStickerPack ? PLACEHOLDER_ANIMATED : PLACEHOLDER_STATIC;

        String accessibility = stickerPack.animatedStickerPack ?
                "Pacote animado com nome " + stickerPack.name :
                "Pacote estático com nome " + stickerPack.name;

        File destFile = new File(outputFile, fileName);
        try (AssetFileDescriptor assetFileDescriptor = context.getAssets()
                .openFd(fileName); InputStream inputStream = assetFileDescriptor.createInputStream(); OutputStream outputStream = new FileOutputStream(
                destFile)) {

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();

            return new Sticker(fileName.trim(), "\uD83D\uDDFF", "", accessibility,
                    stickerPack.identifier
            );
        } catch (IOException exception) {
            throw new StickerPackSaveException(
                    applicationTranslate.translate(R.string.error_create_sticker_placeholder)
                            .log(TAG_LOG, Level.ERROR, exception).get(), exception,
                    ErrorCode.ERROR_PACK_SAVE_SERVICE
            );
        }
    }
}
