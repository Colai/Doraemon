package com.denghui.doraemon.ui.utils;

import android.app.Activity;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import org.jetbrains.annotations.NotNull;

/**
 * 【今日头条 -- 低成本的Android屏幕适配方式】
 * <p>
 * 原理：
 * Android原始初衷，dp保证手机不同屏幕下显示的大小相同
 * px = density * dp;
 * density = dpi / 160;
 * px = dp * (dpi / 160);
 * 但是：这种机制导致，屏幕的宽度可能不是相同的dp宽度
 * 而设计稿，是希望将屏幕宽度填充满的，所以直接用xxxdp并不合适
 * <p>
 * 所以为了 以宽或者高作为一个纬度去适配，保持和设计稿一致的画，我们需要舍弃原来固有逻辑
 * 以最简单的方式达到某一个纬度的标准。以px最为标准，所以我们只需要修改density的值就可以
 * 利用dp来写各种布局，而且和设计稿完全一致。
 * <p>
 * 前提条件
 * 1.density的值以通过Activity或者Application的Context获得的DisplayMetrics去获得
 * -- DisplayMetrics#density 就是上述的density
 * -- DisplayMetrics#densityDpi 就是上述的dpi
 * -- DisplayMetrics#scaledDensity 字体的缩放因子，正常情况下和density相等，但是调节系统字体大小后会改变这个值
 * 2.所有的dp和px转换都是通过density完成的
 * -- 布局中的转化是TypedValue#applyDimension中完成
 * -- BitmapFactory中BitmapFactory#decodeResourceStream也是通过density
 * <p>
 * 具体实现如下：
 * 备注：在activity的onCreate中调用该方法
 */

public class CustomDensityUtil {
    private CustomDensityUtil() {
    }

    //未修改的 density和scaledDensity的值
    private static float noCompatDensity;
    private static float noCompatScaledDensity;

    public static void initCustomDensity(@NotNull Activity activity) {

        //获得application的值DisplayMetrics
        final DisplayMetrics appDisplayMetrics = activity.getApplication().getResources().getDisplayMetrics();

        //如果没有初始化默认值，则进行初始化 (只执行一次)
        if (noCompatDensity == 0) {

            noCompatDensity = appDisplayMetrics.density;
            noCompatScaledDensity = appDisplayMetrics.scaledDensity;

            activity.getApplication().registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    //系统的文字大小发生改变后，修改初始化值（不然如果应用没有退出，这些值都不会生效了）
                    if (newConfig != null && newConfig.fontScale > 0) {
                        noCompatScaledDensity = activity.getApplication().getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }

        //只用noCompatScaledDensity和noCompatDensity去修改
        final float targetDensity = appDisplayMetrics.widthPixels / 360f;
        final float targetScaledDensity = targetDensity * (noCompatScaledDensity / noCompatDensity);
        final int targetDensityDpi = (int) (targetDensity * 160f);

        //防止全局使用application的desity
        appDisplayMetrics.density = targetDensity;
        appDisplayMetrics.scaledDensity = targetScaledDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi;


        final DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaledDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }

}
