package com.liucai.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.liucai.component.base.BaseRelativeLayout;
import com.liucai.component.base.BaseViewFlipper;
import com.liucai.component.base.ItemClickListener;
import com.liucai.component.base.ViewFlipperCarouseInterface;
import com.liucai.component.bean.ViewFlipperCarouselBean;
import com.liucai.core.exception.LcaiHttpException;
import com.liucai.core.util.log.LcaiLogUtils;
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
     * 样式
     * 文字
     * 图片
     */
    public int flipperMode;
    /**
     * 文字大小
     */
    public int fontSize;
    /**
     * 文字颜色
     */
    public int fontColor;
    /**
     * 文字居中模式
     */
    public int centerMode;
    /**
     * 当前指示点颜色
     */
    public Drawable checkColor;
    /**
     * 其他指示点颜色
     */
    public Drawable uncheckColor;

    public List<ViewFlipperCarouselBean> datas;

    private BaseViewFlipper viewFlipper;
    private List<TextView> points;

    private ViewFlipperCarouseInterface carouseInterface;
    private ItemClickListener clickListener;

    @Override
    public int[] setAttrs() {
        return R.styleable.ViewFlipperCarousel;
    }

    public ViewFlipperCarousel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public void setDatas(List<ViewFlipperCarouselBean> datas) {
        this.datas = datas;
        if (flipperMode == 2) {
            if (carouseInterface == null) {
                throw new LcaiHttpException("view 模式，必须实现ViewFlipperCarouseInterface接口");
            }
        }
        if (this.datas.size() <=1) {
            viewFlipper.setSupportGesture(false);
        }
        initFlipperData();
        initPoint();
    }

    public void setCarouseInterface(ViewFlipperCarouseInterface carouseInterface) {
        this.carouseInterface = carouseInterface;
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void init() {
        flipperMode = mTa.getInt(R.styleable.ViewFlipperCarousel_flipper_mode, 0);
        fontSize = mTa.getInt(R.styleable.ViewFlipperCarousel_fontSize, 14);
        fontColor = mTa.getColor(R.styleable.ViewFlipperCarousel_fontColor, Color.parseColor("#1c1c1c"));
        centerMode = mTa.getInt(R.styleable.ViewFlipperCarousel_centerMode, 0);
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
        viewFlipper.setClickListener(new ItemClickListener() {
            @Override
            public void onItemClickListener(int position, Object entity) {
                if (clickListener != null) {
                    if (datas != null && datas.get(position) != null) {
                        clickListener.onItemClickListener(position, datas.get(position).getData());
                    } else {
                        clickListener.onItemClickListener(position,null);
                    }
                }
            }
        });
        addView(viewFlipper);
    }

    public void initFlipperData() {
        if (viewFlipper != null) {
            viewFlipper.removeAllViews();
        }

        if (flipperMode == 1) {
            for (ViewFlipperCarouselBean data : datas) {
                ImageView imageView = new ImageView(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MP, MP);
                imageView.setLayoutParams(params);
                Glide.with(mContext).load(data.getLabel()).into(imageView);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                if (viewFlipper != null) {
                    viewFlipper.addView(imageView);
                }

            }
        } else if (flipperMode == 2) {
            LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(MP, WC);
            for (ViewFlipperCarouselBean bean : datas) {
                View view = carouseInterface.onRender( bean);
                view.setLayoutParams(viewParams);
                if (viewFlipper != null) {
                    viewFlipper.addView(view);
                }
            }
        } else {
            for (ViewFlipperCarouselBean data : datas) {
                TextView textView = new TextView(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MP, MP);
                textView.setLayoutParams(params);
                textView.setText(data.getLabel());
                textView.setTextColor(fontColor);
                textView.setGravity(centerMode == 0 ? Gravity.CENTER : Gravity.CENTER_HORIZONTAL);
                textView.setTextSize(fontSize);
                textView.setSingleLine();
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setOnClickListener(v->{
                    if (clickListener != null) {
                        clickListener.onItemClickListener(0,data.getData());
                    }
                });
                if (viewFlipper != null) {
                    viewFlipper.addView(textView);
                }
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

            for (ViewFlipperCarouselBean data : datas) {
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
