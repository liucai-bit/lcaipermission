package com.liucai.component.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.flexbox.FlexboxLayout;
import com.liucai.component.bean.BaseComponentConfig;

public abstract class BaseFlexboxLayout extends FlexboxLayout {

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

    public BaseFlexboxLayout(@NonNull Context context) {
        this(context, null);
    }

    public BaseFlexboxLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseFlexboxLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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