package com.liucai.component.adapter;

import static android.view.View.GONE;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.liucai.component.BadgeView;
import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.component.base.BaseViewHolder;
import com.liucai.component.bean.BusinessNodeStatusItemConfig;
import com.liucai.component.bean.BusinessNodesStatusBean;
import com.liucai.core.util.common.CommonUtils;
import com.liucai.image.ImageUtils;
import com.liucai.permission.R;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/23
 */
public class BusinessNodeStatusAdapter extends BaseRecycleAdapter<BaseViewHolder, BusinessNodesStatusBean> {

    private BusinessNodeStatusItemConfig config;

    public BusinessNodeStatusAdapter(Context mContext) {
        super(mContext, R.layout.business_nodes_status_layout);
    }

    public void setConfig(BusinessNodeStatusItemConfig config) {
        this.config = config;
        notifyDataSetChanged();
    }

    @Override
    public void onBindView(int position, View mConvertView, BaseViewHolder holder, BusinessNodesStatusBean bean) {
        if (config == null) return; // 增加防御

        ConstraintLayout mNodeStatus = holder.getView(R.id.business_nodes_status);
        mNodeStatus.setPadding(0, CommonUtils.dip2px(mContext, config.itemSpace), 0, CommonUtils.dip2px(mContext, config.itemSpace));

        ImageView imageView = holder.getView(R.id.business_nodes_status_icon);
        if (imageView != null) {
            ViewGroup.LayoutParams existingParams = imageView.getLayoutParams();
            if (existingParams == null) {
                existingParams = new ViewGroup.LayoutParams(
                        CommonUtils.dip2px(mContext, config.iconWidth),
                        CommonUtils.dip2px(mContext, config.iconHeight));
            } else {
                existingParams.width = CommonUtils.dip2px(mContext, config.iconWidth);
                existingParams.height = CommonUtils.dip2px(mContext, config.iconHeight);
            }
            imageView.setLayoutParams(existingParams);
            ImageUtils.loadImage(mContext, bean.getIcon(), imageView);
        }

        TextView textView = holder.getView(R.id.business_nodes_status_string);
        if (textView != null) {
            textView.setTextSize(config.titleSize);
            textView.setTextColor(config.titleColor);
            // 修复复用残留状态的问题
            textView.setEllipsize(null);
            textView.setSingleLine(false);
            textView.setSelected(false);

            if (config.ellipsize == 1) {
                textView.setSingleLine(true);
            } else if (config.ellipsize == 2) {
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
            } else if (config.ellipsize == 3) {
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                textView.setSelected(true);
                textView.setMarqueeRepeatLimit(-1);
            }
            textView.setText(bean.getLabel());
        }

        BadgeView point = holder.getView(R.id.business_nodes_status_point);
        if (point != null) {
            point.setBTextColor(config.badgeColor);
            point.setBTextSize(config.badgeSize);
            point.setBBackground(config.badgeBackground);
            point.setMaxNumber(config.maxNumber);
            point.setMax(config.isMax);
            point.setEadge(bean.getSubscript());
        }

        if (mNodeStatus != null) {
            mNodeStatus.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClickListener(position, bean.getData());
                }
            });
        }
    }
}