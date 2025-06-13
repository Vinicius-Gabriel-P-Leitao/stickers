/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.fragment;

import android.app.Dialog;
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
import br.arch.sticker.R;
import br.arch.sticker.view.core.usecase.component.BottomFadingRecyclerView;
import br.arch.sticker.view.core.util.resolver.UriDetailsResolver;
import br.arch.sticker.view.feature.stickerpack.creation.adapter.MediaPickerAdapter;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.MediaPickerViewModel;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MediaPickerFragment extends BottomSheetDialogFragment {
    private MediaPickerViewModel viewModel;
    private MediaPickerAdapter mediaListAdapter;
    private ProgressBar progressBar;

    private MediaPickerAdapter.OnItemClickListener listener;

    public static MediaPickerFragment newInstance(MediaPickerAdapter.OnItemClickListener listener) {
        MediaPickerFragment fragment = new MediaPickerFragment();
        fragment.setOnItemClickListener(listener);
        return fragment;
    }

    public void setOnItemClickListener(MediaPickerAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle);

        viewModel = new ViewModelProvider(requireActivity()).get(MediaPickerViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_recyclerview_select_media, container, false);
    }

    // @formatter:off
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progress_bar_media);
        progressBar.setVisibility(View.GONE);

        BottomFadingRecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        mediaListAdapter = new MediaPickerAdapter(getContext(), uri -> {
            if (listener != null) {
                listener.onItemClick(uri);
            }

            dismiss();
        });

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mediaListAdapter);

        Button selectButton = view.findViewById(R.id.select_medias_button);
        selectButton.setOnClickListener(listener -> {
            Set<Uri> selectedUris = mediaListAdapter.getSelectedMediaPaths();

            if (selectedUris.size() >= 3) {
                progressBar.setVisibility(View.VISIBLE);
                viewModel.startConversions(selectedUris, requireContext());
            } else {
                Toast.makeText(getContext(), "Selecione pelo menos 3 itens!", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getMimeTypesSupported().observe(
                getViewLifecycleOwner(), mimeTypesSupported -> {
                    List<Uri> uris = UriDetailsResolver.fetchMediaUri(requireContext(), mimeTypesSupported.getMimeTypes());
                    mediaListAdapter.submitList(new ArrayList<>(uris));
                });

        viewModel.getStickerPackResult().observe(
                getViewLifecycleOwner(), result -> {
                    if (result != null) {
                        if (result.isSuccess()) {
                            viewModel.setStickerPackPreview(result.getData());
                            progressBar.setVisibility(View.GONE);

                            dismiss();
                        }

                        if (result.isWarning()) {
                            Toast.makeText(getContext(), "Erro: " + result.getWarningMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }

                        if (result.isFailure()) {
                            Toast.makeText(getContext(), "Erro: " + result.getError().getMessage(), Toast.LENGTH_SHORT).show();
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
}