package com.liucai.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.component.base.BaseLinearLayout;
import com.liucai.component.base.ItemClickListener;
import com.liucai.component.bean.HorizonalLabelBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/7/28
 */
public class HorizonalLabelBar extends BaseLinearLayout {

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

    @Override
    public void init() {
        setOrientation(HORIZONTAL);
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
            LinearLayout.LayoutParams params = new LayoutParams(WC, WC);
            params.setMargins(0, 0, 10, 0);
            textView.setLayoutParams(params);
            textView.setPadding(dip2px(5), dip2px(3), dip2px(5), dip2px(3));
            textView.setOnClickListener(v->{
                if (clickListener != null) {
                    clickListener.onItemClickListener(0,data);
                }
            });
            addView(textView);
        }
    }
}
