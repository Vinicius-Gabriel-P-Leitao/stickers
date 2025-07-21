/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.fragment;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.CHAR_NAME_COUNT_MAX;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import br.arch.sticker.R;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.NameStickerPackViewModel;

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

        View view = inflater.inflate(R.layout.dialog_metadata_stickerpack, container, false);

        ImageButton buttonGrantPermission = view.findViewById(R.id.open_gallery);
        TextInputEditText textInputEditText = view.findViewById(R.id.et_user_input);
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() > CHAR_NAME_COUNT_MAX) {
                    textInputEditText.setError(
                            getString(R.string.input_name_cannot_exceed_stickerpack_size));
                } else {
                    textInputEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        buttonGrantPermission.setOnClickListener(viewAccept -> {
            textInputEditText.setFocusable(true);
            textInputEditText.setFocusableInTouchMode(true);

            String msgErrorNamePackEmpty = getResources().getString(R.string.error_empty_pack_name);
            if (textInputEditText.getText() == null) {
                textInputEditText.setError(msgErrorNamePackEmpty);
                return;
            }

            String inputText = textInputEditText.getText().toString().trim();

            if (inputText.isEmpty()) {
                textInputEditText.setError(msgErrorNamePackEmpty);
                return;
            }

            if (inputText.length() > CHAR_NAME_COUNT_MAX) {
                textInputEditText.setError(
                        getString(R.string.error_name_length_exceeded));
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
