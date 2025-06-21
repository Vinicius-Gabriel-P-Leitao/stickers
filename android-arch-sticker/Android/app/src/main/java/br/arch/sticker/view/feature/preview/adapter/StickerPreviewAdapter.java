/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package br.arch.sticker.view.feature.preview.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.integration.webp.decoder.WebpDrawable;
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import br.arch.sticker.R;
import br.arch.sticker.core.util.BuildStickerUri;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.view.core.util.transformation.CropSquareTransformation;
import br.arch.sticker.view.feature.preview.viewholder.InvalidStickerButtonPreviewViewHolder;
import br.arch.sticker.view.feature.preview.viewholder.StickerPreviewViewHolder;

// @formatter:off
public class StickerPreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnInvalidStickerClickListener {
        void onInvalidStickerClicked();
    }

    private static final float COLLAPSED_STICKER_PREVIEW_BACKGROUND_ALPHA = 1f;
    private static final float EXPANDED_STICKER_PREVIEW_BACKGROUND_ALPHA = 0.2f;
    private static final int VIEW_TYPE_STICKER = 0;
    private static final int VIEW_TYPE_BUTTON = 1;

    @NonNull
    private final StickerPack stickerPack;
    @NonNull
    private final ArrayList<Sticker> invalidStickers;

    private final int cellSize;
    private final int cellPadding;
    private final int errorResource;

    private final ImageView expandedStickerPreview;
    private final LayoutInflater layoutInflater;
    private RecyclerView recyclerView;
    private View clickedStickerPreview;
    float expandedViewLeftX;
    float expandedViewTopY;

    private OnInvalidStickerClickListener invalidStickerClickListener;

    private final RecyclerView.OnScrollListener hideExpandedViewScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dx != 0 || dy != 0) {
                hideExpandedStickerPreview();
            }
        }
    };

    public StickerPreviewAdapter(
            @NonNull final LayoutInflater layoutInflater,
            final int errorResource,
            final int cellSize,
            final int cellPadding,
            @NonNull final StickerPack stickerPack,
            @NonNull ArrayList<Sticker> invalidStickers,
            final ImageView expandedStickerView,
            OnInvalidStickerClickListener invalidStickerClickListener
    ) {
        this.cellSize = cellSize;
        this.cellPadding = cellPadding;
        this.layoutInflater = layoutInflater;
        this.errorResource = errorResource;
        this.stickerPack = stickerPack;
        this.invalidStickers = invalidStickers;
        this.expandedStickerPreview = expandedStickerView;
        this.invalidStickerClickListener = invalidStickerClickListener;
    }

    public StickerPreviewAdapter(
            @NonNull final LayoutInflater layoutInflater,
            final int errorResource,
            final int cellSize,
            final int cellPadding,
            @NonNull final StickerPack stickerPack,
            @NonNull ArrayList<Sticker> invalidStickers,
            final ImageView expandedStickerView
    ) {
        this.cellSize = cellSize;
        this.cellPadding = cellPadding;
        this.layoutInflater = layoutInflater;
        this.errorResource = errorResource;
        this.stickerPack = stickerPack;
        this.invalidStickers = invalidStickers;
        this.expandedStickerPreview = expandedStickerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
        if (viewType == VIEW_TYPE_STICKER) {
            View itemView = layoutInflater.inflate(R.layout.preview_sticker_icon_details, viewGroup, false);
            StickerPreviewViewHolder stickerPreviewViewHolder = new StickerPreviewViewHolder(itemView);

            ViewGroup.LayoutParams layoutParams = stickerPreviewViewHolder.stickerPreviewView.getLayoutParams();
            layoutParams.height = cellSize;
            layoutParams.width = cellSize;

            stickerPreviewViewHolder.stickerPreviewView.setLayoutParams(layoutParams);
            stickerPreviewViewHolder.stickerPreviewView.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
            return stickerPreviewViewHolder;
        } else {
            View itemView = layoutInflater.inflate(R.layout.button_invalid_item_preview, viewGroup, false);
            InvalidStickerButtonPreviewViewHolder invalidStickerButtonPreviewViewHolder = new InvalidStickerButtonPreviewViewHolder(itemView);

            ViewGroup.LayoutParams layoutParams = invalidStickerButtonPreviewViewHolder.materialButton.getLayoutParams();
            layoutParams.height = cellSize;
            layoutParams.width = cellSize;

            invalidStickerButtonPreviewViewHolder.materialButton.setLayoutParams(layoutParams);
            invalidStickerButtonPreviewViewHolder.materialButton.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
            return invalidStickerButtonPreviewViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof StickerPreviewViewHolder previewViewHolder) {
            Sticker sticker = stickerPack.getStickers().get(position);

            previewViewHolder.stickerPreviewView.setImageResource(errorResource);
            previewViewHolder.stickerPreviewView.setImageURI(BuildStickerUri.buildStickerAssetUri(stickerPack.identifier, sticker.imageFileName));
            previewViewHolder.stickerPreviewView.setOnClickListener(view -> expandPreview(position, previewViewHolder.stickerPreviewView));
        }

        if (viewHolder instanceof InvalidStickerButtonPreviewViewHolder invalidStickerButtonPreviewViewHolder) {
            invalidStickerButtonPreviewViewHolder.materialButton.setOnClickListener(view -> {
                if (invalidStickerClickListener != null) {
                    invalidStickerClickListener.onInvalidStickerClicked();
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        recyclerView.addOnScrollListener(hideExpandedViewScrollListener);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnScrollListener(hideExpandedViewScrollListener);
        this.recyclerView = null;
    }

    @Override
    public int getItemCount() {
        int validCount = stickerPack.getStickers().size();
        boolean hasInvalid = !invalidStickers.isEmpty();
        return validCount + (hasInvalid ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        int validCount = stickerPack.getStickers().size();
        if (!invalidStickers.isEmpty() && position == validCount) {
            return VIEW_TYPE_BUTTON;
        }

        return VIEW_TYPE_STICKER;
    }

    private void positionExpandedStickerPreview(int selectedPosition) {
        if (expandedStickerPreview != null) {
            // Calculate the view's center (x, y), then use expandedStickerPreview's height and
            // width to
            // figure out what where to position it.
            final ViewGroup.MarginLayoutParams recyclerViewLayoutParams = ((ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams());
            final int recyclerViewLeftMargin = recyclerViewLayoutParams.leftMargin;
            final int recyclerViewRightMargin = recyclerViewLayoutParams.rightMargin;
            final int recyclerViewWidth = recyclerView.getWidth();
            final int recyclerViewHeight = recyclerView.getHeight();

            final StickerPreviewViewHolder clickedViewHolder = (StickerPreviewViewHolder) recyclerView.findViewHolderForAdapterPosition(selectedPosition);

            if (clickedViewHolder == null) {
                hideExpandedStickerPreview();
                return;
            }

            clickedStickerPreview = clickedViewHolder.itemView;
            final float clickedViewCenterX = clickedStickerPreview.getX() + recyclerViewLeftMargin + clickedStickerPreview.getWidth() / 2f;
            final float clickedViewCenterY = clickedStickerPreview.getY() + clickedStickerPreview.getHeight() / 2f;

            expandedViewLeftX = clickedViewCenterX - expandedStickerPreview.getWidth() / 2f;
            expandedViewTopY = clickedViewCenterY - expandedStickerPreview.getHeight() / 2f;

            // If the new x or y positions are negative, anchor them to 0 to avoid clipping
            // the left side of the device and the top of the recycler view.
            expandedViewLeftX = Math.max(expandedViewLeftX, 0);
            expandedViewTopY = Math.max(expandedViewTopY, 0);

            // If the bottom or right sides are clipped, we need to move the top left positions
            // so that those sides are no longer clipped.
            final float adjustmentX = Math.max(expandedViewLeftX + expandedStickerPreview.getWidth() - recyclerViewWidth - recyclerViewRightMargin, 0);
            final float adjustmentY = Math.max(expandedViewTopY + expandedStickerPreview.getHeight() - recyclerViewHeight, 0);

            expandedViewLeftX -= adjustmentX;
            expandedViewTopY -= adjustmentY;

            expandedStickerPreview.setX(expandedViewLeftX);
            expandedStickerPreview.setY(expandedViewTopY);
        }
    }

    private void expandPreview(int position, View clickedStickerPreview) {
        if (isStickerPreviewExpanded()) {
            hideExpandedStickerPreview();
            return;
        }

        this.clickedStickerPreview = clickedStickerPreview;

        if (expandedStickerPreview != null) {
            positionExpandedStickerPreview(position);

            String imageFileName = stickerPack.getStickers().get(position).imageFileName;

            final Uri stickerAssetUri = BuildStickerUri.buildStickerAssetUri(stickerPack.identifier, imageFileName);
            String extension = imageFileName.substring(imageFileName.lastIndexOf(".") + 1);

            boolean isAnimatedWebp = false;

            if (extension.equals("webp") && android.os.Build.VERSION.SDK_INT >= 28) {
                try {
                    Drawable drawable = Drawable.createFromStream(
                            expandedStickerPreview.getContext().getContentResolver().openInputStream(stickerAssetUri), null);
                    if (drawable instanceof android.graphics.drawable.AnimatedImageDrawable) {
                        isAnimatedWebp = true;
                    }
                } catch (Exception exception) {
                    // NOTE: Ignora para assumir que não é animado
                }
            }

            MultiTransformation<Bitmap> commonTransform = new MultiTransformation<>(
                    new CropSquareTransformation(10f, 5, R.color.catppuccin_overlay2));

            RequestOptions requestOptions = new RequestOptions().override(300, 300);
            RequestManager glide = Glide.with(expandedStickerPreview.getContext());

            if (extension.equals("webp") && isAnimatedWebp) {
                expandedStickerPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                glide.load(stickerAssetUri)
                        .apply(requestOptions)
                        .error(R.drawable.sticker_3rdparty_warning)
                        .transform(WebpDrawable.class, new WebpDrawableTransformation(commonTransform))
                        .into(expandedStickerPreview);
            } else {
                glide.asBitmap()
                        .load(stickerAssetUri)
                        .apply(requestOptions)
                        .error(R.drawable.sticker_3rdparty_warning)
                        .centerCrop()
                        .transform(commonTransform)
                        .into(expandedStickerPreview);
            }

            expandedStickerPreview.setVisibility(View.VISIBLE);
            recyclerView.setAlpha(EXPANDED_STICKER_PREVIEW_BACKGROUND_ALPHA);

            expandedStickerPreview.setOnClickListener(view -> hideExpandedStickerPreview());
        }
    }

    public void hideExpandedStickerPreview() {
        if (isStickerPreviewExpanded() && expandedStickerPreview != null) {
            clickedStickerPreview.setVisibility(View.VISIBLE);
            expandedStickerPreview.setVisibility(View.INVISIBLE);
            recyclerView.setAlpha(COLLAPSED_STICKER_PREVIEW_BACKGROUND_ALPHA);
        }
    }

    private boolean isStickerPreviewExpanded() {
        return expandedStickerPreview != null && expandedStickerPreview.getVisibility() == View.VISIBLE;
    }
}
