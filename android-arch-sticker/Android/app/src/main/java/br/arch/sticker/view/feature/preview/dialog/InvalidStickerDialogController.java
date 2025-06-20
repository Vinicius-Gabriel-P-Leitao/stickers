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

    public void showFixAction(PreviewInvalidStickerViewModel.FixAction action)
        {
            resetDialog();
            if (action instanceof PreviewInvalidStickerViewModel.FixAction.Rename rename) {
                dialog.setTitleText("Nome inválido");
                dialog.setMessageText("Deseja inserir um novo nome?");
                dialog.setVisibilityFixButton(View.VISIBLE);
                dialog.setTextFixButton("Renomear");
                dialog.setVisibilityIgnoreButton(View.VISIBLE);
                dialog.setTextIgnoreButton("Cancelar");

                dialog.setOnFixClick(view -> {
                    dialog.dismiss();
                });
                dialog.setOnIgnoreClick(view -> dialog.dismiss());

            } else if (action instanceof PreviewInvalidStickerViewModel.FixAction.Delete delete) {
                dialog.setTitleText("Pacote vazio");
                dialog.setMessageText("Deseja deletar o pacote?");
                dialog.setVisibilityFixButton(View.VISIBLE);
                dialog.setTextFixButton("Deletar");
                dialog.setVisibilityIgnoreButton(View.VISIBLE);
                dialog.setTextIgnoreButton("Cancelar");

                dialog.setOnFixClick(view -> {
                    dialog.dismiss();
                });

                dialog.setOnIgnoreClick(view -> dialog.dismiss());

            } else if (action instanceof PreviewInvalidStickerViewModel.FixAction.Generic generic) {
                dialog.setTitleText("Erro");
                dialog.setMessageText(generic.message());
                dialog.setVisibilityFixButton(View.GONE);
                dialog.setVisibilityIgnoreButton(View.VISIBLE);
                dialog.setTextIgnoreButton("Fechar");

                dialog.setOnIgnoreClick(v -> dialog.dismiss());
            }

            dialog.show();
        }
}
