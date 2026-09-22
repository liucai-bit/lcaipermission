package com.liucai.component.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.liucai.component.bean.BaseComponentConfig;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/9/18
 */
public abstract class BaseRecyclerView extends RecyclerView {

    public static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    public abstract void init();

    public abstract void initView();

    @NonNull
    protected final Context mContext;
    @Nullable
    protected TypedArray mTa;

    public BaseComponentConfig config;

    /**
     * 子类可重写此方法，返回自身定义的styleable属性数组
     * @return 自定义属性ID数组，基类自动完成属性初始化
     */
    @NonNull
    public int[] setAttrs() {
        return new int[]{};
    }

    public BaseRecyclerView(@NonNull Context context) {
        super(context);
        this.mContext = context;
        config = new BaseComponentConfig();
        initLayout();
        init();
        initView();
    }

    public BaseRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        config = new BaseComponentConfig();
        initLayout();
        initAttr(attrs);
        init();
        autoRecycleTypedArray();
        initView();
    }

    public BaseRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        config = new BaseComponentConfig();
        initLayout();
        initAttr(attrs);
        init();
        autoRecycleTypedArray();
        initView();
    }
    private void initLayout() {
        setLayoutParams(new LinearLayout.LayoutParams(MP, WC));
    }

    /**
     * 初始化自定义属性TypedArray
     * @param attr 布局传入的属性集
     */
    private void initAttr(@Nullable AttributeSet attr) {
        int[] styleableArr = setAttrs();
        if (styleableArr.length > 0 && attr != null) {
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
