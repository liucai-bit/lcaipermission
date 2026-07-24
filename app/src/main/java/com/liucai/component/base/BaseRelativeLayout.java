package com.liucai.component.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
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

    public abstract void initView();

    @NonNull
    protected final Context mContext;
    @Nullable
    protected TypedArray mTa;

    /**
     * 子类可重写此方法，返回自身定义的styleable属性数组
     * @return 自定义属性ID数组，基类自动完成属性初始化
     */
    @NonNull
    public int[] setAttrs() {
        return new int[]{};
    }

    public BaseRelativeLayout(Context context) {
        super(context);
        this.mContext = context;
        init();
        initView();
    }

    public BaseRelativeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initAttr(attrs);
        init();
        autoRecycleTypedArray();
        initView();
    }

    public BaseRelativeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initAttr(attrs);
        init();
        autoRecycleTypedArray();
        initView();
    }

    public BaseRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        initAttr(attrs);
        init();
        autoRecycleTypedArray();
        initView();
    }

    /**
     * 初始化自定义属性TypedArray
     * @param attr 布局传入的属性集
     */
    private void initAttr(@Nullable AttributeSet attr) {
        int[] styleableArr = setAttrs();
        if (styleableArr != null && styleableArr.length > 0 && attr != null) {
            mTa = mContext.obtainStyledAttributes(attr, styleableArr);
        }
    }

    /**
     * 自动回收TypedArray资源，子类无需手动调用recycle，完全避免内存泄漏
     * 子类如果需要提前在init中读取属性，读取完成后可主动调用recycle
     */
    private void autoRecycleTypedArray() {
        if (mTa != null) {
            mTa.recycle();
            mTa = null;
        }
    }

    /**
     * dp单位转px单位，自动适配当前设备屏幕密度
     * @param dpValue 输入dp数值
     * @return 转换后的像素值，非法输入默认返回0
     */
    public int dip2px(float dpValue) {
        if (dpValue <= 0) return 0;
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpValue,
                getResources().getDisplayMetrics()
        );
    }

    /**
     * px单位转dp单位，适配不同屏幕密度下的数值还原
     * @param pxValue 输入像素数值
     * @return 转换后的dp数值，非法输入默认返回0
     */
    public int px2dip(float pxValue) {
        if (pxValue <= 0) return 0;
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                pxValue,
                getResources().getDisplayMetrics()
        );
    }

    /**
     * 生命周期回调，组件销毁时自动释放所有持有的资源
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 二次兜底回收TypedArray，彻底避免内存泄漏
        if (mTa != null) {
            mTa.recycle();
            mTa = null;
        }
    }
}
