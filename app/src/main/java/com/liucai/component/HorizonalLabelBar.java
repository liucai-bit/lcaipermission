package com.liucai.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.flexbox.FlexboxLayout;
import com.liucai.component.base.BaseFlexboxLayout;
import com.liucai.component.base.ItemClickListener;
import com.liucai.component.bean.HorizonalLabelBean;
import com.liucai.core.util.common.CommonUtils;
import com.liucai.permission.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/28
 */
public class HorizonalLabelBar extends BaseFlexboxLayout {

    private int labelWrap;
    private int labelDirection;
    private int labelAlign;

    public List<HorizonalLabelBean> datas;
    private ItemClickListener clickListener;

    public void setDatas(List<HorizonalLabelBean> datas) {
        this.datas = datas;
        removeAllViews();
        initLable();
        requestLayout();
        invalidate();
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public HorizonalLabelBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    @Override
    public int[] setAttrs() {
        return R.styleable.HorizonalLabelBar;
    }

    @Override
    public void init() {
        labelWrap = mTa.getInt(R.styleable.HorizonalLabelBar_labelWrap, 0);
        labelDirection = mTa.getInt(R.styleable.HorizonalLabelBar_labelDirection, 0);
        labelAlign = mTa.getInt(R.styleable.HorizonalLabelBar_labelAlign, 0);
        setFlexWrap(labelWrap);
        setFlexDirection(labelDirection);
        setAlignContent(labelAlign);
    }

    public void setLabelWrap(int labelWrap) {
        setFlexWrap(labelWrap);
    }

    public void setLabelDirection(int labelDirection) {
        setFlexDirection(labelDirection);
    }

    public void setLabelAlign(int labelAlign) {
        setAlignContent(labelAlign);
    }

    @Override
    public void initView() {
        datas = new ArrayList<>();
        initLable();
    }

    public void initLable() {
        for (HorizonalLabelBean data : datas) {
            TextView textView = new TextView(mContext);
            textView.setText(data.getLabel());
            textView.setTextSize(data.getTextSize());
            textView.setTextColor(data.getTextColor());
            if (data.getBorder() > 0) {
                textView.setBackground(mContext.getDrawable(data.getBorder()));
            }
            FlexboxLayout.LayoutParams params =
                    new FlexboxLayout.LayoutParams(WC,WC);
            params.setMargins(0, 0, CommonUtils.dip2px(mContext, 10), CommonUtils.dip2px(mContext, 10));
            textView.setLayoutParams(params);
            textView.setPadding(CommonUtils.dip2px(mContext,5), CommonUtils.dip2px(mContext,3), CommonUtils.dip2px(mContext,5), CommonUtils.dip2px(mContext,3));
            textView.setOnClickListener(v->{
                if (clickListener != null) {
                    clickListener.onItemClickListener(0,data);
                }
            });
            addView(textView);
        }
    }

}
