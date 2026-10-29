package com.liucai.component;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.component.base.BaseRecyclerView;
import com.liucai.component.base.RecycleViewType;
import com.liucai.permission.R;

/**
 * 带上下拉状态判定 + 可选下拉刷新 UI 的 RecyclerView。
 *
 * <p><b>下拉头 UI 行为</b>（默认布局 {@code lcai_adapter_pullrefresh_layout}）：
 * <ul>
 *   <li>未达阈值：文字"下拉刷新"，箭头朝下（rotation = 0）</li>
 *   <li>超过阈值：文字"松开刷新"，箭头旋转 180° 朝上</li>
 *   <li>刷新中：文字"正在刷新..."，图片切换为 {@code lcai_loading_progress} 并无限旋转</li>
 *   <li>刷新结束：文字恢复"下拉刷新"，图片恢复箭头，rotation 归 0</li>
 * </ul>
 *
 * <p><b>功能与 UI 解耦</b>：
 * <ul>
 *   <li>{@code isPullRefresh} / {@code isLoadMore}：功能开关</li>
 *   <li>{@code showRefreshHeader} / {@code showLoadMoreFooter}：UI 开关</li>
 * </ul>
 *
 * @author liucai
 */
public class LcaiRecyclerView extends BaseRecyclerView {
    private static final long LOAD_MORE_DEBOUNCE_MS = 300L;
    private static final long REFRESH_DEBOUNCE_MS = 500L;
    private static final float PULL_DAMPING = 0.5f;
    private static final float MAX_PULL_RATIO = 1.5f;
    private static final int HIDDEN_PULL_THRESHOLD_MULTIPLIER = 10;

    /** 箭头朝下角度（图片本身箭头朝下，作为"下拉"提示） */
    private static final float ARROW_DOWN_ROTATION = 0f;

    /** 箭头朝上角度（作为"松开刷新"提示） */
    private static final float ARROW_UP_ROTATION = 180f;

    private boolean isPullRefresh;
    private boolean isLoadMore;
    private boolean showRefreshHeader;
    private boolean showLoadMoreFooter;

    private int internalViewType = RecycleViewType.DEFAULT;

    private float startX;
    private float startY;
    private float lastTouchY;
    private float pullOffset = 0f;
    @Nullable private View refreshHeaderView;
    @Nullable private TextView defaultHeaderTextView;
    @Nullable private ImageView defaultHeaderImageView;
    @Nullable private ObjectAnimator loadingAnimator;

    /** 当前箭头旋转角度（目标值，用于避免重复动画） */
    private float currentArrowRotation = ARROW_DOWN_ROTATION;

    /** 当前下拉头文案（用于避免每帧重复 setText） */
    private String currentHeaderText = "下拉刷新";

    private int headerHeight = 0;
    private boolean isPulling = false;
    private boolean isRefreshing = false;
    @Nullable private ValueAnimator headerAnimator;

    /** 用户是否通过公开 API 自定义过 header（决定 setAdapter 时是否自动套用 adapter 的布局） */
    private boolean headerUserCustomized = false;

    /** 当前使用的默认 header 布局资源 id（用于避免 setAdapter 重复 inflate） */
    @LayoutRes private int currentHeaderLayoutRes = 0;

    private float hiddenPullDistance = 0f;
    private int hiddenPullThreshold = -1;

    private int touchSlop = -1;
    private long lastLoadMoreTime = 0L;
    private long lastRefreshTime = 0L;

    @Nullable private LcaiRecyclerTypeListener typeListener;

    public LcaiRecyclerView(@NonNull Context context) {
        super(context);
    }

    public LcaiRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LcaiRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @NonNull
    @Override
    public int[] setAttrs() {
        return R.styleable.LcaiRecyclerView;
    }

    @Override
    public void init() {
        if (mTa == null) {
            return;
        }
        isPullRefresh = mTa.getBoolean(R.styleable.LcaiRecyclerView_isPullRefresh, true);
        isLoadMore = mTa.getBoolean(R.styleable.LcaiRecyclerView_isLoadMore, true);
        showRefreshHeader = mTa.getBoolean(R.styleable.LcaiRecyclerView_showRefreshHeader, true);
        showLoadMoreFooter = mTa.getBoolean(R.styleable.LcaiRecyclerView_showLoadMoreFooter, true);
    }

