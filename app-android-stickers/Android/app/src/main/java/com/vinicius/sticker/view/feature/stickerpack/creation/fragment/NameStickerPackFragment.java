/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.stickerpack.creation.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.vinicius.sticker.R;

public class NameStickerPackFragment extends BottomSheetDialogFragment {
    public interface MetadataCallback {
        void onGetMetadata(String namePack);

        void onError(String error);
    }

    public void setCallback(
            MetadataCallback callback
    ) {
        this.callback = callback;
    }

    private MetadataCallback callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_metadata_pack, container, false);

        ImageButton buttonGrantPermission = view.findViewById(R.id.open_gallery);
        buttonGrantPermission.setOnClickListener(viewAccept -> {
            TextInputEditText textInputEditText = view.findViewById(R.id.et_user_input);

            textInputEditText.setFocusable(true);
            textInputEditText.setFocusableInTouchMode(true);

            textInputEditText.post(new Runnable() {
                @Override
                public void run() {
                    textInputEditText.requestFocus();

                    InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.showSoftInput(textInputEditText, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            });

            String msgErrorPackEmpty = getResources().getString(R.string.metadata_name_pack_empty);

            if (textInputEditText.getText() == null) {
                callback.onError(msgErrorPackEmpty);
            }

            String inputText = textInputEditText.getText().toString().trim();
            if (inputText.isEmpty()) {
                callback.onError(msgErrorPackEmpty);
                dismiss();
                return;
            }

            callback.onGetMetadata(inputText);
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
