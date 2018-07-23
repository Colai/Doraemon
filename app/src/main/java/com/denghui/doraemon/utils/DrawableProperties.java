package com.denghui.doraemon.utils;

import android.annotation.SuppressLint;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

public class DrawableProperties {

    private static final int UNSET = -1;

    private int mAlpha = UNSET;
    private ColorFilter mColorFilter = null;
    private int mDither = UNSET;
    private int mFilterBitmap = UNSET;

    public void setColorFilter(ColorFilter colorFilter) {
        mColorFilter = colorFilter;
    }

    public void setDither(boolean dither) {
        mDither = dither ? 1 : 0;
    }

    public void setFilterBitmap(boolean filterBitmap) {
        mFilterBitmap = filterBitmap ? 1 : 0;
    }

    @SuppressLint("Range")
    public void applyTo(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        if (mAlpha != UNSET) {
            drawable.setAlpha(mAlpha);
        }
        if (mColorFilter!=null) {
            drawable.setColorFilter(mColorFilter);
        }
        if (mDither != UNSET) {
            drawable.setDither(mDither != 0);
        }
        if (mFilterBitmap != UNSET) {
            drawable.setFilterBitmap(mFilterBitmap != 0);
        }
    }

}
