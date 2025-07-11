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
import br.arch.sticker.view.core.usecase.component.AlertInputStickerDialog;
import br.arch.sticker.view.core.usecase.component.AlertStickerDialog;
import br.arch.sticker.view.feature.preview.viewmodel.PreviewInvalidStickerPackViewModel;

public class InvalidStickerPackDialogController {
    private final PreviewInvalidStickerPackViewModel viewModel;

    private final AlertStickerDialog alertStickerDialog;
    private final AlertInputStickerDialog alertInputStickerDialog;

    public InvalidStickerPackDialogController(Context context, PreviewInvalidStickerPackViewModel viewModel) {
        this.viewModel = viewModel;
        alertStickerDialog = new AlertStickerDialog(context);
        this.alertInputStickerDialog = new AlertInputStickerDialog(context);
    }

    private void resetDialogs() {
        alertStickerDialog.setVisibilityIgnoreButton(View.GONE);
        alertStickerDialog.setVisibilityFixButton(View.GONE);
        alertStickerDialog.setOnIgnoreClick(null);
        alertStickerDialog.setOnFixClick(null);

        alertInputStickerDialog.setVisibilityIgnoreButton(View.GONE);
        alertInputStickerDialog.setVisibilityFixButton(View.GONE);
        alertInputStickerDialog.setOnFixClick(null);
        alertInputStickerDialog.setTextInput(null);
    }

    public void showFixAction(PreviewInvalidStickerPackViewModel.FixActionStickerPack action) {
        resetDialogs();

        Context alertStickerContext = alertStickerDialog.getContext();
        Context alertInputStickerContext = alertInputStickerDialog.getContext();

        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.Delete delete) {
            alertStickerDialog.setTitleText(alertStickerContext.getString(R.string.dialog_title_invalid_pack));
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
            alertStickerDialog.setTitleText(alertStickerContext.getString(R.string.dialog_title_invalid_thumbnail));
            alertStickerDialog.setMessageText(alertStickerContext.getString(R.string.dialog_message_create_thumbnail));
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
            alertInputStickerDialog.setTitleText(alertInputStickerContext.getString(R.string.dialog_title_invalid_name));
            alertInputStickerDialog.setMessageText(alertInputStickerContext.getString(R.string.dialog_message_insert_new_name));
            alertInputStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertInputStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertInputStickerDialog.setTextInput(alertInputStickerContext.getString(R.string.dialog_rename));
            alertInputStickerDialog.setTextFixButton(alertInputStickerContext.getString(R.string.dialog_rename));
            alertInputStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(renameStickerPack);
                alertInputStickerDialog.dismiss();
            });

            alertInputStickerDialog.show();
        }

        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.ResizeStickerPack resizeStickerPack) {
            alertStickerDialog.setTitleText(alertStickerContext.getString(R.string.dialog_title_fix_pack_size));
            alertStickerDialog.setMessageText(alertStickerContext.getString(R.string.dialog_message_remove_extra_stickers));
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
            alertStickerDialog.setTitleText(alertStickerContext.getString(R.string.dialog_title_cleanup_urls));
            alertStickerDialog.setMessageText(alertStickerContext.getString(R.string.dialog_message_cleanup_urls));
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
