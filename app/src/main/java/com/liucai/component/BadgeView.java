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
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.R;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/9/11
 */
public class BadgeView extends BaseLinearLayout {
    /**背景颜色*/
    private Drawable background;
    /**字体大小*/
    private int textSize;
    /**文字颜色*/
    private int textColor;
    /**最大显示数量*/
    private int maxNumber;
    /**是否支持最大显示*/
    private boolean isMax;
    /**角标数字*/
    private int eadge;
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
        background = mTa.getDrawable(R.styleable.BadgeView_bBackground);
        textSize = mTa.getInt(R.styleable.BadgeView_bTextSize, 8);
        textColor = mTa.getInt(R.styleable.BadgeView_bTextColor, R.color.text_1c1c1c);
        maxNumber = mTa.getInt(R.styleable.BadgeView_bMaxNumber, 99);
        isMax = mTa.getBoolean(R.styleable.BadgeView_bIsMax, false);
        eadge = mTa.getInt(R.styleable.BadgeView_bBadge, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int minSize = dip2px(18);
        int size = Math.max(minSize, Math.max(getMeasuredWidth(), getMeasuredHeight()));
        setMeasuredDimension(size, size);
    }

    @Override
    public void initView() {
        setPadding(dip2px(2),dip2px(2),dip2px(2),dip2px(2));
        //设置背景颜色
        if (background != null) {
            setBackground(background);
        }
        setGravity(Gravity.CENTER);
        textView = new TextView(mContext);
        //设置文字大小
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize);
        //设置文字颜色
        textView.setTextColor(textColor);
        String mEadge = isMax ? (eadge > maxNumber ? (maxNumber + "+") : eadge + "") : eadge + "";
        if (TextUtils.isEmpty(mEadge) || TextUtils.equals(mEadge, "0")) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            //设置文字角标
            textView.setText(mEadge);
        }
        addView(textView);
    }


    /**
     * 设置角标
     * @param eadge
     */
    public void setEadge(int eadge) {
        String mEadge = isMax ? (eadge > maxNumber ? maxNumber + "+" : eadge + "") : eadge + "";
        if (TextUtils.isEmpty(mEadge) || TextUtils.equals(mEadge, "0")) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            //设置文字角标
            textView.setText(mEadge);
        }
    }

    /**
     * 设置文字颜色
     * @param textColor
     */
    public void setBTextColor(int textColor) {
        textView.setTextColor(textColor);
    }

    /**
     * 设置文字大小
     * @param textSize
     */
    public void setBTextSize(int textSize) {
        textView.setTextSize(textSize);
    }

    /**
     * 设置背景颜色
     * @param bBackground
     */
    public void setBBackground(Drawable bBackground) {
        setBackground(bBackground);
    }

    /**
     * 设置最大显示
     * @param maxNumber
     */
    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }

    /**
     * 设置是否开启最大显示
     * @param isMax
     */
    public void setMax(boolean isMax) {
        this.isMax = isMax;
    }
}
