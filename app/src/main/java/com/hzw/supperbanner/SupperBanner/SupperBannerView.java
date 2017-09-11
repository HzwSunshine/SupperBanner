package com.hzw.supperbanner.SupperBanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.hzw.supperbanner.R;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 超级轮播图
 * Created by hzw on 2016/10/30.
 */

public class SupperBannerView extends FrameLayout implements View.OnTouchListener {

    private static final int PAGER_SIZE_INDEX = 4;
    private static final int MESSAGE_WHAT = 1314;
    private LinearLayout indicatorGroup;
    private BannerHandler bannerHandler;
    private ViewPager pager;
    private int pagerMaxSize;
    //指示器的间距，宽度和高度，宽度必须大于等于高度
    private float indicatorSpace, indicatorWidth, indicatorHeight;
    //指示器的默认颜色和选中颜色
    private int indicatorColor, selectIndicatorColor;
    //广告的展示时间和切换时间
    private int showDuration, pagerChangeDuration;
    //是否显示指示器,是否自动轮播
    private boolean isShowIndicator, isAutoScroll;
    private boolean isFirstMove = true;
    private BannerPagerAdapter adapter;
    private List<String> urls;
    private ArraySet<ImageView> destroyViews = new ArraySet<>();

    private boolean isClip;
    private float clipWidth;
    private float clipSpace;
    private float clipPercent;

    public SupperBannerView(Context context) {
        super(context);
        init(null, 0);
    }

