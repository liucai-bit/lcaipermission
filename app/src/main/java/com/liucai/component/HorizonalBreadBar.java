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
import com.liucai.permission.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HUAWEI
 * @program lctipsdialog
 * @description 面包屑效果
 * @Date 2026/7/28
 */
public class HorizonalBreadBar extends BaseLinearLayout {

    public List<String> datas;

    private int textSize;
    private int textColor;
    private Drawable background;
    private int minWidth = dip2px(60);

    public HorizonalBreadBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    @Override
    public int[] setAttrs() {
        return R.styleable.HorizonalBreadBar;
    }

    public void setDatas(List<String> datas) {
        this.datas = datas;
        removeAllViews();
        initBread();
        requestLayout();
        invalidate();
    }

    @Override
    public void init() {
        datas = new ArrayList<>();
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

    public void initBread() {

        for (String data : datas) {
            TextView textView = new TextView(mContext);
            textView.setText(data);
            textView.setGravity(Gravity.CENTER);
            textView.setMinWidth(minWidth);
            textView.setTextColor(textColor);
            textView.setTextSize(textSize);
            textView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int textContentWidth = textView.getMeasuredWidth()+dip2px(20);
            int textContentHeight = textView.getMeasuredHeight()+dip2px(10);
            LayoutParams params = new LayoutParams(textContentWidth> minWidth ? textContentWidth: WC,textContentHeight);
            params.setMargins(dip2px(5),0,dip2px(5),0);
            textView.setLayoutParams(params);
            textView.setBackground(background);
            textView.setPadding(dip2px(10),dip2px(5),dip2px(10),dip2px(5));
            addView(textView);
        }
    }
}
