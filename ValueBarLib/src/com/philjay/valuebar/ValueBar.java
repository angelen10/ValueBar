
package com.philjay.valuebar;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.philjay.valuebar.colors.BarColorFormatter;

import java.text.DecimalFormat;

/**
 * ValueBar is a custom View for displaying values in an edgy bar.
 * 
 * @author Philipp Jahoda
 */
public class ValueBar extends View implements AnimatorUpdateListener {

    /** minimum value the bar can display */
    private float mMinVal = 0f;

    /** maximum value the bar can display */
    private float mMaxVal = 100f;

    /** the value the bar currently displays */
    private float mValue = 75f;

    /** space between bar and borders of view */
    private int mOffset = 1;

    private RectF mBar;

    private Paint mBarPaint;
    private Paint mBorderPaint;
    private Paint mTextPaint;
    private Paint mOverlayPaint;

    private ObjectAnimator mAnimator;

    private boolean mDrawBorder = true;
    private boolean mDrawValueText = true;
    private boolean mDrawMinMaxText = true;
    private boolean mTouchEnabled = true;

    private BarColorFormatter mColorFormatter;
    private ValueTextFormatter mValueTextFormatter;

    public ValueBar(Context context) {
        super(context);
        init();
    }

    public ValueBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ValueBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Do all preparations.
     */
    private void init() {

        Utils.init(getResources());

        mBar = new RectF();
        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setStyle(Paint.Style.FILL);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(Utils.convertDpToPixel(2f));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(Utils.convertDpToPixel(18f));

        mOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOverlayPaint.setStyle(Paint.Style.FILL);
        mOverlayPaint.setColor(Color.WHITE);
        mOverlayPaint.setAlpha(120);

        mColorFormatter = new DefaultColorFormatter(Color.rgb(39, 140, 230));
        mValueTextFormatter = new DefaultValueTextFormatter();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        prepareBarSize();

        mBarPaint.setColor(mColorFormatter.getColor(mValue, mMaxVal, mMinVal));

        // draw the value-bar
        canvas.drawRect(mBar, mBarPaint);

        // draw the border
        if (mDrawBorder)
            canvas.drawRect(mOffset, mOffset, getWidth() - mOffset, getHeight() - mOffset,
                    mBorderPaint);

        drawText(canvas);
    }

    /**
     * Draws all text on the ValueBar.
     * 
     * @param canvas
     */
    private void drawText(Canvas canvas) {

        if (mDrawValueText) {

            String text = mValueTextFormatter.getValueText(mValue, mMaxVal, mMinVal);

            float textHeight = Utils.calcTextHeight(mTextPaint, text) * 1.5f;
            float textWidth = Utils.calcTextWidth(mTextPaint, text);

            float x = mBar.right - textHeight / 2f;
            float y = getHeight() / 2f + textWidth / 2f;

            if (x < textHeight)
                x = textHeight;

            // draw overlay
            canvas.drawRect(x - textHeight / 1.5f - textHeight / 2f, 0 + mOffset, mBar.right,
                    getHeight() - mOffset,
                    mOverlayPaint);

            canvas.save();

            canvas.rotate(270, x, y);
            canvas.drawText(text,
                    x,
                    y,
                    mTextPaint);
            canvas.restore();
        }

        if (mDrawMinMaxText) {

        }
    }

    /**
     * Prepares the bar according to the current value.
     */
    private void prepareBarSize() {

        float length = (((float) getWidth() - mOffset * 2f) / (mMaxVal - mMinVal)) * mValue;

        mBar.set(mOffset, mOffset, length - mOffset, getHeight() - mOffset);
    }

    /**
     * Sets the minimum and maximum value the bar can display.
     * 
     * @param min
     * @param max
     */
    public void setMinMax(float min, float max) {
        mMaxVal = max;
        mMinVal = min;
    }

    /**
     * Returns the maximum value the bar can display.
     * 
     * @return
     */
    public float getMax() {
        return mMaxVal;
    }

    /**
     * Returns the minimum value the bar can display.
     * 
     * @return
     */
    public float getMin() {
        return mMinVal;
    }

    /**
     * Sets the actual value the bar displays.
     * 
     * @param value
     */
    public void setValue(float value) {
        mValue = value;
    }

    /**
     * Returns the currently displayed value.
     * 
     * @return
     */
    public float getValue() {
        return mValue;
    }

    /**
     * Returns the bar that represents the value.
     * 
     * @return
     */
    public RectF getBar() {
        return mBar;
    }

    /**
     * Animates the bar from a specific value to a specific value.
     * 
     * @param from
     * @param to
     * @param durationMillis
     */
    public void animate(float from, float to, int durationMillis) {
        mValue = from;
        mAnimator = ObjectAnimator.ofFloat(this, "value", mValue, to);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.setDuration(durationMillis);
        mAnimator.addUpdateListener(this);
        mAnimator.start();
    }

    /**
     * Animates the bar up from it's minimum value to the specified value.
     * 
     * @param to
     * @param durationMillis
     */
    public void animateUp(float to, int durationMillis) {

        mValue = mMinVal;
        mAnimator = ObjectAnimator.ofFloat(this, "value", mValue, to);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.setDuration(durationMillis);
        mAnimator.addUpdateListener(this);
        mAnimator.start();
    }

    /**
     * Animates the bar down from it's current value to the specified value.
     * 
     * @param to
     * @param durationMillis
     */
    public void animateDown(float to, int durationMillis) {

        mAnimator = ObjectAnimator.ofFloat(this, "value", mValue, to);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.setDuration(durationMillis);
        mAnimator.addUpdateListener(this);
        mAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator va) {
        invalidate();
    }

