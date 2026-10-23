package com.liucai.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.component.base.BaseLinearLayout;
import com.liucai.core.util.common.CommonUtils;
import com.liucai.permission.R;

import java.util.ArrayList;
import java.util.List;

public class HorizonalBreadBar extends BaseLinearLayout {

    @NonNull
    private final List<String> datas = new ArrayList<>();

    private int textSize;
    private int textColor;
    @Nullable
    private Drawable background;
    private int minWidth;

    public HorizonalBreadBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    @Override
    public int[] setAttrs() {
        return R.styleable.HorizonalBreadBar;
    }

    @Override
    public void init() {
        // 修复：mContext 在字段初始化时还是 null，必须在 init() 中赋值
        minWidth = CommonUtils.dip2px(mContext, 60);

        if (mTa == null) {
            textSize = 12;
            textColor = Color.parseColor("#1C1C1C");
            background = mContext.getDrawable(R.mipmap.risk_back);
            return;
        }

        textSize = mTa.getInt(R.styleable.HorizonalBreadBar_textSize, 12);
        textColor = mTa.getColor(R.styleable.HorizonalBreadBar_textColor, Color.parseColor("#1C1C1C"));
        background = mTa.getDrawable(R.styleable.HorizonalBreadBar_breadBackground);

        if (background == null) {
            background = mContext.getDrawable(R.mipmap.risk_back);
        }
    }

    @Override
    public void initView() {
        setOrientation(HORIZONTAL);
        initBread();
    }

    /**
     * 设置数据并刷新 UI
     */
    public void setDatas(@Nullable List<String> newDatas) {
        this.datas.clear();
        if (newDatas != null) {
            this.datas.addAll(newDatas);
        }
        initBread();
    }

    @NonNull
    public List<String> getDatas() {
        return new ArrayList<>(datas);
    }

    private void initBread() {
        removeAllViews();
        if (datas.isEmpty()) return;

        for (String data : datas) {
            TextView textView = new TextView(mContext);
            textView.setText(data);
            textView.setGravity(Gravity.CENTER);
            textView.setMinWidth(minWidth);
            textView.setTextColor(textColor);
            textView.setTextSize(textSize);
            textView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            int textContentWidth = textView.getMeasuredWidth() + CommonUtils.dip2px(mContext, 10);
            int textContentHeight = textView.getMeasuredHeight() + CommonUtils.dip2px(mContext, 5);

            LayoutParams params = new LayoutParams(Math.max(textContentWidth, minWidth), textContentHeight);
            params.setMargins(CommonUtils.dip2px(mContext, 5), 0, 0, 0);
            textView.setLayoutParams(params);
            textView.setBackground(background);
            textView.setPadding(CommonUtils.dip2px(mContext, 5), CommonUtils.dip2px(mContext, 3),
                    CommonUtils.dip2px(mContext, 5), CommonUtils.dip2px(mContext, 3));
            addView(textView);
        }
    }
}