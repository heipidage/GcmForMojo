package com.swjtu.gcmformojo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Created by kevin on 2017/2/19.
 */

public class CurrentUserListView extends ListView {
    private static final String TAG = "CurrentUserListView";

    private int mDownX;
    private int mDownY;
    private int mScreenWidth;
    private boolean isDeleteShow;
    private ViewGroup  mCurrentView;
    private int mDeleteWidth;
    private LinearLayout.LayoutParams mItemParam;
    private boolean isAllowClick;
    private ViewGroup clickView;

    public CurrentUserListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScreenWidth(context);
    }

    private void initScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(ev);
                break;
            case MotionEvent.ACTION_UP:
                actionUp(ev);
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void actionDown(MotionEvent event) {
        mDownX = (int) event.getX();
        mDownY = (int) event.getY();
        clickView = (ViewGroup) getChildAt(pointToPosition(mDownX, mDownY) - getFirstVisiblePosition());
        if (clickView == null) {
            return;
        }
        if (isDeleteShow) {
            if (!clickView.equals(mCurrentView)) {
                hideDelete();
            }
        }

        mCurrentView = (ViewGroup) getChildAt(pointToPosition(mDownX, mDownY) - getFirstVisiblePosition());
        mDeleteWidth = mCurrentView.getChildAt(1).getLayoutParams().width;
        mItemParam = (LinearLayout.LayoutParams) mCurrentView.getChildAt(0).getLayoutParams();
        mItemParam.width = mScreenWidth;
        mCurrentView.getChildAt(0).setLayoutParams(mItemParam);
    }

    private void actionMove(MotionEvent event) {
        int nowX = (int) event.getX();
        int nowY = (int) event.getY();
        int disX = nowX - mDownX;
        int disY = nowY - mDownY;

        if (clickView == null) {
            return;
        }

        if (Math.abs(disX) > Math.abs(disY) && Math.abs(disY) < 20) {
            if (isDeleteShow && nowX > mDownX) {
                if (disX >= mDeleteWidth) {
                    disX = mDeleteWidth;
                }
                mItemParam.leftMargin = disX - mDeleteWidth;
                mCurrentView.getChildAt(0).setLayoutParams(mItemParam);
                isAllowClick = false;
            } else if (!isDeleteShow && nowX < mDownX) {
                if (-disX >= mDeleteWidth) {
                    disX = -mDeleteWidth;
                }
                mItemParam.leftMargin = disX;
                mCurrentView.getChildAt(0).setLayoutParams(mItemParam);
                isAllowClick = false;
            }
        }
    }

    private void actionUp(MotionEvent event) {
        if (clickView == null) {
            return;
        }

        if (-mItemParam.leftMargin > mDeleteWidth / 2) {
            mItemParam.leftMargin = -mDeleteWidth;
            mCurrentView.getChildAt(0).setLayoutParams(mItemParam);
            isDeleteShow = true;
        } else {
            hideDelete();
        }
    }

    public void hideDelete() {
        mItemParam.leftMargin = 0;
        mCurrentView.getChildAt(0).setLayoutParams(mItemParam);
        isDeleteShow = false;
    }

    public boolean isAllowClick() {
        return isAllowClick;
    }
}
