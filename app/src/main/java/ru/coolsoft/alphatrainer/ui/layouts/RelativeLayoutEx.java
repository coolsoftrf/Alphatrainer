package ru.coolsoft.alphatrainer.ui.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by Bobby© on 26.04.2015.
 * Extension of Relative Layout that supports fractional coordinates (relative to its dimensions)
 * and dispatches touch events so that ripple backgrounds animate appropriately
 */
public class RelativeLayoutEx extends RelativeLayout {
    public RelativeLayoutEx(Context context) {
        super(context);
    }

    public RelativeLayoutEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RelativeLayoutEx(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable bcg = getBackground();
            if (bcg != null) {
                bcg.setHotspot(ev.getX(), ev.getY());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public float getXFraction() {
        return getX() / getWidth(); // TODO: guard divide-by-zero
    }

    public void setXFraction(float xFraction) {
        // TODO: cache width
        final int width = getWidth();
        setX((width > 0) ? (xFraction * width) : -9999);
    }

    public float getYFraction() {
        return getY() / getHeight(); // TODO: guard divide-by-zero
    }

    public void setYFraction(float yFraction) {
        // TODO: cache height
        final int height = getHeight();
        setY((height > 0) ? (yFraction * height) : -9999);
    }
}
