/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package br.arch.sticker.domain.service.fetch;

import static br.arch.sticker.core.validation.StickerPackValidator.STICKER_SIZE_MIN;
import static br.arch.sticker.domain.data.content.StickerContentProvider.AUTHORITY_URI;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import br.arch.sticker.BuildConfig;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.content.InvalidWebsiteUrlException;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerPackException;
import br.arch.sticker.core.error.throwable.sticker.StickerFileException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackValidatorException;
import br.arch.sticker.core.error.throwable.sticker.StickerValidatorException;
import br.arch.sticker.core.validation.StickerPackValidator;
import br.arch.sticker.core.validation.StickerValidator;
import br.arch.sticker.domain.data.mapper.StickerPackMapper;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.dto.ListStickerPackValidationResult;
import br.arch.sticker.domain.dto.StickerPackValidationResult;
import br.arch.sticker.domain.service.update.UpdateStickerService;
import br.arch.sticker.domain.util.StickerPackPlaceholder;

public class FetchStickerPackService {
    private final static String TAG_LOG = FetchStickerPackService.class.getSimpleName();

    private final StickerPackPlaceholder stickerPackPlaceholder;
    private final StickerPackValidator stickerPackValidator;
    private final UpdateStickerService updateStickerService;
    private final FetchStickerService fetchStickerService;
    private final StickerValidator stickerValidator;
    private final Context context;

    public FetchStickerPackService(Context context) {
        this.context = context.getApplicationContext();
        this.stickerValidator = new StickerValidator(this.context);
        this.fetchStickerService = new FetchStickerService(this.context);
        this.stickerPackValidator = new StickerPackValidator(this.context);
        this.updateStickerService = new UpdateStickerService(this.context);
        this.stickerPackPlaceholder = new StickerPackPlaceholder(this.context);
    }

    @NonNull
    public ListStickerPackValidationResult fetchStickerPackListFromContentProvider() throws FetchStickerPackException {
        final Cursor cursor = context.getContentResolver().query(AUTHORITY_URI, null, null, null, null);
        if (cursor == null) {
            throw new FetchStickerPackException(
                    "Não foi possível buscar no content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY,
                    ErrorCode.ERROR_CONTENT_PROVIDER
            );
        }

        final HashSet<String> stickerPackIdentifierSet = new HashSet<>();
        final ArrayList<StickerPack> allStickerPacks;
        final ArrayList<StickerPack> invalidPacks = new ArrayList<>();
        final HashMap<StickerPack, List<Sticker>> validPacksWithInvalidStickers = new HashMap<>();

        if (cursor.moveToFirst()) {
            allStickerPacks = new ArrayList<>(buildListStickerPack(cursor));
        } else {
            cursor.close();
            throw new FetchStickerPackException("Nenhum pacote de figurinhas encontrado no content provider",
                    ErrorCode.ERROR_CONTENT_PROVIDER
            );
        }

        if (allStickerPacks.isEmpty()) {
            throw new FetchStickerPackException("Deve haver pelo menos um pacote de adesivos no aplicativo",
                    ErrorCode.ERROR_EMPTY_STICKERPACK
            );
        }

        for (StickerPack stickerPack : allStickerPacks) {
            Log.d(TAG_LOG, "ID" + stickerPack.identifier);
            if (!stickerPackIdentifierSet.add(stickerPack.identifier)) {
                throw new StickerPackValidatorException(String.format(
                        "Os identificadores dos pacotes de figurinhas devem ser únicos, há mais de um pacote com identificador: %s",
                        stickerPack.identifier
                ), ErrorCode.DUPLICATE_IDENTIFIER
                );
            }
        }

        allStickerPacks.removeIf(stickerPack -> {
            try {
                stickerPackValidator.verifyStickerPackValidity(stickerPack);
                List<Sticker> invalidStickers = new ArrayList<>();

                stickerPack.getStickers().removeIf(sticker -> {
                    if (!sticker.stickerIsValid.isEmpty()) {

                        invalidStickers.add(sticker);

                        return true;
                    }

                    try {
                        stickerValidator.verifyStickerValidity(stickerPack.identifier, sticker,
                                stickerPack.animatedStickerPack
                        );
                        return false;
                    } catch (StickerFileException | StickerValidatorException exception) {
                        String packId, fileName, errorCodeName;
                        ErrorCode errorCode;

                        if (exception instanceof StickerFileException stickerFileException) {
                            packId = stickerFileException.getStickerPackIdentifier();
                            fileName = stickerFileException.getFileName();
                            errorCode = stickerFileException.getErrorCode();
                            errorCodeName = stickerFileException.getErrorCodeName();
                        } else {
                            StickerValidatorException StickerValidatorException = (StickerValidatorException) exception;
                            packId = StickerValidatorException.getStickerPackIdentifier();
                            fileName = StickerValidatorException.getFileName();
                            errorCode = StickerValidatorException.getErrorCode();
                            errorCodeName = StickerValidatorException.getErrorCodeName();
                        }

                        updateStickerService.updateInvalidSticker(packId, fileName, errorCode);

                        sticker.setStickerIsInvalid(errorCodeName);
                        invalidStickers.add(sticker);
                        return true;
                    }
                });

                if (stickerPack.getStickers().isEmpty()) {
                    invalidPacks.add(stickerPack);
                    return true;
                }

                if (!invalidStickers.isEmpty()) {
                    validPacksWithInvalidStickers.put(stickerPack, invalidStickers);
                    return true;
                }

                return false;
            } catch (StickerPackValidatorException | InvalidWebsiteUrlException appCoreStateException) {
                invalidPacks.add(stickerPack);
                return true;
            }
        });

        return new ListStickerPackValidationResult(allStickerPacks, invalidPacks, validPacksWithInvalidStickers);
    }

