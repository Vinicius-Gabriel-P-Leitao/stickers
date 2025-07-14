/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.usecase.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import br.arch.sticker.R;

public class RangeSelectorOverlayView extends View {
    enum Handle {LEFT, RIGHT}

    public interface OnRangeChangeListener {
        void onRangeChanged(float seconds);
    }

    private OnRangeChangeListener listener;

    private float selectedSeconds = 5f;

    private float secondsToPx;

    private final float handleWidth = dpToPx(20f);
    private final float handleLineHeight = dpToPx(40f);
    private final float handleCornerRadius = dpToPx(8f);
    private final float selectionStrokeWidth = dpToPx(6f);

    private float leftHandleX;
    private float rightHandleX;
    private boolean isInitialised = false;
    private Handle draggingHandle = null;

    private final Paint handlePaint = new Paint();
    private final Paint selectionPaint = new Paint();
    private final Paint handleBackgroundPaint = new Paint();

    public RangeSelectorOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(15f);
        selectionPaint.setColor(ContextCompat.getColor(context, R.color.catppuccin_mantle));

        handlePaint.setColor(ContextCompat.getColor(context, R.color.catppuccin_red));
        handlePaint.setStrokeWidth(10f);

        handleBackgroundPaint.setStyle(Paint.Style.FILL);
        handleBackgroundPaint.setColor(selectionPaint.getColor());
    }

    public void setOnRangeChangeListener(OnRangeChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        secondsToPx = dpToPx(65f);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (!isInitialised && getWidth() > 0) {
            float selectedWidth = selectedSeconds * secondsToPx;
            float centerX = getWidth() / 2f;
            leftHandleX = centerX - selectedWidth / 2f;
            rightHandleX = leftHandleX + selectedWidth;
            isInitialised = true;
        }

        rightHandleX = leftHandleX + selectedSeconds * secondsToPx;

        final float halfStroke = selectionStrokeWidth / 2f;
        final float padding = dpToPx(10);

        final float left = leftHandleX - padding + halfStroke;
        final float right = rightHandleX + padding - halfStroke;
        final float bottom = getHeight() - halfStroke;

        canvas.drawRoundRect(left, halfStroke, right, bottom, dpToPx(8), dpToPx(8), selectionPaint);

        canvas.drawRoundRect(leftHandleX - handleWidth / 3f, halfStroke, leftHandleX, bottom,
                handleCornerRadius, handleCornerRadius, handleBackgroundPaint);

        canvas.drawRoundRect(rightHandleX, halfStroke, rightHandleX + handleWidth / 3f, bottom,
                handleCornerRadius, handleCornerRadius, handleBackgroundPaint);

        final float centerY = getHeight() / 2f;
        final float handleTop = centerY - handleLineHeight / 2f;
        final float handleBottom = centerY + handleLineHeight / 2f;

        canvas.drawLine(leftHandleX, handleTop, leftHandleX, handleBottom, handlePaint);
        canvas.drawLine(rightHandleX, handleTop, rightHandleX, handleBottom, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float halfStroke = selectionStrokeWidth / 2f;
        final float padding = dpToPx(10);
        final float maxX = getWidth() - padding - halfStroke;

        final float touchHandleWidth = 30f;
        final float x = event.getX();

        final boolean isInLeftHandle = Math.abs(x - leftHandleX) < touchHandleWidth;
        final boolean isInRightHandle = Math.abs(x - rightHandleX) < touchHandleWidth;

        float minSeconds = 1f;
        float maxSeconds = 5f;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInLeftHandle) {
                    draggingHandle = Handle.LEFT;
                    return true;
                } else if (isInRightHandle) {
                    draggingHandle = Handle.RIGHT;
                    return true;
                }
                return false;

            case MotionEvent.ACTION_MOVE:
                if (draggingHandle == Handle.LEFT) {
                    float minWidth = minSeconds * secondsToPx;
                    float newLeft = Math.min(x, rightHandleX - minWidth);
                    leftHandleX = Math.max(padding + halfStroke, newLeft);

                    float newWidth = rightHandleX - leftHandleX;
                    selectedSeconds = Math.max(minSeconds, Math.min(maxSeconds, newWidth / secondsToPx));

                    if (listener != null) {
                        listener.onRangeChanged(selectedSeconds);
                    }
                    invalidate();
                    return true;
                } else if (draggingHandle == Handle.RIGHT) {
                    float minWidth = minSeconds * secondsToPx;
                    float newRight = Math.max(x, leftHandleX + minWidth);
                    rightHandleX = Math.min(maxX, newRight);

                    float newWidth = rightHandleX - leftHandleX;
                    selectedSeconds = Math.max(minSeconds, Math.min(maxSeconds, newWidth / secondsToPx));

                    if (listener != null) {
                        listener.onRangeChanged(selectedSeconds);
                    }
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                draggingHandle = null;
                performClick();
                return true;
        }

        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}