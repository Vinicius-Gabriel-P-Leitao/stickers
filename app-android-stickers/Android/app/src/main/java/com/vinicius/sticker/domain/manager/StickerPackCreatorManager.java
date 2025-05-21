/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

package com.vinicius.sticker.domain.manager;

import static com.vinicius.sticker.domain.data.database.repository.SelectStickerPacks.getStickerPackIdentifier;
import static com.vinicius.sticker.domain.data.database.repository.SelectStickerPacks.identifierPackIsPresent;
import static com.vinicius.sticker.domain.builder.StickerPackContentJsonParserBuilder.readStickerPack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.JsonReader;
import android.util.Log;

import com.vinicius.sticker.core.exception.StickerPackSaveException;
import com.vinicius.sticker.domain.builder.StickerPackContentJsonBuilder;
import com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.pattern.CallbackResult;
import com.vinicius.sticker.domain.service.save.StickerPackSaveService;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StickerPackCreatorManager {

    private static final List<Sticker> stickers = new ArrayList<>();
    private final static String uuidPack = UUID.randomUUID().toString();

    @FunctionalInterface
    public interface JsonValidateCallback {
        void onJsonValidateDataComplete(String contentJson);
    }

    @FunctionalInterface
    public interface SavedStickerPackCallback {
        void onSavedStickerPack(CallbackResult callbackResult);
    }

    public static void generateJsonPack(Context context, boolean isAnimatedPack, List<File> fileList, String namePack, JsonValidateCallback jsonValidateCallback, SavedStickerPackCallback savedStickerPackCallback) {
        try {
            stickers.clear();
            StickerPackContentJsonBuilder builder = new StickerPackContentJsonBuilder();

            builder.setIdentifier(uuidPack).setName(namePack.trim()).setPublisher("vinicius").setTrayImageFile("thumbnail.jpg").setImageDataVersion("1").setAvoidCache(false).setPublisherWebsite("").setPublisherEmail("").setPrivacyPolicyWebsite("").setLicenseAgreementWebsite("").setAnimatedStickerPack(isAnimatedPack);

            for (File file : fileList) {
                boolean exists = false;
                for (Sticker sticker : stickers) {
                    if (sticker.imageFileName.equals(file.getName())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    stickers.add(new Sticker(file.getName().trim(), List.of("\uD83D\uDDFF"), "Sticker pack"));
                }
            }

            for (Sticker sticker : stickers) {
                builder.addSticker(sticker.imageFileName, sticker.emojis, sticker.accessibilityText);
            }

            String contentJson = builder.build();
            try (JsonReader jsonReader = new JsonReader(new StringReader(contentJson))) {
                StickerPack stickerPack = readStickerPack(jsonReader);

                if (jsonValidateCallback != null) {
                    jsonValidateCallback.onJsonValidateDataComplete(contentJson);
                }

                StickerDatabaseHelper dbHelper = StickerDatabaseHelper.getInstance(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String isPresent = null;
                if (identifierPackIsPresent(db, stickerPack.identifier)) {
                    isPresent = getStickerPackIdentifier(db, stickerPack.identifier);
                }

                StickerPackSaveService.generateStructureForSavePack(context, stickerPack, isPresent, callbackResult -> {
                    switch (callbackResult.getStatus()) {
                        case SUCCESS:
                            if (savedStickerPackCallback != null) {
                                savedStickerPackCallback.onSavedStickerPack(CallbackResult.success(callbackResult.getData()));
                            } else {
                                Log.d("SaveStickerPack", "Callback não foi retornada corretamente!");
                            }
                            break;
                        case WARNING:
                            Log.w("SaveStickerPack", callbackResult.getWarningMessage());
                            break;
                        case FAILURE:
                            if (callbackResult.getError() instanceof StickerPackSaveException exception) {
                                Log.e("SaveStickerPack", Objects.requireNonNull(callbackResult.getError().getMessage()));
                                savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(exception));
                            } else {
                                savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(new StickerPackSaveException("Erro interno desconhecido!")));
                            }
                            break;
                    }
                });
            }
        } catch (JSONException | IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
