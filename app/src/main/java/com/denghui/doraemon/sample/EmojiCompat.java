package com.denghui.doraemon.sample;

/**
 * 官方支持的Emoji
 *
 * https://blog.csdn.net/cjpx00008/article/details/74009162
 * 官方写的很详细的案例
 * https://github.com/googlesamples/android-EmojiCompat/blob/master/app/src/main/java/com/example/android/emojicompat/CustomTextView.java
 *
 * 这个库向下兼容到4.4
 * 使用：
 * 1。初始化（两种方式）
 *      1。在线下载字体库 FontRequest
 *      2。使用本地字体库并初始化    EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
 *                                EmojiCompat.init(config);
 * 2。使用
 *      1。使用EmojiCompat的控件
 *      2。使用自定义的Custom TextViews，重写其中的方法，并调用Emoji其中的方法
 *      3。包装文本信息：EmojiCompat.get().process("neutral face \uD83D\uDE10");
 *
 * 3。原理
 *      Emoji有自己的一套编码格式，只要我们的字体库能解读这样的编码，就可以显示Emoji表情，
 *      新增的Emoji表情在原来的系统上不能进行解读，所以利用适配库去进行解读。
 *
 */
public class EmojiCompat {
}
