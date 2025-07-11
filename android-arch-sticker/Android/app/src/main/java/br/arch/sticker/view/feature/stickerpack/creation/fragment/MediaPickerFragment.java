/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.arch.sticker.R;
import br.arch.sticker.core.error.throwable.base.AppCoreStateException;
import br.arch.sticker.view.core.usecase.component.BottomFadingRecyclerView;
import br.arch.sticker.view.core.util.resolver.UriDetailsResolver;
import br.arch.sticker.view.feature.editor.activity.StickerEditorActivity;
import br.arch.sticker.view.feature.stickerpack.creation.adapter.MediaPickerAdapter;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.StickerPackCreationViewModel;

public class MediaPickerFragment extends BottomSheetDialogFragment {
    private StickerPackCreationViewModel viewModel;
    private MediaPickerAdapter mediaListAdapter;
    private ProgressBar progressBar;

    private MediaPickerAdapter.OnItemClickListener listener;

    public void setOnItemClickListener(MediaPickerAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle);

        viewModel = new ViewModelProvider(requireActivity()).get(StickerPackCreationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_recyclerview_select_media, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progress_bar_media);
        progressBar.setVisibility(View.GONE);

        BottomFadingRecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        mediaListAdapter = new MediaPickerAdapter(getContext(), itemClickListener -> {
            if (listener != null) {
                listener.onItemClick(itemClickListener);
            }

            dismiss();
        });

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mediaListAdapter);

        Button selectButton = view.findViewById(R.id.select_medias_button);
        selectButton.setOnClickListener(listener -> {
            final Set<Uri> selectedUris = mediaListAdapter.getSelectedMediaPaths();

            if (selectedUris.isEmpty()) {
                Toast.makeText(
                        getContext(), getString(R.string.error_message_select_least_media), Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedUris.size() == 1) {
                Intent intent = new Intent(getContext(), StickerEditorActivity.class);
                intent.setData(selectedUris.iterator().next());
                startActivity(intent);
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            viewModel.startConversions(selectedUris);
        });

        viewModel.getMimeTypesSupported().observe(getViewLifecycleOwner(), mimeTypesSupported -> {
            List<Uri> uris = UriDetailsResolver.fetchMediaUri(requireContext(), mimeTypesSupported.getMimeTypes());
            mediaListAdapter.submitList(new ArrayList<>(uris));
        });

        viewModel.getStickerPackResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    viewModel.setStickerPackPreview(result.getData());
                    progressBar.setVisibility(View.GONE);

                    dismiss();
                }

                if (result.isWarning()) {
                    Toast.makeText(getContext(), result.getWarningMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }

                if (result.isFailure()) {
                    if (result.getError() instanceof AppCoreStateException appCoreStateException) {
                        String errorMessage = getString(appCoreStateException.getErrorCode().getMessageResId());
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(getContext(), result.getError().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
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

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        viewModel.setCancelConversions();
        Toast.makeText(requireActivity(), getString(R.string.error_message_process_canceled), Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setCancelConversions();
    }
}