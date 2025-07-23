/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.dialog;

import android.content.Context;
import android.view.View;

import br.arch.sticker.R;
import br.arch.sticker.view.core.usecase.component.InputAlertStickerDialog;
import br.arch.sticker.view.core.usecase.component.AlertStickerDialog;
import br.arch.sticker.view.feature.preview.viewmodel.PreviewInvalidStickerPackViewModel;

public class InvalidStickerPackDialogController {
    private final PreviewInvalidStickerPackViewModel viewModel;

    private final AlertStickerDialog alertStickerDialog;
    private final InputAlertStickerDialog inputAlertStickerDialog;

    public InvalidStickerPackDialogController(Context context, PreviewInvalidStickerPackViewModel viewModel) {
        this.viewModel = viewModel;
        alertStickerDialog = new AlertStickerDialog(context);
        this.inputAlertStickerDialog = new InputAlertStickerDialog(context);
    }

    private void resetDialogs() {
        alertStickerDialog.setVisibilityIgnoreButton(View.GONE);
        alertStickerDialog.setVisibilityFixButton(View.GONE);
        alertStickerDialog.setOnIgnoreClick(null);
        alertStickerDialog.setOnFixClick(null);

        inputAlertStickerDialog.setVisibilityIgnoreButton(View.GONE);
        inputAlertStickerDialog.setVisibilityFixButton(View.GONE);
        inputAlertStickerDialog.setOnFixClick(null);
        inputAlertStickerDialog.setTextInput(null);
    }

    public void showFixAction(PreviewInvalidStickerPackViewModel.FixActionStickerPack action) {
        resetDialogs();

        Context alertStickerContext = alertStickerDialog.getContext();
        Context alertInputStickerContext = inputAlertStickerDialog.getContext();

        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.Delete delete) {
            alertStickerDialog.setTitleText(alertStickerContext.getString(R.string.error_invalid_pack));
            alertStickerDialog.setMessageText(alertStickerContext.getString(R.string.dialog_message_delete_pack));
            alertStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertStickerDialog.setTextFixButton(alertStickerContext.getString(R.string.dialog_delete));
            alertStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(delete);
                alertStickerDialog.dismiss();
            });

            alertStickerDialog.setTextIgnoreButton(alertStickerContext.getString(R.string.dialog_cancel));
            alertStickerDialog.setOnIgnoreClick(view -> alertStickerDialog.dismiss());

            alertStickerDialog.show();
        }

        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.NewThumbnail newThumbnail) {
            alertStickerDialog.setTitleText(alertStickerContext.getString(R.string.error_invalid_thumbnail));
            alertStickerDialog.setMessageText(alertStickerContext.getString(R.string.dialog_create_thumbnail_message));
            alertStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertStickerDialog.setTextFixButton(alertStickerContext.getString(R.string.dialog_refactor));
            alertStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(newThumbnail);
                alertStickerDialog.dismiss();
            });

            alertStickerDialog.setTextIgnoreButton(alertStickerContext.getString(R.string.dialog_cancel));
            alertStickerDialog.setOnIgnoreClick(view -> alertStickerDialog.dismiss());

            alertStickerDialog.show();
        }

        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.RenameStickerPack renameStickerPack) {
            inputAlertStickerDialog.setTitleText(alertInputStickerContext.getString(R.string.error_invalid_pack_name));
            inputAlertStickerDialog.setMessageText(alertInputStickerContext.getString(R.string.dialog_insert_new_name_message));
            inputAlertStickerDialog.setVisibilityFixButton(View.VISIBLE);
            inputAlertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            inputAlertStickerDialog.setTextInput(alertInputStickerContext.getString(R.string.dialog_rename));
            inputAlertStickerDialog.setTextFixButton(alertInputStickerContext.getString(R.string.dialog_rename));
            inputAlertStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(renameStickerPack);
                inputAlertStickerDialog.dismiss();
            });

            inputAlertStickerDialog.show();
        }

        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.ResizeStickerPack resizeStickerPack) {
            alertStickerDialog.setTitleText(alertStickerContext.getString(R.string.dialog_fix_stickerpack));
            alertStickerDialog.setMessageText(alertStickerContext.getString(R.string.dialog_remove_extra_stickers_message));
            alertStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertStickerDialog.setTextFixButton(alertStickerContext.getString(R.string.dialog_refactor));
            alertStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(resizeStickerPack);
                alertStickerDialog.dismiss();
            });

            alertStickerDialog.setTextIgnoreButton(alertStickerContext.getString(R.string.dialog_cancel));
            alertStickerDialog.setOnIgnoreClick(view -> alertStickerDialog.dismiss());

            alertStickerDialog.show();
        }

        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.CleanUpUrl cleanUpUrl) {
            alertStickerDialog.setTitleText(alertStickerContext.getString(R.string.dialog_cleanup_urls_title));
            alertStickerDialog.setMessageText(alertStickerContext.getString(R.string.dialog_cleanup_urls_message));
            alertStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertStickerDialog.setTextFixButton(alertStickerContext.getString(R.string.dialog_refactor));
            alertStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(cleanUpUrl);
                alertStickerDialog.dismiss();
            });

            alertStickerDialog.setTextIgnoreButton(alertStickerContext.getString(R.string.dialog_cancel));
            alertStickerDialog.setOnIgnoreClick(view -> alertStickerDialog.dismiss());

            alertStickerDialog.show();
        }
    }
}
