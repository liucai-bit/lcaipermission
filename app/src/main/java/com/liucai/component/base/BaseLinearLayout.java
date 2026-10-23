package com.liucai.component.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.component.bean.BaseComponentConfig;

/**
 * 通用自定义 LinearLayout 基类。
 * <p>封装属性读取、资源回收等通用能力，所有子类自动继承无需重复实现。
 *
 * <p>使用约束：
 * <ul>
 *   <li>子类必须实现 {@link #init()} 和 {@link #initView()}</li>
 *   <li>若子类需读取自定义属性，重写 {@link #setAttrs()} 并在 {@link #init()} 中通过 {@link #mTa} 读取。
 *       注意：{@link #mTa} 仅在 {@link #init()} 执行期间有效，执行完毕后会自动回收并置空。</li>
 * </ul>
 *
 * @author liucai
 */
public abstract class BaseLinearLayout extends LinearLayout {

    public static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    /** 初始化配置（读取属性等） */
    public abstract void init();

    /** 初始化视图（addView 等操作） */
    public abstract void initView();

    @NonNull
    protected final Context mContext;

    /** 属性集，仅在 init() 期间可用，init() 结束后会被自动回收置为 null */
    @Nullable
    protected TypedArray mTa;

    @NonNull
    public BaseComponentConfig config;

    /**
     * 子类可重写此方法，返回自身定义的 styleable 属性数组。
     * @return 自定义属性 ID 数组，基类自动完成属性初始化与回收
     */
    @NonNull
    public int[] setAttrs() {
        return new int[]{};
    }

    // ==================== 构造函数 ====================

    public BaseLinearLayout(@NonNull Context context) {
        this(context, null);
    }

    public BaseLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        this.config = new BaseComponentConfig();
        initAttr(attrs, defStyleAttr);
        init();
        autoRecycleTypedArray();
        initView();
    }

    public BaseLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        this.config = new BaseComponentConfig();
        initAttr(attrs, defStyleAttr);
        init();
        autoRecycleTypedArray();
        initView();
    }

    // ==================== 内部逻辑 ====================

    /**
     * 初始化自定义属性 TypedArray。
     */
    private void initAttr(@Nullable AttributeSet attr, int defStyleAttr) {
        int[] styleableArr = setAttrs();
        if (styleableArr != null && styleableArr.length > 0 && attr != null) {
            mTa = mContext.obtainStyledAttributes(attr, styleableArr, defStyleAttr, 0);
        }
    }

    /**
     * 自动回收 TypedArray 资源。
     * <p>子类无需手动调用 recycle。若子类在 init() 中提前读取完毕，也可主动回收，基类这里做了判空兜底。
     */
    private void autoRecycleTypedArray() {
        if (mTa != null) {
            mTa.recycle();
            mTa = null;
        }
    }
}