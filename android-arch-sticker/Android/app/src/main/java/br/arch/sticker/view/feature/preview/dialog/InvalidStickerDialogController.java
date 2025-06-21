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
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.view.core.usecase.component.InvalidStickersDialog;
import br.arch.sticker.view.feature.preview.viewmodel.PreviewInvalidStickerViewModel;

public class InvalidStickerDialogController {
    private final InvalidStickersDialog dialog;
    private final PreviewInvalidStickerViewModel viewModel;

    public InvalidStickerDialogController(Context context, PreviewInvalidStickerViewModel viewModel)
        {
            dialog = new InvalidStickersDialog(context);
            this.viewModel = viewModel;
        }

    private void resetDialog()
        {
            dialog.setVisibilityFixButton(View.GONE);
            dialog.setVisibilityIgnoreButton(View.GONE);
            dialog.setOnFixClick(null);
            dialog.setOnIgnoreClick(null);
        }

    public void showFixAction(PreviewInvalidStickerViewModel.FixActionSticker action)
        {
            resetDialog();
            if (action instanceof PreviewInvalidStickerViewModel.FixActionSticker.Delete delete) {
                Sticker sticker = delete.sticker();
                String stickerPackIdentifier = delete.stickerPackIdentifier();
                int resourceString = delete.codeProvider().getMessageResId();

                dialog.setTitleText(dialog.getContext().getString(R.string.dialog_delete));
                dialog.setMessageText(dialog.getContext().getString(resourceString));
                dialog.setVisibilityFixButton(View.VISIBLE);
                dialog.setVisibilityIgnoreButton(View.VISIBLE);

                dialog.setTextFixButton(dialog.getContext().getString(R.string.dialog_delete));
                dialog.setOnFixClick(view -> {
                    viewModel.onFixActionConfirmed(action, dialog.getContext(), sticker, stickerPackIdentifier);
                    dialog.dismiss();
                });

                dialog.setTextIgnoreButton(dialog.getContext().getString(R.string.dialog_ignore));
                dialog.setOnIgnoreClick(view -> dialog.dismiss());
            }

            dialog.show();
        }
}
