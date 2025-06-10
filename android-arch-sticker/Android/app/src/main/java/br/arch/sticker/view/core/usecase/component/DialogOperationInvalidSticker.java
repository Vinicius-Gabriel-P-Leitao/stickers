/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.usecase.component;

import android.app.Dialog;
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

import java.util.ArrayList;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.view.core.usecase.definition.StickerPackHandler;
import br.arch.sticker.view.feature.preview.activity.PreviewStickerInvalidActivity;

public class DialogOperationInvalidSticker extends DialogFragment {
    private static final String STICKER_PACK_IDENTIFIER = "sticker_pack_identifier";
    private static final String STICKER_PACK_NAME = "sticker_pack_name";
    private static final String STICKER_LIST = "sticker_list";
    private StickerPackHandler handler;

    public void setStickerPackHandler(StickerPackHandler handler) {
        this.handler = handler;
    }

    public static DialogOperationInvalidSticker newInstance(String stickerPackIdentifier, String stickerPackName, List<Sticker> stickers) {
        DialogOperationInvalidSticker fragment = new DialogOperationInvalidSticker();

        Bundle args = new Bundle();
        args.putString(STICKER_PACK_IDENTIFIER, stickerPackIdentifier);
        args.putString(STICKER_PACK_NAME, stickerPackName);
        args.putParcelableArrayList(STICKER_LIST, new ArrayList<>(stickers));

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
        View view = inflater.inflate(R.layout.dialog_invalid_sticker, null);

        Button buttonFixPack = view.findViewById(R.id.button_fix_stickers);
        buttonFixPack.setOnClickListener(fragment -> {
            if (getArguments() == null) return;
            ArrayList<Sticker> stickerArrayList = getArguments().getParcelableArrayList(STICKER_LIST);

            if (stickerArrayList == null) {
                return;
            }

            Intent intent = new Intent(fragment.getContext(), PreviewStickerInvalidActivity.class);
            // TODO: Mandar lista de stickers e identificador do pacote.

            fragment.getContext().startActivity(intent);
            dismiss();
        });

        Button buttonDeletePack = view.findViewById(R.id.button_ignore_invalid_sticker);
        buttonDeletePack.setOnClickListener(fragment -> {
            if (getArguments() == null) return;

            String stickerPackIdentifier = getArguments().getString(STICKER_PACK_IDENTIFIER);
            String stickerPackName = getArguments().getString(STICKER_PACK_NAME);

            if (stickerPackIdentifier == null || stickerPackName == null) {
                return;
            }

            handler.addStickerPackToWhatsApp(stickerPackIdentifier, stickerPackName);
            dismiss();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface ->

        {
            AlertDialog dialog = (AlertDialog) dialogInterface;
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        return alertDialog;
    }
}
