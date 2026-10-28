package com.liucai.component;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
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
 * <p><b>功能与 UI 解耦</b>：
 * <ul>
 *   <li>{@code isPullRefresh} / {@code isLoadMore}：功能开关，决定是否触发 REFRESH / LOADING 回调</li>
 *   <li>{@code showRefreshHeader} / {@code showLoadMoreFooter}：UI 开关，决定是否展示下拉头 / footer 状态</li>
 * </ul>
 *
 * <p>四种组合：
 * <pre>
 *  功能开 + UI 开：手势下拉→展示 header→触发刷新；滑到底→展示 footer→触发加载
 *  功能开 + UI 关：手势下拉到阈值→触发刷新（无视觉反馈）；滑到底→触发加载（无 footer）
 *  功能关 + UI 开：UI 开关无效（功能不触发）
 *  功能关 + UI 关：全部关闭
 * </pre>
 *
 * <p>默认值（XML 可覆盖）：
 * <ul>
 *   <li>isPullRefresh = true</li>
 *   <li>isLoadMore = true</li>
 *   <li>showRefreshHeader = false</li>
 *   <li>showLoadMoreFooter = false</li>
 * </ul>
 *
 * <p><b>注意：</b>上拉加载 footer 是否占位由 {@link BaseRecycleAdapter#openMultiMode()} 决定。
 * 若 UI 关闭时想彻底移除 footer 占位，请让 Adapter 的 openMultiMode() 返回 false；
 * 此时 {@link #setViewType(int)} 也只更新内部状态，不驱动 Adapter。
 *
 * @author liucai
 */
public class LcaiRecyclerView extends BaseRecyclerView {
    private static final long LOAD_MORE_DEBOUNCE_MS = 300L;
    private static final long REFRESH_DEBOUNCE_MS = 500L;
    private static final float PULL_DAMPING = 0.5f;
    private static final float MAX_PULL_RATIO = 1.5f;
    private static final int HIDDEN_PULL_THRESHOLD_MULTIPLIER = 10;
    private boolean isPullRefresh = true;
    private boolean isLoadMore = true;
    private boolean showRefreshHeader = true;
    private boolean showLoadMoreFooter = false;
    private int internalViewType = RecycleViewType.DEFAULT;
    private float startX;
    private float startY;
    private float lastTouchY;
    private float pullOffset = 0f;
    @Nullable private View refreshHeaderView;
    @Nullable private TextView defaultHeaderTextView;
    private int headerHeight = 0;
    private boolean isPulling = false;
    private boolean isRefreshing = false;
    @Nullable private ValueAnimator headerAnimator;
    /** 无 UI 下拉累积距离 */
    private float hiddenPullDistance = 0f;
    /** 无 UI 下拉阈值缓存 */
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
        setRefreshHeader(createDefaultHeader());
    }

    private View createDefaultHeader() {
        TextView tv = new TextView(getContext());
        tv.setText("下拉刷新");
        tv.setTextColor(Color.parseColor("#666666"));
        tv.setTextSize(14f);
        tv.setGravity(Gravity.CENTER);
        int padding = (int) (getResources().getDisplayMetrics().density * 16 + 0.5f);
        tv.setPadding(0, padding, 0, padding);
        tv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        defaultHeaderTextView = tv;
        return tv;
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (refreshHeaderView != null && getMeasuredWidth() > 0) {
            refreshHeaderView.measure(
                    MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            headerHeight = refreshHeaderView.getMeasuredHeight();
            refreshHeaderView.layout(0, 0, getMeasuredWidth(), headerHeight);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 仅当 UI 开启时绘制下拉头效果
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

        // ---- 上拉加载更多（isLoadMore 关闭则不触发）----
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 有 UI 且正在刷新：消费所有事件
        // 无 UI 时刷新不阻断手势（用户没有视觉反馈，阻断手势会很奇怪）
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
                // 有 UI 的下拉模式：更新下拉偏移
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
                        // 已到顶部且继续下拉
                        if (showRefreshHeader && headerHeight > 0) {
                            // ★ 有 UI：进入下拉模式，跟手 + 展示 header
                            isPulling = true;
                            pullOffset = 0f;
                            lastTouchY = ev.getY();
                            return true;
                        } else if (!isRefreshing) {
                            // ★ 无 UI：累积下拉距离，超阈值直接触发刷新
                            hiddenPullDistance += (ev.getY() - lastTouchY);
                            lastTouchY = ev.getY();
                            if (hiddenPullDistance >= getHiddenPullThreshold()) {
                                triggerRefreshNoUI();
                            }
                        }
                    } else {
                        // 非下拉：重置累积
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

        if (defaultHeaderTextView != null) {
            defaultHeaderTextView.setText(pullOffset >= headerHeight ? "松开刷新" : "下拉刷新");
        }
    }

    /** 有 UI 的下拉刷新：松手超阈值触发 */
    private void startRefresh() {
        isRefreshing = true;
        if (defaultHeaderTextView != null) {
            defaultHeaderTextView.setText("正在刷新...");
        }
        animatePullOffset(headerHeight, 200);
        if (typeListener != null) {
            typeListener.onTypeChange(RecycleViewType.REFRESH);
        }
    }

    /** 无 UI 的下拉刷新：下拉距离超阈值直接触发 */
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

    /**
     * 结束下拉刷新，执行回弹动画。
     * <p>无论 UI 开关状态，刷新完成后必须调用本方法。
     */
    public void stopRefresh() {
        if (!isRefreshing) {
            return;
        }
        isRefreshing = false;
        if (defaultHeaderTextView != null) {
            defaultHeaderTextView.setText("下拉刷新");
        }
        if (showRefreshHeader) {
            animatePullOffset(0f, 250);
        } else {
            // 无 UI：直接归零，无需动画
            pullOffset = 0f;
            invalidate();
        }
    }

    /** 当前是否处于刷新中。 */
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

    public void setTypeListener(@Nullable LcaiRecyclerTypeListener typeListener) {
        this.typeListener = typeListener;
    }

    public void setRefreshHeader(@Nullable View header) {
        if (header == null) {
            header = createDefaultHeader();
        } else {
            if (header.getLayoutParams() == null) {
                header.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            if (header != defaultHeaderTextView) {
                this.defaultHeaderTextView = null;
            }
        }
        this.refreshHeaderView = header;
        this.headerHeight = 0;
        requestLayout();
        invalidate();
    }

    public void setRefreshHeaderLayout(@LayoutRes int layoutRes) {
        View v = LayoutInflater.from(getContext()).inflate(layoutRes, this, false);
        setRefreshHeader(v);
    }

    /**
     * 设置是否启用下拉刷新功能（是否触发 REFRESH 回调）。
     * <p>关闭时会中止进行中的下拉手势。
     */
    public void setPullRefresh(boolean enabled) {
        if (this.isPullRefresh == enabled) return;
        this.isPullRefresh = enabled;
        if (!enabled && isPulling) {
            isPulling = false;
            animatePullOffset(0f, 200);
        }
        hiddenPullDistance = 0f;
    }

    /**
     * 设置是否启用上拉加载功能（是否触发 LOADING 回调）。
     */
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

    /**
     * 设置是否展示下拉刷新头 UI。
     * <p>关闭时仍可触发下拉刷新（通过无 UI 下拉手势），只是没有视觉反馈。
     * <p>关闭时会中止进行中的下拉手势，并把偏移归零。
     */
    public void setShowRefreshHeader(boolean show) {
        if (this.showRefreshHeader == show) return;
        this.showRefreshHeader = show;
        if (!show) {
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
     * <p>关闭时仍会触发 LOADING 回调，只是 {@link #setViewType(int)} 不再驱动 Adapter footer。
     * <p>关闭瞬间会把 Adapter footer 状态重置为 DEFAULT，避免残留"正在加载..."。
     * <p>注意：footer 占位 item 仍由 Adapter 的 openMultiMode() 决定。
     */
    public void setShowLoadMoreFooter(boolean show) {
        if (this.showLoadMoreFooter == show) return;
        this.showLoadMoreFooter = show;
        if (show) {
            // UI 开启时把内部状态刷到 adapter
            syncViewTypeToAdapter(internalViewType);
        } else {
            // UI 关闭时重置 adapter footer，避免残留 LOADING
            syncViewTypeToAdapter(RecycleViewType.DEFAULT);
        }
    }

    public boolean isShowRefreshHeader() {
        return showRefreshHeader;
    }

    public boolean isShowLoadMoreFooter() {
        return showLoadMoreFooter;
    }

    // ========== 状态同步 ==========

    /**
     * 更新加载状态。
     * <p>showLoadMoreFooter=true 时同步给 Adapter footer；
     * 为 false 时仅记录到内部状态，用于防抖判断，不影响 Adapter。
     */
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

    /**
     * 读取当前加载状态。
     * <p>UI 开启时优先读 Adapter；UI 关闭时读内部记录。
     */
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
        super.onDetachedFromWindow();
    }
}