package com.liucai.component;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.flexbox.FlexboxLayout;
import com.liucai.component.base.BaseFlexboxLayout;
import com.liucai.component.base.ItemClickListener;
import com.liucai.component.bean.HorizonalLabelBean;
import com.liucai.component.mode.TextViewMode;
import com.liucai.core.util.common.CommonUtils;
import com.liucai.permission.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 横向标签组件（基于 FlexboxLayout，支持自动换行）。
 *
 * <p>使用约束：
 * <ul>
 *   <li>通过 {@link #setDatas(List)} 更新数据并自动刷新 UI</li>
 *   <li>单独修改某个标签样式无需重建列表，可通过 {@link #getDatas()} 获取后重新 setDatas 触发重绘</li>
 * </ul>
 *
 * @author liucai
 */
public class HorizonalLabelBar extends BaseFlexboxLayout {

    private int labelWrap;
    private int labelDirection;
    private int labelAlign;

    /** 内部维护的数据源，防止外部直接修改导致 UI 不同步 */
    @NonNull
    private final List<HorizonalLabelBean> datas = new ArrayList<>();

    @Nullable
    private ItemClickListener clickListener;

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
        if (mTa == null) {
            return;
        }
        labelWrap = mTa.getInt(R.styleable.HorizonalLabelBar_labelWrap, 0);
        labelDirection = mTa.getInt(R.styleable.HorizonalLabelBar_labelDirection, 0);
        labelAlign = mTa.getInt(R.styleable.HorizonalLabelBar_labelAlign, 0);

        setFlexWrap(labelWrap);
        setFlexDirection(labelDirection);
        setAlignContent(labelAlign);
    }

    @Override
    public void initView() {
        initLabels();
    }

    // ==================== 对外 API ====================

    /**
     * 设置标签数据并刷新 UI。
     * <p>传入 null 等价于清空。
     */
    public void setDatas(@Nullable List<HorizonalLabelBean> newDatas) {
        this.datas.clear();
        if (newDatas != null) {
            this.datas.addAll(newDatas);
        }
        initLabels();
    }

    /**
     * 获取当前数据源的只读副本。
     * <p>若需修改列表，请修改后重新调用 {@link #setDatas(List)} 触发重绘。
     */
    @NonNull
    public List<HorizonalLabelBean> getDatas() {
        return new ArrayList<>(datas);
    }

    /**
     * 设置标签点击事件。
     */
    public void setClickListener(@Nullable ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLabelWrap(int labelWrap) {
        this.labelWrap = labelWrap;
        setFlexWrap(labelWrap);
    }

    public void setLabelDirection(int labelDirection) {
        this.labelDirection = labelDirection;
        setFlexDirection(labelDirection);
    }

    public void setLabelAlign(int labelAlign) {
        this.labelAlign = labelAlign;
        setAlignContent(labelAlign);
    }

    // ==================== 内部实现 ====================

    /**
     * 重建标签 View。
     */
    private void initLabels() {
        removeAllViews();
        if (datas.isEmpty()) {
            return;
        }
        for (int i = 0; i < datas.size(); i++) {
            HorizonalLabelBean bean = datas.get(i);
            if (bean == null || TextUtils.isEmpty(bean.getLabel())) {
                continue;
            }
            final int position = i;
            View labelView = createLabelView(bean, position);
            addView(labelView);
        }
    }

    /**
     * 创建单个标签 View。
     */
    private TextView createLabelView(@NonNull HorizonalLabelBean bean, int position) {
        TextView textView = new TextView(mContext);
        textView.setText(bean.getLabel());
        textView.setTextSize(bean.getTextSize());
        textView.setTextColor(bean.getTextColor());
        textView.setTypeface(Typeface.defaultFromStyle(TextViewMode.getTypeface(bean.getFontStyle())));

        if (bean.getBorder() > 0) {
            try {
                textView.setBackground(mContext.getDrawable(bean.getBorder()));
            } catch (Exception e) {
                // 防呆：避免错误资源 ID 导致崩溃
                e.printStackTrace();
            }
        }

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(WC, WC);
        params.setMargins(0, 0, CommonUtils.dip2px(mContext, 10), CommonUtils.dip2px(mContext, 10));
        textView.setLayoutParams(params);
        textView.setPadding(CommonUtils.dip2px(mContext, 5), CommonUtils.dip2px(mContext, 3),
                CommonUtils.dip2px(mContext, 5), CommonUtils.dip2px(mContext, 3));

        textView.setOnClickListener(v -> {
            if (clickListener != null) {
                // ✅ 修复：传递真实的 position，而不是死代码 0
                clickListener.onItemClickListener(position, bean);
            }
        });
        return textView;
    }

    // ==================== 兼容老版本 API ====================

    /**
     * @deprecated 拼写错误，请使用 {@link #initLabels()}
     */
    @Deprecated
    public void initLable() {
        initLabels();
    }
}