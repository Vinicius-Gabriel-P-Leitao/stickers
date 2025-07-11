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
import br.arch.sticker.view.feature.preview.viewmodel.PreviewInvalidStickerViewModel;

public class InvalidStickerDialogController {
    private final static int MAX_QUALITY = 20;

    private final PreviewInvalidStickerViewModel viewModel;

    private final AlertStickerDialog alertStickerDialog;
    private final AlertInputStickerDialog alertInputStickerDialog;

    public InvalidStickerDialogController(Context context, PreviewInvalidStickerViewModel viewModel) {
        this.viewModel = viewModel;
        this.alertStickerDialog = new AlertStickerDialog(context);
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

    public void showFixAction(PreviewInvalidStickerViewModel.FixActionSticker action) {
        resetDialogs();

        Context alertStickerContext = alertStickerDialog.getContext();
        Context alertInputStickerContext = alertInputStickerDialog.getContext();

        if (action instanceof PreviewInvalidStickerViewModel.FixActionSticker.Delete delete) {
            int resourceString = delete.codeProvider().getMessageResId();

            alertStickerDialog.setTitleText(alertStickerContext.getString(R.string.dialog_delete));
            alertStickerDialog.setMessageText(alertStickerContext.getString(resourceString));
            alertStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertStickerDialog.setTextFixButton(alertStickerContext.getString(R.string.dialog_delete));
            alertStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(delete);
                alertStickerDialog.dismiss();
            });

            alertStickerDialog.setTextIgnoreButton(alertStickerContext.getString(R.string.dialog_ignore));
            alertStickerDialog.setOnIgnoreClick(view -> alertStickerDialog.dismiss());

            alertStickerDialog.show();
        }

        if (action instanceof PreviewInvalidStickerViewModel.FixActionSticker.ResizeFile resizeFile) {
            int resourceString = resizeFile.codeProvider().getMessageResId();

            alertInputStickerDialog.setTitleText(alertInputStickerContext.getString(R.string.dialog_delete));
            alertInputStickerDialog.setMessageText(alertInputStickerContext.getString(resourceString));
            alertInputStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertInputStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertInputStickerDialog.setTextInput(alertInputStickerContext.getString(R.string.input_refactor_quality_sticker));
            alertInputStickerDialog.setTextFixButton(alertInputStickerContext.getString(R.string.dialog_refactor));
            alertInputStickerDialog.setOnFixClick(view -> {
                String input = alertInputStickerDialog.getUserInput();

                if (input.isEmpty()) {
                    alertInputStickerDialog.showError(alertInputStickerContext.getString(R.string.error_message_quality_sticker_empty));
                    return;
                }

                try {
                    int value = Integer.parseInt(input);
                    if (value > MAX_QUALITY) {
                        alertInputStickerDialog.showError(alertInputStickerContext.getString(R.string.error_message_quality_sticker_length));
                        return;
                    }

                    PreviewInvalidStickerViewModel.FixActionSticker.ResizeFile newAction = resizeFile.withQuality(value);
                    viewModel.onFixActionConfirmed(newAction);
                    alertInputStickerDialog.dismiss();
                } catch (NumberFormatException numberFormatException) {
                    alertInputStickerDialog.showError(alertInputStickerContext.getString(R.string.error_message_quality_invalid_number));
                }
            });

            alertInputStickerDialog.show();
        }
    }
}
