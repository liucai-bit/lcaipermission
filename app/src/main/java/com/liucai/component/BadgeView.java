package com.liucai.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.component.base.BaseLinearLayout;
import com.liucai.core.util.common.CommonUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.R;

/**
 * @author liucai
 * @program lcpermission
 * @description 角标 View
 * @Date 2026/9/11
 */
public class BadgeView extends BaseLinearLayout {
    /** 背景颜色 */
    private Drawable background;
    /** 字体大小 (sp) */
    private int textSize;
    /** 文字颜色 */
    private int textColor;
    /** 最大显示数量 */
    private int maxNumber;
    /** 是否支持最大显示 */
    private boolean isMax;
    /** 角标数字 */
    private int eadge;

    @Nullable
    private TextView textView;

    public BadgeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    @Override
    public int[] setAttrs() {
        return R.styleable.BadgeView;
    }

    @Override
    public void init() {
        if (mTa == null) return;
        background = mTa.getDrawable(R.styleable.BadgeView_bBackground);
        textSize = mTa.getInt(R.styleable.BadgeView_bTextSize, 8);
        // 修复颜色使用 getColor 而不是 getInt
        textColor = mTa.getColor(R.styleable.BadgeView_bTextColor, mContext.getColor(R.color.lcai_1c1c1c));
        maxNumber = mTa.getInt(R.styleable.BadgeView_bMaxNumber, 99);
        isMax = mTa.getBoolean(R.styleable.BadgeView_bIsMax, false);
        eadge = mTa.getInt(R.styleable.BadgeView_bBadge, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int minSize = CommonUtils.dip2px(mContext, 18);
        int size = Math.max(minSize, Math.max(getMeasuredWidth(), getMeasuredHeight()));
        setMeasuredDimension(size, size);
    }

    @Override
    public void initView() {
        setPadding(CommonUtils.dip2px(mContext, 1), CommonUtils.dip2px(mContext, 1),
                CommonUtils.dip2px(mContext, 1), CommonUtils.dip2px(mContext, 1));
        if (background != null) {
            setBackground(background);
        }
        setGravity(Gravity.CENTER);

        textView = new TextView(mContext);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        textView.setTextColor(textColor);
        addView(textView);

        refreshBadge();
    }

    private void refreshBadge() {
        if (textView == null) return;
        String mEadge = isMax ? (eadge > maxNumber ? (maxNumber + "+") : eadge + "") : eadge + "";
        if (TextUtils.isEmpty(mEadge) || TextUtils.equals(mEadge, "0")) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            textView.setText(mEadge);
        }
    }

    /**
     * 设置角标数字
     */
    public void setEadge(int eadge) {
        this.eadge = eadge;
        refreshBadge();
    }

    public void setBTextColor(int textColor) {
        this.textColor = textColor;
        if (textView != null) {
            textView.setTextColor(textColor);
        }
    }

    /**
     * 设置文字大小，传入的是 SP 数值
     */
    public void setBTextSize(int textSize) {
        this.textSize = textSize;
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        }
    }

    public void setBBackground(Drawable bBackground) {
        this.background = bBackground;
        if (bBackground != null) {
            setBackground(bBackground);
        }
    }

    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }

    public void setMax(boolean isMax) {
        this.isMax = isMax;
    }
}