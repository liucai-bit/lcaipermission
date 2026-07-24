package com.liucai.component.base;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/23
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {

    private View mConvertView;
    private Map<Integer, View> viewMap;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        viewMap = new HashMap<>();
        this.mConvertView = itemView;
        this.mConvertView.setTag(this);
    }

    public BaseViewHolder getViewHolder() {
        if (mConvertView == null) {
            return new BaseViewHolder(mConvertView);
        }
        return (BaseViewHolder) mConvertView.getTag();
    }

    public <T extends View> T getView(int viewId) {
        View view = viewMap.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            viewMap.put(viewId, view);
        }
        return (T) view;
    }
}
