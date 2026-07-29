package com.liucai.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.liucai.component.adapter.BusinessNodeStatusAdapter;
import com.liucai.component.base.BaseLinearLayout;
import com.liucai.component.base.ItemClickListener;
import com.liucai.component.bean.BusinessNodesStatusBean;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.R;

import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description 业务节点状态展示组件
 * @Date 2026/7/23
 */
public class BusinessNodeStatusBar extends BaseLinearLayout {

    private int titleSize;
    private int titleColor;
    private int columns;
    private Drawable barBackground;
    private String title;
    @Nullable
    private TextView mTitle;
    @Nullable
    private BusinessNodeStatusAdapter adapter;
    @Nullable
    private RecyclerView recyclerView;
    @Nullable
    public ItemClickListener clickListener;
    @Nullable
    private List<BusinessNodesStatusBean> datas;

    @Override
    public int[] setAttrs() {
        return R.styleable.BusinessNodeStatusBar;
    }

    public BusinessNodeStatusBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        // 转换dp为px，避免硬编码像素值适配不同屏幕密度
        int paddingPx = dip2px(10);
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }

    @Override
    public void init() {
        titleSize = mTa.getInt(R.styleable.BusinessNodeStatusBar_titleSize, 16);
        titleColor = mTa.getColor(R.styleable.BusinessNodeStatusBar_titleColor, mContext.getColor(R.color.text_1c1c1c));
        columns = mTa.getInt(R.styleable.BusinessNodeStatusBar_business_columns, 0);
        title = mTa.getString(R.styleable.BusinessNodeStatusBar_title);
        barBackground = mTa.getDrawable(R.styleable.BusinessNodeStatusBar_barBackground);
        // 边界校验：非法列数自动适配为默认值1，避免GridLayoutManager抛出异常
        if (columns <= 0) columns = 1;

        // 版本兼容处理，兼容Android 5.0以下系统背景设置API
        if (barBackground != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackground(barBackground);
            } else {
                setBackgroundDrawable(barBackground);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackground(new ColorDrawable(Color.WHITE));
            } else {
                setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            }
        }
    }

    @Override
    public void initView() {
        initTitle();
        initGrid();
    }

    public void initTitle() {
        mTitle = new TextView(mContext);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int marginBottomPx = dip2px(10);
        params.setMargins(0, 0, 0, marginBottomPx);
        mTitle.setLayoutParams(params);
        mTitle.setTextColor(titleColor);
        mTitle.setTextSize(titleSize);
        mTitle.setVisibility(GONE);

        // 空值校验：空标题自动隐藏控件
        if (!TextUtils.isEmpty(title)) {
            mTitle.setVisibility(VISIBLE);
            mTitle.setText(title);
        }
        addView(mTitle);
    }

    public void initGrid() {
        recyclerView = new RecyclerView(mContext);
        LayoutParams recyclerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        recyclerView.setLayoutParams(recyclerParams);

        adapter = new BusinessNodeStatusAdapter(mContext);
        recyclerView.setLayoutManager(adapter.getGridManager(columns));
        recyclerView.setAdapter(adapter);

        if (clickListener != null) {
            adapter.setClickListener(clickListener);
        }
        addView(recyclerView);
    }

    /**
     * 更新标题文本
     * @param title 标题文本，为空时自动隐藏标题控件
     */
    public void setTitle(@Nullable String title) {
        if (mTitle == null) return;
        if (TextUtils.isEmpty(title)) {
            mTitle.setVisibility(GONE);
            return;
        }
        mTitle.setVisibility(VISIBLE);
        mTitle.setText(title);
    }


    /**
     * 更新节点数据集
     * @param datas 节点数据
     */
    public void setDatas(@Nullable List<BusinessNodesStatusBean> datas) {
        this.datas = datas;
        if (adapter != null) {
            adapter.setData(datas);
        }
    }

    public void setColumns(@NonNull int columns) {
        this.columns = columns;
        recyclerView.setLayoutManager(adapter.getGridManager(columns));
    }

    /**
     * 设置节点点击监听
     * @param clickListener 点击回调接口
     */
    public void setItemClickListener(@Nullable ItemClickListener clickListener) {
        this.clickListener = clickListener;
        if (adapter != null) {
            adapter.setClickListener(clickListener);
        }
    }

    /**
     * 获取当前节点适配器实例，支持外部动态更新单条数据
     * @return 当前适配器实例
     */
    @Nullable
    public BusinessNodeStatusAdapter getAdapter() {
        return adapter;
    }

    /**
     * 重写生命周期方法，组件销毁时释放资源，避免内存泄漏
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
            recyclerView.removeAllViews();
        }
        adapter = null;
        recyclerView = null;
        mTitle = null;
        datas = null;
        clickListener = null;
    }
}
