package com.hzw.supperbanner.SupperBanner;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * author: hzw
 * time: 2019/1/22 下午5:33
 * description: 循环的ViewPager，解决了RecyclerView嵌套时的一些bug
 */
public class LoopView extends android.support.v4.view.ViewPager {

    private static final int MSG_WHAT = 1314;
    private LoopListener loopListener;
    private LoopHandler handler;
    private Adapter adapter;
    private boolean scrollEnable;
    private boolean isFirstMove;
    private int lastPosition = -1;
    private int lastSelect = -1;
    private int duration;
    private int pageSize;
    private int loopSize;


    public LoopView(@NonNull Context context) {
        this(context, null);
    }

    public LoopView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setPagerScrollDuration();
        duration = 3000;
    }

    public void setLoopData(int loopSize, int duration, boolean onePageScroll,
                            @NonNull LoopListener<? extends LoopHolder> listener) {
        if (loopSize == 0) {
            return;
        }
        duration = duration < 1000 ? 1000 : duration;
        this.duration = duration;
        this.loopSize = loopSize;
        scrollEnable = loopSize != 1 || onePageScroll;
        loopListener = listener;
        //尽量减小ViewPager的item数量
        pageSize = loopSize > 2 ? loopSize * 2 : 6;
        if (adapter == null) {
            adapter = new Adapter();
            setAdapter(adapter);
            addOnPageChangeListener(adapter);
            handler = new LoopHandler(this);
        } else {
            adapter.notifyDataSetChanged();
        }
        setCurrentItem(loopSize, false);
        startLoop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (isFirstMove) {
                    isFirstMove = false;
                    stopLoop();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isFirstMove = true;
                startLoop();
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void stopLoop() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public synchronized void startLoop() {
        if (handler != null && scrollEnable) {
            handler.removeCallbacksAndMessages(null);
            handler.sendEmptyMessageDelayed(MSG_WHAT, duration);
        }
    }

    private static class LoopHandler extends Handler {

        private WeakReference<LoopView> reference;

        LoopHandler(LoopView loopView) {
            reference = new WeakReference<>(loopView);
        }

        @Override
        public void handleMessage(Message msg) {
            LoopView loopView = reference.get();
            if (loopView != null) {
                loopView.callback.handleMessage();
                sendEmptyMessageDelayed(MSG_WHAT, loopView.duration);
            } else {
                removeCallbacksAndMessages(null);
            }
        }
    }

    private interface LoopHandlerCallback {
        void handleMessage();
    }

    private LoopHandlerCallback callback = new LoopHandlerCallback() {
        @Override
        public void handleMessage() {
            int nextIndex = getCurrentItem() + 1;
            if (nextIndex == pageSize - 1) {//最后一个
                nextIndex = loopSize > 2 ? (loopSize - 2) : loopSize;
                setCurrentItem(nextIndex, false);
                nextIndex += 1;
            }
            setCurrentItem(nextIndex, true);
        }
    };

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == GONE) {
            stopLoop();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            startLoop();
        } else {
            stopLoop();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestLayout();
        startLoop();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLoop();
    }

    private class Adapter extends PagerAdapter implements OnPageChangeListener {

        private Queue<LoopHolder> items = new ArrayDeque<>();

        @Override
        public int getCount() {
            return pageSize;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            position = getPosition(position);
            LoopHolder holder = items.poll();
            if (holder == null) {
                holder = loopListener.onCreateItem(container, position);
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loopListener.itemClick(v, getCurrentItem() % loopSize);
                    }
                });
            }
            loopListener.onBindItem(holder, position);
            container.addView(holder.itemView);
            return holder.itemView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
            items.add((LoopHolder) ((View) object).getTag());
        }

        private int getPosition(int position) {
            return position % loopSize;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            loopListener.changeListener(getPosition(position), positionOffset);
        }

        @Override
        public void onPageSelected(int position) {
            position = getPosition(position);
            if (lastSelect != position) {
                loopListener.selectListener(position);
            }
            lastSelect = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void finishUpdate(@NonNull ViewGroup container) {
            int position = getCurrentItem();
            if (position != lastPosition) {
                if (position == pageSize - 1) {//最后一个
                    int index = loopSize == 1 ? loopSize : loopSize - 1;
                    setCurrentItem(index, false);
                } else if (position == 0) {//第一个
                    setCurrentItem(loopSize, false);
                }
            }
            lastPosition = position;
        }
    }

    private void setPagerScrollDuration() {
        try {
            Field field = android.support.v4.view.ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            field.set(this, new SlowScroller(getContext()));
        } catch (Exception ignored) {
        }
    }

    private class SlowScroller extends Scroller {
        private static final int mDuration = 600;

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
    }

    public interface LoopListener<T extends LoopHolder> {
        @NonNull
        T onCreateItem(ViewGroup container, int position);

        void onBindItem(T holder, int position);

        void changeListener(int position, float offset);

        void itemClick(View view, int position);

        void selectListener(int position);
    }

    public abstract static class LoopHolder {
        public View itemView;

        public LoopHolder(@NonNull View itemView) {
            this.itemView = itemView;
            this.itemView.setTag(this);
        }
    }

    //经过内存分析，不调用release()方法并不会引起内存泄漏
    void release() {
        clearOnPageChangeListeners();
        loopListener = null;
        callback = null;
        stopLoop();
        handler = null;
        if (adapter != null) {
            adapter.items.clear();
            adapter.items = null;
            adapter = null;
        }
    }


}
