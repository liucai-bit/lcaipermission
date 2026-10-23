package com.liucai.component;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.component.base.BaseRecyclerView;
import com.liucai.component.base.RecycleViewType;
import com.liucai.permission.R;

/**
 * 带上下拉状态判定的 RecyclerView。
 *
 * <p>职责：
 * <ul>
 *   <li>滚动到底部时通知 {@link LcaiRecyclerTypeListener#onTypeChange(int)}（LOADING）</li>
 *   <li>滚动到顶部时通知 {@link LcaiRecyclerTypeListener#onTypeChange(int)}（REFRESH）</li>
 *   <li>触摸方向判定：纵向自己处理、横滑让给父容器，滚到头时让出拦截权</li>
 * </ul>
 *
 * <p>使用约束：
 * <ul>
 *   <li>下拉刷新 / 上拉加载的实际 UI 由外部实现，本类只负责"什么时候该触发"</li>
 *   <li>加载状态以 {@link BaseRecycleAdapter#getViewType()} 为唯一真相源，
 *       本类不再冗余维护一份</li>
 *   <li>适配器需要配合 {@link BaseRecycleAdapter} 使用（用于识别 footer / 读取状态）</li>
 * </ul>
 *
 * @author liucai
 */
public class LcaiRecyclerView extends BaseRecyclerView {

    /** 上拉加载触发防抖间隔（ms） */
    private static final long LOAD_MORE_DEBOUNCE_MS = 300L;

    /** 下拉刷新触发防抖间隔（ms） */
    private static final long REFRESH_DEBOUNCE_MS = 500L;

    private boolean isPullRefresh;
    private boolean isLoadMore;

    /** 手势起点 */
    private float startX;
    private float startY;

    /**
     * 触摸判定阈值（px）。
     * <p>基类会在构造函数中回调 {@link #init()} / {@link #initView()}，
     * 而子类字段初始化晚于 super()，因此这里用懒加载避免时序问题。
     */
    private int touchSlop = -1;

    /** 上一次加载 / 刷新触发的时间戳，用于防抖 */
    private long lastLoadMoreTime = 0L;
    private long lastRefreshTime = 0L;

    @Nullable
    private LcaiRecyclerTypeListener typeListener;

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
        // 防御：代码直接 new 时不走 AttributeSet，mTa 可能为 null
        if (mTa == null) {
            return;
        }
        isPullRefresh = mTa.getBoolean(R.styleable.LcaiRecyclerView_isPullRefresh, false);
        isLoadMore = mTa.getBoolean(R.styleable.LcaiRecyclerView_isLoadMore, false);
    }

    @Override
    public void initView() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                handleScroll(recyclerView, dy);
            }
        });
    }

    private int getTouchSlop() {
        if (touchSlop <= 0) {
            touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }
        return touchSlop;
    }

    // ==================== 滚动状态判定 ====================

    /**
     * 判定当前滚动位置是否需要触发加载 / 刷新。
     *
     * @param dy 滚动距离：正数表示内容向上滚（用户上拉），负数表示内容向下滚（用户下拉）
     */
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

        // 真实数据条目数（若适配器开启 footer 模式，需排除 footer）
        int dataCount = adapter.getItemCount();
        int currentViewType = RecycleViewType.DEFAULT;
        if (adapter instanceof BaseRecycleAdapter) {
            BaseRecycleAdapter<?, ?> baseAdapter = (BaseRecycleAdapter<?, ?>) adapter;
            currentViewType = baseAdapter.getViewType();
            if (baseAdapter.openMultiMode()) {
                dataCount = Math.max(0, dataCount - 1);
            }
        }
        if (dataCount <= 0) {
            return;
        }

        // ---- 上拉加载更多 ----
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

        // ---- 下拉刷新 ----
        if (isPullRefresh && dy < 0) {
            int firstVisible = llm.findFirstCompletelyVisibleItemPosition();
            boolean reachedTop = firstVisible == 0 && !rv.canScrollVertically(-1);
            if (reachedTop) {
                long now = SystemClock.uptimeMillis();
                if (now - lastRefreshTime >= REFRESH_DEBOUNCE_MS) {
                    lastRefreshTime = now;
                    if (typeListener != null) {
                        typeListener.onTypeChange(RecycleViewType.REFRESH);
                    }
                }
            }
        }
    }

    // ==================== 手势拦截 ====================

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                // 默认由自己处理，方向确定后再决定是否让给父容器
                disallowParentIntercept(true);
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(ev.getX() - startX);
                float dy = Math.abs(ev.getY() - startY);
                if (dx < getTouchSlop() && dy < getTouchSlop()) {
                    // 还没超过阈值，不判定方向
                    break;
                }

                if (dy > dx) {
                    // 纵向手势：能滚动就自己处理，滚到头则让出拦截权
                    boolean fingerDown = ev.getY() > startY;
                    boolean fingerUp = ev.getY() < startY;
                    boolean canScrollUp = canScrollVertically(-1);
                    boolean canScrollDown = canScrollVertically(1);
                    if ((fingerDown && canScrollUp) || (fingerUp && canScrollDown)) {
                        disallowParentIntercept(true);
                    } else {
                        disallowParentIntercept(false);
                    }
                } else {
                    // 横向手势：交给父容器（兼容 ViewPager / 侧滑）
                    disallowParentIntercept(false);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                disallowParentIntercept(false);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 通知整条父链禁止 / 允许拦截触摸事件。
     * <p>标准 {@link android.view.ViewGroup#requestDisallowInterceptTouchEvent(boolean)}
     * 会自动向上传播，但个别第三方容器重写时未调 super，逐层通知更保险。
     */
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
     * 更新加载状态。
     * <p>会同步到 {@link BaseRecycleAdapter#setLoadState(int)}，保持 Adapter 与 View 一致。
     * <p>适配器未开启 footer 模式时本方法无效（没有 footer 可展示）。
     */
    public void setViewType(int viewType) {
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
     * <p>优先从 Adapter 读取（唯一真相源），无 Adapter 时返回 {@link RecycleViewType#DEFAULT}。
     */
    public int getViewType() {
        RecyclerView.Adapter<?> adapter = getAdapter();
        if (adapter instanceof BaseRecycleAdapter) {
            return ((BaseRecycleAdapter<?, ?>) adapter).getViewType();
        }
        return RecycleViewType.DEFAULT;
    }

    public boolean isLoadMore() {
        return isLoadMore;
    }

    public boolean isPullRefresh() {
        return isPullRefresh;
    }
}