    public SupperBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SupperBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.SupperBannerView, defStyleAttr, 0);
        indicatorColor = array.getColor(R.styleable.SupperBannerView_indicatorColor, Color.GRAY);
        selectIndicatorColor = array.getColor(R.styleable.SupperBannerView_selectIndicatorColor, Color.RED);
        indicatorSpace = array.getDimension(R.styleable.SupperBannerView_indicatorSpace, dip2px(2));
        indicatorWidth = array.getDimension(R.styleable.SupperBannerView_indicatorWidth, dip2px(21));
        indicatorHeight = array.getDimension(R.styleable.SupperBannerView_indicatorHeight, dip2px(7));
        showDuration = array.getInteger(R.styleable.SupperBannerView_showDuration, 3000);
        pagerChangeDuration = array.getInteger(R.styleable.SupperBannerView_pagerChangeDuration, 500);
        isShowIndicator = array.getBoolean(R.styleable.SupperBannerView_isShowIndicator, true);
        isAutoScroll = array.getBoolean(R.styleable.SupperBannerView_isAutoScroll, true);
        isClip = array.getBoolean(R.styleable.SupperBannerView_isClipLayout, false);
        if (isClip) {
            clipWidth = array.getDimension(R.styleable.SupperBannerView_clipWidth, 0);
            clipSpace = array.getDimension(R.styleable.SupperBannerView_clipSpace, dip2px(20));
            clipPercent = array.getFloat(R.styleable.SupperBannerView_clipPercent, 0.7f);
        }
        array.recycle();

        pager = new BannerPager(getContext());
        this.addView(pager);
        initClipLayout();
        LayoutParams paramsIndicator = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsIndicator.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        paramsIndicator.bottomMargin = (int) dip2px(20);
        indicatorGroup = new LinearLayout(getContext());
        this.addView(indicatorGroup, paramsIndicator);
        pager.setOnTouchListener(this);
        setPagerScrollDuration(pager);
    }

    private void initClipLayout() {
        if (!isClip) return;
        setClipChildren(false);
        pager.setClipChildren(false);
        setClipWidth((int) clipWidth);
    }

    public void setClipWidth(final int clipWidth) {
        if (clipWidth == 0) return;
        this.clipWidth = clipWidth;
        LayoutParams params = (LayoutParams) pager.getLayoutParams();
        params.gravity = Gravity.CENTER;
        params.width = clipWidth;
        params.height = LayoutParams.MATCH_PARENT;
        pager.setLayoutParams(params);
        float space = clipWidth * (1 - clipPercent) / 2;
        clipSpace = clipSpace - space;
        pager.setPageMargin((int) clipSpace);
        pager.setPageTransformer(true, new ZoomTransformer(clipPercent));
        pager.post(new Runnable() {
            @Override
            public void run() {
                int width = getWidth();
                float temp = width * 1.0f / clipWidth;
                int num = width / clipWidth;
                num = temp > num ? num + 1 : num;
                pager.setOffscreenPageLimit(num);
            }
        });
    }

    /**
     * 启动轮播
     *
     * @param urls 轮播的URL
     */
    public void start(List<String> urls) {
        if (urls.size() == 0) return;
        this.urls = urls;
        pagerMaxSize = this.urls.size() * PAGER_SIZE_INDEX;
        //指示器显示，或者图片个数不小于1时，显示指示器
        if (isShowIndicator && this.urls.size() != 1) {
            indicatorGroup.removeAllViews();
            //循环添加指示器
            for (int i = 0; i < this.urls.size(); i++) {
                IndicatorView indicator = new IndicatorView(getContext());
                indicatorGroup.addView(indicator);
                indicator.init(indicatorColor, selectIndicatorColor, indicatorWidth,
                        indicatorHeight, indicatorSpace, pagerChangeDuration);
                indicator.setOffset(i == 0 ? 1 : 0);
            }
        } else {
            indicatorGroup.removeAllViews();
        }
        if (adapter == null) {
            adapter = new BannerPagerAdapter();
            pager.setAdapter(adapter);
            pager.addOnPageChangeListener(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        pager.setCurrentItem(this.urls.size(), false);
        if (loadListener != null) loadListener.currentPage(0);
        if (isAutoScroll && bannerHandler == null) {
            bannerHandler = new BannerHandler();
            resume();
        }
    }


    /**
     * 轮播暂停
     */
    public void pause() {
        if (bannerHandler != null) {
            bannerHandler.removeMessages(MESSAGE_WHAT);
        }
    }

    /**
     * 轮播恢复
     */
    public void resume() {
        if (bannerHandler != null) {
            bannerHandler.removeMessages(MESSAGE_WHAT);
            bannerHandler.sendEmptyMessageDelayed(MESSAGE_WHAT, showDuration);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == pager) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (isFirstMove) {
                        pause();
                        isFirstMove = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isFirstMove = true;
                    resume();
                    break;
            }
        }
        return false;
    }

    private class BannerPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
        private int lastPosition = -1;

        @Override
        public int getCount() {
            return pagerMaxSize;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = getContent(position);
            container.addView(imageView);
            return imageView;
        }

        private ImageView getContent(int position) {
            ImageView imageView;
            if (destroyViews.size() > 0) {
                imageView = destroyViews.valueAt(0);
                destroyViews.remove(imageView);
            } else {
                imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (clickListener != null) {
                            clickListener.click(v, pager.getCurrentItem() % urls.size());
                        }
                    }
                });
            }
            loadImage(imageView, position % urls.size());
            return imageView;
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
            int position = pager.getCurrentItem();
            if (position == 0) {
                position = urls.size();
                pager.setCurrentItem(position, false);
            } else if (position == pagerMaxSize - 1) {
                position = urls.size() - 1;
                pager.setCurrentItem(position, false);
            }
        }

        private void loadImage(ImageView view, int position) {
            if (loadListener != null) loadListener.loadImage(view, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            destroyViews.add((ImageView) object);
            container.removeView((View) object);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            position += 1;
            IndicatorView indicator = (IndicatorView) indicatorGroup.getChildAt(position % urls.size());
            IndicatorView indicatorLast = (IndicatorView) indicatorGroup.getChildAt((position % urls.size()) - 1);
            if (indicator != null) {
                indicator.setOffset(positionOffset);
            }
            if (indicatorLast != null) {
                indicatorLast.setOffset(1 - positionOffset);
            } else {
                indicatorLast = (IndicatorView) indicatorGroup.getChildAt(urls.size() - 1);
                if (indicatorLast != null) {
                    indicatorLast.setOffset(1 - positionOffset);
                }
            }
        }

        @Override
        public void onPageSelected(int position) {
            position = position % urls.size();
            if (loadListener != null && lastPosition != position) {
                loadListener.currentPage(position);
                lastPosition = position;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    private class BannerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int index = pager.getCurrentItem() + 1;
            if (index == pagerMaxSize - 1) {
                index = urls.size() * 2 - 2;
                pager.setCurrentItem(index, false);
                index += 1;
            }
            pager.setCurrentItem(index, true);
            bannerHandler.sendEmptyMessageDelayed(MESSAGE_WHAT, showDuration);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == GONE) pause();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            resume();
        } else {
            pause();
        }
    }

    private class BannerPager extends ViewPager {
        private boolean isFirst = true;

        public BannerPager(Context context) {
            super(context);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (getAdapter() != null && !isFirst) {
                try {
                    Field field = android.support.v4.view.ViewPager
                            .class.getDeclaredField("mFirstLayout");
                    field.setAccessible(true);
                    field.set(this, false);
                } catch (Exception ignored) {
                }
            }
            if (getWidth() != 0) isFirst = false;
        }
    }

    private void setPagerScrollDuration(ViewPager pager) {
        try {
            Field field = android.support.v4.view.ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            SlowScroller mScroller = new SlowScroller(getContext());
            mScroller.setDuration(pagerChangeDuration);
            field.set(pager, mScroller);
        } catch (Exception ignored) {
        }
    }

    private class SlowScroller extends Scroller {
        private int mDuration = 1000;

        SlowScroller(Context context) {
            super(context);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        void setDuration(int duration) {
            mDuration = duration;
        }
    }

    private float dip2px(float dipValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }

    public interface ItemClickListener {
        void click(View view, int position);
    }

    private ItemClickListener clickListener;

    public void setItemClickListener(ItemClickListener listener) {
        clickListener = listener;
    }

    public interface ImageLoadListener {
        void loadImage(ImageView imageView, int position);

        void currentPage(int position);
    }

    private ImageLoadListener loadListener;

    public void setImageLoadListener(ImageLoadListener loadListener) {
        this.loadListener = loadListener;
    }


}