package com.liucai.component.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.component.bean.BaseComponentConfig;

public abstract class BaseRelativeLayout extends RelativeLayout {
    public static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    public abstract void init();
    public abstract void initView();

    @NonNull
    protected final Context mContext;
    @Nullable
    protected TypedArray mTa;
    @NonNull
    public BaseComponentConfig config;

    @NonNull
    public int[] setAttrs() {
        return new int[]{};
    }

    public BaseRelativeLayout(@NonNull Context context) {
        this(context, null);
    }

    public BaseRelativeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseRelativeLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BaseRelativeLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        this.config = new BaseComponentConfig();
        initAttr(attrs, defStyleAttr);
        init();
        autoRecycleTypedArray();
        initView();
    }

    private void initAttr(@Nullable AttributeSet attr, int defStyleAttr) {
        int[] styleableArr = setAttrs();
        if (styleableArr != null && styleableArr.length > 0 && attr != null) {
            mTa = mContext.obtainStyledAttributes(attr, styleableArr, defStyleAttr, 0);
        }
    }

    private void autoRecycleTypedArray() {
        if (mTa != null) {
            mTa.recycle();
            mTa = null;
        }
    }
}