package com.liucai.component.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/23
 */
public abstract class BaseRecycleAdapter<VH extends BaseViewHolder,T> extends RecyclerView.Adapter<VH> {

    public static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    @NonNull
    public Context mContext;
    @Nullable
    private List<T> datas;
    @Nullable
    private JSONArray arrays;
    private final int layoutId;
    private final Type tActualType;
    @Nullable
    public ItemClickListener clickListener;
    public abstract void onBindView(int position, View mConvertView, VH holder, T object);

    @NonNull
    public RecyclerView.LayoutManager getLineManager() {
        return new LinearLayoutManager(mContext);
    }

    @NonNull
    public RecyclerView.LayoutManager getGridManager(int cloumns) {
        return new GridLayoutManager(mContext, cloumns);
    }

    @NonNull
    public RecyclerView.LayoutManager getHorManager() {
        return new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
    }

    public BaseRecycleAdapter(@NonNull Context mContext, int layoutId) {
        this.mContext = mContext;
        this.layoutId = layoutId;
        Type superType = getClass().getGenericSuperclass();
        assert superType != null;
        Type[] typeArguments = ((ParameterizedType) superType).getActualTypeArguments();
        tActualType = typeArguments[1];
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(@NonNull JSONArray arrays) {
        if (this.arrays==arrays) return;
        this.arrays = arrays;
        this.datas = null;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(@NonNull List<T> datas) {
        if (this.datas==datas) return;
        this.datas = datas;
        this.arrays = null;
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        if (position < 0 || position >= getItemCount()) return null;
        if (datas != null) return datas.get(position);
        if (arrays != null) {
            return arrays.getJSONObject(position).toJavaObject(tActualType);
        }
        return null;
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        VH viewHolder;
        try {
            viewHolder = (VH) new BaseViewHolder(view);
        } catch (Exception e) {
            throw new IllegalArgumentException("BaseViewHolder 类型不匹配，请确认VH泛型约束正确", e);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (datas != null) {
            onBindView(position,holder.getmConvertView(),holder,datas.get(position));
        } else if (arrays != null) {
            onBindView(position,holder.getmConvertView(),holder,arrays.getJSONObject(position).toJavaObject(tActualType));
        }
    }

    @Override
    public int getItemCount() {
        if (datas != null) return datas.size();
        if (arrays != null) return arrays.size();
        return 0;
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
                mContext.getResources().getDisplayMetrics()
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
                mContext.getResources().getDisplayMetrics()
        );
    }
}
