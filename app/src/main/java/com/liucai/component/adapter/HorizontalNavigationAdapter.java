package com.liucai.component.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.component.base.BaseViewHolder;
import com.liucai.permission.R;

/**
 * @author HUAWEI
 * @program lctipsdialog
 * @description
 * @Date 2026/7/27
 */
public class HorizontalNavigationAdapter extends BaseRecycleAdapter<BaseViewHolder, String> {

    private int position;

    private Drawable checkDrawable;
    private Drawable unCheckDrawable;

    private void setPosition(int position) {
        this.position = position;
        notifyDataSetChanged();
    }

    public void setCheckDrawable(Drawable checkDrawable) {
        this.checkDrawable = checkDrawable;
    }

    public HorizontalNavigationAdapter(@NonNull Context mContext) {
        super(mContext, R.layout.horizontal_navigetion_item_layout);
        checkDrawable = mContext.getDrawable(R.drawable.horiaontal_navgation_check);
        unCheckDrawable = mContext.getDrawable(R.drawable.horiaontal_navgation_uncheck);
    }

    @Override
    public void onBindView(int i, View view, BaseViewHolder baseViewHolder, String s) {
        TextView textView = baseViewHolder.getView(R.id.navigation_bar_text);
        View lineView = baseViewHolder.getView(R.id.navigation_bar_line);
        textView.setText(s);
        if (position == i) {
            lineView.setBackground(checkDrawable);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }else{
            textView.setTypeface(Typeface.DEFAULT);
            lineView.setBackground(unCheckDrawable);
        }

        textView.setOnClickListener(v->{
            setPosition(i);
            if (clickListener != null) {
                clickListener.onItemClickListener(i, s);
            }
        });
    }
}
