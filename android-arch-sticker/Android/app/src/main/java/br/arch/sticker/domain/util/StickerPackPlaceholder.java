/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import br.arch.sticker.core.error.code.SaveErrorCode;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabase;
import br.arch.sticker.domain.data.database.repository.InsertStickerPackRepo;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;

public class StickerPackPlaceholder {
    public final static String PLACEHOLDER_ANIMATED = "placeholder_animated.webp";
    public final static String PLACEHOLDER_STATIC = "placeholder_static.webp";

    private static boolean isCreatingPlaceholder = false;

    private final InsertStickerPackRepo insertStickerPackRepo;
    private final Context context;

    public StickerPackPlaceholder(Context context)
        {
            StickerDatabase instance = StickerDatabase.getInstance(context);
            SQLiteDatabase writableDatabase = instance.getWritableDatabase();

            this.context = context;
            this.insertStickerPackRepo = new InsertStickerPackRepo(writableDatabase);
        }

    public Sticker makeAndSaveStickerPlaceholder(StickerPack stickerPack)
        {
            if (isCreatingPlaceholder) {
                return null;
            }

            isCreatingPlaceholder = true;

            try {
                Sticker stickerPlaceholder = this.makeStickerPlaceholder(context, stickerPack);
                CallbackResult<Sticker> insertedSticker = insertStickerPackRepo.insertSticker(stickerPlaceholder, stickerPack.identifier);

                return insertedSticker.getData();
            } finally {
                isCreatingPlaceholder = false;
            }
        }

    public Sticker makeStickerPlaceholder(Context context, StickerPack stickerPack)
        {
            String fileName = stickerPack.animatedStickerPack
                              ? PLACEHOLDER_ANIMATED
                              : PLACEHOLDER_STATIC;

            String accessibility = stickerPack.animatedStickerPack
                                   ? "Pacote animado com nome " + stickerPack.name
                                   : "Pacote estático com nome " + stickerPack.name;

            File cacheFile = new File(context.getCacheDir(), fileName);
            try (AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd(fileName);
                 InputStream inputStream = assetFileDescriptor.createInputStream();
                 OutputStream outputStream = new FileOutputStream(cacheFile)) {

                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.flush();

                return new Sticker(fileName.trim(), "\uD83D\uDDFF", "", accessibility, stickerPack.identifier);
            } catch (IOException exception) {
                throw new StickerPackSaveException("Erro ao criar placeholder para o pacote de figurinhas!", exception,
                        SaveErrorCode.ERROR_PACK_SAVE_SERVICE);
            }
        }
}
