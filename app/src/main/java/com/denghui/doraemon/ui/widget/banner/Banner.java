package com.denghui.doraemon.ui.widget.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.denghui.doraemon.R;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.support.v4.view.ViewPager.OnPageChangeListener;
import static android.support.v4.view.ViewPager.PageTransformer;

/**
 * Image模式 轮播 的Banner组件，支持内置的 指示器 和 切换动画
 * 使用指南：
 * 1。在xml布局中使用该自定义View，声明自定义配置
 * 2。在java中获取该组件的引用，通过setImages和setBannerTitles设置显示的资源url和指示器title
 * 3。可在动态配置属性，和事件监听器，然后start(),RecycleView的每次bind数据的时候都得start()，开始轮播之旅
 *
 * 组件包含，该目录下的java文件和attr下属性声明，drawable和layout下的布局文件
 */
public class Banner extends FrameLayout implements OnPageChangeListener {

    /**
     * 初始默认值
     */
    public static class BannerConfig {
        /**
         * indicator style
         */
        public static final int NOT_INDICATOR = 0;
        public static final int CIRCLE_INDICATOR = 1;
        public static final int NUM_INDICATOR = 2;
        public static final int NUM_INDICATOR_TITLE = 3;
        public static final int CIRCLE_INDICATOR_TITLE = 4;
        public static final int CIRCLE_INDICATOR_TITLE_INSIDE = 5;
        /**
         * indicator gravity
         */
        public static final int LEFT = 5;
        public static final int CENTER = 6;
        public static final int RIGHT = 7;

        /**
         * banner
         */
        public static final int PADDING_SIZE = 5;
        public static final int TIME = 2000;
        public static final int DURATION = 800;
        public static final boolean IS_AUTO_PLAY = true;
        public static final boolean IS_SCROLL = true;

        /**
         * title style
         */
        public static final int TITLE_BACKGROUND = -1;
        public static final int TITLE_HEIGHT = -1;
        public static final int TITLE_TEXT_COLOR = -1;
        public static final int TITLE_TEXT_SIZE = -1;

    }

    /**
     * banner点击监听
     */
    public interface OnBannerListener {
        void onBannerClick(int position);
    }

    public interface ImageLoaderInterface<T extends View> extends Serializable {

        /**
         * 绑定显示的item的View的数据
         *
         * @param context
         * @param path
         * @param imageView
         */
        void displayImage(Context context, Object path, T imageView);

        /**
         * 显示的item的View
         *
         * @param context
         * @return
         */
        T createImageView(Context context);

    }

    public String tag = "Banner";
    private int mIndicatorMargin = BannerConfig.PADDING_SIZE;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int indicatorSize;
    private int bannerBackgroundImage;
    private int bannerStyle = BannerConfig.CIRCLE_INDICATOR;
    private int delayTime = BannerConfig.TIME;
    private int scrollTime = BannerConfig.DURATION;
    private boolean isAutoPlay = BannerConfig.IS_AUTO_PLAY;
    private boolean isScroll = BannerConfig.IS_SCROLL;
    private int mIndicatorSelectedResId = R.drawable.banner_gray_radius;
    private int mIndicatorUnselectedResId = R.drawable.banner_white_radius;
    private int mLayoutResId = R.layout.banner;
    private int titleHeight;
    private int titleBackground;
    private int titleTextColor;
    private int titleTextSize;
    private int count = 0;
    private int currentItem;
    private int gravity = -1;
    private int lastPosition = 1;
    private int scaleType = 1;
    private List<String> titles;
    private List imageUrls;
    private List<View> imageViews;
    private List<ImageView> indicatorImages;
    private Context context;
    private BannerViewPager viewPager;
    private TextView bannerTitle, numIndicatorInside, numIndicator;
    private LinearLayout indicator, indicatorInside, titleView;
    private ImageView bannerDefaultImage;
    private ImageLoaderInterface imageLoader;
    private BannerPagerAdapter adapter;
    private OnPageChangeListener mOnPageChangeListener;
    private BannerScroller mScroller;
    private OnBannerListener listener;
    private DisplayMetrics dm;

