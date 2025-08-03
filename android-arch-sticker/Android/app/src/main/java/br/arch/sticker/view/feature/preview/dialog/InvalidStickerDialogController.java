/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.dialog;

import static br.arch.sticker.view.feature.preview.viewmodel.PreviewInvalidStickerViewModel.FixActionSticker.*;

import android.content.Context;
import android.view.View;

import br.arch.sticker.R;
import br.arch.sticker.view.core.usecase.component.InputAlertStickerDialog;
import br.arch.sticker.view.core.usecase.component.AlertStickerDialog;
import br.arch.sticker.view.feature.preview.viewmodel.PreviewInvalidStickerViewModel;

public class InvalidStickerDialogController {
    private final static int MAX_QUALITY = 20;

    private final PreviewInvalidStickerViewModel viewModel;

    private final AlertStickerDialog alertStickerDialog;
    private final InputAlertStickerDialog inputAlertStickerDialog;

    public InvalidStickerDialogController(Context context, PreviewInvalidStickerViewModel viewModel) {
        this.viewModel = viewModel;
        this.alertStickerDialog = new AlertStickerDialog(context);
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

    public void showFixAction(PreviewInvalidStickerViewModel.FixActionSticker action) {
        resetDialogs();

        Context alertStickerContext = alertStickerDialog.getContext();
        Context alertInputStickerContext = inputAlertStickerDialog.getContext();

        if (action instanceof Delete delete) {
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

            alertStickerDialog.setTextIgnoreButton(alertStickerContext.getString(R.string.dialog_cancel));
            alertStickerDialog.setOnIgnoreClick(view -> alertStickerDialog.dismiss());

            alertStickerDialog.show();
        }

        if (action instanceof ResizeFile resizeFile) {
            int resourceString = resizeFile.codeProvider().getMessageResId();

            inputAlertStickerDialog.setTitleText(alertInputStickerContext.getString(R.string.dialog_delete));
            inputAlertStickerDialog.setMessageText(alertInputStickerContext.getString(resourceString));
            inputAlertStickerDialog.setVisibilityFixButton(View.VISIBLE);
            inputAlertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            inputAlertStickerDialog.setTextInput(alertInputStickerContext.getString(R.string.input_quality_sticker));
            inputAlertStickerDialog.setTextFixButton(alertInputStickerContext.getString(R.string.dialog_refactor));
            inputAlertStickerDialog.setOnFixClick(view -> {
                String input = inputAlertStickerDialog.getUserInput();

                if (input.isEmpty()) {
                    inputAlertStickerDialog.showError(alertInputStickerContext.getString(R.string.error_quality_empty));
                    return;
                }

                try {
                    int value = Integer.parseInt(input);
                    if (value > MAX_QUALITY) {
                        inputAlertStickerDialog.showError(
                                alertInputStickerContext.getString(R.string.error_invalid_quality_number));
                        return;
                    }

                    ResizeFile newAction = resizeFile.withQuality(value);
                    viewModel.onFixActionConfirmed(newAction);
                    inputAlertStickerDialog.dismiss();
                } catch (NumberFormatException numberFormatException) {
                    inputAlertStickerDialog.showError(
                            alertInputStickerContext.getString(R.string.error_invalid_quality_number));
                } finally {
                    inputAlertStickerDialog.dismiss();
                }
            });

            inputAlertStickerDialog.show();
        }
    }
}
