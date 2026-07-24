package com.liucai.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liucai.component.adapter.BusinessNodeStatusAdapter;
import com.liucai.component.base.BaseLinearLayout;
import com.liucai.component.base.ItemClickListener;
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/23
 */
public class BusinessNodeStatusBar extends BaseLinearLayout {

    private int titleSize;
    private int titleColor;
    private int cloumns;
    private Drawable background;
    private String title;
    private TextView mTitle;
    private BusinessNodeStatusAdapter adapter;
    public ItemClickListener clickListener;
    public JSONArray arrays;

    @Override
    public int[] setAttrs() {
        return R.styleable.BusinessNodeStatusBar;
    }

    public BusinessNodeStatusBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setPadding(10,10,10,10);
    }

    @Override
    public void init() {
        titleSize = mTa.getInt(R.styleable.BusinessNodeStatusBar_titleSize, 16);
        titleColor = mTa.getColor(R.styleable.BusinessNodeStatusBar_titleColor, mContext.getColor(R.color.text_1c1c1c));
        cloumns = mTa.getInt(R.styleable.BusinessNodeStatusBar_cloumns, 0);
        title = mTa.getString(R.styleable.BusinessNodeStatusBar_title);
        background = mTa.getDrawable(R.styleable.BusinessNodeStatusBar_barBackground);
        if (background != null) {
            setBackground(background);
        } else {
            setBackgroundColor(Color.WHITE);
        }
        mTa.recycle();
        initTitle();
        initGrid();
    }

    public void initTitle() {
        mTitle= new TextView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WC, WC);
        params.setMargins(0, 0, 0, 10);
        mTitle.setLayoutParams(params);
        mTitle.setTextColor(titleColor);
        mTitle.setTextSize(titleSize);
        mTitle.setVisibility(GONE);
        if (!TextUtils.isEmpty(title)) {
            mTitle.setVisibility(VISIBLE);
            mTitle.setText(title);
        }
        addView(mTitle);
    }

    public void initGrid() {
        RecyclerView recyclerView = new RecyclerView(mContext);
        adapter = new BusinessNodeStatusAdapter(mContext);
        recyclerView.setLayoutManager(adapter.getGridManager(cloumns));
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(clickListener);
        addView(recyclerView);
    }

    public void setTitle(String title) {
        if (mTitle != null) {
            mTitle.setVisibility(VISIBLE);
            mTitle.setText(title);
        }
    }

    public void setArrays(JSONArray arrays) {
        this.arrays = arrays;
        if (adapter != null) {
            adapter.setData(arrays);
        }
    }

    public void setItemClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
