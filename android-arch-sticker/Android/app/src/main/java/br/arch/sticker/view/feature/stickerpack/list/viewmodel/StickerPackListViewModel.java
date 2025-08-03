/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.list.viewmodel;

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.arch.sticker.R;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.service.delete.DeleteStickerPackPathService;
import br.arch.sticker.domain.service.delete.DeleteStickerPackService;
import br.arch.sticker.domain.service.update.UpdateStickerPackService;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class StickerPackListViewModel extends AndroidViewModel {
    private static final String TAG_LOG = StickerPackListViewModel.class.getSimpleName();

    private final DeleteStickerPackPathService deleteStickerPackPathService;
    private final DeleteStickerPackService deleteStickerPackService;
    private final UpdateStickerPackService updateStickerPackService;
    private final ApplicationTranslate applicationTranslate;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Pair<Boolean, String>> deletedStickerPack = new MutableLiveData<>();
    private final MutableLiveData<Pair<String, String>> updatedStickerPackName = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public StickerPackListViewModel(@NonNull Application application) {
        super(application);
        Context context = getApplication().getApplicationContext();
        this.updateStickerPackService = new UpdateStickerPackService(context);
        this.deleteStickerPackService = new DeleteStickerPackService(context);
        this.deleteStickerPackPathService = new DeleteStickerPackPathService(context);
        this.applicationTranslate = new ApplicationTranslate(context.getResources());
    }

    public MutableLiveData<Pair<Boolean, String>> getDeletedStickerPack() {
        return deletedStickerPack;
    }

    public MutableLiveData<Pair<String, String>> getUpdatedStickerPackName() {
        return updatedStickerPackName;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void startDeleted(String stickerPackIdentifier) {
        executor.submit(() -> {
            CallbackResult<Boolean> resultAsset = deleteStickerPackService.deleteStickerPack(stickerPackIdentifier);
            if (resultAsset.isFailure()) {
                errorMessageLiveData.postValue(resultAsset.getError().getMessage());
                return;
            }

            if (resultAsset.isWarning()) {
                errorMessageLiveData.postValue(resultAsset.getWarningMessage());
                return;
            }

            CallbackResult<Boolean> resultDB = deleteStickerPackPathService.deleteStickerPackPath(
                    stickerPackIdentifier);
            if (resultDB.isFailure()) {
                errorMessageLiveData.postValue(resultDB.getError().getMessage());
                return;
            }

            if (resultDB.isWarning()) {
                errorMessageLiveData.postValue(resultDB.getWarningMessage());
                return;
            }

            deletedStickerPack.postValue(
                    new Pair<>((resultDB.getData() && resultAsset.getData()), stickerPackIdentifier));
        });
    }

    public void startRename(String stickerPackIdentifier, String newName) {
        executor.submit(() -> {
            if (updateStickerPackService.updateStickerFileName(stickerPackIdentifier, newName)) {
                updatedStickerPackName.postValue(new Pair<>(stickerPackIdentifier, newName));
                return;
            }

            errorMessageLiveData.postValue(
                    applicationTranslate.translate(R.string.error_update_sticker_pack_name).log(TAG_LOG, Level.ERROR)
                            .get());
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}
