package com.liucai.component.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public abstract class BaseTextView extends AppCompatTextView {

    public abstract void init();
    public abstract void initView();

    @NonNull
    protected Context mContext;
    @Nullable
    protected TypedArray mTa;

    @NonNull
    public int[] setAttrs() {
        return new int[]{};
    }

    public BaseTextView(@NonNull Context context) {
        this(context, null);
    }

    public BaseTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
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