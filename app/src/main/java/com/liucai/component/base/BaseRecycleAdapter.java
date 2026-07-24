package com.liucai.component.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;

import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/23
 */
public abstract class BaseRecycleAdapter<VH extends BaseViewHolder,T> extends RecyclerView.Adapter<VH> {

    public Context mContext;
    private List<T> datas;
    private JSONArray arrays;
    private int layoutId;
    public RecyclerView.LayoutManager manager;
    public ItemClickListener clickListener;
    private boolean isT;

    public abstract void onBindView(int position, View mConvertView, VH holder, T object);

    public RecyclerView.LayoutManager getLineManager() {
        manager = new LinearLayoutManager(mContext);
        return manager;
    }

    public RecyclerView.LayoutManager getGridManager(int cloumns) {
        manager = new GridLayoutManager(mContext, cloumns);
        return manager;
    }

    public RecyclerView.LayoutManager getHorManager() {
        manager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        return manager;
    }

    public BaseRecycleAdapter(Context mContext, int layoutId) {
        this.mContext = mContext;
        this.layoutId = layoutId;
    }

    public void setData(JSONArray arrays) {
        this.arrays = arrays;
        notifyDataSetChanged();
    }

    public void setData(List<T> datas) {
        this.isT = true;
        this.datas = datas;
        notifyDataSetChanged();
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(layoutId, null);
        return (VH) new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (isT) {
            onBindView(position, null, holder, datas.get(position));
        }else{
            onBindView(position, null, holder, (T) arrays.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return isT ? (datas!=null ? datas.size() : 0) : (arrays!=null ? arrays.size() : 0);
    }
}
