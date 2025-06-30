/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.dialog;

import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.view.core.usecase.component.InvalidStickersDialog;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.PermissionRequestViewModel;

public class PermissionRequestDialog {
    private final PermissionRequestViewModel permissionRequestViewModel;

    private final ActivityResultLauncher<String[]> permissionLauncher;
    private final InvalidStickersDialog dialog;
    private final AppCompatActivity activity;

    public PermissionRequestDialog(AppCompatActivity activity, ActivityResultLauncher<String[]> permissionLauncher)
        {
            this.activity = activity;
            this.permissionLauncher = permissionLauncher;
            this.dialog = new InvalidStickersDialog(activity);
            this.permissionRequestViewModel = new ViewModelProvider(activity).get(PermissionRequestViewModel.class);

        }

    public void showPermissionDialog()
        {
            dialog.setTitleText(activity.getString(R.string.dialog_permission_title));
            dialog.setMessageText(activity.getString(R.string.dialog_message_permission));

            dialog.setTextFixButton(activity.getString(R.string.dialog_button_permission_accept));
            dialog.setOnFixClick(view -> requestPermissionsLogic());

            dialog.setTextIgnoreButton(activity.getString(R.string.dialog_button_permission_cancel));
            dialog.setOnIgnoreClick(view -> dialog.dismiss());

            dialog.show();
        }

    private void requestPermissionsLogic()
        {
            String[] permissionsToRequest = permissionRequestViewModel.getPermissionsToRequest().getValue();
            if (permissionsToRequest == null || permissionsToRequest.length == 0) {
                permissionRequestViewModel.setPermissionGranted();
                dialog.dismiss();
                return;
            }

            List<String> permissionsNotGranted = new ArrayList<>();
            for (String permission : permissionsToRequest) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNotGranted.add(permission);
                }
            }

            if (permissionsNotGranted.isEmpty()) {
                permissionRequestViewModel.setPermissionGranted();
                dialog.dismiss();
            } else {
                permissionLauncher.launch(permissionsNotGranted.toArray(new String[0]));
            }
        }

    public void dismiss()
        {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
}