    /**
     * Set this to true to enable drawing the border around the bar, or false to
     * disable it.
     * 
     * @param enabled
     */
    public void setDrawBorder(boolean enabled) {
        mDrawBorder = enabled;
    }

    /**
     * Sets the width of the border around the bar (if drawn).
     * 
     * @param width
     */
    public void setBorderWidth(float width) {
        mBorderPaint.setStrokeWidth(width);
    }

    /**
     * Sets the color of the border around the bar (if drawn).
     * 
     * @param color
     */
    public void setBorderColor(int color) {
        mBorderPaint.setColor(color);
    }

    /**
     * Sets a custom formatter that formats the value-text. Provide null to
     * reset all changes and use the default formatter.
     * 
     * @param formatter
     */
    public void setValueTextFormatter(ValueTextFormatter formatter) {

        if (formatter == null)
            formatter = new DefaultValueTextFormatter();
        mValueTextFormatter = formatter;
    }

    /**
     * Sets a custom BarColorFormatter for the ValueBar. Implement the
     * BarColorFormatter interface in your own formatter class and return
     * whatever color you like from the getColor(...) method. You can for
     * example make the color depend on the current value of the bar. Provide
     * null to reset all changes.
     * 
     * @param formatter
     */
    public void setColorFormatter(BarColorFormatter formatter) {

        if (formatter == null)
            formatter = new DefaultColorFormatter(Color.rgb(39, 140, 230));
        mColorFormatter = formatter;
    }

    /**
     * Sets the color the ValueBar should have.
     * 
     * @param color
     */
    public void setColor(int color) {
        mColorFormatter = new DefaultColorFormatter(color);
    }

    /**
     * Returns the paint object that is used for drawing the bar.
     * 
     * @return
     */
    public Paint getBarPaint() {
        return mBarPaint;
    }

    /**
     * Set an offset in pixels that defines the space that is left between the
     * bar and the borders of the View. Default: 1.
     * 
     * @param offsetPx
     */
    public void setOffset(int offsetPx) {

        if (offsetPx < 0)
            offsetPx = 0;
        mOffset = offsetPx;
    }

    /**
     * Set this to true to enable touch gestures on the ValueBar.
     * 
     * @param enabled
     */
    public void setTouchEnabled(boolean enabled) {
        mTouchEnabled = enabled;
    }

    public void setDrawValueText(boolean enabled) {
        mDrawValueText = enabled;
    }

    public void setDrawMinMaxText(boolean enabled) {
        mDrawMinMaxText = enabled;
    }

    /**
     * Sets a GestureDetector for the ValueBar to receive callbacks on gestures.
     * 
     * @param gd
     */
    public void setGestureDetector(GestureDetector gd) {
        mGestureDetector = gd;
    }

    /**
     * Sets a selectionlistener for callbacks when selecting values on the
     * ValueBar.
     * 
     * @param l
     */
    public void setValueBarSelectionListener(ValueBarSelectionListener l) {
        mSelectionListener = l;
    }

    /** listener called when a value has been selected on touch */
    private ValueBarSelectionListener mSelectionListener;

    /** gesturedetector for recognizing single-taps */
    private GestureDetector mGestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mTouchEnabled) {

            if (mSelectionListener == null)
                Log.w("ValueBar",
                        "No SelectionListener specified. Use setSelectionListener(...) to set a listener for callbacks when selecting values.");

            // if the detector recognized a gesture, consume it
            if (mGestureDetector != null && mGestureDetector.onTouchEvent(e))
                return true;

            float x = e.getX();
            float y = e.getY();

            if (x > mOffset && x < getWidth() - mOffset) {

                switch (e.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        updateValue(x, y);
                        invalidate();
                    case MotionEvent.ACTION_MOVE:
                        updateValue(x, y);
                        invalidate();
                        if (mSelectionListener != null)
                            mSelectionListener.onSelectionUpdate(mValue, mMaxVal, mMinVal, this);
                        break;
                    case MotionEvent.ACTION_UP:
                        updateValue(x, y);
                        invalidate();
                        if (mSelectionListener != null)
                            mSelectionListener.onValueSelected(mValue, mMaxVal, mMinVal, this);
                        break;
                }
            }

            return true;
        }
        else
            return super.onTouchEvent(e);
    }

    /**
     * Updates the value on the ValueBar depending on the touch position.
     * 
     * @param x
     * @param y
     */
    private void updateValue(float x, float y) {

        float factor = (x - mOffset) / (getWidth() - mOffset * 2f);

        mValue = mMaxVal * factor;
    }

    /**
     * Default BarColorFormatter class that supports a single color.
     * 
     * @author Philipp Jahoda
     */
    private class DefaultColorFormatter implements BarColorFormatter {

        private int mColor;

        public DefaultColorFormatter(int color) {
            mColor = color;
        }

        @Override
        public int getColor(float value, float maxVal, float minVal) {
            return mColor;
        }
    }

    /**
     * Default ValueTextFormatter that simply returns the value as a string.
     * 
     * @author Philipp Jahoda
     */
    private class DefaultValueTextFormatter implements ValueTextFormatter {

        private DecimalFormat mFormat;

        public DefaultValueTextFormatter() {
            mFormat = new DecimalFormat("###,###,##0.00");
        }

        @Override
        public String getValueText(float value, float maxVal, float minVal) {
            return mFormat.format(value);
        }
    }
}
