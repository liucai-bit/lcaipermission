package com.liucai.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.liucai.component.adapter.BusinessNodeStatusAdapter;
import com.liucai.component.base.BaseLinearLayout;
import com.liucai.component.base.ItemClickListener;
import com.liucai.component.bean.BusinessNodeStatusItemConfig;
import com.liucai.component.bean.BusinessNodesStatusBean;
import com.liucai.core.util.common.CommonUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 业务节点状态展示组件。
 *
 * <p>用于展示带图标、文本和角标的网格/列表布局。支持从 XML 配置标题、背景、列数等属性。
 *
 * @author liucai
 */
public class BusinessNodeStatusBar extends BaseLinearLayout {

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
    private ItemClickListener clickListener;
    @Nullable
    private List<BusinessNodesStatusBean> datas;
    @NonNull
    private BusinessNodeStatusItemConfig statusItemConfig;

    public BusinessNodeStatusBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        // 默认 padding 10dp
        int paddingPx = CommonUtils.dip2px(mContext, 10);
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }

    @NonNull
    @Override
    public int[] setAttrs() {
        return R.styleable.BusinessNodeStatusBar;
    }

    @Override
    public void init() {
        statusItemConfig = new BusinessNodeStatusItemConfig();

        // 防御：代码直接 new 时无 AttributeSet
        if (mTa == null) {
            columns = 1;
            title = "";
            datas = new ArrayList<>();
            setBackgroundColor(Color.WHITE);
            return;
        }

        config.titleSize = mTa.getInt(R.styleable.BusinessNodeStatusBar_titleSize, 16);
        config.titleColor = mTa.getColor(R.styleable.BusinessNodeStatusBar_titleColor, mContext.getColor(R.color.text_1c1c1c));
        config.textStyle = mTa.getString(R.styleable.BusinessNodeStatusBar_titleStyle);
        columns = mTa.getInt(R.styleable.BusinessNodeStatusBar_columns, 0);
        title = mTa.getString(R.styleable.BusinessNodeStatusBar_title);
        barBackground = mTa.getDrawable(R.styleable.BusinessNodeStatusBar_barBackground);

        // 修复了原代码 iconHeight 读错属性的 Bug，以及颜色读取使用 getColor 而非 getInt
        statusItemConfig
                .setBadgeSize(mTa.getInt(R.styleable.BusinessNodeStatusBar_badgeSize, 12))
                .setEllipsize(mTa.getInt(R.styleable.BusinessNodeStatusBar_ellipsize, 1))
                .setBadgeColor(mTa.getColor(R.styleable.BusinessNodeStatusBar_badgeColor, mContext.getColor(R.color.text_1c1c1c)))
                .setBadgeBackground(mTa.getDrawable(R.styleable.BusinessNodeStatusBar_badgeBackground))
                .setMaxNumber(mTa.getInt(R.styleable.BusinessNodeStatusBar_maxNumber, 99))
                .setIsMax(mTa.getBoolean(R.styleable.BusinessNodeStatusBar_isMax, false))
                .setIconWidth(mTa.getInt(R.styleable.BusinessNodeStatusBar_iconWidth, 25))
                .setIconHeight(mTa.getInt(R.styleable.BusinessNodeStatusBar_iconHeight, 25))
                .setItemSpace(mTa.getInt(R.styleable.BusinessNodeStatusBar_itemSpace, 0))
                .setTitleSize(mTa.getInt(R.styleable.BusinessNodeStatusBar_itemTextSize, 12))
                .setTitleColor(mTa.getColor(R.styleable.BusinessNodeStatusBar_itemTextColor, Color.parseColor("#1C1C1C")));

        datas = new ArrayList<>();
        // 边界校验：非法列数自动适配为默认值1
        if (columns <= 0) {
            columns = 1;
        }
        // 版本兼容与背景设置处理
        if (barBackground != null) {
            setBackground(barBackground);
        } else {
            setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public void initView() {
        initTitle();
        initGrid();
    }

    private void initTitle() {
        mTitle = new TextView(mContext);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int marginBottomPx = CommonUtils.dip2px(mContext, 10);
        params.setMargins(0, 0, 0, marginBottomPx);
        mTitle.setLayoutParams(params);
        mTitle.setTextColor(config.titleColor);
        mTitle.setTextSize(config.titleSize);
        mTitle.setVisibility(GONE);

        if (TextUtils.equals(config.textStyle, "1")) {
            mTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else if (TextUtils.equals(config.textStyle, "2")) {
            mTitle.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        }

        if (!TextUtils.isEmpty(title)) {
            mTitle.setVisibility(VISIBLE);
            mTitle.setText(title);
        }
        addView(mTitle);
    }

    private void initGrid() {
        recyclerView = new RecyclerView(mContext);
        LayoutParams recyclerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        recyclerView.setLayoutParams(recyclerParams);

        adapter = new BusinessNodeStatusAdapter(mContext);
        adapter.setConfig(statusItemConfig);
        adapter.setData(datas);
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
        this.title = title;
        if (mTitle == null) return;
        if (TextUtils.isEmpty(title)) {
            mTitle.setVisibility(GONE);
        } else {
            mTitle.setVisibility(VISIBLE);
            mTitle.setText(title);
        }
    }

    public void setTitleColor(int titleColor) {
        if (mTitle != null && titleColor != 0) {
            mTitle.setTextColor(titleColor);
        }
    }

    public void setTitleSize(int titleSize) {
        if (mTitle != null && titleSize > 0) {
            mTitle.setTextSize(titleSize);
        }
    }

    @NonNull
    public BusinessNodeStatusItemConfig getStatusItemConfig() {
        return this.statusItemConfig;
    }

    public void setStatusItemConfig(@NonNull BusinessNodeStatusItemConfig statusItemConfig) {
        this.statusItemConfig = statusItemConfig;
        if (adapter != null) {
            adapter.setConfig(statusItemConfig);
        }
    }

    /**
     * 更新节点数据集
     * @param datas 节点数据
     */
    public void setDatas(@Nullable List<BusinessNodesStatusBean> datas) {
        this.datas = datas != null ? datas : new ArrayList<>();
        if (adapter != null) {
            adapter.setData(this.datas);
        }
    }

    public void setColumns(int columns) {
        if (columns <= 0) {
            return;
        }
        this.columns = columns;
        if (adapter != null && recyclerView != null) {
            recyclerView.setLayoutManager(adapter.getGridManager(columns));
        }
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
     */
    @Nullable
    public BusinessNodeStatusAdapter getAdapter() {
        return adapter;
    }
}