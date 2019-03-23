package com.cfox.horizontalscrollpickviewdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;


public class HorizontalScrollPickView extends LinearLayout {

    private static final int INVALID_POINTER = -1;
    private Context mContext;
    private Scroller mScroller;
    private int mBeforeIndex;
    private int mSelectedIndex = 0;
    private int mDuration = 320;
    private int mTouchSlop;
    private int mLastMotionX;
    private int mLastMotionY;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDoAction = false;
    private SelectListener mSelectListener;
    private int mHalfScreenSize;
    private Integer[] mWidths;
    boolean mLayoutSuccess = false;
    private PickAdapter mAdapter;

    public interface SelectListener {
        void onSelect(int beforePosition, int position);
    }

    public static abstract class PickAdapter {
        public abstract int getCount();
        public abstract View getPositionView(int position, ViewGroup parent, LayoutInflater inflater);
        public void initView(View view){};
        public void selectView(View view){};
        public void preView(View view){};
    }

    public HorizontalScrollPickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setSelectListener(SelectListener selectListener) {
        this.mSelectListener = selectListener;
    }

    public void setAdapter(PickAdapter adapter) {
        this.mAdapter = adapter;
        if (this.mAdapter == null) return;
        mWidths = new Integer[this.mAdapter.getCount()];
        addViews();
    }

    private void init(Context context) {
        mContext = context;
        mScroller = new Scroller(context, new DecelerateInterpolator());
        setOrientation(LinearLayout.HORIZONTAL);
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        Point displaySize = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(displaySize);
        mHalfScreenSize = displaySize.x / 2;
    }


    private void addViews() {
        if (mAdapter == null) return;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View view = mAdapter.getPositionView(i, this, LayoutInflater.from(mContext));
            final int index = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveToPoint(index);
                }
            });
            addView(view);
        }
    }

    public void setDefaultSelectedIndex(int selectedIndex) {
        this.mSelectedIndex = selectedIndex;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return scrollEvent(ev) || super.dispatchTouchEvent(ev);
    }

    private boolean scrollEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = (int) ev.getX();
                mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                mIsDoAction = false;
                return !super.dispatchTouchEvent(ev);
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                final int x = (int) ev.getX(activePointerIndex);
                final int y = (int) ev.getY(activePointerIndex);
                int deltaX = mLastMotionX - x;
                int deltaY = mLastMotionY - y;
                int absDeltaX = Math.abs(deltaX);
                int absDeltaY = Math.abs(deltaY);
                if (!mIsDoAction && absDeltaX > mTouchSlop && absDeltaX > absDeltaY) {
                    mIsDoAction = true;
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    if (deltaX > 0) {
                        moveRight();
                    } else {
                        moveLeft();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER;
                break;

            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsDoAction;
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
        super.computeScroll();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mLayoutSuccess) return;
        mLayoutSuccess = true;
        int childCount = getChildCount();
        int childLeft;
        int childRight;
        int selectedMode = mSelectedIndex;
        int widthOffset = 0;
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (i < selectedMode) {
                widthOffset += childView.getMeasuredWidth();
            }
        }

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            mWidths[i] = childView.getMeasuredWidth();
            if (i != 0) {
                View preView = getChildAt(i - 1);
                childLeft = preView.getRight();
                childRight = childLeft + childView.getMeasuredWidth();
            } else {
                childLeft = (getWidth() - getChildAt(selectedMode).getMeasuredWidth()) / 2 - widthOffset;
                childRight = childLeft + childView.getMeasuredWidth();
            }
            childView.layout(childLeft, childView.getTop(), childRight, childView.getMeasuredHeight());
            initChildView(childView);
        }

        selectView(getChildAt(selectedMode));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    private void moveLeft() {
        moveToPoint(mSelectedIndex - 1);
    }

    private void moveRight() {
        moveToPoint(mSelectedIndex + 1);
    }

    private void moveToPoint(int index) {
        if (mAdapter == null) return;
        if (index < 0 || index >= mAdapter.getCount() || index == mSelectedIndex) return;
        mBeforeIndex = mSelectedIndex;
        View toView = getChildAt(index);
        int[] screens = new int[2];
        toView.getLocationOnScreen(screens);
        int moveSize = Math.round((screens[0] + mWidths[index] / 2.0F) - mHalfScreenSize);
        mScroller.startScroll(getScrollX(), 0, moveSize, 0, mDuration);
        scrollToNext(mBeforeIndex, index);
        mSelectedIndex = index;
        invalidate();
    }

    private void scrollToNext(int lastIndex, int selectIndex) {
        if (mAdapter == null) return;
        View preView = getChildAt(lastIndex);
        if (preView != null) {
            mAdapter.preView(preView);
        }
        View selectView = getChildAt(selectIndex);
        if (selectView != null) {
            mAdapter.selectView(selectView);
        }

        if (mSelectListener != null) {
            mSelectListener.onSelect(lastIndex, selectIndex);
        }
    }

    private void selectView(View view) {
        if (mAdapter == null || view == null) return;
        mAdapter.selectView(view);
    }

    private void initChildView(View view) {
        if (mAdapter == null || view == null) return;
        mAdapter.initView(view);
    }
}
