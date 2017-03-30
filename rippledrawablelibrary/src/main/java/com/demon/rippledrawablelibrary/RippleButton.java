package com.demon.rippledrawablelibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * RippleDrawable 在按钮上的应用(这里以圆角矩形为例)
 *
 * author: demon.zhang
 * time  : 17/3/30 下午5:17
 */

public class RippleButton extends Button {
    public RippleButton(Context context) {
        super(context);
    }

    public RippleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RippleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        RippleDrawable.RippleEffect.applyRippleOfRoundRect(this,
                getResources().getDimensionPixelOffset(R.dimen.round_cornor));
    }
}
