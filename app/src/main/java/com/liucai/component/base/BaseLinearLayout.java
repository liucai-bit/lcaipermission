package com.liucai.component.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.component.bean.BaseComponentConfig;
import com.liucai.core.util.common.CommonUtils;

/**
 * @author liucai
 * @program lcpermission
 * @description 通用自定义LinearLayout基类
 * 封装属性读取、单位转换等通用能力，所有子类自动继承无需重复实现
 * @Date 2026/7/23
 */
public abstract class BaseLinearLayout extends LinearLayout {

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

    public BaseLinearLayout(@NonNull Context context) {
        super(context);
        this.mContext = context;
        config = new BaseComponentConfig();
        initLayout();
        init();
        initView();
    }

    public BaseLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        config = new BaseComponentConfig();
        initLayout();
        initAttr(attrs);
        init();
        autoRecycleTypedArray();
        initView();
    }

    public BaseLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        config = new BaseComponentConfig();
        initLayout();
        initAttr(attrs);
        init();
        autoRecycleTypedArray();
        initView();
    }

    public BaseLinearLayout(@NonNull Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        config = new BaseComponentConfig();
        initLayout();
        initAttr(attrs);
        init();
        autoRecycleTypedArray();
        initView();
    }

    private void initLayout() {
        setLayoutParams(new LayoutParams(MP, WC));
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
    public int dip2px(int dpValue) {
        return CommonUtils.dip2px(mContext, dpValue);
    }

    /**
     * px单位转dp单位，适配不同屏幕密度下的数值还原
     * @param pxValue 输入像素数值
     * @return 转换后的dp数值，非法输入默认返回0
     */
    public int px2dip(int pxValue) {
        return CommonUtils.px2dip(mContext,pxValue);
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
