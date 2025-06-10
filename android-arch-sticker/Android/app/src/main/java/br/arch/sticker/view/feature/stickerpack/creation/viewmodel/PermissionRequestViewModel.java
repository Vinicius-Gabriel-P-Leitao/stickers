/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.viewmodel;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import br.arch.sticker.view.feature.stickerpack.creation.fragment.PermissionRequestFragment;

public class PermissionRequestViewModel extends ViewModel {
    private final MutableLiveData<Boolean> permissionGranted = new MutableLiveData<>();
    private final MutableLiveData<Boolean> permissionDenied = new MutableLiveData<>();
    private final MutableLiveData<String[]> permissionsToRequest = new MutableLiveData<>();

    public PermissionRequestViewModel() {
        permissionsToRequest.setValue(new String[0]);
    }

    public LiveData<Boolean> getPermissionGranted() {
        return permissionGranted;
    }

    public LiveData<Boolean> getPermissionDenied() {
        return permissionDenied;
    }

    public MutableLiveData<String[]> getPermissionsToRequest() {
        return permissionsToRequest;
    }

    public void setPermissionGranted() {
        permissionGranted.setValue(true);
    }

    public void setPermissionDenied() {
        permissionDenied.setValue(true);
    }

    public void setPermissions(String[] permissions) {
        permissionsToRequest.setValue(permissions);
    }

    public static void launchPermissionRequest(FragmentActivity activity) {
        PermissionRequestFragment permissionRequestFragment = new PermissionRequestFragment();
        permissionRequestFragment.show(activity.getSupportFragmentManager(), PermissionRequestFragment.class.getSimpleName());
    }
}
