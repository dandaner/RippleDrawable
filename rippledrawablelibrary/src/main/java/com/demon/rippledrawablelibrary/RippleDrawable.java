package com.demon.rippledrawablelibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 自定义波纹效果,兼容api >= 14
 * author: demon.zhang
 * time  : 17/2/23 上午10:19
 */
@UiThread
public class RippleDrawable extends Drawable implements View.OnTouchListener {

    private static final String TAG = "RippleDrawable";
    private static final boolean DEBUG = BuildConfig.LOG_ENABLE;

    private Paint mRipplePaint;
    private Path mClipPath = new Path();

    private final RippleAnimImpl mRippleAnim;

    private View mTargetView;
    private Rect mTargetBound;
    private RectF mTargetRectF;

    /**
     * 该标记作用:
     * 1.优化性能
     * 2.如果不设置该标识,那么当mTargetView手动调用setVisibility()的
     * 时候,RippleDrawable会强制重新被显示。
     */
    private boolean mIsTouchMode;
    /**
     * 圆形切割背景半径,只有在RIPPLE_TYPE_CIRCLE类型时生效
     */
    private float mClipCircleRadius;
    /**
     * RoundRect切割背景的Cornor,只有在RIPPLE_TYPE_ROUND_RECT类型时生效
     */
    private float mClipRoundRectCornor;

    /**
     * Ripple cliped by circle
     */
    public static final int RIPPLE_TYPE_CIRCLE = 1;
    /**
     * Ripple cliped by rect
     */
    public static final int RIPPLE_TYPE_RECT = 2;
    /**
     * Ripple cliped by rect with round rect
     */
    public static final int RIPPLE_TYPE_ROUND_RECT = 3;

