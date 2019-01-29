package com.hzw.supperbanner.SupperBanner;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

/**
 * Banner指示器的示例
 */
public class IndicatorView extends View {

    private int indicatorColor, selectIndicatorColor;
    private ColorPaint paint = new ColorPaint();
    private RectF rectF = new RectF();
    private ObjectAnimator mAnimator;
    private Path path = new Path();
    private int pageChangeDuration;
    private float height, width;

    public IndicatorView(Context context) {
        super(context);
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(int indicatorColor, int selectIndicatorColor, float width,
                     float height, float indicatorSpace, int pageChangeDuration) {
        this.indicatorColor = indicatorColor;
        this.selectIndicatorColor = selectIndicatorColor;
        this.width = width;
        this.height = height;
        this.pageChangeDuration = pageChangeDuration;
        paint.setColor(indicatorColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
        params.leftMargin = (int) (indicatorSpace / 2);
        params.rightMargin = (int) (indicatorSpace / 2);
        setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((int) width, (int) height);
    }

    public void setOffset(float offset) {
        //偏移量在0到1之间
        offset = offset > 1 ? 1 : offset;
        offset = offset < 0 ? 0 : offset;

        float x = (width - height) * (1 - offset) / 2.0f;
        path.reset();
        rectF.set(x, 0, height + x, height);
        path.arcTo(rectF, 90, 180);
        rectF.set(width - height - x, 0, width - x, height);
        path.arcTo(rectF, 270, 180);
        path.close();
        colorAnim((int) (pageChangeDuration * offset));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    private void colorAnim(int currentTime) {
        if (mAnimator == null) {
            mAnimator = ObjectAnimator.ofInt(paint, paint.PAINT_COLOR, indicatorColor, selectIndicatorColor);
            mAnimator.setEvaluator(new ArgbEvaluator());
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.setDuration(pageChangeDuration);
        }
        mAnimator.setCurrentPlayTime(currentTime);
    }

    private class ColorPaint extends Paint {
        final Property<ColorPaint, Integer> PAINT_COLOR =
                new Property<ColorPaint, Integer>(Integer.class, "PaintColor") {
                    @Override
                    public Integer get(ColorPaint object) {
                        return object.getColor();
                    }

                    @Override
                    public void set(ColorPaint object, Integer value) {
                        object.setColor(value);
                    }
                };
    }

}
