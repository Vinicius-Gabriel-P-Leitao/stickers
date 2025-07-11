/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PermissionSettingsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> openSettingsRequested = new MutableLiveData<>();
    private final MutableLiveData<Boolean> permissionDenied = new MutableLiveData<>();

    public LiveData<Boolean> getOpenSettingsRequested()
        {
            return openSettingsRequested;
        }

    public LiveData<Boolean> getPermissionDenied()
        {
            return permissionDenied;
        }

    public void setOpenSettingsRequested()
        {
            openSettingsRequested.setValue(true);
        }

    public void resetOpenSettingsRequested()
        {
            openSettingsRequested.setValue(false);
        }

    public void setPermissionDenied()
        {
            permissionDenied.setValue(true);
        }
}
