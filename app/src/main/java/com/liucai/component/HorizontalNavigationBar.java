package com.liucai.component;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.liucai.component.adapter.HorizontalNavigationAdapter;
import com.liucai.component.base.BaseLinearLayout;
import com.liucai.component.base.ItemClickListener;
import com.liucai.core.util.common.CommonUtils;

import java.util.List;

public class HorizontalNavigationBar extends BaseLinearLayout {

    @Nullable
    private HorizontalNavigationAdapter adapter;
    @Nullable
    private ItemClickListener clickListener;
    @Nullable
    private List<String> pendingDatas;
    @Nullable
    private View bottomLineView;

    public HorizontalNavigationBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init() {
    }

    @Override
    public void initView() {
        setOrientation(VERTICAL);

        RecyclerView recyclerView = new RecyclerView(mContext);
        recyclerView.setHorizontalScrollBarEnabled(false);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(MP, WC);
        recyclerView.setLayoutParams(params);

        adapter = new HorizontalNavigationAdapter(mContext);
        recyclerView.setLayoutManager(adapter.getHorManager());
        recyclerView.setAdapter(adapter);
        addView(recyclerView);

        // ✅ 修复：必须将监听桥接到 adapter
        adapter.setClickListener((position, object) -> {
            if (clickListener != null) {
                clickListener.onItemClickListener(position, object);
            }
        });

        // 处理早期设置的数据
        if (pendingDatas != null) {
            adapter.setData(pendingDatas);
            pendingDatas = null;
        }

        // 底部边框线
        bottomLineView = new View(mContext);
        LayoutParams lineParams = new LayoutParams(MP, CommonUtils.dip2px(mContext, 1));
        bottomLineView.setLayoutParams(lineParams);
        bottomLineView.setBackgroundColor(Color.parseColor("#999999"));
        addView(bottomLineView);
    }

    public void setDatas(@Nullable List<String> datas) {
        if (adapter != null) {
            adapter.setData(datas);
        } else {
            pendingDatas = datas;
        }
    }

    public void setClickListener(@Nullable ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setSelectedPosition(int position) {
        if (adapter != null) {
            adapter.setSelectedPosition(position);
        }
    }
}