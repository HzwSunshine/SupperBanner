package com.hzw.supperbanner.SupperBanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hzw.supperbanner.R;

import java.lang.reflect.Field;
import java.util.List;

import static com.bumptech.glide.Glide.with;

/**
 * 超级轮播图
 * Created by hzw on 2016/10/30.
 */

public class SupperBannerView extends FrameLayout implements View.OnTouchListener {

    private static final int PAGER_SIZE_INDEX = 8;
    private static final int MESSAGE_WHAT = 1314;
    private LinearLayout indicatorGroup;
    private BannerHandler bannerHandler;
    private Runnable bannerRunnable;
    private ViewPager pager;
    private int pagerMaxSize;
    //指示器的间距，宽度和高度，宽度必须大于等于高度
    private float indicatorSpace, indicatorWidth, indicatorHeight;
    //指示器的默认颜色和选中颜色
    private int indicatorColor, selectIndicatorColor;
    //广告的展示时间和切换时间
    private int showDuration, pagerChangeDuration;
    //只有一个广告时是否可以滑动,和是否自动轮播
    private boolean onePageCanScroll, isAutoScroll;
    private boolean isHasMessage = true;
    private boolean isFirstMove = true;
    private BannerPagerAdapter adapter;
    private List<String> urls;
    private int width, height;
    private boolean isPagerUpdate;

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
        onePageCanScroll = array.getBoolean(R.styleable.SupperBannerView_onePageCanScroll, false);
        isAutoScroll = array.getBoolean(R.styleable.SupperBannerView_isAutoScroll, true);
        array.recycle();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pager = new ViewPager(getContext());
        this.addView(pager, params);
        FrameLayout.LayoutParams paramsIndicator = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsIndicator.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        paramsIndicator.bottomMargin = (int) dip2px(20);
        indicatorGroup = new LinearLayout(getContext());
        this.addView(indicatorGroup, paramsIndicator);
        pager.setOnTouchListener(this);
        setPagerScrollDuration(pager);
        pager.post(new Runnable() {
            @Override
            public void run() {
                width = pager.getWidth();
                height = pager.getHeight();
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
        if (this.urls.size() == 1) {
            pagerMaxSize = onePageCanScroll ? PAGER_SIZE_INDEX : this.urls.size();
        } else {
            pagerMaxSize = this.urls.size() * PAGER_SIZE_INDEX;
        }
        if (urls.size() != 1 || onePageCanScroll) {
            indicatorGroup.removeAllViews();
            //循环添加指示器
            for (int i = 0; i < this.urls.size(); i++) {
                IndicatorView indicator = new IndicatorView(getContext());
                indicatorGroup.addView(indicator);
                indicator.init(indicatorColor, selectIndicatorColor, indicatorWidth,
                        indicatorHeight, indicatorSpace, pagerChangeDuration);
                indicator.setOffset(i == 0 ? 1 : 0);
            }
        }
        if (adapter == null) {
            adapter = new BannerPagerAdapter();
            pager.setAdapter(adapter);
            pager.addOnPageChangeListener(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        pager.setCurrentItem(this.urls.size(), false);

        if (isAutoScroll && bannerHandler == null) {
            bannerHandler = new BannerHandler();
            bannerRunnable = new Runnable() {
                public void run() {
                    bannerHandler.sendEmptyMessageDelayed(MESSAGE_WHAT, showDuration);
                }
            };
            bannerRunnable.run();
        }
    }

    /**
     * 轮播暂停
     */
    public void pause() {
        if (bannerHandler != null) {
            isHasMessage = false;
            bannerHandler.removeMessages(MESSAGE_WHAT);
        }
    }

    /**
     * 轮播恢复
     */
    public void resume() {
        if (bannerHandler != null && !isHasMessage) {
            isHasMessage = true;
            bannerHandler.sendEmptyMessageDelayed(MESSAGE_WHAT, showDuration);
        }
    }

    /**
     * 取消轮播
     */
    public void cancel() {
        if (bannerHandler != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
            bannerHandler.removeMessages(MESSAGE_WHAT);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == pager) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (isFirstMove && bannerHandler != null) {
                        isFirstMove = false;
                        isHasMessage = false;
                        bannerHandler.removeMessages(MESSAGE_WHAT);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (bannerHandler != null && !isHasMessage) {
                        isFirstMove = true;
                        isHasMessage = true;
                        bannerHandler.sendEmptyMessageDelayed(MESSAGE_WHAT, showDuration);
                    }
                    break;
            }
        }
        return false;
    }

    private class BannerPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
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
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            loadImage(imageView, urls.get(position % urls.size()));
            container.addView(imageView);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.click(v, pager.getCurrentItem() % urls.size());
                    }
                }
            });
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

        private void loadImage(ImageView view, String url) {
            if (width == 0 || height == 0) {
                with(getContext()).load(url).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view);
            } else {
                with(getContext()).load(url).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(width, height)
                        .into(view);
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            position += 1;
            IndicatorView indicator = (IndicatorView) indicatorGroup.getChildAt(position % urls.size());
            IndicatorView indicatorLast = (IndicatorView) indicatorGroup.getChildAt((position % urls.size()) - 1);
            isPagerUpdate = true;
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
            pager.setCurrentItem(index, true);
            bannerHandler.post(bannerRunnable);
            if (!isPagerUpdate) {//如果ViewPager没有更新的话手动更新Indicator
                for (int i = 0; i < urls.size(); i++) {
                    IndicatorView indicator = (IndicatorView) indicatorGroup.getChildAt(i);
                    if (indicator != null) {
                        indicator.setOffset(0);
                    }
                }
            }
            isPagerUpdate = false;
        }
    }

    private void setPagerScrollDuration(ViewPager pager) {
        try {
            Field field = android.support.v4.view.ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            SlowScroller mScroller = new SlowScroller(getContext(), new DecelerateInterpolator());
            mScroller.setDuration(pagerChangeDuration);
            field.set(pager, mScroller);
        } catch (Exception ignored) {
        }
    }

    private class SlowScroller extends Scroller {
        private int mDuration = 1000;

        SlowScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
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


}
