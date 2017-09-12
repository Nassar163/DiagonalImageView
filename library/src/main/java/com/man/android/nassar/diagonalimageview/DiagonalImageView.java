package com.man.android.nassar.diagonalimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.IntDef;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by nassar on 12/09/2017.
 * <p>
 * Note: if position set NONE mask won't be applied
 * <p>
 * POSITION    DIRECTION
 * <p>
 * TOP         LEFT |  RIGHT
 * BOTTOM      LEFT |  RIGHT
 * LEFT        TOP  |  BOTTOM
 * RIGHT       TOP  |  BOTTOM
 **/


public class DiagonalImageView extends AppCompatImageView {

    public static final String TAG = DiagonalImageView.class.getSimpleName();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, LEFT, RIGHT, TOP, BOTTOM})
    public @interface Position {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LEFT, RIGHT, TOP, BOTTOM})
    public @interface Direction {
    }

    public static final int NONE = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 4;
    public static final int LEFT = 8;

    private final Path mClipPath = new Path();
    private final Path mBorderPath = new Path();

    private final Paint mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Region mClickRegion = new Region();
    private RectF mClickRect = new RectF();

    private int mPosition;
    private int mDirection;
    private int mOverlap;
    private int mBorderColor;
    private int mBorderSize;

    private boolean mBorderEnabled;

    public DiagonalImageView(Context context) {
        super(context);
        init(context, null);
    }

    public DiagonalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        setLayerType(LAYER_TYPE_HARDWARE, null);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DiagonalImageView);
        try {
            mPosition = a.getInteger(R.styleable.DiagonalImageView_di_position, NONE);
            mDirection = a.getInteger(R.styleable.DiagonalImageView_di_direction, RIGHT);
            mOverlap = a.getDimensionPixelSize(R.styleable.DiagonalImageView_di_overlap, 0);
            mBorderSize = a.getDimensionPixelSize(R.styleable.DiagonalImageView_di_borderSize, 0);
            mBorderColor = a.getColor(R.styleable.DiagonalImageView_di_borderColor, Color.BLACK);
            mBorderEnabled = a.getBoolean(R.styleable.DiagonalImageView_di_borderEnabled, false);

            mBorderPaint.setColor(mBorderColor);
            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderPaint.setStrokeWidth(mBorderSize);
        } finally {
            a.recycle();
        }
    }

    public void set(@Position int position, @Direction int direction) {
        if (mPosition != position || mDirection != direction) {
            mClipPath.reset();
            mBorderPath.reset();
        }
        mPosition = position;
        mDirection = direction;
        postInvalidate();
    }

    public void setPosition(@Position int position) {
        if (mPosition != position) {
            mClipPath.reset();
            mBorderPath.reset();
        }
        mPosition = position;
        postInvalidate();
    }

    public void setDirection(@Direction int direction) {
        if (mDirection != direction) {
            mClipPath.reset();
            mBorderPath.reset();
        }
        mDirection = direction;
        postInvalidate();
    }

    public void setBorderEnabled(boolean enabled) {
        mBorderEnabled = enabled;
        postInvalidate();
    }

    public
    @Position
    int getPosition() {
        return mPosition;
    }

    public
    @Direction
    int getDirection() {
        return mDirection;
    }

    public boolean isBorderEnabled() {
        return mBorderEnabled;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mClipPath.isEmpty()) {
            super.onDraw(canvas);
            return;
        }

        int saveCount = canvas.save();
        canvas.clipPath(mClipPath);
        super.onDraw(canvas);
        if (!mBorderPath.isEmpty()) {
            canvas.drawPath(mBorderPath, mBorderPaint);
        }
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!mClipPath.isEmpty()) {
            canvas.clipPath(mClipPath);
        }
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed) {
            return;
        }

        if (mClipPath.isEmpty()) {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();

            if (width <= 0 || height <= 0) {
                return;
            }

            setClipPath(width, height);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mClickRegion.isEmpty()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Point point = new Point();
                    point.x = (int) event.getX();
                    point.y = (int) event.getY();
                    //Log.d(TAG, "point: " + point);
                    if (!mClickRegion.contains(point.x, point.y)) {
                        //Log.d(TAG, "clicked outside");
                        return false;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    private void setClipPath(final int width, final int height) {
        mClipPath.reset();
        mBorderPath.reset();

        switch (mPosition) {
            case TOP:
                if (mDirection == LEFT) {
                    mClipPath.moveTo(0, 0);
                    mClipPath.lineTo(width, mOverlap);
                    mClipPath.lineTo(width, height);
                    mClipPath.lineTo(0, height);

                    if (mBorderEnabled) {
                        mBorderPath.moveTo(0, 0);
                        mBorderPath.lineTo(width, mOverlap);
                    }
                } else {
                    mClipPath.moveTo(0, mOverlap);
                    mClipPath.lineTo(width, 0);
                    mClipPath.lineTo(width, height);
                    mClipPath.lineTo(0, height);

                    if (mBorderEnabled) {
                        mBorderPath.moveTo(0, mOverlap);
                        mBorderPath.lineTo(width, 0);
                    }
                }
                break;
            case RIGHT:
                if (mDirection == TOP) {
                    mClipPath.moveTo(0, 0);
                    mClipPath.lineTo(width, 0);
                    mClipPath.lineTo(width - mOverlap, height);
                    mClipPath.lineTo(0, height);

                    if (mBorderEnabled) {
                        mBorderPath.moveTo(width, 0);
                        mBorderPath.lineTo(width - mOverlap, height);
                    }
                } else {
                    mClipPath.moveTo(0, 0);
                    mClipPath.lineTo(width - mOverlap, 0);
                    mClipPath.lineTo(width, height);
                    mClipPath.lineTo(0, height);

                    if (mBorderEnabled) {
                        mBorderPath.moveTo(width - mOverlap, 0);
                        mBorderPath.lineTo(width, height);
                    }
                }
                break;
            case BOTTOM:
                if (mDirection == LEFT) {
                    mClipPath.moveTo(0, 0);
                    mClipPath.lineTo(width, 0);
                    mClipPath.lineTo(width, height - mOverlap);
                    mClipPath.lineTo(0, height);

                    if (mBorderEnabled) {
                        mBorderPath.moveTo(0, height);
                        mBorderPath.lineTo(width, height - mOverlap);
                    }
                } else {
                    mClipPath.moveTo(0, 0);
                    mClipPath.lineTo(width, 0);
                    mClipPath.lineTo(width, height);
                    mClipPath.lineTo(0, height - mOverlap);

                    if (mBorderEnabled) {
                        mBorderPath.moveTo(0, height - mOverlap);
                        mBorderPath.lineTo(width, height);
                    }
                }
                break;
            case LEFT:
                if (mDirection == TOP) {
                    mClipPath.moveTo(0, 0);
                    mClipPath.lineTo(width, 0);
                    mClipPath.lineTo(width, height);
                    mClipPath.lineTo(mOverlap, height);

                    if (mBorderEnabled) {
                        mBorderPath.moveTo(0, 0);
                        mBorderPath.lineTo(mOverlap, height);
                    }
                } else {
                    mClipPath.moveTo(mOverlap, 0);
                    mClipPath.lineTo(width, 0);
                    mClipPath.lineTo(width, height);
                    mClipPath.lineTo(0, height);

                    if (mBorderEnabled) {
                        mBorderPath.moveTo(mOverlap, 0);
                        mBorderPath.lineTo(0, height);
                    }
                }
                break;
        }

        mClipPath.close();
        mClipPath.computeBounds(mClickRect, true);
        mClickRegion.setPath(mClipPath, new Region((int) mClickRect.left, (int) mClickRect.top, (int) mClickRect.right, (int) mClickRect.bottom));
        mBorderPath.close();
    }
}