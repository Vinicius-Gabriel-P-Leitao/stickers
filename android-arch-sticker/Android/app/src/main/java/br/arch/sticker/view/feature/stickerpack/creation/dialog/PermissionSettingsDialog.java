/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.dialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import br.arch.sticker.R;
import br.arch.sticker.view.core.usecase.component.AlertStickerDialog;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.PermissionSettingsViewModel;

public class PermissionSettingsDialog {
    private final PermissionSettingsViewModel permissionSettingsViewModel;
    private final AlertStickerDialog dialog;
    private final AppCompatActivity activity;

    public PermissionSettingsDialog(AppCompatActivity activity)
        {
            this.activity = activity;
            this.dialog = new AlertStickerDialog(activity);
            this.permissionSettingsViewModel = new ViewModelProvider(activity).get(PermissionSettingsViewModel.class);

        }

    public void showSettingsDialog()
        {
            dialog.setTitleText(activity.getString(R.string.dialog_settings_permission_title));
            dialog.setMessageText(activity.getString(R.string.dialog_settings_message_permission));

            dialog.setTextFixButton(activity.getString(R.string.dialog_settings_button_permission_accept));
            dialog.setOnFixClick(view -> {
                permissionSettingsViewModel.setOpenSettingsRequested();
                dialog.dismiss();
            });

            dialog.setTextIgnoreButton(activity.getString(R.string.dialog_settings_button_permission_cancel));
            dialog.setOnIgnoreClick(view -> {
                permissionSettingsViewModel.setPermissionDenied();
                dialog.dismiss();
            });

            dialog.show();
        }

    public void dismiss()
        {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
}
