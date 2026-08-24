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
import com.liucai.core.util.log.LcaiLogUtils;

import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/27
 */
public class HorizontalNavigationBar extends BaseLinearLayout {

    private HorizontalNavigationAdapter adapter;
    public ItemClickListener clickListener;


    public HorizontalNavigationBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDatas(List<String> datas) {
        if (adapter != null) {
            adapter.setData(datas);
            LcaiLogUtils.d("数据",datas.size());
        }
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void init() {

    }

    @Override
    public void initView() {
        setOrientation(VERTICAL);
        initComponent();
    }

    public void initComponent() {
        RecyclerView recyclerView = new RecyclerView(mContext);
        adapter = new HorizontalNavigationAdapter(mContext);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(MP, WC);
        recyclerView.setLayoutParams(params);
        recyclerView.setLayoutManager(adapter.getHorManager());
        recyclerView.setAdapter(adapter);
        addView(recyclerView);

        adapter.setClickListener((postion,object)->{
            if (clickListener != null) {
                clickListener.onItemClickListener(postion, object);
            }
        });

        //底部边框线
        View view = new View(mContext);
        LayoutParams lineParams = new LayoutParams(MP, dip2px(1));
        view.setLayoutParams(lineParams);
        view.setBackgroundColor(Color.parseColor("#999999"));
        addView(view);
    }
}
