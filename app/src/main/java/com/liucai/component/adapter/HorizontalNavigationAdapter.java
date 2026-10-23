package com.liucai.component.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.component.base.BaseViewHolder;
import com.liucai.permission.R;

/**
 * @author liucai
 * @program lcpermission
 * @description 横向导航栏适配器
 * @Date 2026/7/27
 */
public class HorizontalNavigationAdapter extends BaseRecycleAdapter<BaseViewHolder, String> {

    private int selectedPosition = 0;

    @Nullable
    private Drawable checkDrawable;
    @Nullable
    private Drawable unCheckDrawable;

    public HorizontalNavigationAdapter(@NonNull Context mContext) {
        super(mContext, R.layout.horizontal_navigetion_item_layout);
        checkDrawable = mContext.getDrawable(R.drawable.horiaontal_navgation_check);
        unCheckDrawable = mContext.getDrawable(R.drawable.horiaontal_navgation_uncheck);
    }

    /**
     * 设置当前选中的 Tab 位置。
     * <p>对外暴露，支持初始化时指定选中项。
     */
    public void setSelectedPosition(int position) {
        if (this.selectedPosition == position) {
            return;
        }
        int oldPosition = this.selectedPosition;
        this.selectedPosition = position;

        // 局部刷新，避免全量刷新带来的性能损耗
        if (oldPosition >= 0 && oldPosition < getItemCount()) {
            notifyItemChanged(oldPosition);
        }
        if (position >= 0 && position < getItemCount()) {
            notifyItemChanged(position);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setCheckDrawable(@Nullable Drawable checkDrawable) {
        this.checkDrawable = checkDrawable;
        // 样式变化需要刷新 UI
        if (getItemCount() > 0) {
            notifyItemChanged(selectedPosition);
        }
    }

    public void setUnCheckDrawable(@Nullable Drawable unCheckDrawable) {
        this.unCheckDrawable = unCheckDrawable;
        // 样式变化需要刷新 UI
        if (getItemCount() > 0) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void onBindView(int position, View view, BaseViewHolder holder, String s) {
        TextView textView = holder.getView(R.id.navigation_bar_text);
        View lineView = holder.getView(R.id.navigation_bar_line);

        textView.setText(s);

        if (selectedPosition == position) {
            if (lineView != null) {
                lineView.setBackground(checkDrawable);
            }
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            if (lineView != null) {
                lineView.setBackground(unCheckDrawable);
            }
            textView.setTypeface(Typeface.DEFAULT);
        }

        textView.setOnClickListener(v -> {
            if (selectedPosition != position) {
                setSelectedPosition(position);
                if (clickListener != null) {
                    clickListener.onItemClickListener(position, s);
                }
            }
        });
    }
}