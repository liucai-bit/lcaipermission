package com.liucai.component.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.liucai.core.util.text.TextUtils;

/**
 * @author liucai
 * @program lctipsdialog
 * @description
 * @Date 2026/7/24
 */
public abstract class BaseRelativeLayout extends RelativeLayout {
    public static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    public abstract void init();

    public Context mContext;
    public TypedArray mTa;

    public int[] setAttrs() {
        return new int[]{};
    }

    public BaseRelativeLayout(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public BaseRelativeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initAttr(attrs);
        init();
    }

    public BaseRelativeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initAttr(attrs);
        init();
    }

    public BaseRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        initAttr(attrs);
        init();
    }

    private void initAttr(AttributeSet attr) {
        if (setAttrs() != null && setAttrs().length > 0) {
            mTa = mContext.obtainStyledAttributes(attr, setAttrs());
        }
    }

    /**
     * dp转换成px
     * @param dpValue
     * @return
     */
    public int dip2px( float dpValue) {
        if (TextUtils.isEmpty(dpValue + "")) {
            return 0;
        }
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }
    /**
     * px转换成dp
     * @param pxValue
     * @return
     */
    public int px2dip(float pxValue) {
        if (TextUtils.isEmpty(pxValue + "")) {
            return 0;
        }
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pxValue, getResources().getDisplayMetrics());
    }
}
