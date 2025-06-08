/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.stickerpack.creation.viewmodel;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vinicius.sticker.view.feature.stickerpack.creation.fragment.PermissionSettingsFragment;

public class PermissionSettingsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> permissionGranted = new MutableLiveData<>();
    private final MutableLiveData<Boolean> permissionDenied = new MutableLiveData<>();

    public LiveData<Boolean> getPermissionGranted() {
        return permissionGranted;
    }

    public LiveData<Boolean> getPermissionDenied() {
        return permissionDenied;
    }

    public void setPermissionGranted() {
        permissionGranted.setValue(true);
    }

    public void setPermissionDenied() {
        permissionDenied.setValue(true);
    }

    public static void launchPermissionSettings(FragmentActivity activity) {
        PermissionSettingsFragment permissionSettingsFragment = new PermissionSettingsFragment();
        permissionSettingsFragment.show(activity.getSupportFragmentManager(), PermissionSettingsFragment.class.getSimpleName());
    }
}