    @Override
    public void initView() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                handleScroll(recyclerView, dy);
            }
        });
        // 默认下拉头：使用 lcai_adapter_pullrefresh_layout
        applyDefaultHeader(R.layout.lcai_adapter_pullrefresh_layout);
    }

    /**
     * 重写 setAdapter：若用户未自定义 header，且 adapter 重写了 getPullRefreshLayout()，
     * 则自动套用 adapter 提供的下拉头布局。
     */
    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        if (!headerUserCustomized && adapter instanceof BaseRecycleAdapter) {
            BaseRecycleAdapter<?, ?> ba = (BaseRecycleAdapter<?, ?>) adapter;
            int layoutRes = ba.getPullRefreshLayout();
            if (layoutRes != 0 && layoutRes != currentHeaderLayoutRes) {
                applyDefaultHeader(layoutRes);
            }
        }
    }

    // ==================== 下拉头构造 ====================

    private void applyDefaultHeader(@LayoutRes int layoutRes) {
        View v = LayoutInflater.from(getContext()).inflate(layoutRes, null, false);
        // 手动补 LayoutParams（root 传 null 时 inflate 不解析 layout_ 属性）
        if (v.getLayoutParams() == null) {
            v.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        bindDefaultHeaderViews(v);
        this.refreshHeaderView = v;
        this.currentHeaderLayoutRes = layoutRes;
        this.headerHeight = 0;
        requestLayout();
        invalidate();
    }

    /**
     * 尝试从 header 中识别默认控件（箭头 ImageView + 文案 TextView）。
     * <p>识别不到则对应字段置 null，不控制。
     */
    private void bindDefaultHeaderViews(View v) {
        defaultHeaderImageView = v.findViewById(R.id.lcai_pull_refresh_m1);
        defaultHeaderTextView = v.findViewById(R.id.lcai_pull_refresh_t1);

        currentArrowRotation = ARROW_DOWN_ROTATION;
        if (defaultHeaderImageView != null) {
            defaultHeaderImageView.animate().cancel();
            defaultHeaderImageView.setRotation(ARROW_DOWN_ROTATION);
            defaultHeaderImageView.setImageResource(
                    isRefreshing ? R.drawable.lcai_loading_progress : R.mipmap.lcai_pull_refresh);
        }
        if (defaultHeaderTextView != null) {
            String text = isRefreshing ? "正在刷新..." : "下拉刷新";
            defaultHeaderTextView.setText(text);
            currentHeaderText = text;
        }
        if (isRefreshing && defaultHeaderImageView != null) {
            startLoadingAnimation();
        }
    }

    private int getTouchSlop() {
        if (touchSlop <= 0) {
            touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }
        return touchSlop;
    }

    private int getHiddenPullThreshold() {
        if (hiddenPullThreshold <= 0) {
            hiddenPullThreshold = getTouchSlop() * HIDDEN_PULL_THRESHOLD_MULTIPLIER;
        }
        return hiddenPullThreshold;
    }

    // ==================== 测量 / 绘制 ====================

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (refreshHeaderView != null && getMeasuredWidth() > 0) {
            refreshHeaderView.measure(
                    MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            headerHeight = refreshHeaderView.getMeasuredHeight();
            refreshHeaderView.layout(0, 0, getMeasuredWidth(), headerHeight);

            // 首次自动刷新时 headerHeight 从 0 变为真实值，需要把 header 展开到正确位置
            if (isRefreshing && showRefreshHeader && headerHeight > 0 && pullOffset < headerHeight) {
                pullOffset = headerHeight;
                invalidate();
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (showRefreshHeader && pullOffset > 0f && refreshHeaderView != null && headerHeight > 0) {
            int save = canvas.save();
            canvas.translate(0, pullOffset);
            super.dispatchDraw(canvas);
            canvas.restoreToCount(save);

            save = canvas.save();
            canvas.translate(0, pullOffset - headerHeight);
            refreshHeaderView.draw(canvas);
            canvas.restoreToCount(save);
        } else {
            super.dispatchDraw(canvas);
        }
    }

    // ==================== 滚动状态判定 ====================

    private void handleScroll(@NonNull RecyclerView rv, int dy) {
        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        if (!(lm instanceof LinearLayoutManager)) {
            return;
        }
        LinearLayoutManager llm = (LinearLayoutManager) lm;

        RecyclerView.Adapter<?> adapter = rv.getAdapter();
        if (adapter == null) {
            return;
        }

        int dataCount = adapter.getItemCount();
        int currentViewType = getViewType();
        if (adapter instanceof BaseRecycleAdapter) {
            BaseRecycleAdapter<?, ?> baseAdapter = (BaseRecycleAdapter<?, ?>) adapter;
            if (baseAdapter.openMultiMode()) {
                dataCount = Math.max(0, dataCount - 1);
            }
        }
        if (dataCount <= 0) {
            return;
        }

        if (isLoadMore && dy > 0) {
            int lastVisible = llm.findLastVisibleItemPosition();
            boolean reachedEnd = lastVisible >= dataCount - 1 && !rv.canScrollVertically(1);
            if (reachedEnd
                    && currentViewType != RecycleViewType.LOADING
                    && currentViewType != RecycleViewType.END
                    && currentViewType != RecycleViewType.ERROR) {
                long now = SystemClock.uptimeMillis();
                if (now - lastLoadMoreTime >= LOAD_MORE_DEBOUNCE_MS) {
                    lastLoadMoreTime = now;
                    if (typeListener != null) {
                        typeListener.onTypeChange(RecycleViewType.LOADING);
                    }
                }
            }
        }
    }

    // ==================== 手势拦截 & 下拉驱动 ====================

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isRefreshing && showRefreshHeader) {
            return true;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                lastTouchY = ev.getY();
                hiddenPullDistance = 0f;
                disallowParentIntercept(true);
                return super.dispatchTouchEvent(ev);

            case MotionEvent.ACTION_MOVE:
                if (isPulling) {
                    updatePullOffset(ev.getY());
                    return true;
                }

                float dx = Math.abs(ev.getX() - startX);
                float dy = Math.abs(ev.getY() - startY);
                if (dx < getTouchSlop() && dy < getTouchSlop()) {
                    break;
                }

                if (dy > dx) {
                    boolean fingerDown = ev.getY() > startY;
                    boolean fingerUp = ev.getY() < startY;
                    boolean canScrollUp = canScrollVertically(-1);
                    boolean canScrollDown = canScrollVertically(1);

                    if (fingerDown && !canScrollUp && isPullRefresh) {
                        if (showRefreshHeader && headerHeight > 0) {
                            isPulling = true;
                            pullOffset = 0f;
                            lastTouchY = ev.getY();
                            return true;
                        } else if (!isRefreshing) {
                            hiddenPullDistance += (ev.getY() - lastTouchY);
                            lastTouchY = ev.getY();
                            if (hiddenPullDistance >= getHiddenPullThreshold()) {
                                triggerRefreshNoUI();
                            }
                        }
                    } else {
                        hiddenPullDistance = 0f;
                        lastTouchY = ev.getY();
                    }

                    if ((fingerDown && canScrollUp) || (fingerUp && canScrollDown)) {
                        disallowParentIntercept(true);
                    } else {
                        disallowParentIntercept(false);
                    }
                } else {
                    hiddenPullDistance = 0f;
                    disallowParentIntercept(false);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                hiddenPullDistance = 0f;
                if (isPulling) {
                    isPulling = false;
                    if (pullOffset >= headerHeight && ev.getActionMasked() == MotionEvent.ACTION_UP) {
                        startRefresh();
                    } else {
                        animatePullOffset(0f, 250);
                    }
                    return true;
                }
                disallowParentIntercept(false);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void updatePullOffset(float currentY) {
        float delta = currentY - lastTouchY;
        pullOffset += delta * PULL_DAMPING;
        float maxOffset = headerHeight * MAX_PULL_RATIO;
        pullOffset = Math.max(0f, Math.min(pullOffset, maxOffset));
        lastTouchY = currentY;
        invalidate();

        boolean overThreshold = pullOffset >= headerHeight;

        // 文案 + 箭头方向
        updateHeaderText(overThreshold ? "松开刷新" : "下拉刷新");
        if (loadingAnimator == null) {
            setArrowRotation(overThreshold ? ARROW_UP_ROTATION : ARROW_DOWN_ROTATION);
        }
    }

    private void updateHeaderText(String text) {
        if (defaultHeaderTextView == null) return;
        if (TextUtils.equals(currentHeaderText, text)) return;
        currentHeaderText = text;
        defaultHeaderTextView.setText(text);
    }

    /**
     * 箭头旋转到目标角度（150ms 平滑过渡）。
     * <p>刷新中（loadingAnimator != null）不调用本方法，避免与 loading 旋转冲突。
     */
    private void setArrowRotation(float target) {
        if (defaultHeaderImageView == null) return;
        if (Math.abs(currentArrowRotation - target) < 1f) return;
        currentArrowRotation = target;
        defaultHeaderImageView.animate()
                .rotation(target)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    // ==================== 刷新 ====================

    private void startRefresh() {
        isRefreshing = true;

        updateHeaderText("正在刷新...");
        if (defaultHeaderImageView != null) {
            // 取消下拉阶段的箭头旋转动画，切换到 loading 旋转
            defaultHeaderImageView.animate().cancel();
            defaultHeaderImageView.setRotation(0f);
            defaultHeaderImageView.setImageResource(R.drawable.lcai_loading_progress);
            startLoadingAnimation();
        }

        animatePullOffset(headerHeight, 200);
        if (typeListener != null) {
            typeListener.onTypeChange(RecycleViewType.REFRESH);
        }
    }

    private void triggerRefreshNoUI() {
        if (isRefreshing) return;
        long now = SystemClock.uptimeMillis();
        if (now - lastRefreshTime < REFRESH_DEBOUNCE_MS) return;
        lastRefreshTime = now;
        isRefreshing = true;
        if (typeListener != null) {
            typeListener.onTypeChange(RecycleViewType.REFRESH);
        }
    }

    private void startLoadingAnimation() {
        if (defaultHeaderImageView == null) return;
        stopLoadingAnimation();
        loadingAnimator = ObjectAnimator.ofFloat(defaultHeaderImageView, View.ROTATION, 0f, 360f);
        loadingAnimator.setDuration(800);
        loadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        loadingAnimator.setInterpolator(new LinearInterpolator());
        loadingAnimator.start();
    }

    private void stopLoadingAnimation() {
        if (loadingAnimator != null) {
            loadingAnimator.cancel();
            loadingAnimator = null;
        }
    }

    /**
     * 结束下拉刷新，恢复箭头 UI 并回弹。
     * <p>无论 UI 开关状态，<b>刷新完成后必须调用本方法。
     */
    public void stopRefresh() {
        if (!isRefreshing) {
            return;
        }
        isRefreshing = false;

        stopLoadingAnimation();
        if (defaultHeaderImageView != null) {
            defaultHeaderImageView.animate().cancel();
            defaultHeaderImageView.setRotation(ARROW_DOWN_ROTATION);
            defaultHeaderImageView.setImageResource(R.mipmap.lcai_pull_refresh);
        }
        currentArrowRotation = ARROW_DOWN_ROTATION;
        updateHeaderText("下拉刷新");

        if (showRefreshHeader) {
            animatePullOffset(0f, 250);
        } else {
            pullOffset = 0f;
            invalidate();
        }
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    private void animatePullOffset(float target, long duration) {
        if (headerAnimator != null) {
            headerAnimator.cancel();
        }
        headerAnimator = ValueAnimator.ofFloat(pullOffset, target);
        headerAnimator.setDuration(duration);
        headerAnimator.setInterpolator(new DecelerateInterpolator());
        headerAnimator.addUpdateListener(a -> {
            pullOffset = (float) a.getAnimatedValue();
            invalidate();
        });
        headerAnimator.start();
    }

    private void disallowParentIntercept(boolean disallow) {
        ViewParent parent = getParent();
        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
            parent = parent.getParent();
        }
    }

    // ==================== 对外 API ====================

    public void setTypeListener(@Nullable LcaiRecyclerTypeListener typeListener) {
        this.typeListener = typeListener;
    }

    /**
     * 设置自定义下拉头 View。
     * <p>传 null 恢复默认布局 {@code lcai_adapter_pullrefresh_layout}。
     * <p>自定义 View 若包含 id 为 {@code lcai_pull_refresh_m1}（ImageView）与
     * {@code lcai_pull_refresh_t1}（TextView）的控件，组件会自动接管文案与箭头旋转；
     * 否则组件不控制这些 UI，需由调用方自行处理。
     */
    public void setRefreshHeader(@Nullable View header) {
        if (header == null) {
            headerUserCustomized = false;
            applyDefaultHeader(R.layout.lcai_adapter_pullrefresh_layout);
            return;
        }
        headerUserCustomized = true;
        if (header.getLayoutParams() == null) {
            header.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        bindDefaultHeaderViews(header);
        this.refreshHeaderView = header;
        this.headerHeight = 0;
        requestLayout();
        invalidate();
    }

    /**
     * 通过布局资源设置自定义下拉头。
     * <p>布局内若含 {@code lcai_pull_refresh_m1} / {@code lcai_pull_refresh_t1}，
     * 组件会自动接管文案与箭头旋转。
     */
    public void setRefreshHeaderLayout(@LayoutRes int layoutRes) {
        headerUserCustomized = true;
        applyDefaultHeader(layoutRes);
    }

    // ========== 功能开关 ==========

    public void setPullRefresh(boolean enabled) {
        if (this.isPullRefresh == enabled) return;
        this.isPullRefresh = enabled;
        if (!enabled && isPulling) {
            isPulling = false;
            animatePullOffset(0f, 200);
        }
        hiddenPullDistance = 0f;
    }

    public void setLoadMore(boolean enabled) {
        if (this.isLoadMore == enabled) return;
        this.isLoadMore = enabled;
    }

    public boolean isPullRefresh() {
        return isPullRefresh;
    }

    public boolean isLoadMore() {
        return isLoadMore;
    }

    // ========== UI 开关 ==========

    /**
     * 设置是否展示下拉刷新头 UI。
     * <p>关闭时仍可触发下拉刷新（无 UI 下拉手势），只是没有视觉反馈。
     * <p>关闭时会中止进行中的下拉手势并归零偏移；若正在刷新则暂停 loading 动画。
     * <p>重新开启时若正在刷新，会恢复"正在刷新..."文案与 loading 旋转。
     */
    public void setShowRefreshHeader(boolean show) {
        if (this.showRefreshHeader == show) return;
        this.showRefreshHeader = show;
        if (show) {
            if (isRefreshing) {
                updateHeaderText("正在刷新...");
                if (defaultHeaderImageView != null) {
                    defaultHeaderImageView.setImageResource(R.drawable.lcai_loading_progress);
                    startLoadingAnimation();
                }
                if (headerHeight > 0) {
                    pullOffset = headerHeight;
                    invalidate();
                }
            }
        } else {
            stopLoadingAnimation();
            if (isPulling) {
                isPulling = false;
            }
            pullOffset = 0f;
            if (headerAnimator != null) {
                headerAnimator.cancel();
                headerAnimator = null;
            }
            invalidate();
        }
    }

    /**
     * 设置是否展示上拉加载 footer UI。
     * <p>关闭时仍会触发 LOADING 回调，只是 setViewType 不再驱动 Adapter footer。
     */
    public void setShowLoadMoreFooter(boolean show) {
        if (this.showLoadMoreFooter == show) return;
        this.showLoadMoreFooter = show;
        if (show) {
            syncViewTypeToAdapter(internalViewType);
        } else {
            syncViewTypeToAdapter(RecycleViewType.DEFAULT);
            if (internalViewType == RecycleViewType.LOADING) {
                internalViewType = RecycleViewType.DEFAULT;
            }
        }
    }

    public boolean isShowRefreshHeader() {
        return showRefreshHeader;
    }

    public boolean isShowLoadMoreFooter() {
        return showLoadMoreFooter;
    }

    // ========== 状态同步 ==========

    public void setViewType(int viewType) {
        this.internalViewType = viewType;
        if (showLoadMoreFooter) {
            syncViewTypeToAdapter(viewType);
        }
    }

    private void syncViewTypeToAdapter(int viewType) {
        RecyclerView.Adapter<?> adapter = getAdapter();
        if (adapter instanceof BaseRecycleAdapter) {
            BaseRecycleAdapter<?, ?> baseAdapter = (BaseRecycleAdapter<?, ?>) adapter;
            if (baseAdapter.openMultiMode()) {
                baseAdapter.setLoadState(viewType);
            }
        }
    }

    public int getViewType() {
        if (showLoadMoreFooter) {
            RecyclerView.Adapter<?> adapter = getAdapter();
            if (adapter instanceof BaseRecycleAdapter) {
                return ((BaseRecycleAdapter<?, ?>) adapter).getViewType();
            }
        }
        return internalViewType;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (headerAnimator != null) {
            headerAnimator.cancel();
            headerAnimator = null;
        }
        stopLoadingAnimation();
        super.onDetachedFromWindow();
    }
}