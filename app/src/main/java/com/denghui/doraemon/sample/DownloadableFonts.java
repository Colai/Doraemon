package com.denghui.doraemon.sample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.FontRes;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.TextView;

import com.denghui.doraemon.R;

/**
 * 可下载字体
 * Android O （8.0 26+ 新特性）
 * Support Library 26 可让API14+都可以使用
 * <p>
 * https://fatetypo.xyz/downloadblefonts/
 * https://www.zhihu.com/question/38615247
 * https://www.bilibili.com/read/cv29647/
 * <p>
 * 开源库
 * https://github.com/chrisjenx/Calligraphy
 * <p>
 * 使用自定义字体Android O使用自定义字体
 * https://zhuanlan.zhihu.com/p/26437573
 * https://www.jianshu.com/p/69fa4261a8e3
 * 1。在res下创建 font 文件夹
 * 2。将.otf或.ttf字体库 放入该文件夹中
 * 3。在TextView中使用   android:fontFamily="@font/merriweather_regular"
 * 4。在Java中使用 ：Typeface typefaceLato = getResources().getFont(R.font.lato_regular);
 * mTextIntro.setTypeface(typefaceLato);
 * <p>
 * 使用 字体系列
 * 1。设置一个套字体系列 <font-family  <font 。。。
 * 2。在不同情况下使用 不同的字体 fontStyle 和 fontWeight
 * 3。同上使用 fontFamily
 * <p>
 * 增加字体可读性
 * letterSpacing字间距
 * lineSpacingExtra行间距
 * <p>
 * 超级官方的教程
 * https://blog.stylingandroid.com/category/android-o/fonts/
 * <p>
 * <p>
 * Span支持字体设置
 * https://stackoverflow.com/questions/4819049/how-can-i-use-typefacespan-or-stylespan-with-a-custom-typeface%E3%80%81
 * https://codeday.me/bug/20170723/45605.html
 * 1。继承TypefaceSpan
 * 2。重写updateDrawState 和 updateMeasureState
 * 3。就可以获得自定义的Span字体格式TypefaceSpan
 * 4。接下来就是Span的使用问题了
 */
public class DownloadableFonts {

    /**
     * 设置常规字体样式
     *
     * @param textView
     */
    public static void normal(TextView textView) {
        if (textView == null) {
            return;
        }
        //textView.setTypeface(getTypefaceById(textView.getContext(), R.font.xxx_regular));
    }

    /**
     * 根据字体id添加获取字体
     *
     * @param context
     * @param id
     * @return
     */
    private static Typeface getTypefaceById(Context context, @FontRes int id) {
        try {
            return ResourcesCompat.getFont(context, id);
        } catch (Resources.NotFoundException ignore) {
            ignore.printStackTrace();
        }
        return Typeface.DEFAULT;
    }
}
