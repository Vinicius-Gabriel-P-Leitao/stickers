/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.fragment;

import static android.app.Activity.RESULT_OK;

import static br.arch.sticker.view.feature.editor.activity.StickerEditorActivity.FILE_STICKER_DATA;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import br.arch.sticker.R;
import br.arch.sticker.core.error.throwable.base.AppCoreStateException;
import br.arch.sticker.view.core.usecase.component.BottomFadingRecyclerView;
import br.arch.sticker.view.core.util.resolver.UriDetailsResolver;
import br.arch.sticker.view.feature.editor.activity.StickerEditorActivity;
import br.arch.sticker.view.feature.stickerpack.creation.adapter.MediaPickerAdapter;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.StickerPackCreationViewModel;

public class MediaPickerFragment extends BottomSheetDialogFragment {
    private MediaPickerAdapter.OnItemClickListener listener;


    private StickerPackCreationViewModel StickerPackCreationViewModel;
    private MediaPickerAdapter mediaListAdapter;
    private ProgressBar progressBar;

    private ActivityResultLauncher<Intent> launcher;

    public void setOnItemClickListener(MediaPickerAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle);

        StickerPackCreationViewModel = new ViewModelProvider(requireActivity()).get(StickerPackCreationViewModel.class);


        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                progressBar.setVisibility(View.GONE);

                Intent data = result.getData();
                if (data != null) {
                    String fileUri = data.getStringExtra(FILE_STICKER_DATA);
                    if (fileUri != null) {
                        try {
                            File file = new File(fileUri);
                            List<File> files = new ArrayList<>();
                            files.add(file);

                            StickerPackCreationViewModel.generateStickerPack(files);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
        });

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
                Toast.makeText(getContext(), getString(R.string.error_select_at_least_one_media), Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedUris.size() == 1) {
                progressBar.setVisibility(View.VISIBLE);
                tryLaunchEditor(selectedUris.iterator().next());
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            StickerPackCreationViewModel.startConversions(selectedUris);
        });

        StickerPackCreationViewModel.getMimeTypesSupported().observe(getViewLifecycleOwner(), mimeTypesSupported -> {
            List<Uri> uris = UriDetailsResolver.fetchMediaUri(requireContext(), mimeTypesSupported.getMimeTypes());
            mediaListAdapter.submitList(new ArrayList<>(uris));
        });

        StickerPackCreationViewModel.getStickerPackResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    StickerPackCreationViewModel.setStickerPackPreview(result.getData());
                    progressBar.setVisibility(View.GONE);

                    dismiss();
                }

                if (result.isWarning()) {
                    Toast.makeText(getContext(), result.getWarningMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }

                if (result.isFailure()) {
                    if (result.getError() instanceof AppCoreStateException appCoreStateException) {
                        Toast.makeText(getContext(), getString(appCoreStateException.getErrorCode().getMessageResId()), Toast.LENGTH_SHORT).show();
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
        StickerPackCreationViewModel.setCancelConversions();
        Toast.makeText(requireActivity(), getString(R.string.error_process_canceled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        StickerPackCreationViewModel.setCancelConversions();
    }

    private void tryLaunchEditor(Uri uri) {
        Intent intent = new Intent(requireContext(), StickerEditorActivity.class);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        launcher.launch(intent);
    }
}