package com.hzw.supperbanner.SupperBanner;

import android.graphics.Paint;
import android.util.Property;

public class ColorPaint extends Paint {
    public final static Property<ColorPaint, Integer> PAINT_COLOR =
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