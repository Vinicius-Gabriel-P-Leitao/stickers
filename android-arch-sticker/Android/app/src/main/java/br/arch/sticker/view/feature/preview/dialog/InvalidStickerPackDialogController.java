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
            alertStickerDialog.setTitleText("Pacote invalido!");
            alertStickerDialog.setMessageText("Deletar pacote.");
            alertStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertStickerDialog.setTextFixButton("Deletar");
            alertStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(delete);
                alertStickerDialog.dismiss();
            });

            alertStickerDialog.setTextIgnoreButton("Cancelar");
            alertStickerDialog.setOnIgnoreClick(view -> alertStickerDialog.dismiss());

            alertStickerDialog.show();
        }

        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.NewThumbnail newThumbnail) {
            alertStickerDialog.setTitleText("Thumbnail invalida!");
            alertStickerDialog.setMessageText("Criar nova thumbnail para o pacote.");
            alertStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertStickerDialog.setTextFixButton("Gerar nova");
            alertStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(newThumbnail);
                alertStickerDialog.dismiss();
            });

            alertStickerDialog.setTextIgnoreButton("Cancelar");
            alertStickerDialog.setOnIgnoreClick(view -> alertStickerDialog.dismiss());

            alertStickerDialog.show();
        }

        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.RenameStickerPack renameStickerPack) {
            alertInputStickerDialog.setTitleText("Nome inválido");
            alertInputStickerDialog.setMessageText("Deseja inserir um novo nome?");
            alertInputStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertInputStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertInputStickerDialog.setTextInput("Renomear");
            alertInputStickerDialog.setTextFixButton("Renomear");
            alertInputStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(renameStickerPack);
                alertInputStickerDialog.dismiss();
            });

            alertInputStickerDialog.show();
        }


        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.ResizeStickerPack resizeStickerPack) {
            alertStickerDialog.setTitleText("Corrigir tamanho do pacote!");
            alertStickerDialog.setMessageText("Apagar figurinhas sobressalentes.");
            alertStickerDialog.setVisibilityFixButton(View.VISIBLE);
            alertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

            alertStickerDialog.setTextFixButton("Corrigir");
            alertStickerDialog.setOnFixClick(view -> {
                viewModel.onFixActionConfirmed(resizeStickerPack);
                alertStickerDialog.dismiss();
            });

            alertStickerDialog.setTextIgnoreButton("Cancelar");
            alertStickerDialog.setOnIgnoreClick(view -> alertStickerDialog.dismiss());

            alertStickerDialog.show();
        }


        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.CleanUpUrl cleanUpUrl) {

        }


        if (action instanceof PreviewInvalidStickerPackViewModel.FixActionStickerPack.RefactorUrl refactorUrl) {

        }
    }
}
