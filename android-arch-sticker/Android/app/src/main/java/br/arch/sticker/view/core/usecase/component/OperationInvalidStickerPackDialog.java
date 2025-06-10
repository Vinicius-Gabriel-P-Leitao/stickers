/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.usecase.component;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import br.arch.sticker.R;
import br.arch.sticker.domain.service.delete.DeleteStickerAssetService;
import br.arch.sticker.domain.service.delete.DeleteStickerPackService;
import br.arch.sticker.view.feature.preview.activity.PreviewStickerInvalidActivity;

public class OperationInvalidStickerPackDialog extends DialogFragment {
    private static final String ARG_VALUE = "sticker_pack";
    private OnDialogActionListener listener;

    public OperationInvalidStickerPackDialog() {
    }

    public interface OnDialogActionListener {
        void onReloadRequested();
    }

    public static OperationInvalidStickerPackDialog newInstance(String stickerPackIdentifier) {
        OperationInvalidStickerPackDialog fragment = new OperationInvalidStickerPackDialog();
        Bundle args = new Bundle();
        args.putString(ARG_VALUE, stickerPackIdentifier);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AlterDialogStyle);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_invalid_sticker_pack, null);

        Button buttonFixPack = view.findViewById(R.id.button_fix_pack);
        buttonFixPack.setOnClickListener(fragment -> {
            Intent intent = new Intent(fragment.getContext(), PreviewStickerInvalidActivity.class);
            fragment.getContext().startActivity(intent);
            dismiss();
        });

        Button buttonDeletePack = view.findViewById(R.id.button_delete_pack);
        buttonDeletePack.setOnClickListener(fragment -> {
            String stickerPackIdentifier = null;

            if (getArguments() != null) {
                stickerPackIdentifier = getArguments().getString(ARG_VALUE);
            }

            if (stickerPackIdentifier != null) {
                DeleteStickerPackService.deleteStickerPack(requireActivity(), stickerPackIdentifier);
                DeleteStickerAssetService.deleteAllStickerAssetsInPack(requireActivity(), stickerPackIdentifier);

                listener.onReloadRequested();
                dismiss();
            }

            dismiss();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            AlertDialog dialog = (AlertDialog) dialogInterface;
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        return alertDialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDialogActionListener) {
            listener = (OnDialogActionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " deve implementar OnDialogActionListener");
        }
    }
}
