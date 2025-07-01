/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.list.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.service.delete.DeleteStickerPackPathService;
import br.arch.sticker.domain.service.delete.DeleteStickerPackService;

public class StickerPackListViewModel extends AndroidViewModel {
    private final DeleteStickerPackPathService deleteStickerPackPathService;
    private final DeleteStickerPackService deleteStickerPackService;

    private final MutableLiveData<Pair<Boolean, String>> deletedStickerPack = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public StickerPackListViewModel(@NonNull Application application)
        {
            super(application);
            Context context = getApplication().getApplicationContext();
            this.deleteStickerPackService = new DeleteStickerPackService(context);
            this.deleteStickerPackPathService = new DeleteStickerPackPathService(context);
        }

    public MutableLiveData<Pair<Boolean, String>> getDeletedStickerPack()
        {
            return deletedStickerPack;
        }

    public MutableLiveData<String> getErrorMessageLiveData()
        {
            return errorMessageLiveData;
        }

    public void startDeleted(String stickerPackIdentifier)
        {
            new Thread(() -> {
                CallbackResult<Boolean> resultAsset = deleteStickerPackService.deleteStickerPack(stickerPackIdentifier);
                if (resultAsset.isFailure()) {
                    errorMessageLiveData.postValue(resultAsset.getError().getMessage());
                    return;
                }

                if (resultAsset.isWarning()) {
                    errorMessageLiveData.postValue(resultAsset.getWarningMessage());
                    return;
                }

                CallbackResult<Boolean> resultDB = deleteStickerPackPathService.deleteStickerPackPath(stickerPackIdentifier);
                if (resultDB.isFailure()) {
                    errorMessageLiveData.postValue(resultDB.getError().getMessage());
                    return;
                }

                if (resultDB.isWarning()) {
                    errorMessageLiveData.postValue(resultDB.getWarningMessage());
                    return;
                }

                deletedStickerPack.postValue(new Pair<>((resultDB.getData() && resultAsset.getData()), stickerPackIdentifier));
            }).start();
        }
}