    private WeakHandler handler = new WeakHandler();

    public Banner(Context context) {
        this(context, null);
    }

    public Banner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

        titles = new ArrayList<>();             //title
        imageUrls = new ArrayList<>();          //显示内容的地址
        imageViews = new ArrayList<>();         //ViewPager的Item
        indicatorImages = new ArrayList<>();    //指示器View
        dm = context.getResources().getDisplayMetrics();
        indicatorSize = dm.widthPixels / 80;
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        imageViews.clear();
        handleTypedArray(context, attrs);

        View view = LayoutInflater.from(context).inflate(mLayoutResId, this, true);
        bannerDefaultImage = (ImageView) view.findViewById(R.id.bannerDefaultImage);
        viewPager = (BannerViewPager) view.findViewById(R.id.bannerViewPager);

        titleView = (LinearLayout) view.findViewById(R.id.titleView);
        indicator = (LinearLayout) view.findViewById(R.id.circleIndicator);
        indicatorInside = (LinearLayout) view.findViewById(R.id.indicatorInside);
        bannerTitle = (TextView) view.findViewById(R.id.bannerTitle);
        numIndicator = (TextView) view.findViewById(R.id.numIndicator);
        numIndicatorInside = (TextView) view.findViewById(R.id.numIndicatorInside);
        bannerDefaultImage.setImageResource(bannerBackgroundImage);

