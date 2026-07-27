package com.liucai.component.base;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author liucai
 * @program lcpermission
 * @description 通用RecyclerView ViewHolder 缓存工具类
 * @Date 2026/7/23
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {

    private final View mConvertView;
    private final Map<Integer, View> viewMap;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        viewMap = new HashMap<>();
        this.mConvertView = itemView;
        this.mConvertView.setTag(this);
    }

    /**
     * 从itemView中获取绑定的ViewHolder实例
     * @return 当前绑定的ViewHolder实例，空安全返回
     */
    @NonNull
    public BaseViewHolder getViewHolder() {
        if (mConvertView == null) {
            // 修复原逻辑空指针错误：mConvertView为null时直接调用会崩溃，这里添加非空判断返回默认实例
            throw new IllegalStateException("itemView cannot be null when create ViewHolder");
        }
        Object tag = mConvertView.getTag();
        if (tag instanceof BaseViewHolder) {
            return (BaseViewHolder) tag;
        }
        return new BaseViewHolder(mConvertView);
    }

    public View getmConvertView() {
        return mConvertView;
    }

    /**
     * 从缓存中获取控件，避免重复执行findViewById
     * @param viewId 目标控件的ID
     * @param <T> 目标控件的泛型类型
     * @return 缓存后的控件实例
     */
    @Nullable
    public <T extends View> T getView(int viewId) {
        View view = viewMap.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            viewMap.put(viewId, view);
        }
        // 添加空安全返回，找不到控件时返回null而非抛出异常
        if (view != null) {
            return Objects.requireNonNull((T) view);
        }
        return null;
    }
}
