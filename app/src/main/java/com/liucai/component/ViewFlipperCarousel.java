package com.liucai.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.liucai.component.base.BaseRelativeLayout;
import com.liucai.component.base.BaseViewFlipper;
import com.liucai.permission.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liucai
 * @program lctipsdialog
 * @description 
 * @Date 2026/7/24
 */public class ViewFlipperCarousel extends BaseRelativeLayout {

    private final int POINT_HEIGHT = 5;
    private final int POINT_CEICLE_WIDTH = 5;
    private final int POINT_LINE_WIDTH = 15;

    /**
     * 0 纵向
     * 1 横向
     */
    public int direction;
    /**
     * 速度/s
     */
    public int speed;
    /**
     * 自动播放
     */
    public boolean autoPlay;
    /**
     * 支持手势
     */
    public boolean isSupportGesture;
    /**
     * 显示指示点
     */
    public boolean showReferencePoint;
    /**
     * 指示点最大数量
     */
    public int maxPointSize;
    /**
     * 指示点位置
     */
    public int indicatePoint;
    /**
     * 指示点样式
     */
    public int pointStyle;
    /**
     * 当前指示点颜色
     */
    public Drawable checkColor;
    /**
     * 其他指示点颜色
     */
    public Drawable uncheckColor;

    public List<String> datas;

    private BaseViewFlipper viewFlipper;
    private List<TextView> points;

    @Override
    public int[] setAttrs() {
        return R.styleable.ViewFlipperCarousel;
    }

    public ViewFlipperCarousel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDatas(List<String> datas) {
        this.datas = datas;
        initFlipperData();
        initPoint();
    }

    @Override
    public void init() {
        direction = mTa.getInt(R.styleable.ViewFlipperCarousel_direction, 1);
        speed = mTa.getInt(R.styleable.ViewFlipperCarousel_speed, 3000);
        autoPlay = mTa.getBoolean(R.styleable.ViewFlipperCarousel_autoPlay, true);
        isSupportGesture = mTa.getBoolean(R.styleable.ViewFlipperCarousel_isSupportGesture, true);
        showReferencePoint = mTa.getBoolean(R.styleable.ViewFlipperCarousel_showReferencePoint, true);
        maxPointSize = mTa.getInt(R.styleable.ViewFlipperCarousel_maxPointSize, 6);
        indicatePoint = mTa.getInt(R.styleable.ViewFlipperCarousel_indicatePoint, 0);
        pointStyle = mTa.getInt(R.styleable.ViewFlipperCarousel_pointStyle, 0);
        uncheckColor = mTa.getDrawable(R.styleable.ViewFlipperCarousel_uncheckColor);
        checkColor = mTa.getDrawable(R.styleable.ViewFlipperCarousel_checkColor);
        if (uncheckColor == null) {
            uncheckColor = mContext.getDrawable(R.drawable.view_flipper_uncheck);
        }
        if (checkColor == null) {
            checkColor = mContext.getDrawable(R.drawable.view_flipper_check);
        }
    }

    @Override
    public void initView() {
        initViewFlipper();
    }

    public void initViewFlipper() {
        viewFlipper = new BaseViewFlipper(mContext);
        viewFlipper.setSupportGesture(isSupportGesture);
        viewFlipper.setDirection(direction);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MP, MP);
        viewFlipper.setLayoutParams(params);
        viewFlipper.setAutoStart(autoPlay);
        viewFlipper.setFlipInterval(speed);
        viewFlipper.setChangeListener((position)->{
            notifyPoint(position);
        });
        addView(viewFlipper);
    }

    public void initFlipperData() {
        if (viewFlipper != null) {
            viewFlipper.removeAllViews();
        }
        for (String data : datas) {
            ImageView imageView = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MP, MP);
            imageView.setLayoutParams(params);
            Glide.with(mContext).load(data).into(imageView);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            if (viewFlipper != null) {
                viewFlipper.addView(imageView);
            }
        }
    }

    public void initPoint() {
        if (showReferencePoint) {
            points = new ArrayList<>();
            LinearLayout pointLayout = new LinearLayout(mContext);
            RelativeLayout.LayoutParams pointParams = new RelativeLayout.LayoutParams(WC, WC);
            if (indicatePoint == 0) {
                pointParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                pointParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            } else if (indicatePoint == 1) {
                pointParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                pointParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            } else if (indicatePoint == 2) {
                pointParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                pointParams.addRule(RelativeLayout.ALIGN_END, RelativeLayout.TRUE);
            } else if (indicatePoint == 3) {
                pointParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                pointParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            } else if (indicatePoint == 4) {
                pointParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                pointParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            } else if (indicatePoint == 5) {
                pointParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                pointParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            } else {
                pointParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                pointParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            }
            pointLayout.setLayoutParams(pointParams);
            pointLayout.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams cricleParams = new LinearLayout.LayoutParams(dip2px(POINT_CEICLE_WIDTH), dip2px(POINT_HEIGHT));
            cricleParams.setMargins(dip2px(5),dip2px(5),dip2px(5),dip2px(5));

            for (String data : datas) {
                if (showReferencePoint) {
                    TextView textView = new TextView(mContext);
                    points.add(textView);
                    textView.setLayoutParams(cricleParams);
                    textView.setBackground(uncheckColor);
                    pointLayout.addView(textView);
                }
            }
            addView(pointLayout);
            notifyPoint(0);
        }
    }

    public void notifyPoint(int position) {
        if (points != null && points.size() > 0) {
            LinearLayout.LayoutParams cricleParams = new LinearLayout.LayoutParams(dip2px(POINT_CEICLE_WIDTH), dip2px(POINT_HEIGHT));
            cricleParams.setMargins(dip2px(5),dip2px(5),dip2px(5),dip2px(5));
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(dip2px(POINT_LINE_WIDTH), dip2px(POINT_HEIGHT));
            lineParams.setMargins(dip2px(5),dip2px(5),dip2px(5),dip2px(5));
            TextView textView = points.get(position);
            for (TextView textView1 : points) {
                if (textView == textView1) {
                    if (pointStyle == 0) {
                        textView1.setLayoutParams(cricleParams);
                    } else {
                        textView1.setLayoutParams(lineParams);
                    }
                    textView1.setBackground(checkColor);
                } else {
                    textView1.setLayoutParams(cricleParams);
                    textView1.setBackground(uncheckColor);
                }
            }
        }
    }
}
