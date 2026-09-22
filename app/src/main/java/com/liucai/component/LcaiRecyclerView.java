package com.liucai.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
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
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/9/18
 */
public class LcaiRecyclerView extends BaseRecyclerView {

    private static final int TOUCH_SLOP = 10;   // 手势判定阈值（px）

    private int viewType = RecycleViewType.DEFAULT;
    private boolean isPullRefresh;
    private boolean isLoadMore;

    /**
     * 用于手势方向判定
     */
    private float startX, startY;

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
        isPullRefresh = mTa.getBoolean(R.styleable.LcaiRecyclerView_isPullRefresh, false);
        isLoadMore = mTa.getBoolean(R.styleable.LcaiRecyclerView_isLoadMore, false);
    }

    @Override
    public void initView() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LayoutManager lm = recyclerView.getLayoutManager();
                if (!(lm instanceof LinearLayoutManager)) return;
                LinearLayoutManager llm = (LinearLayoutManager) lm;
                int lastVisible = llm.findLastVisibleItemPosition();
                int total = llm.getItemCount();
                viewType = RecycleViewType.DEFAULT;
                if (recyclerView.getAdapter() instanceof BaseRecycleAdapter) {
                    viewType = ((BaseRecycleAdapter) recyclerView.getAdapter()).getViewType();
                }
                //上拉加载更多
                boolean reachedEnd = lastVisible >= total - 1 && !recyclerView.canScrollVertically(1);
                if (isLoadMore && reachedEnd && viewType != RecycleViewType.ERROR && viewType != RecycleViewType.END && viewType != RecycleViewType.LOADING) {
                    viewType = RecycleViewType.LOADING;
                    if (typeListener != null) {
                        typeListener.onTypeChange(RecycleViewType.LOADING);
                    }
                }

                //下拉刷新
                int firstCompletelyVisible = llm.findFirstCompletelyVisibleItemPosition();
                boolean reachedTop = firstCompletelyVisible == 0 && dy < 0 && !recyclerView.canScrollVertically(-1);

                if (isPullRefresh && reachedTop && typeListener != null) {
                    typeListener.onTypeChange(RecycleViewType.REFRESH);
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                startX = ev.getX();
                startY = ev.getY();
                setDisallowIntercept(true);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float dx = Math.abs(ev.getX() - startX);
                float dy = Math.abs(ev.getY() - startY);

                // 尚未超过手势阈值，不做方向判定
                if (dy < TOUCH_SLOP && dx < TOUCH_SLOP) break;
                boolean canScrollUp   = canScrollVertically(-1);   // 还能往上滑(内容在下)
                boolean canScrollDown = canScrollVertically(1);    // 还能往下滑(内容在上)
                if (dy > dx) {
                    if (ev.getY() > startY && canScrollUp) {
                        //向下滑动
                        setDisallowIntercept(true); // 能滚，自己处理
                    } else if (ev.getY() < startY && canScrollDown) {
                        //想上滑动
                        setDisallowIntercept(true); // 能滚，自己处理
                    }else {
                        setDisallowIntercept(false); // 滚到头了，交给父容器
                    }
                } else {
                    // 横向手势：交给父容器（兼容 ViewPager 等）
                    setDisallowIntercept(false);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                setDisallowIntercept(false);
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 逐层向上请求父容器（避免只通知了一层，被更外层拦截）
     * getParent() 可能为 null（未 attach 时），做判空
     */
    private void setDisallowIntercept(boolean disallow) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
        }
    }

    public void setTypeListener(LcaiRecyclerTypeListener typeListener) {
        this.typeListener = typeListener;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public int getViewType() {
        return viewType;
    }

    public boolean isLoadMore() {
        return isLoadMore;
    }

    public boolean isPullRefresh() {
        return isPullRefresh;
    }
}