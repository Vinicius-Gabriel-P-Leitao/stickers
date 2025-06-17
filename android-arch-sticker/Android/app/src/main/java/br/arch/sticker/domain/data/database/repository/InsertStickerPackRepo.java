/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabase.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabase.ANIMATED_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabase.AVOID_CACHE;
import static br.arch.sticker.domain.data.database.StickerDatabase.FK_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabase.FK_STICKER_PACKS;
import static br.arch.sticker.domain.data.database.StickerDatabase.IMAGE_DATA_VERSION;
import static br.arch.sticker.domain.data.database.StickerDatabase.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabase.LICENSE_AGREEMENT_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabase.PRIVACY_POLICY_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabase.PUBLISHER_EMAIL;
import static br.arch.sticker.domain.data.database.StickerDatabase.PUBLISHER_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabase.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabase.STICKER_FILE_EMOJI_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabase.STICKER_FILE_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabase.STICKER_IS_VALID;
import static br.arch.sticker.domain.data.database.StickerDatabase.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabase.STICKER_PACK_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabase.STICKER_PACK_PUBLISHER_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabase.STICKER_PACK_TRAY_IMAGE_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabase.TABLE_STICKER;
import static br.arch.sticker.domain.data.database.StickerDatabase.TABLE_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabase.TABLE_STICKER_PACKS;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.List;

import br.arch.sticker.core.error.code.SaveErrorCode;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerPackException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.dto.StickerPackValidationResult;
import br.arch.sticker.domain.service.fetch.FetchStickerPackService;


// @formatter:off
public class InsertStickerPackRepo {
    private final SQLiteDatabase dbHelper;

    public InsertStickerPackRepo(SQLiteDatabase dbHelper) {
        this.dbHelper = dbHelper;
    }

    @NonNull
    public CallbackResult<StickerPack> insertStickerPack(StickerPack stickerPack) {
        ContentValues stickerPacksValues = new ContentValues();
        stickerPacksValues.put(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.androidPlayStoreLink);
        stickerPacksValues.put(IOS_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.iosAppStoreLink);

        long stickerPackId = dbHelper.insert(TABLE_STICKER_PACKS, null, stickerPacksValues);

        if (stickerPackId != -1) {
            ContentValues stickerPackValues = writeStickerPackToContentValues(stickerPack, stickerPackId);
            long result = dbHelper.insert(TABLE_STICKER_PACK, null, stickerPackValues);

            if (result != -1) {
                for (Sticker sticker : stickerPack.getStickers()) {
                    ContentValues stickerValues = writeStickerToContentValues(sticker);
                    dbHelper.insert(TABLE_STICKER, null, stickerValues);
                }

                return CallbackResult.success(stickerPack);
            } else {
                return CallbackResult.failure(new StickerPackSaveException(
                        "Erro ao inserir pacote.",
                        SaveErrorCode.ERROR_PACK_SAVE_DB));
            }
        } else {
            return CallbackResult.failure(new StickerPackSaveException(
                    "Erro ao inserir detalhe dos pacotes.",
                    SaveErrorCode.ERROR_PACK_SAVE_DB));
        }
    }

    @NonNull
    public CallbackResult<StickerPack> insertSticker(Context context, List<Sticker> stickers, String stickerPackIdentifier) {
            try{
                StickerPackValidationResult stickerPack =
                    FetchStickerPackService.fetchStickerPackFromContentProvider(context, stickerPackIdentifier);

                if (!stickerPack.stickerPack().identifier.isEmpty()) {
                    for (Sticker sticker : stickers) {
                        ContentValues stickerValues = writeStickerToContentValues(sticker);
                        dbHelper.insert(TABLE_STICKER, null, stickerValues);
                    }
                        return CallbackResult.success(stickerPack.stickerPack());
                } else {
                        return CallbackResult.failure(new StickerPackSaveException(
                                "Erro ao inserir pacote, o identificador é vázio", SaveErrorCode.ERROR_PACK_SAVE_DB));
                }
            } catch (FetchStickerPackException exception) {
               return CallbackResult.failure(exception);
            }
    }

    @NonNull
    public static ContentValues writeStickerPackToContentValues(StickerPack stickerPack, long stickerPackId) {
        ContentValues stickerPackValues = new ContentValues();
        stickerPackValues.put(STICKER_PACK_IDENTIFIER_IN_QUERY, stickerPack.identifier);
        stickerPackValues.put(STICKER_PACK_NAME_IN_QUERY, stickerPack.name);
        stickerPackValues.put(STICKER_PACK_PUBLISHER_IN_QUERY, stickerPack.publisher);
        stickerPackValues.put(STICKER_PACK_TRAY_IMAGE_IN_QUERY, stickerPack.trayImageFile);
        stickerPackValues.put(PUBLISHER_EMAIL, stickerPack.publisherEmail);
        stickerPackValues.put(PUBLISHER_WEBSITE, stickerPack.publisherWebsite);
        stickerPackValues.put(PRIVACY_POLICY_WEBSITE, stickerPack.privacyPolicyWebsite);
        stickerPackValues.put(LICENSE_AGREEMENT_WEBSITE, stickerPack.licenseAgreementWebsite);
        stickerPackValues.put(ANIMATED_STICKER_PACK, stickerPack.animatedStickerPack ? 1 : 0);
        stickerPackValues.put(FK_STICKER_PACKS, stickerPackId);
        stickerPackValues.put(IMAGE_DATA_VERSION, stickerPack.imageDataVersion);
        stickerPackValues.put(AVOID_CACHE, stickerPack.avoidCache ? 1 : 0);

        return stickerPackValues;
    }

    @NonNull
    public static ContentValues writeStickerToContentValues(Sticker sticker) {
        ContentValues stickerValues = new ContentValues();
        stickerValues.put(STICKER_FILE_NAME_IN_QUERY, sticker.imageFileName);
        stickerValues.put(STICKER_FILE_EMOJI_IN_QUERY, String.valueOf(sticker.emojis));
        stickerValues.put(STICKER_IS_VALID, sticker.stickerIsValid);
        stickerValues.put(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY, sticker.accessibilityText);
        stickerValues.put(FK_STICKER_PACK, sticker.uuidPack);

        return stickerValues;
    }
}
