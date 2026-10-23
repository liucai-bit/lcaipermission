package com.liucai.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.component.base.BaseRelativeLayout;
import com.liucai.component.base.BaseViewFlipper;
import com.liucai.component.base.ItemClickListener;
import com.liucai.component.base.ViewFlipperCarouseInterface;
import com.liucai.component.bean.ViewFlipperCarouselBean;
import com.liucai.core.exception.LcaiHttpException;
import com.liucai.core.util.common.CommonUtils;
import com.liucai.image.ImageUtils;
import com.liucai.permission.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 轮播组件（文字 / 图片 / 自定义 View 三种模式）。
 *
 * <p>职责：
 * <ul>
 *   <li>承载三种内容源：文本（{@code flipperMode=0}）、图片（{@code flipperMode=1}）、
 *       自定义 View（{@code flipperMode=2}）</li>
 *   <li>支持手势滑动 / 自动播放 / 指示点，指示点位置、样式可配</li>
 *   <li>点击回调统一回传业务数据 {@link ViewFlipperCarouselBean#getData()}</li>
 * </ul>
 *
 * <p>使用约束：
 * <ul>
 *   <li>自定义 View 模式（{@code flipperMode=2}）时，<b>必须在 setDatas 之前</b>
 *       调用 {@link #setCarouseInterface(ViewFlipperCarouseInterface)}；
 *       若先 setDatas 后 setCarouseInterface，内部会自动重建内容</li>
 *   <li>指示点只展示前 {@code maxPointSize} 个；数据量超过该值时，超出部分页
 *       不会高亮任何指示点（PageIndicator 常见限制）</li>
 * </ul>
 *
 * @author liucai
 */
public class ViewFlipperCarousel extends BaseRelativeLayout {

    // ==================== 常量 ====================

    private static final int POINT_HEIGHT_DP      = 5;
    private static final int POINT_CIRCLE_WIDTH_DP = 5;
    private static final int POINT_LINE_WIDTH_DP   = 15;
    private static final int POINT_MARGIN_DP       = 5;

    private static final int MODE_TEXT  = 0;
    private static final int MODE_IMAGE = 1;
    private static final int MODE_VIEW  = 2;

    private static final int DEFAULT_SPEED_MS     = 3000;
    private static final int DEFAULT_MAX_POINTS   = 6;
    private static final int DEFAULT_FONT_SIZE    = 14;
    private static final int DEFAULT_FONT_COLOR   = 0xFF1C1C1C;

    // ==================== 配置字段（private，全部通过 setter 修改） ====================

    private int direction;
    private int speed;
    private boolean autoPlay;
    private boolean isSupportGesture;
    private boolean showReferencePoint;
    private int maxPointSize;
    private int indicatePoint;
    private int pointStyle;
    private int flipperMode;
    private int fontSize;
    private int fontColor;
    private int centerMode;
    @Nullable
    private Drawable checkColor;
    @Nullable
    private Drawable uncheckColor;

    // ==================== 内部状态 ====================

    @NonNull
    private final List<ViewFlipperCarouselBean> datas = new ArrayList<>();
    @NonNull
    private final List<TextView> points = new ArrayList<>();

    @Nullable
    private BaseViewFlipper viewFlipper;
    @Nullable
    private LinearLayout pointLayout;
    @Nullable
    private ViewFlipperCarouseInterface carouseInterface;
    @Nullable
    private ItemClickListener clickListener;

    /** 指示点布局参数，initPoint 时重建一次以复用 */
    @Nullable
    private LinearLayout.LayoutParams circleParams;
    @Nullable
    private LinearLayout.LayoutParams lineParams;

    // ==================== 构造 ====================

    public ViewFlipperCarousel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    @Override
    public int[] setAttrs() {
        return R.styleable.ViewFlipperCarousel;
    }

    // ==================== 基类生命周期 ====================

    @Override
    public void init() {
        if (mTa == null) {
            // 代码直接 new 时无 AttributeSet，走默认值
            direction = 1;
            speed = DEFAULT_SPEED_MS;
            autoPlay = true;
            isSupportGesture = true;
            showReferencePoint = true;
            maxPointSize = DEFAULT_MAX_POINTS;
            indicatePoint = 0;
            pointStyle = 0;
            flipperMode = MODE_TEXT;
            fontSize = DEFAULT_FONT_SIZE;
            fontColor = DEFAULT_FONT_COLOR;
            centerMode = 0;
            uncheckColor = mContext.getDrawable(R.drawable.view_flipper_uncheck);
            checkColor = mContext.getDrawable(R.drawable.view_flipper_check);
            return;
        }
        flipperMode        = mTa.getInt(R.styleable.ViewFlipperCarousel_flipper_mode, MODE_TEXT);
        fontSize           = mTa.getInt(R.styleable.ViewFlipperCarousel_fontSize, DEFAULT_FONT_SIZE);
        fontColor          = mTa.getColor(R.styleable.ViewFlipperCarousel_fontColor, DEFAULT_FONT_COLOR);
        centerMode         = mTa.getInt(R.styleable.ViewFlipperCarousel_centerMode, 0);
        direction          = mTa.getInt(R.styleable.ViewFlipperCarousel_direction, 1);
        speed              = mTa.getInt(R.styleable.ViewFlipperCarousel_speed, DEFAULT_SPEED_MS);
        autoPlay           = mTa.getBoolean(R.styleable.ViewFlipperCarousel_autoPlay, true);
        isSupportGesture   = mTa.getBoolean(R.styleable.ViewFlipperCarousel_isSupportGesture, true);
        showReferencePoint = mTa.getBoolean(R.styleable.ViewFlipperCarousel_showReferencePoint, true);
        maxPointSize       = mTa.getInt(R.styleable.ViewFlipperCarousel_maxPointSize, DEFAULT_MAX_POINTS);
        indicatePoint      = mTa.getInt(R.styleable.ViewFlipperCarousel_indicatePoint, 0);
        pointStyle         = mTa.getInt(R.styleable.ViewFlipperCarousel_pointStyle, 0);
        uncheckColor       = mTa.getDrawable(R.styleable.ViewFlipperCarousel_uncheckColor);
        checkColor         = mTa.getDrawable(R.styleable.ViewFlipperCarousel_checkColor);
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
        initPoint();
    }

    private void initViewFlipper() {
        viewFlipper = new BaseViewFlipper(mContext);
        viewFlipper.setSupportGesture(isSupportGesture);
        viewFlipper.setDirection(direction);
        if (speed > 0) {
            viewFlipper.setFlipInterval(speed);
        }
        // 初始不自动播放，等 setDatas 时判断数据量再启动
        viewFlipper.setAutoStart(false);
        viewFlipper.setLayoutParams(new RelativeLayout.LayoutParams(MP, MP));
        viewFlipper.setChangeListener(this::notifyPoint);
        viewFlipper.setClickListener((position, entity) -> dispatchClick(position));
        addView(viewFlipper);

        pointLayout = new LinearLayout(mContext);
        pointLayout.setOrientation(LinearLayout.HORIZONTAL);
        // 先给一个默认布局参数，避免 initPoint 提前返回时 pointLayout 没有约束
        pointLayout.setLayoutParams(buildPointLayoutParams());
        addView(pointLayout);
    }

    // ==================== 对外 setter ====================

    public void setDirection(int direction) {
        this.direction = direction;
        if (viewFlipper != null) {
            viewFlipper.setDirection(direction);
        }
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        if (viewFlipper != null && speed > 0) {
            viewFlipper.setFlipInterval(speed);
        }
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
        syncAutoPlay();
    }

    public void setSupportGesture(boolean supportGesture) {
        this.isSupportGesture = supportGesture;
        if (viewFlipper != null) {
            viewFlipper.setSupportGesture(supportGesture);
        }
    }

    public void setShowReferencePoint(boolean showReferencePoint) {
        this.showReferencePoint = showReferencePoint;
        initPoint();
    }

    public void setMaxPointSize(int maxPointSize) {
        this.maxPointSize = Math.max(1, maxPointSize);
        initPoint();
    }

    public void setIndicatePoint(int indicatePoint) {
        this.indicatePoint = indicatePoint;
        initPoint();
    }

    public void setPointStyle(int pointStyle) {
        this.pointStyle = pointStyle;
        notifyPoint(currentIndex());
    }

    public void setFlipperMode(int flipperMode) {
        if (this.flipperMode == flipperMode) {
            return;
        }
        this.flipperMode = flipperMode;
        // 切模式后重建内容；重建完由 syncAutoPlay 恢复播放状态
        initFlipperData();
        initPoint();
        syncAutoPlay();
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        if (flipperMode == MODE_TEXT) {
            initFlipperData();
        }
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
        if (flipperMode == MODE_TEXT) {
            initFlipperData();
        }
    }

    public void setCenterMode(int centerMode) {
        this.centerMode = centerMode;
        if (flipperMode == MODE_TEXT) {
            initFlipperData();
        }
    }

    public void setCheckColor(@Nullable Drawable checkColor) {
        this.checkColor = checkColor;
        notifyPoint(currentIndex());
    }

    public void setUncheckColor(@Nullable Drawable uncheckColor) {
        this.uncheckColor = uncheckColor;
        notifyPoint(currentIndex());
    }

    /**
     * 设置自定义 View 渲染回调。
     * <p>如果当前已经在 {@link #MODE_VIEW} 模式下且已有数据，
     * 会立即用新回调重建内容。
     */
    public void setCarouseInterface(@Nullable ViewFlipperCarouseInterface carouseInterface) {
        this.carouseInterface = carouseInterface;
        if (flipperMode == MODE_VIEW && !datas.isEmpty()) {
            initFlipperData();
            syncAutoPlay();
        }
    }

    public void setClickListener(@Nullable ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * 设置数据源。
     * <p>内部处理：
     * <ul>
     *   <li>参数为 null 时视为空列表</li>
     *   <li>数据量 ≤ 1 时停止自动播放</li>
     *   <li>数据量 &gt; 1 时按 {@link #autoPlay} 启动自动播放</li>
     *   <li>重建内容与指示点，并切回第一页</li>
     * </ul>
     */
    public void setDatas(@Nullable List<ViewFlipperCarouselBean> datas) {
        if (flipperMode == MODE_VIEW && carouseInterface == null) {
            throw new LcaiHttpException(
                    "view 模式，必须实现 ViewFlipperCarouseInterface 接口");
        }
        this.datas.clear();
        if (datas != null) {
            this.datas.addAll(datas);
        }
        if (viewFlipper != null) {
            viewFlipper.stopFlipping();
        }
        initFlipperData();
        initPoint();
        resetDisplayedChild();
        syncAutoPlay();
    }

    // ==================== 对外 getter ====================

    public int getDirection()              { return direction; }
    public int getSpeed()                  { return speed; }
    public boolean isAutoPlay()            { return autoPlay; }
    public boolean isSupportGesture()      { return isSupportGesture; }
    public boolean isShowReferencePoint()  { return showReferencePoint; }
    public int getMaxPointSize()           { return maxPointSize; }
    public int getIndicatePoint()          { return indicatePoint; }
    public int getPointStyle()             { return pointStyle; }
    public int getFlipperMode()            { return flipperMode; }
    public int getFontSize()               { return fontSize; }
    public int getFontColor()              { return fontColor; }
    public int getCenterMode()             { return centerMode; }

    @Nullable
    public Drawable getCheckColor()        { return checkColor; }
    @Nullable
    public Drawable getUncheckColor()      { return uncheckColor; }

    /** 返回数据副本，外部修改不影响内部状态。 */
    @NonNull
    public List<ViewFlipperCarouselBean> getDatas() {
        return new ArrayList<>(datas);
    }

    /** 当前展示的索引（0 起）。 */
    public int getCurrentIndex() {
        return currentIndex();
    }

    /** 内部 {@link BaseViewFlipper}，一般业务不需要访问，特殊场景（如外部控制翻页）可获取。 */
    @Nullable
    public BaseViewFlipper getViewFlipper() {
        return viewFlipper;
    }

    // ==================== 内容渲染 ====================

    private void initFlipperData() {
        if (viewFlipper == null) {
            return;
        }
        viewFlipper.removeAllViews();
        if (datas.isEmpty()) {
            return;
        }
        switch (flipperMode) {
            case MODE_IMAGE:
                renderImageItems();
                break;
            case MODE_VIEW:
                renderCustomViewItems();
                break;
            case MODE_TEXT:
            default:
                renderTextItems();
                break;
        }
        // ViewAnimator.removeAllViews 后 mWhichChild 可能保留旧值，clamp 到合法区间
        clampDisplayedChild();
    }

    private void renderImageItems() {
        for (int i = 0; i < datas.size(); i++) {
            ViewFlipperCarouselBean bean = datas.get(i);
            final int index = i;
            ImageView iv = new ImageView(mContext);
            // 在 FrameLayout 系（ViewAnimator）下用 FrameLayout.LayoutParams
            iv.setLayoutParams(new FrameLayout.LayoutParams(MP, MP));
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            ImageUtils.loadImage(mContext, bean.getLabel(), iv);
            iv.setOnClickListener(v -> dispatchClick(index));
            viewFlipper.addView(iv);
        }
    }

    private void renderCustomViewItems() {
        if (carouseInterface == null) {
            return;
        }
        for (int i = 0; i < datas.size(); i++) {
            ViewFlipperCarouselBean bean = datas.get(i);
            final int index = i;
            View view = carouseInterface.onRender(bean);
            if (view == null) {
                // 业务返回 null 时跳过，避免 addView 崩
                continue;
            }
            // 每个 View 独立 LayoutParams，避免共享同一实例
            view.setLayoutParams(new FrameLayout.LayoutParams(MP, WC));
            view.setOnClickListener(v -> dispatchClick(index));
            viewFlipper.addView(view);
        }
    }

    private void renderTextItems() {
        int gravity = resolveTextGravity();
        for (int i = 0; i < datas.size(); i++) {
            ViewFlipperCarouselBean bean = datas.get(i);
            final int index = i;
            TextView tv = new TextView(mContext);
            tv.setLayoutParams(new FrameLayout.LayoutParams(MP, MP));
            tv.setText(bean.getLabel());
            tv.setTextColor(fontColor);
            tv.setTextSize(fontSize);
            tv.setGravity(gravity);
            tv.setSingleLine(true);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setOnClickListener(v -> dispatchClick(index));
            viewFlipper.addView(tv);
        }
    }

    private int resolveTextGravity() {
        switch (centerMode) {
            case 1:  return Gravity.LEFT  | Gravity.CENTER_VERTICAL;
            case 2:  return Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            case 0:
            default: return Gravity.CENTER;
        }
    }

    /**
     * 统一点击事件分发入口。
     */
    private void dispatchClick(int position) {
        if (clickListener == null) {
            return;
        }
        if (position < 0 || position >= datas.size()) {
            clickListener.onItemClickListener(position, null);
            return;
        }
        clickListener.onItemClickListener(position, datas.get(position).getData());
    }

    // ==================== 自动播放同步 ====================

    /**
     * 依据当前 {@link #datas} 和 {@link #autoPlay} 同步播放状态。
     * <p>数据量 ≤ 1 时强制停止，避免单条数据反复闪。
     */
    private void syncAutoPlay() {
        if (viewFlipper == null) {
            return;
        }
        boolean shouldPlay = autoPlay && datas.size() > 1;
        viewFlipper.setAutoStart(shouldPlay);
        if (shouldPlay) {
            viewFlipper.setSupportGesture(isSupportGesture);
            if (speed > 0) {
                viewFlipper.setFlipInterval(speed);
            }
            viewFlipper.startFlipping();
        } else {
            viewFlipper.stopFlipping();
        }
    }

    // ==================== 指示点 ====================

    private void initPoint() {
        if (pointLayout == null) {
            return;
        }
        pointLayout.removeAllViews();
        points.clear();
        circleParams = null;
        lineParams = null;

        // 无论是否展示，都先确保布局参数存在，避免 setShowReferencePoint(true) 后位置错乱
        pointLayout.setLayoutParams(buildPointLayoutParams());
        pointLayout.setOrientation(LinearLayout.HORIZONTAL);

        if (!showReferencePoint || datas.isEmpty()) {
            return;
        }

        circleParams = buildCircleParams();
        lineParams = buildLineParams();

        int visibleCount = Math.min(datas.size(), Math.max(1, maxPointSize));
        for (int i = 0; i < visibleCount; i++) {
            TextView dot = new TextView(mContext);
            dot.setLayoutParams(circleParams);
            dot.setBackground(uncheckColor);
            points.add(dot);
            pointLayout.addView(dot);
        }
        notifyPoint(currentIndex());
    }

    private RelativeLayout.LayoutParams buildPointLayoutParams() {
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(WC, WC);
        switch (indicatePoint) {
            case 1:  // 底部左
                p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                p.addRule(RelativeLayout.ALIGN_PARENT_START,  RelativeLayout.TRUE);
                break;
            case 2:  // 底部右
                p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                p.addRule(RelativeLayout.ALIGN_PARENT_END,    RelativeLayout.TRUE);
                break;
            case 3:  // 顶部居中
                p.addRule(RelativeLayout.ALIGN_PARENT_TOP,   RelativeLayout.TRUE);
                p.addRule(RelativeLayout.CENTER_HORIZONTAL,  RelativeLayout.TRUE);
                break;
            case 4:  // 顶部左
                p.addRule(RelativeLayout.ALIGN_PARENT_TOP,   RelativeLayout.TRUE);
                p.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                break;
            case 5:  // 顶部右
                p.addRule(RelativeLayout.ALIGN_PARENT_TOP,   RelativeLayout.TRUE);
                p.addRule(RelativeLayout.ALIGN_PARENT_END,   RelativeLayout.TRUE);
                break;
            case 0:
            default: // 底部居中
                p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                p.addRule(RelativeLayout.CENTER_HORIZONTAL,   RelativeLayout.TRUE);
                break;
        }
        return p;
    }

    private LinearLayout.LayoutParams buildCircleParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                CommonUtils.dip2px(mContext, POINT_CIRCLE_WIDTH_DP),
                CommonUtils.dip2px(mContext, POINT_HEIGHT_DP));
        int m = CommonUtils.dip2px(mContext, POINT_MARGIN_DP);
        p.setMargins(m, m, m, m);
        return p;
    }

    private LinearLayout.LayoutParams buildLineParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                CommonUtils.dip2px(mContext, POINT_LINE_WIDTH_DP),
                CommonUtils.dip2px(mContext, POINT_HEIGHT_DP));
        int m = CommonUtils.dip2px(mContext, POINT_MARGIN_DP);
        p.setMargins(m, m, m, m);
        return p;
    }

    /**
     * 高亮当前页对应的指示点。
     * <p>越界时清除所有选中，避免残留旧高亮。
     */
    private void notifyPoint(int position) {
        if (points.isEmpty() || circleParams == null || lineParams == null) {
            return;
        }
        boolean outOfRange = position < 0 || position >= points.size();
        for (int i = 0; i < points.size(); i++) {
            TextView dot = points.get(i);
            boolean selected = !outOfRange && i == position;
            dot.setLayoutParams(selected && pointStyle != 0 ? lineParams : circleParams);
            dot.setBackground(selected ? checkColor : uncheckColor);
        }
    }

    // ==================== 内部辅助 ====================

    private int currentIndex() {
        if (viewFlipper == null) {
            return 0;
        }
        int index = viewFlipper.getDisplayedChild();
        return index < 0 ? 0 : index;
    }

    private void resetDisplayedChild() {
        if (viewFlipper != null && viewFlipper.getChildCount() > 0) {
            viewFlipper.setDisplayedChild(0);
            notifyPoint(0);
        }
    }

    /**
     * 将 {@code displayedChild} 限制在合法区间内。
     * <p>重建内容后子 View 数量可能减少，不改会把 {@code getCurrentView()} 置于越界状态。
     */
    private void clampDisplayedChild() {
        if (viewFlipper == null) {
            return;
        }
        int childCount = viewFlipper.getChildCount();
        if (childCount <= 0) {
            return;
        }
        int current = viewFlipper.getDisplayedChild();
        if (current < 0 || current >= childCount) {
            viewFlipper.setDisplayedChild(0);
        }
    }

    // ==================== 生命周期 ====================

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 视图脱离时停止播放，避免内部 Handler / 动画持续占用资源
        if (viewFlipper != null) {
            viewFlipper.stopFlipping();
        }
    }
}