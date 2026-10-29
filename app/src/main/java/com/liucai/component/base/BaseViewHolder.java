package com.liucai.component.base;

import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 通用 RecyclerView ViewHolder。
 *
 * <p>职责：
 * <ul>
 *   <li>缓存 itemView 的子控件（{@link #getView(int)}），避免重复 findViewById</li>
 *   <li>提供常用 setter 便捷方法（setText / setImageResource / setVisibility ...）</li>
 *   <li>提供 @NonNull 版本 {@link #requireView(int)}，用于业务确认必存在的控件</li>
 * </ul>
 *
 * <p>使用约束：
 * <ul>
 *   <li>所有方法仅在主线程调用（RecyclerView 的约束）</li>
 *   <li>若 itemView 的子 View 结构发生动态变更（addView / removeView），
 *       需调用 {@link #clearViewCache()} 清缓存</li>
 * </ul>
 *
 * @author liucai
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {

    /** int -> View 缓存，SparseArray 避免 Integer 装箱开销 */
    @NonNull
    private final SparseArray<View> viewCache = new SparseArray<>();

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setTag(this);
    }

    // ==================== 原 API 兼容 ====================

    /**
     * 原 API 兼容：返回自身。
     * <p>该方法是历史遗留，等价于 {@code return this}，如果需要从 View 反查请用 {@link #of(View)}。
     */
    @NonNull
    public BaseViewHolder getViewHolder() {
        return this;
    }

    /** 原 API 兼容。等价于 {@link #itemView}。 */
    @NonNull
    public View getmConvertView() {
        return itemView;
    }

    /**
     * 从 itemView 的 tag 反查 ViewHolder（业务侧一般不需要）。
     * <p>返回 null 表示该 View 不是由 BaseViewHolder 包装的。
     */
    @Nullable
    public static BaseViewHolder of(@Nullable View itemView) {
        if (itemView == null) {
            return null;
        }
        Object tag = itemView.getTag();
        return tag instanceof BaseViewHolder ? (BaseViewHolder) tag : null;
    }

    // ==================== 核心：View 缓存 ====================

    /**
     * 从缓存中获取控件，避免重复 findViewById。
     * <p>找不到返回 null，<b>不缓存 null</b>，避免下次继续重复查找。
     * <p>业务确定控件一定存在时，优先用 {@link #requireView(int)}。
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends View> T getView(@IdRes int viewId) {
        View view = viewCache.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            if (view != null) {
                viewCache.put(viewId, view);
            }
        }
        return (T) view;
    }

    /**
     * 获取控件，找不到直接抛 {@link IllegalStateException}。
     * <p>适用于业务逻辑上"控件必须存在"的场景，尽早暴露 XML 问题。
     */
    @NonNull
    public <T extends View> T requireView(@IdRes int viewId) {
        T view = getView(viewId);
        if (view == null) {
            throw new IllegalStateException(
                    "View (id=" + viewId + ") not found in "
                            + itemView.getClass().getSimpleName());
        }
        return view;
    }

    /**
     * 清空缓存。
     * <p>仅在 itemView 子视图结构发生动态变更（addView / removeView 等）后需要调用；
     * 常规使用无需调用。
     */
    public void clearViewCache() {
        viewCache.clear();
    }

    // ==================== 文本相关 ====================

    /** 设置文本；传 null 等价于清空。 */
    @NonNull
    public BaseViewHolder setText(@IdRes int viewId, @Nullable CharSequence text) {
        TextView tv = getView(viewId);
        if (tv != null) {
            tv.setText(text);
        }
        return this;
    }

    /**
     * 设置文本资源。
     */
    @NonNull
    public BaseViewHolder setTextRes(@IdRes int viewId, @StringRes int textResId) {
        TextView tv = getView(viewId);
        if (tv != null) {
            tv.setText(textResId);
        }
        return this;
    }

    @NonNull
    public BaseViewHolder setTextColor(@IdRes int viewId, @ColorInt int color) {
        TextView tv = getView(viewId);
        if (tv != null) {
            tv.setTextColor(color);
        }
        return this;
    }

    /** 设置文字大小，单位 SP（与 {@code TextView.setTextSize(float)} 一致）。 */
    @NonNull
    public BaseViewHolder setTextSize(@IdRes int viewId, float sp) {
        TextView tv = getView(viewId);
        if (tv != null) {
            tv.setTextSize(sp);
        }
        return this;
    }

    // ==================== 可见性 / 状态 ====================

    @NonNull
    public BaseViewHolder setVisibility(@IdRes int viewId, int visibility) {
        View v = getView(viewId);
        if (v != null) {
            v.setVisibility(visibility);
        }
        return this;
    }

    @NonNull
    public BaseViewHolder setEnabled(@IdRes int viewId, boolean enabled) {
        View v = getView(viewId);
        if (v != null) {
            v.setEnabled(enabled);
        }
        return this;
    }

    @NonNull
    public BaseViewHolder setSelected(@IdRes int viewId, boolean selected) {
        View v = getView(viewId);
        if (v != null) {
            v.setSelected(selected);
        }
        return this;
    }

    // ==================== 背景 / 图片 ====================

    @NonNull
    public BaseViewHolder setBackgroundColor(@IdRes int viewId, @ColorInt int color) {
        View v = getView(viewId);
        if (v != null) {
            v.setBackgroundColor(color);
        }
        return this;
    }

    @NonNull
    public BaseViewHolder setBackground(@IdRes int viewId, @Nullable Drawable drawable) {
        View v = getView(viewId);
        if (v != null) {
            v.setBackground(drawable);
        }
        return this;
    }

    @NonNull
    public BaseViewHolder setImageResource(@IdRes int viewId, @DrawableRes int resId) {
        ImageView iv = getView(viewId);
        if (iv != null) {
            iv.setImageResource(resId);
        }
        return this;
    }

    // ==================== 点击事件 ====================

    @NonNull
    public BaseViewHolder setOnClickListener(@IdRes int viewId,
                                             @Nullable View.OnClickListener listener) {
        View v = getView(viewId);
        if (v != null) {
            v.setOnClickListener(listener);
        }
        return this;
    }

    @NonNull
    public BaseViewHolder setOnLongClickListener(@IdRes int viewId,
                                                 @Nullable View.OnLongClickListener listener) {
        View v = getView(viewId);
        if (v != null) {
            v.setOnLongClickListener(listener);
        }
        return this;
    }
}