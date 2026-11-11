package com.liucai.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.flexbox.FlexboxLayout;
import com.liucai.component.base.BaseFlexboxLayout;
import com.liucai.component.base.ItemClickListener;
import com.liucai.component.bean.HorizonalLabelBean;
import com.liucai.component.mode.TextViewMode;
import com.liucai.core.util.common.CommonUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/28
 */
@SuppressWarnings("unused")
public class HorizonalLabelBar extends BaseFlexboxLayout {

    private String labelPadding;
    private String labelMargin;

    public List<HorizonalLabelBean> datas;
    private ItemClickListener clickListener;

    public void setDatas(List<HorizonalLabelBean> datas) {
        this.datas = datas == null ? new ArrayList<>() : datas;
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
        if (mTa!=null){
            int labelWrap = mTa.getInt(R.styleable.HorizonalLabelBar_labelWrap, 0);
            int labelDirection = mTa.getInt(R.styleable.HorizonalLabelBar_labelDirection, 0);
            int labelAlign = mTa.getInt(R.styleable.HorizonalLabelBar_labelAlign, 0);
            labelPadding = mTa.getString(R.styleable.HorizonalLabelBar_labelPadding);
            labelMargin = mTa.getString(R.styleable.HorizonalLabelBar_labelMagrin);
            setFlexWrap(labelWrap);
            setFlexDirection(labelDirection);
            setAlignContent(labelAlign);
        }

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


    /**
     * 直接设置 padding 字符串，规则与 XML 中 labelPadding 一致。
     * "5"          四边都是 5dp
     * "3,8"        上下 3dp，左右 8dp
     * "5,3,5,3"    左 5、上 3、右 5、下 3
     * null/""      恢复默认（左右 5dp，上下 3dp）
     */
    public void setLabelPadding(String labelPadding) {
        this.labelPadding = labelPadding;
        refreshLabels();
    }

    /** 四边统一 padding（dp） */
    public void setLabelPadding(int padding) {
        setLabelPadding(String.valueOf(padding));
    }

    /** 上下 / 左右 padding（dp） */
    public void setLabelPadding(int topBottom, int leftRight) {
        setLabelPadding(topBottom + "," + leftRight);
    }

    /** 左、上、右、下 padding（dp） */
    public void setLabelPadding(int left, int top, int right, int bottom) {
        setLabelPadding(left + "," + top + "," + right + "," + bottom);
    }

    // ==================== 动态设置 margin ====================

    /**
     * 直接设置 margin 字符串，规则与 XML 中 labelMagrin 一致。
     * "5"          四边都是 5dp
     * "3,8"        上下 3dp，左右 8dp
     * "5,3,5,3"    左 5、上 3、右 5、下 3
     * null/""      恢复默认（无 margin）
     */
    public void setLabelMargin(String labelMargin) {
        this.labelMargin = labelMargin;
        refreshLabels();
    }

    /** 四边统一 margin（dp） */
    public void setLabelMargin(int margin) {
        setLabelMargin(String.valueOf(margin));
    }

    /** 上下 / 左右 margin（dp） */
    public void setLabelMargin(int topBottom, int leftRight) {
        setLabelMargin(topBottom + "," + leftRight);
    }

    /** 左、上、右、下 margin（dp） */
    public void setLabelMargin(int left, int top, int right, int bottom) {
        setLabelMargin(left + "," + top + "," + right + "," + bottom);
    }

    // ==================== 内部刷新 ====================

    /**
     * margin / padding 变更后重新生成子 View。
     * 在 XML 里已经配置过静态属性的场景，如果 setDatas 之前调用 setter，
     * 此处 datas 为空会直接 return，等 setDatas 时统一生效。
     */
    private void refreshLabels() {
        if (datas == null || datas.isEmpty()) {
            return;
        }
        removeAllViews();
        initLable();
        requestLayout();
        invalidate();
    }

    @Override
    public void initView() {
        datas = new ArrayList<>();
        initLable();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void initLable() {
        for (HorizonalLabelBean data : datas) {
            TextView textView = new TextView(mContext);
            textView.setText(data.getLabel());
            textView.setTextSize(data.getTextSize());
            textView.setTextColor(data.getTextColor());
            textView.setTypeface(Typeface.defaultFromStyle(TextViewMode.getTypeface(data.getFontStyle())));
            if (data.getBorder() > 0) {
                textView.setBackground(mContext.getDrawable(data.getBorder()));
            }

            FlexboxLayout.LayoutParams params =
                    new FlexboxLayout.LayoutParams(WC, WC);

            String[] margins = TextUtils.isEmpty(labelMargin) ? new String[0] : labelMargin.split(",");
            if (margins.length > 0) {
                if (margins.length == 1) {
                    int margin = CommonUtils.dip2px(mContext, Integer.parseInt(margins[0].trim()));
                    params.setMargins(margin, margin, margin, margin);
                } else if (margins.length == 2) {
                    int marginTopBottom = CommonUtils.dip2px(mContext, Integer.parseInt(margins[0].trim()));
                    int marginLeftRight = CommonUtils.dip2px(mContext, Integer.parseInt(margins[1].trim()));
                    params.setMargins(marginLeftRight, marginTopBottom, marginLeftRight, marginTopBottom);
                } else if (margins.length == 4) {
                    int marginTop = CommonUtils.dip2px(mContext, Integer.parseInt(margins[1].trim()));
                    int marginBottom = CommonUtils.dip2px(mContext, Integer.parseInt(margins[3].trim()));
                    int marginLeft = CommonUtils.dip2px(mContext, Integer.parseInt(margins[0].trim()));
                    int marginRight = CommonUtils.dip2px(mContext, Integer.parseInt(margins[2].trim()));
                    params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
                }
            }
            textView.setLayoutParams(params);

            int padLeft = CommonUtils.dip2px(mContext, 5);
            int padTop = CommonUtils.dip2px(mContext, 3);
            int padRight = CommonUtils.dip2px(mContext, 5);
            int padBottom = CommonUtils.dip2px(mContext, 3);

            String[] paddings = TextUtils.isEmpty(labelPadding) ? new String[0] : labelPadding.split(",");
            if (paddings.length == 1) {
                int padding = CommonUtils.dip2px(mContext, Integer.parseInt(paddings[0].trim()));
                padLeft = padTop = padRight = padBottom = padding;
            } else if (paddings.length == 2) {
                int paddingTopBottom = CommonUtils.dip2px(mContext, Integer.parseInt(paddings[0].trim()));
                int paddingLeftRight = CommonUtils.dip2px(mContext, Integer.parseInt(paddings[1].trim()));
                padLeft = padRight = paddingLeftRight;
                padTop = padBottom = paddingTopBottom;
            } else if (paddings.length == 4) {
                padLeft = CommonUtils.dip2px(mContext, Integer.parseInt(paddings[0].trim()));
                padTop = CommonUtils.dip2px(mContext, Integer.parseInt(paddings[1].trim()));
                padRight = CommonUtils.dip2px(mContext, Integer.parseInt(paddings[2].trim()));
                padBottom = CommonUtils.dip2px(mContext, Integer.parseInt(paddings[3].trim()));
            }
            textView.setPadding(padLeft, padTop, padRight, padBottom);

            textView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClickListener(0, data);
                }
            });
            addView(textView);
        }
    }
}