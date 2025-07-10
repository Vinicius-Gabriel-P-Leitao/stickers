/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.AVOID_CACHE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.FK_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.IMAGE_DATA_VERSION;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PUBLISHER_EMAIL;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PUBLISHER_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_IS_VALID;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_TRAY_IMAGE_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER_PACK;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import androidx.annotation.NonNull;

import br.arch.sticker.core.error.code.SaveErrorCode;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerPackException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;

public class InsertStickerPackRepo {
    private final static String TAG_LOG = InsertStickerPackRepo.class.getSimpleName();

    private final SQLiteDatabase database;

    public InsertStickerPackRepo(SQLiteDatabase database) {
        this.database = database;
    }

    @NonNull
    public static ContentValues writeStickerPackToContentValues(StickerPack stickerPack) {
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
        stickerPackValues.put(IMAGE_DATA_VERSION, stickerPack.imageDataVersion);
        stickerPackValues.put(AVOID_CACHE, stickerPack.avoidCache ? 1 : 0);
        stickerPackValues.put(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.androidPlayStoreLink);
        stickerPackValues.put(IOS_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.iosAppStoreLink);
        stickerPackValues.put(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.androidPlayStoreLink);
        stickerPackValues.put(IOS_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.iosAppStoreLink);

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

    @NonNull
    public CallbackResult<StickerPack> insertStickerPack(StickerPack stickerPack) throws StickerPackSaveException {
        if (stickerPack == null || stickerPack.identifier == null) {
            return CallbackResult.failure(new StickerPackSaveException("Pacote de figurinhas inválido ou identificador nulo.", SaveErrorCode.ERROR_PACK_SAVE_DB));
        }

        try {
            ContentValues stickerPackValues = writeStickerPackToContentValues(stickerPack);
            long result = database.insert(TABLE_STICKER_PACK, null, stickerPackValues);

            if (result != -1) {
                for (Sticker sticker : stickerPack.getStickers()) {
                    ContentValues stickerValues = writeStickerToContentValues(sticker);
                    database.insert(TABLE_STICKER, null, stickerValues);
                }

                return CallbackResult.success(stickerPack);
            } else {
                return CallbackResult.failure(new StickerPackSaveException("Erro ao inserir pacote.", SaveErrorCode.ERROR_PACK_SAVE_DB));
            }
        } catch (SQLiteException sqLiteException) {
            Log.e(TAG_LOG, "Erro de banco ao inserir pacote: " +
                    sqLiteException.getMessage(), sqLiteException);

            return CallbackResult.failure(new StickerPackSaveException("Erro de banco ao inserir pacote.", sqLiteException, SaveErrorCode.ERROR_PACK_SAVE_DB));
        } catch (Exception exception) {
            Log.e(TAG_LOG,
                    "Erro inesperado ao inserir pacote: " + exception.getMessage(), exception);

            return CallbackResult.failure(new StickerPackSaveException("Erro inesperado ao salvar pacote de figurinhas no banco de dados.", exception, SaveErrorCode.ERROR_PACK_SAVE_DB));
        }
    }

    @NonNull
    public CallbackResult<Sticker> insertSticker(Sticker sticker, String stickerPackIdentifier) throws FetchStickerPackException {
        if (sticker == null || sticker.imageFileName == null || stickerPackIdentifier == null ||
                stickerPackIdentifier.isEmpty()) {
            return CallbackResult.failure(new StickerPackSaveException("Dados da figurinha inválidos ou identificador de pacote ausente.", SaveErrorCode.ERROR_PACK_SAVE_DB));
        }

        try {
            ContentValues stickerValues = writeStickerToContentValues(sticker);
            database.insert(TABLE_STICKER, null, stickerValues);

            return CallbackResult.success(sticker);
        } catch (SQLiteException sqLiteException) {
            Log.e(TAG_LOG, "Erro de banco ao inserir figurinha: " +
                    sqLiteException.getMessage(), sqLiteException);

            return CallbackResult.failure(new StickerPackSaveException("Erro no banco ao inserir figurinha.", sqLiteException, SaveErrorCode.ERROR_PACK_SAVE_DB));
        } catch (Exception exception) {
            Log.e(TAG_LOG,
                    "Erro inesperado ao inserir figurinha: " + exception.getMessage(), exception);

            return CallbackResult.failure(new StickerPackSaveException("Erro inesperado ao salvar figurinha no banco de dados.", exception, SaveErrorCode.ERROR_PACK_SAVE_DB));
        }
    }
}