        initViewPagerScroll();
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Banner);
        mIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.Banner_indicator_width, indicatorSize);
        mIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.Banner_indicator_height, indicatorSize);
        mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.Banner_indicator_margin, BannerConfig.PADDING_SIZE);
        mIndicatorSelectedResId = typedArray.getResourceId(R.styleable.Banner_indicator_drawable_selected, R.drawable.banner_gray_radius);
        mIndicatorUnselectedResId = typedArray.getResourceId(R.styleable.Banner_indicator_drawable_unselected, R.drawable.banner_white_radius);
        scaleType = typedArray.getInt(R.styleable.Banner_image_scale_type, scaleType);

        //开始自动轮播的延迟
        delayTime = typedArray.getInt(R.styleable.Banner_delay_time, BannerConfig.TIME);

        scrollTime = typedArray.getInt(R.styleable.Banner_scroll_time, BannerConfig.DURATION);
        isAutoPlay = typedArray.getBoolean(R.styleable.Banner_is_auto_play, BannerConfig.IS_AUTO_PLAY);

        titleBackground = typedArray.getColor(R.styleable.Banner_title_background, BannerConfig.TITLE_BACKGROUND);
        titleHeight = typedArray.getDimensionPixelSize(R.styleable.Banner_title_height, BannerConfig.TITLE_HEIGHT);
        titleTextColor = typedArray.getColor(R.styleable.Banner_title_textcolor, BannerConfig.TITLE_TEXT_COLOR);
        titleTextSize = typedArray.getDimensionPixelSize(R.styleable.Banner_title_textsize, BannerConfig.TITLE_TEXT_SIZE);

        mLayoutResId = typedArray.getResourceId(R.styleable.Banner_banner_layout, mLayoutResId);

        //在没有轮播内容的时候，显示的图片
        bannerBackgroundImage = typedArray.getResourceId(R.styleable.Banner_banner_default_image, R.drawable.no_banner);

        typedArray.recycle();
    }

    private void initViewPagerScroll() {
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            mScroller = new BannerScroller(viewPager.getContext());
            mScroller.setDuration(scrollTime);
            mField.set(viewPager, mScroller);
        } catch (Exception e) {
            Log.e(tag, e.getMessage());
        }
    }


    /**
     * 设置自动播放
     *
     * @param isAutoPlay
     * @return
     */
    public Banner isAutoPlay(boolean isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
        return this;
    }

    /**
     * 设置图片加载器
     *
     * @param imageLoader
     * @return
     */
    public Banner setImageLoader(ImageLoaderInterface imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    /**
     * 设置轮播开始延迟时间
     *
     * @param delayTime
     * @return
     */
    public Banner setDelayTime(int delayTime) {
        this.delayTime = delayTime;
        return this;
    }

    /**
     * 设置指示器位置
     *
     * @param type
     * @return
     */
    public Banner setIndicatorGravity(int type) {
        switch (type) {
            case BannerConfig.LEFT:
                this.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                break;
            case BannerConfig.CENTER:
                this.gravity = Gravity.CENTER;
                break;
            case BannerConfig.RIGHT:
                this.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                break;
        }
        return this;
    }

    /**
     * 设置轮播切换效果
     *
     * @param transformer
     * @return
     */
    public Banner setBannerAnimation(Class<? extends PageTransformer> transformer) {
        try {
            setPageTransformer(true, transformer.newInstance());
        } catch (Exception e) {
            Log.e(tag, "Please set the PageTransformer class");
        }
        return this;
    }

    /**
     * 设置ViewPager的缓存数，因为PagerAdapter所以该值意义不大
     *
     * @param limit
     * @return
     */
    public Banner setOffscreenPageLimit(int limit) {
        if (viewPager != null) {
            viewPager.setOffscreenPageLimit(limit);
        }
        return this;
    }

    /**
     * 设置切换动画
     *
     * @param reverseDrawingOrder
     * @param transformer
     * @return
     */
    public Banner setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer) {
        viewPager.setPageTransformer(reverseDrawingOrder, transformer);
        return this;
    }

    /**
     * 设置title
     *
     * @param titles
     * @return
     */
    public Banner setBannerTitles(List<String> titles) {
        this.titles = titles;
        return this;
    }

    /**
     * 设置指示器的样式
     *
     * @param bannerStyle
     * @return
     */
    public Banner setBannerStyle(int bannerStyle) {
        this.bannerStyle = bannerStyle;
        return this;
    }

    /**
     * 设置是否支持滑动
     *
     * @param isScroll
     * @return
     */
    public Banner setViewPagerIsScroll(boolean isScroll) {
        this.isScroll = isScroll;
        return this;
    }

    /**
     * 设置显示图片的地址
     *
     * @param imageUrls
     * @return
     */
    public Banner setImages(List<?> imageUrls) {
        this.imageUrls = imageUrls;
        this.count = imageUrls.size();
        return this;
    }

    /**
     * 更细显示地址和title
     *
     * @param imageUrls
     * @param titles
     */
    public void update(List<?> imageUrls, List<String> titles) {
        this.titles.clear();
        this.titles.addAll(titles);
        update(imageUrls);
    }

    /**
     * 更新显示地址
     *
     * @param imageUrls
     */
    public void update(List<?> imageUrls) {
        this.imageUrls.clear();
        this.imageViews.clear();
        this.indicatorImages.clear();
        this.imageUrls.addAll(imageUrls);
        this.count = this.imageUrls.size();
        start();
    }

    /**
     * 更新指示器样式
     *
     * @param bannerStyle
     */
    public void updateBannerStyle(int bannerStyle) {
        indicator.setVisibility(GONE);
        numIndicator.setVisibility(GONE);
        numIndicatorInside.setVisibility(GONE);
        indicatorInside.setVisibility(GONE);
        bannerTitle.setVisibility(View.GONE);
        titleView.setVisibility(View.GONE);
        this.bannerStyle = bannerStyle;
        start();
    }

    /**
     * 开始轮播,除了调用更新会主动调用之外，其他的都需要手动开启。
     *
     * @return
     */
    public Banner start() {
        setBannerStyleUI();
        setImageList(imageUrls);
        setData();
        return this;
    }

    /**
     * 开始自动轮播（在start()之后调用）
     */
    public void startAutoPlay() {
        handler.removeCallbacks(task);
        handler.postDelayed(task, delayTime);
    }

    /**
     * 停止自动轮播
     */
    public void stopAutoPlay() {
        handler.removeCallbacks(task);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoPlay();
    }

    private void setTitleStyleUI() {
        if (titles.size() != imageUrls.size()) {
            throw new RuntimeException("[Banner] --> The number of titles and images is different");
        }
        if (titleBackground != -1) {
            titleView.setBackgroundColor(titleBackground);
        }
        if (titleHeight != -1) {
            titleView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, titleHeight));
        }
        if (titleTextColor != -1) {
            bannerTitle.setTextColor(titleTextColor);
        }
        if (titleTextSize != -1) {
            bannerTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
        }
        if (titles != null && titles.size() > 0) {
            bannerTitle.setText(titles.get(0));
            bannerTitle.setVisibility(View.VISIBLE);
            titleView.setVisibility(View.VISIBLE);
        }
    }

    private void setBannerStyleUI() {
        int visibility = count > 1 ? View.VISIBLE : View.GONE;
        switch (bannerStyle) {
            case BannerConfig.CIRCLE_INDICATOR:
                indicator.setVisibility(visibility);
                break;
            case BannerConfig.NUM_INDICATOR:
                numIndicator.setVisibility(visibility);
                break;
            case BannerConfig.NUM_INDICATOR_TITLE:
                numIndicatorInside.setVisibility(visibility);
                setTitleStyleUI();
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE:
                indicator.setVisibility(visibility);
                setTitleStyleUI();
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE:
                indicatorInside.setVisibility(visibility);
                setTitleStyleUI();
                break;
        }
    }

    private void initImages() {
        imageViews.clear();
        if (bannerStyle == BannerConfig.CIRCLE_INDICATOR ||
                bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE ||
                bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE) {
            createIndicator();
        } else if (bannerStyle == BannerConfig.NUM_INDICATOR_TITLE) {
            numIndicatorInside.setText("1/" + count);
        } else if (bannerStyle == BannerConfig.NUM_INDICATOR) {
            numIndicator.setText("1/" + count);
        }
    }

    /**
     * 初始化Image
     *
     * @param imagesUrl
     */
    private void setImageList(List<?> imagesUrl) {
        if (imagesUrl == null || imagesUrl.size() <= 0) {
            bannerDefaultImage.setVisibility(VISIBLE);
            Log.e(tag, "The image data set is empty.");
            return;
        }
        bannerDefaultImage.setVisibility(GONE);
        initImages();
        /**
         * <= count + 1   相当于  < count + 2
         */
        for (int i = 0; i <= count + 1; i++) {
            View imageView = null;
            if (imageLoader != null) {
                imageView = imageLoader.createImageView(context);
            }
            if (imageView == null) {
                imageView = new ImageView(context);
            }
            setScaleType(imageView);
            Object url = null;
            /**
             * 第0各是 urlList的最后一个
             * 最后一个是 urlList的第一个
             */
            if (i == 0) {
                url = imagesUrl.get(count - 1);
            } else if (i == count + 1) {
                url = imagesUrl.get(0);
            } else {
                url = imagesUrl.get(i - 1);
            }
            imageViews.add(imageView);
            if (imageLoader != null)
                imageLoader.displayImage(context, url, imageView);
            else
                Log.e(tag, "Please set images loader.");
        }
    }

    private void setScaleType(View imageView) {
        if (imageView instanceof ImageView) {
            ImageView view = ((ImageView) imageView);
            switch (scaleType) {
                case 0:
                    view.setScaleType(ScaleType.CENTER);
                    break;
                case 1:
                    view.setScaleType(ScaleType.CENTER_CROP);
                    break;
                case 2:
                    view.setScaleType(ScaleType.CENTER_INSIDE);
                    break;
                case 3:
                    view.setScaleType(ScaleType.FIT_CENTER);
                    break;
                case 4:
                    view.setScaleType(ScaleType.FIT_END);
                    break;
                case 5:
                    view.setScaleType(ScaleType.FIT_START);
                    break;
                case 6:
                    view.setScaleType(ScaleType.FIT_XY);
                    break;
                case 7:
                    view.setScaleType(ScaleType.MATRIX);
                    break;
            }

        }
    }

    private void createIndicator() {
        indicatorImages.clear();
        indicator.removeAllViews();
        indicatorInside.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
            params.leftMargin = mIndicatorMargin;
            params.rightMargin = mIndicatorMargin;
            if (i == 0) {
                imageView.setImageResource(mIndicatorSelectedResId);
            } else {
                imageView.setImageResource(mIndicatorUnselectedResId);
            }
            indicatorImages.add(imageView);
            if (bannerStyle == BannerConfig.CIRCLE_INDICATOR ||
                    bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE)
                indicator.addView(imageView, params);
            else if (bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE)
                indicatorInside.addView(imageView, params);
        }
    }

    private void setData() {
        currentItem = 1;
        if (adapter == null) {
            adapter = new BannerPagerAdapter();
            viewPager.addOnPageChangeListener(this);
        }
        viewPager.setAdapter(adapter);
        viewPager.setFocusable(true);
        viewPager.setCurrentItem(1);
        if (gravity != -1)
            indicator.setGravity(gravity);
        if (isScroll && count > 1) {
            viewPager.setScrollable(true);
        } else {
            viewPager.setScrollable(false);
        }
        if (isAutoPlay)
            startAutoPlay();
    }

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            if (count > 1 && isAutoPlay) {
                currentItem = currentItem % (count + 1) + 1;
                if (currentItem == 1) {
                    viewPager.setCurrentItem(currentItem, false);
                    handler.post(task);
                } else {
                    viewPager.setCurrentItem(currentItem);
                    handler.postDelayed(task, delayTime);
                }
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isAutoPlay) {
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
                    || action == MotionEvent.ACTION_OUTSIDE) {
                startAutoPlay();
            } else if (action == MotionEvent.ACTION_DOWN) {
                stopAutoPlay();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 返回真实的位置
     *
     * @param position
     * @return 下标从0开始
     */
    public int toRealPosition(int position) {
        int realPosition = (position - 1) % count;
        if (realPosition < 0)
            realPosition += count;
        return realPosition;
    }

    /*
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_DRAGGING = 1;
        public static final int SCROLL_STATE_SETTLING = 2;
    */
    @Override
    public void onPageScrollStateChanged(int state) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrollStateChanged(state);
        }
        switch (state) {
            case 0://No operation
                if (currentItem == 0) {
                    viewPager.setCurrentItem(count, false);
                } else if (currentItem == count + 1) {
                    viewPager.setCurrentItem(1, false);
                }
                break;
            case 1://start Sliding
                if (currentItem == count + 1) {
                    viewPager.setCurrentItem(1, false);
                } else if (currentItem == 0) {
                    viewPager.setCurrentItem(count, false);
                }
                break;
            case 2://end Sliding
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(toRealPosition(position), positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        currentItem = position;
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(toRealPosition(position));
        }

        /**
         * 设置指示器的逻辑
         */
        if (bannerStyle == BannerConfig.CIRCLE_INDICATOR ||
                bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE ||
                bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE) {
            indicatorImages.get((lastPosition - 1 + count) % count).setImageResource(mIndicatorUnselectedResId);
            indicatorImages.get((position - 1 + count) % count).setImageResource(mIndicatorSelectedResId);
            lastPosition = position;
        }
        if (position == 0) position = count;
        if (position > count) position = 1;
        switch (bannerStyle) {
            case BannerConfig.CIRCLE_INDICATOR:
                break;
            case BannerConfig.NUM_INDICATOR:
                numIndicator.setText(position + "/" + count);
                break;
            case BannerConfig.NUM_INDICATOR_TITLE:
                numIndicatorInside.setText(position + "/" + count);
                bannerTitle.setText(titles.get(position - 1));
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE:
                bannerTitle.setText(titles.get(position - 1));
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE:
                bannerTitle.setText(titles.get(position - 1));
                break;
        }

    }

    /**
     * @param listener
     * @return
     */
    public Banner setOnBannerListener(OnBannerListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 用于需要精确知道切换的过程中的百分比等值，则增加该监听
     *
     * @param onPageChangeListener
     */
    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }


    /**
     * 可设置Banner是否接受手势操作
     */
    public static class BannerViewPager extends ViewPager {
        private boolean scrollable = true;

        public BannerViewPager(Context context) {
            super(context);
        }

        public BannerViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (this.scrollable) {
                if (getCurrentItem() == 0 && getChildCount() == 0) {
                    return false;
                }
                return super.onTouchEvent(ev);
            } else {
                return false;
            }
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (this.scrollable) {
                if (getCurrentItem() == 0 && getChildCount() == 0) {
                    return false;
                }
                return super.onInterceptTouchEvent(ev);
            } else {
                return false;
            }
        }

        public void setScrollable(boolean scrollable) {
            this.scrollable = scrollable;
        }
    }

    /**
     * PagerAdapter，主要是处理 循环 的ViewPager
     */
    private class BannerPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            container.addView(imageViews.get(position));
            View view = imageViews.get(position);
            if (listener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onBannerClick(toRealPosition(position));
                    }
                });
            }
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }


    /**
     * 可设置banner滑动的时间
     */
    private class BannerScroller extends Scroller {
        private int mDuration = BannerConfig.DURATION;

        public BannerScroller(Context context) {
            super(context);
        }

        public BannerScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public BannerScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        public void setDuration(int time) {
            mDuration = time;
        }
    }

    /**
     * 以下四个静态内部类主要是起到 定时器 的作用
     */
    private static class WeakHandler {
        private final Handler.Callback mCallback; // hard reference to Callback. We need to keep callback in memory
        private final ExecHandler mExec;
        private Lock mLock = new ReentrantLock();
        @SuppressWarnings("ConstantConditions")
        @VisibleForTesting
        final ChainedRef mRunnables = new ChainedRef(mLock, null);

        public WeakHandler() {
            mCallback = null;
            mExec = new ExecHandler();
        }

        public WeakHandler(@Nullable Handler.Callback callback) {
            mCallback = callback; // Hard referencing body
            mExec = new ExecHandler(new WeakReference<>(callback)); // Weak referencing inside ExecHandler
        }

        public WeakHandler(@NonNull Looper looper) {
            mCallback = null;
            mExec = new ExecHandler(looper);
        }

        public WeakHandler(@NonNull Looper looper, @NonNull Handler.Callback callback) {
            mCallback = callback;
            mExec = new ExecHandler(looper, new WeakReference<>(callback));
        }

        public final boolean post(@NonNull Runnable r) {
            return mExec.post(wrapRunnable(r));
        }

        public final boolean postAtTime(@NonNull Runnable r, long uptimeMillis) {
            return mExec.postAtTime(wrapRunnable(r), uptimeMillis);
        }

        public final boolean postAtTime(Runnable r, Object token, long uptimeMillis) {
            return mExec.postAtTime(wrapRunnable(r), token, uptimeMillis);
        }

        public final boolean postDelayed(Runnable r, long delayMillis) {
            return mExec.postDelayed(wrapRunnable(r), delayMillis);
        }

        public final boolean postAtFrontOfQueue(Runnable r) {
            return mExec.postAtFrontOfQueue(wrapRunnable(r));
        }

        public final void removeCallbacks(Runnable r) {
            final WeakRunnable runnable = mRunnables.remove(r);
            if (runnable != null) {
                mExec.removeCallbacks(runnable);
            }
        }

        public final void removeCallbacks(Runnable r, Object token) {
            final WeakRunnable runnable = mRunnables.remove(r);
            if (runnable != null) {
                mExec.removeCallbacks(runnable, token);
            }
        }

        public final boolean sendMessage(Message msg) {
            return mExec.sendMessage(msg);
        }

        public final boolean sendEmptyMessage(int what) {
            return mExec.sendEmptyMessage(what);
        }

        public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
            return mExec.sendEmptyMessageDelayed(what, delayMillis);
        }

        public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
            return mExec.sendEmptyMessageAtTime(what, uptimeMillis);
        }

        public final boolean sendMessageDelayed(Message msg, long delayMillis) {
            return mExec.sendMessageDelayed(msg, delayMillis);
        }

        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            return mExec.sendMessageAtTime(msg, uptimeMillis);
        }

        public final boolean sendMessageAtFrontOfQueue(Message msg) {
            return mExec.sendMessageAtFrontOfQueue(msg);
        }

        public final void removeMessages(int what) {
            mExec.removeMessages(what);
        }

        public final void removeMessages(int what, Object object) {
            mExec.removeMessages(what, object);
        }

        public final void removeCallbacksAndMessages(Object token) {
            mExec.removeCallbacksAndMessages(token);
        }

        public final boolean hasMessages(int what) {
            return mExec.hasMessages(what);
        }

        public final boolean hasMessages(int what, Object object) {
            return mExec.hasMessages(what, object);
        }

        public final Looper getLooper() {
            return mExec.getLooper();
        }

        private WeakRunnable wrapRunnable(@NonNull Runnable r) {
            //noinspection ConstantConditions
            if (r == null) {
                throw new NullPointerException("Runnable can't be null");
            }
            final ChainedRef hardRef = new ChainedRef(mLock, r);
            mRunnables.insertAfter(hardRef);
            return hardRef.wrapper;
        }
    }

    private static class ExecHandler extends Handler {
        private final WeakReference<Callback> mCallback;

        ExecHandler() {
            mCallback = null;
        }

        ExecHandler(WeakReference<Callback> callback) {
            mCallback = callback;
        }

        ExecHandler(Looper looper) {
            super(looper);
            mCallback = null;
        }

        ExecHandler(Looper looper, WeakReference<Callback> callback) {
            super(looper);
            mCallback = callback;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mCallback == null) {
                return;
            }
            final Handler.Callback callback = mCallback.get();
            if (callback == null) { // Already disposed
                return;
            }
            callback.handleMessage(msg);
        }
    }

    private static class WeakRunnable implements Runnable {
        private final WeakReference<Runnable> mDelegate;
        private final WeakReference<ChainedRef> mReference;

        WeakRunnable(WeakReference<Runnable> delegate, WeakReference<ChainedRef> reference) {
            mDelegate = delegate;
            mReference = reference;
        }

        @Override
        public void run() {
            final Runnable delegate = mDelegate.get();
            final ChainedRef reference = mReference.get();
            if (reference != null) {
                reference.remove();
            }
            if (delegate != null) {
                delegate.run();
            }
        }
    }

    private static class ChainedRef {
        @Nullable
        ChainedRef next;
        @Nullable
        ChainedRef prev;
        @NonNull
        final Runnable runnable;
        @NonNull
        final WeakRunnable wrapper;

        @NonNull
        Lock lock;

        public ChainedRef(@NonNull Lock lock, @NonNull Runnable r) {
            this.runnable = r;
            this.lock = lock;
            this.wrapper = new WeakRunnable(new WeakReference<>(r), new WeakReference<>(this));
        }

        public WeakRunnable remove() {
            lock.lock();
            try {
                if (prev != null) {
                    prev.next = next;
                }
                if (next != null) {
                    next.prev = prev;
                }
                prev = null;
                next = null;
            } finally {
                lock.unlock();
            }
            return wrapper;
        }

        public void insertAfter(@NonNull ChainedRef candidate) {
            lock.lock();
            try {
                if (this.next != null) {
                    this.next.prev = candidate;
                }

                candidate.next = this.next;
                this.next = candidate;
                candidate.prev = this;
            } finally {
                lock.unlock();
            }
        }

        @Nullable
        public WeakRunnable remove(Runnable obj) {
            lock.lock();
            try {
                ChainedRef curr = this.next; // Skipping head
                while (curr != null) {
                    if (curr.runnable == obj) { // We do comparison exactly how Handler does inside
                        return curr.remove();
                    }
                    curr = curr.next;
                }
            } finally {
                lock.unlock();
            }
            return null;
        }
    }
}
