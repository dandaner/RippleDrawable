package com.demon.rippledrawablelibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * RippleDrawable 在布局场景的应用
 *
 * author: demon.zhang
 * time  : 17/3/30 下午5:22
 */

public class RippleLayout extends FrameLayout {
    public RippleLayout(Context context) {
        super(context);
    }

    public RippleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RippleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        RippleDrawable.RippleEffect.applyRippleOfRect(this);
    }
}
