/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.update;

import android.content.Context;
import android.util.Log;

import br.arch.sticker.core.error.ErrorCodeProvider;
import br.arch.sticker.domain.data.database.StickerDatabase;
import br.arch.sticker.domain.data.database.repository.UpdateStickerPackRepo;
import br.arch.sticker.domain.service.fetch.FetchStickerService;

public class UpdateStickerService {
    private final static String TAG_LOG = FetchStickerService.class.getSimpleName();

    public static void updateInvalidSticker(Context context, String stickerPackIdentifier, String fileName, ErrorCodeProvider errorCode)
        {
            StickerDatabase instance = StickerDatabase.getInstance(context);

            Log.d(TAG_LOG, String.format("Realizando upgrade no sticker invalido identificador: %S,file: %s", stickerPackIdentifier, fileName));

            if (errorCode instanceof Enum<?>) {
                String name = ((Enum<?>) errorCode).name();
                Log.d(TAG_LOG, "Código do erro (name): " + name);

                UpdateStickerPackRepo.updateInvalidSticker(instance, stickerPackIdentifier, fileName, name);
            }
        }
}