    public StickerPackValidationResult fetchStickerPackFromContentProvider(String stickerPackIdentifier)
            throws FetchStickerPackException {
        final Cursor cursor = context.getContentResolver()
                .query(Uri.withAppendedPath(AUTHORITY_URI, stickerPackIdentifier), null, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            throw new FetchStickerPackException(String.format("Não foi possível buscar no content provider, %s",
                    BuildConfig.CONTENT_PROVIDER_AUTHORITY
            ), ErrorCode.ERROR_CONTENT_PROVIDER
            );
        }

        final StickerPack stickerPack;
        if (cursor.moveToFirst()) {
            stickerPack = buildStickerPackWithStickers(cursor);
        } else {
            cursor.close();
            throw new FetchStickerPackException("Nenhum pacote de figurinhas encontrado no content provider",
                    ErrorCode.ERROR_EMPTY_STICKERPACK
            );
        }

        List<Sticker> invalidStickers = new ArrayList<>();

        try {
            stickerPackValidator.verifyStickerPackValidity(stickerPack);

            stickerPack.getStickers().removeIf(sticker -> {
                if (!sticker.stickerIsValid.isEmpty()) {
                    invalidStickers.add(sticker);
                    return true;
                }

                try {
                    stickerValidator.verifyStickerValidity(stickerPack.identifier, sticker,
                            stickerPack.animatedStickerPack
                    );
                    return false;
                } catch (StickerFileException | StickerValidatorException exception) {
                    invalidStickers.add(sticker);

                    if (exception instanceof StickerFileException stickerFileException) {
                        boolean updated = updateStickerService.updateInvalidSticker(
                                stickerFileException.getStickerPackIdentifier(), stickerFileException.getFileName(),
                                stickerFileException.getErrorCode()
                        );
                    }

                    if (exception instanceof StickerValidatorException stickerValidatorException) {
                        boolean updated = updateStickerService.updateInvalidSticker(
                                stickerValidatorException.getStickerPackIdentifier(),
                                stickerValidatorException.getFileName(), stickerValidatorException.getErrorCode()
                        );
                    }

                    return true;
                }
            });

            if (stickerPack.getStickers().isEmpty()) {
                throw new FetchStickerPackException(
                        "Pacote de figurinhas inválido: não restaram stickers após a validação.",
                        ErrorCode.ERROR_EMPTY_STICKERPACK, new Object[]{stickerPack}
                );
            }

            return new StickerPackValidationResult(stickerPack, invalidStickers);
        } catch (StickerPackValidatorException | InvalidWebsiteUrlException exception) {
            throw new FetchStickerPackException(
                    exception.getMessage() != null ? exception.getMessage() : "Pacote de figurinhas invalido",
                    exception.getCause(), exception.getErrorCode(), new Object[]{stickerPack}
            );
        }
    }

    @NonNull
    private ArrayList<StickerPack> buildListStickerPack(Cursor cursor) {
        ArrayList<StickerPack> stickerPackList = new ArrayList<>();
        cursor.moveToFirst();

        do {
            StickerPack stickerPack = buildStickerPackWithStickers(cursor);
            stickerPackList.add(stickerPack);
        } while (cursor.moveToNext());

        return stickerPackList;
    }

    @NonNull
    private StickerPack buildStickerPackWithStickers(Cursor cursor) {
        StickerPack stickerPack = StickerPackMapper.writeCursorToStickerPack(cursor);

        List<Sticker> stickers = fetchStickerService.fetchListStickerForPack(stickerPack.identifier);
        if (stickers.size() < STICKER_SIZE_MIN) {
            Sticker placeholderSticker = stickerPackPlaceholder.makeAndSaveStickerPlaceholder(stickerPack);
            if (placeholderSticker == null) {
                throw new StickerPackSaveException("Não foi possivel criar placeholder.", ErrorCode.ERROR_PACK_SAVE_DB);
            }

            stickers.add(placeholderSticker);
        }

        stickerPack.setStickers(stickers);

        return stickerPack;
    }
}
