package com.hzw.supperbanner.SupperBanner;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * 功能：
 * Created by 何志伟 on 2017/9/5.
 */

class ZoomTransformer implements ViewPager.PageTransformer {

    private float scale, delay;

    ZoomTransformer(float scale) {
        this.scale = scale;
        delay = 1 - scale;
    }

    @Override
    public void transformPage(View page, float position) {
        if (position < -1 || position > 1) {
            page.setScaleX(scale);
            page.setScaleY(scale);
        } else if (position <= 1) { // [-1,1]
            if (position < 0) {
                float scale = 1 + delay * position;
                page.setScaleX(scale);
                page.setScaleY(scale);
            } else {
                float scale = 1 - delay * position;
                page.setScaleX(scale);
                page.setScaleY(scale);
            }
        }
    }
}
