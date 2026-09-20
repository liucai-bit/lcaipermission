package com.liucai.component.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.permission.R;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/23
 */
public abstract class BaseRecycleAdapter<VH extends BaseViewHolder, T> extends RecyclerView.Adapter<VH> {

    @NonNull
    public Context mContext;
    @Nullable
    private List<T> datas;
    @Nullable
    private JSONArray arrays;
    private final int layoutId;
    private final Type tActualType;
    private String endTips = "已加载完全部数据";
    private String loadingTips = "正在加载...";
    private String errorTips = "加载失败，点击重试";
    private int viewType = RecycleViewType.DEFAULT;
    private int startPosition = 0;
    @Nullable
    public ItemClickListener clickListener;
    public boolean openMultiMode() {
        return false;
    }

    public boolean loadOver() {
        return false;
    }

    public abstract void onBindView(int position, View mConvertView, VH holder, T object);

    public int getMultiLayout() {
        return R.layout.lcai_adapter_multi_layout;
    }

    public void setLoadState(int state) {
        this.viewType = state;
        // 只刷新最后一个 item (Footer)
        notifyItemChanged(getDataCount());
    }

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

    public void appendData(@NonNull JSONArray arrays) {
        if (arrays != null && !arrays.isEmpty()) {
            if (this.arrays == null) {
                this.arrays = new JSONArray();
            }
            int startPosition = getDataCount();
            this.arrays.addAll(arrays);
            notifyItemRangeInserted(startPosition, arrays.size());
            setLoadState(RecycleViewType.DEFAULT);
            LcaiLogUtils.d("数据加载完，重置item状态");

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(@NonNull JSONArray arrays) {
        if (this.arrays == arrays) return;
        this.arrays = arrays;
        this.datas = null;
        notifyDataSetChanged();
    }

    public void appendData(@NonNull List<T> datas) {
        if (datas != null && !datas.isEmpty()) {
            if (this.datas == null) {
                this.datas = new ArrayList<>();
            }
            this.startPosition = getDataCount();
            this.datas.addAll(datas);
            notifyItemRangeInserted(startPosition, datas.size());
            setLoadState(RecycleViewType.DEFAULT);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(@NonNull List<T> datas) {
        if (this.datas == datas) return;
        this.datas = datas;
        this.arrays = null;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getDataCount() && openMultiMode()){
            return viewType;
        }
        return RecycleViewType.DEFAULT;
    }

    public T getItem(int position) {
        if (position < 0 || position >= getDataCount()) {
            return null;
        }
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
        View view = null;
        if (viewType == RecycleViewType.LOADING || viewType==RecycleViewType.END || viewType==RecycleViewType.ERROR) {
            view = LayoutInflater.from(mContext).inflate(getMultiLayout(), parent,false);
        } else {
            view = LayoutInflater.from(mContext).inflate(layoutId, parent,false);
        }

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
        if (holder.getItemViewType() == RecycleViewType.LOADING) {
            TextView tv = holder.getView(R.id.lcai_adapter_multi_t1);
            if(tv != null) tv.setText(loadingTips);
            if (!loadOver() && viewType == RecycleViewType.LOADING) {
                holder.itemView.post(() -> {
                    // 再次检查状态，防止重复加载
                    if (viewType == RecycleViewType.LOADING && !loadOver()) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (clickListener != null) {
                                clickListener.loadMore();
                            }
                        }, 100);
                    }
                });
            } else {
                this.viewType = RecycleViewType.END;
            }
            return;
        }
        if (holder.getItemViewType() == RecycleViewType.END) {
            TextView tv = holder.getView(R.id.lcai_adapter_multi_t1);
            if(tv != null) tv.setText(endTips);
            return;
        }
        if (holder.getItemViewType() == RecycleViewType.ERROR) {
            TextView tv = holder.getView(R.id.lcai_adapter_multi_t1);
            if(tv != null) tv.setText(errorTips);
            holder.itemView.setOnClickListener(v -> {
                viewType = RecycleViewType.LOADING;
                tv.setText(loadingTips);
                if (clickListener != null) {
                    clickListener.loadMore();
                }
            });
            return;
        }
        // 正常 Item 绑定
        T item = getItem(position);
        if (item != null) {
            onBindView(position, holder.getmConvertView(), holder, item);
        }
    }

    @Override
    public int getItemCount() {
        int size = 0;
        if (datas != null) size = datas.size();
        if (arrays != null) size = arrays.size();
        if (openMultiMode()) {
            return size + 1;
        }
        return size;
    }

    private int getDataCount() {
        if (datas != null) return datas.size();
        if (arrays != null) return arrays.size();
        return 0;
    }

    public int getViewType() {
        return viewType;
    }
}
