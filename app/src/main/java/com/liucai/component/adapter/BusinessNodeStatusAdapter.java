package com.liucai.component.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintSet;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.component.base.BaseViewHolder;
import com.liucai.component.bean.BusinessNodeStatusItemConfig;
import com.liucai.component.bean.BusinessNodesStatusBean;
import com.liucai.core.util.common.CommonUtils;
import com.liucai.json.JSONUtils;
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
        ImageView imageView = holder.getView(R.id.business_nodes_status_icon);
        ViewGroup.LayoutParams imageParams = new LinearLayout.LayoutParams(CommonUtils.dip2px(mContext,config.iconWidth), CommonUtils.dip2px(mContext,config.iconHeight));
        imageView.setLayoutParams(imageParams);
        Glide.with(mContext).load(bean.getIcon()).into(imageView);

        TextView textView = holder.getView(R.id.business_nodes_status_string);
        textView.setTextSize(config.titleSize);
        textView.setTextColor(config.titleColor);
        textView.setText(bean.getLabel());

        TextView point = holder.getView(R.id.business_nodes_status_point);
        if (bean.getSubscript() > 0) {
            point.setVisibility(VISIBLE);
            point.setText(String.valueOf(bean.getSubscript()));
        } else {
            point.setVisibility(GONE);
        }

        RelativeLayout relativeLayout = holder.getView(R.id.business_nodes_status);
        RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(MP, WC);
        relativeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        relativeLayoutParams.setMargins(0, 0, 0, CommonUtils.dip2px(mContext,config.itemSpace));
        relativeLayout.setLayoutParams(relativeLayoutParams);
        holder.getView(R.id.business_nodes_status).setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClickListener(position, bean.getData());
            }
        });
    }
}
