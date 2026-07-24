package com.liucai.component.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.component.base.BaseViewHolder;
import com.liucai.json.JSONUtils;
import com.liucai.permission.R;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/23
 */
public class BusinessNodeStatusAdapter extends BaseRecycleAdapter<BaseViewHolder, JSONObject> {

    public BusinessNodeStatusAdapter(Context mContext) {
        super(mContext, R.layout.business_nodes_status_layout);
    }

    @Override
    public void onBindView(int position, View mConvertView, BaseViewHolder holder, JSONObject object) {
        Integer label = JSONUtils.getInt(object, "icon", 0);
        if (label > 0) {
            ((ImageView)holder.getView(R.id.business_nodes_status_icon)).setImageResource(label);
        }

        ((TextView)holder.getView(R.id.business_nodes_status_string)).setText(JSONUtils.getString(object,"label",""));
    }
}
