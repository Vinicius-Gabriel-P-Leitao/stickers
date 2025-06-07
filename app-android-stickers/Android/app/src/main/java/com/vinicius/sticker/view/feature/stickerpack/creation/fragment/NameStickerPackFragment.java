/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.stickerpack.creation.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.vinicius.sticker.R;
import com.vinicius.sticker.view.feature.stickerpack.creation.viewmodel.NameStickerPackViewModel;

public class NameStickerPackFragment extends BottomSheetDialogFragment {
    private NameStickerPackViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(NameStickerPackViewModel.class);

        View view = inflater.inflate(R.layout.dialog_metadata_pack, container, false);

        ImageButton buttonGrantPermission = view.findViewById(R.id.open_gallery);
        buttonGrantPermission.setOnClickListener(viewAccept -> {
            TextInputEditText textInputEditText = view.findViewById(R.id.et_user_input);
            textInputEditText.setFocusable(true);
            textInputEditText.setFocusableInTouchMode(true);

            String msgErrorNamePackEmpty = getResources().getString(R.string.metadata_name_pack_empty);
            if (textInputEditText.getText() == null) {
                viewModel.setErrorNameStickerPack(msgErrorNamePackEmpty);
                dismiss();
                return;
            }

            String inputText = textInputEditText.getText().toString().trim();
            if (inputText.isEmpty()) {
                viewModel.setErrorNameStickerPack(msgErrorNamePackEmpty);
                dismiss();
                return;
            }

            viewModel.setNameStickerPack(inputText);
            dismiss();
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        return dialog;
    }
}
