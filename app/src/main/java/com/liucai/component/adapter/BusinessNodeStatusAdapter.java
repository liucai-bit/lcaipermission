package com.liucai.component.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.component.base.BaseViewHolder;
import com.liucai.component.bean.BusinessNodesStatusBean;
import com.liucai.json.JSONUtils;
import com.liucai.permission.R;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/23
 */
public class BusinessNodeStatusAdapter extends BaseRecycleAdapter<BaseViewHolder, BusinessNodesStatusBean> {

    public BusinessNodeStatusAdapter(Context mContext) {
        super(mContext, R.layout.business_nodes_status_layout);
    }

    @Override
    public void onBindView(int position, View mConvertView, BaseViewHolder holder, BusinessNodesStatusBean bean) {
        ImageView imageView = holder.getView(R.id.business_nodes_status_icon);
        Glide.with(mContext).load(bean.getIcon()).into(imageView);
        TextView textView = holder.getView(R.id.business_nodes_status_string);
        textView.setText(bean.getLabel());
        TextView point = holder.getView(R.id.business_nodes_status_point);
        if (bean.getSubscript() > 0) {
            point.setVisibility(VISIBLE);
            point.setText(String.valueOf(bean.getSubscript()));
        } else {
            point.setVisibility(GONE);
        }
    }
}
