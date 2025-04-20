package com.vinicius.sticker.ui.view;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CropSquareTransformation extends BitmapTransformation {
    private final float borderRadius;
    private final int borderWidth;
    private final int borderColor;

    public CropSquareTransformation(float borderRadius, int borderWidth, int borderColor) {
        this.borderRadius = borderRadius;
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {

        int size = Math.min(toTransform.getWidth(), toTransform.getHeight());
        int x = (toTransform.getWidth() - size) / 2;
        int y = (toTransform.getHeight() - size) / 2;

        Bitmap squared = Bitmap.createBitmap(toTransform, x, y, size, size);

        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        RectF rect = new RectF(0f, 0f, size, size);
        canvas.drawRoundRect(rect, borderRadius, borderRadius, paint);

        if (borderWidth > 0) {
            Paint borderPaint = new Paint();
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setColor(borderColor);
            borderPaint.setStrokeWidth(borderWidth);
            borderPaint.setAntiAlias(true);
            canvas.drawRoundRect(rect, borderRadius, borderRadius, borderPaint);
        }

        return result;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(("CropSquareGlideTransformation" +
                borderRadius + borderWidth + borderColor).getBytes(StandardCharsets.UTF_8));
    }

}