    private int mRippleType = RIPPLE_TYPE_RECT;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RIPPLE_TYPE_CIRCLE,
            RIPPLE_TYPE_RECT,
            RIPPLE_TYPE_ROUND_RECT
    })
    public @interface RippleType {
    }

    public RippleDrawable(@NonNull View view) {
        attachToView(view);
        getRipplePaint();
        mRippleAnim = new RippleAnimImpl();
        mTargetRectF = new RectF();
    }

    private void attachToView(@NonNull View view) {
        this.mTargetView = view;
        view.setOnTouchListener(this);

        // shut down hardware acc for canvas#clipPath() on sdk<=18 devices.
        // work around for canvas#clipPath() unsupportExp
        // @see https://code.google.com/p/android/issues/detail?id=58737
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        LayerDrawable layerDrawable = new LayerDrawable(view.getBackground() != null ?
                new Drawable[]{view.getBackground(), this} : new Drawable[]{this});
        view.setBackgroundDrawable(layerDrawable);
    }

    private float getMaxRadius() {
        return (float) Math.sqrt(Math.pow(mTargetView.getWidth(), 2) + Math.pow(mTargetView.getHeight(), 2));
    }

    private Paint getRipplePaint() {
        if (mRipplePaint == null) {
            mRipplePaint = new Paint();
            mRipplePaint.setAntiAlias(true);
            mRipplePaint.setStyle(Paint.Style.FILL);
        }
        return mRipplePaint;
    }

    public void setRipplePaintColor(@ColorInt int color) {
        Paint paint = getRipplePaint();
        paint.setColor(color);
    }

    public void setClipCircleRadius(float clipCircleRadius) {
        this.mClipCircleRadius = clipCircleRadius;
    }

    public void setClipRoundRectCornor(float clipRoundRectCornor) {
        this.mClipRoundRectCornor = clipRoundRectCornor;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mIsTouchMode && mTargetView.isEnabled() && mTargetBound != null) {
            final int SAVE_COUNT = canvas.save();
            drawBackground(canvas);
            drawRipple(canvas);
            canvas.restoreToCount(SAVE_COUNT);
        }
    }

    private void drawRipple(Canvas canvas) {
        canvas.drawCircle(mRippleAnim.mCx, mRippleAnim.mCy, mRippleAnim.getRadius(), getRipplePaint());
    }

    private void drawBackground(Canvas canvas) {
        mClipPath.reset();
        switch (mRippleType) {
            case RIPPLE_TYPE_CIRCLE:
                canvas.drawCircle(mTargetBound.centerX(),
                        mTargetBound.centerY(), mClipCircleRadius, mRipplePaint);
                mClipPath.addCircle(mTargetBound.centerX(),
                        mTargetBound.centerY(), mClipCircleRadius, Path.Direction.CW);
                break;
            case RIPPLE_TYPE_RECT:
                canvas.drawRect(mTargetBound, mRipplePaint);
                mClipPath.addRect(mTargetBound.left, mTargetBound.top, mTargetBound.right,
                        mTargetBound.bottom, Path.Direction.CW);
                break;
            case RIPPLE_TYPE_ROUND_RECT:
                mTargetRectF.set(mTargetBound);

                canvas.drawRoundRect(mTargetRectF, mClipRoundRectCornor, mClipRoundRectCornor, mRipplePaint);
                mClipPath.addRoundRect(mTargetRectF, mClipRoundRectCornor, mClipRoundRectCornor, Path.Direction.CW);
                break;
        }
        canvas.clipPath(mClipPath);
    }

    public void setRippleType(@RippleType int rippleType) {
        this.mRippleType = rippleType;
    }

    @Override
    public void setAlpha(int alpha) {
        getRipplePaint().setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        getRipplePaint().setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                mRippleAnim.applyPressAnim(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                mRippleAnim.applyReleaseAnim(checkPosition(event.getX(), event.getY()));
                break;
            case MotionEvent.ACTION_CANCEL:
                mRippleAnim.applyReleaseAnim(false);
                break;
        }
        return true;
    }

    private boolean checkPosition(float x, float y) {
        return mTargetBound != null && mTargetBound.contains((int) x, (int) y);
    }

    private void ensureBound() {
        if (mTargetBound == null && mTargetView.getWidth() > 0) {
            mTargetBound = new Rect(0, 0, mTargetView.getWidth(), mTargetView.getHeight());
            // BUG : 5.0以下的手机,如果设置的mClipRoundRectCornor属性比控件本身高度的一半还大,绘制的背景以及波纹就会被扭曲。
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mClipRoundRectCornor = Math.min(mClipRoundRectCornor, mTargetBound.height() * 0.5f);
            }
        }
    }

    private class RippleAnimImpl {

        private final float INIT_RADIUS = 0;

        private ObjectAnimator mRippleAnim;
        private float mRadius = INIT_RADIUS;
        private float mCx, mCy;

        public void setRadius(float radius) {
            this.mRadius = radius;
            invalidateSelf();
        }

        public float getRadius() {
            return mRadius;
        }

        private void applyPressAnim(float x, float y) {
            if (DEBUG) {
                Log.d(TAG, "applyPressAnim");
            }
            mIsTouchMode = true;
            ensureBound();
            cancel();
            reset();
            this.mCx = x;
            this.mCy = y;
            mRippleAnim = ObjectAnimator.ofFloat(this, "radius", INIT_RADIUS, getMaxRadius());
            mRippleAnim.setDuration(2000);
            mRippleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            mRippleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setRadius((Float) animation.getAnimatedValue());
                }
            });
            mRippleAnim.start();
        }

        private void applyReleaseAnim(final boolean isClickAction) {
            if (DEBUG) {
                Log.d(TAG, "applyReleaseAnim");
            }
            cancel();
            float maxRadius = getMaxRadius();
            if (mRadius < maxRadius) {
                mRippleAnim = ObjectAnimator.ofFloat(this, "radius", mRadius, maxRadius);
                mRippleAnim.setDuration(200);
                mRippleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                mRippleAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mIsTouchMode = false;
                        // must refresh to clean drawable
                        invalidateSelf();
                        performClick(isClickAction);
                    }
                });
                // 理论上不需要设置addUpdateListener,很奇怪,有时候不执行setRadius,所以显示的强制调用
                mRippleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        setRadius((Float) animation.getAnimatedValue());
                    }
                });
                mRippleAnim.start();
            } else {
                mIsTouchMode = false;
                // must refresh to clean drawable
                invalidateSelf();
                performClick(isClickAction);
            }
        }

        private void cancel() {
            if (mRippleAnim != null) {
                mRippleAnim.cancel();
            }
        }

        private void reset() {
            this.mRadius = INIT_RADIUS;
        }

        private void performClick(boolean isClickAction) {
            if (isClickAction && mTargetView != null) {
                mTargetView.performClick();
            }
        }
    }

    /**
     * 默认提供的几种样式模板
     *
     * 分别为Circle , Rect, RoundRect
     */
    public static final class RippleEffect {
        public static RippleDrawable applyRippleOfCircle(@NonNull View target, float circleRadius) {
            RippleDrawable rippleDrawable = new RippleDrawable(target);
            rippleDrawable.setRippleType(RIPPLE_TYPE_CIRCLE);
            rippleDrawable.setClipCircleRadius(circleRadius);
            rippleDrawable.setRipplePaintColor(target.getResources().getColor(R.color.ripple_color));
            return rippleDrawable;
        }

        public static RippleDrawable applyRippleOfRect(@NonNull View target) {
            RippleDrawable rippleDrawable = new RippleDrawable(target);
            rippleDrawable.setRippleType(RIPPLE_TYPE_RECT);
            rippleDrawable.setRipplePaintColor(target.getResources().getColor(R.color.ripple_color));
            return rippleDrawable;
        }

        public static RippleDrawable applyRippleOfRoundRect(@NonNull View target, float roundRectCornor) {
            RippleDrawable rippleDrawable = new RippleDrawable(target);
            rippleDrawable.setRippleType(RIPPLE_TYPE_ROUND_RECT);
            rippleDrawable.setClipRoundRectCornor(roundRectCornor);
            rippleDrawable.setRipplePaintColor(target.getResources().getColor(R.color.ripple_color));
            return rippleDrawable;
        }
    }
}
