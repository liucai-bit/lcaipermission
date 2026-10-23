package com.liucai.component.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.ViewFlipper;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.liucai.permission.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 支持手势滑动翻页的 ViewFlipper。
 *
 * <p>能力：
 * <ul>
 *   <li>横向 / 纵向滑动翻页，跟手带阻尼，松手后按距离或速度判定是否翻页</li>
 *   <li>未达到翻页阈值时平滑回弹</li>
 *   <li>单 item 时退化为普通可点击 View，不干扰父容器滚动</li>
 *   <li>区分"点击"和"滑动"，点击通过 {@link ItemClickListener} 分发</li>
 *   <li>手势结束后按 {@link #isAutoStart()} 恢复自动播放，避免强制重启已关闭的播放</li>
 * </ul>
 *
 * <p>交互约定：
 * <ul>
 *   <li>多 item：滑动方向与 {@link #getDirection()} 一致才接管；不一致时让给子 View 或父容器</li>
 *   <li>纵向模式且父容器还能滚时，优先让父容器滚动</li>
 *   <li>程序化调用 {@link #showNext()} / {@link #showPrevious()} 时使用补间动画</li>
 *   <li>手势翻页使用属性动画，翻页过程中被打断立即归位，不残留中间态</li>
 * </ul>
 *
 * @author liucai
 */
public class BaseViewFlipper extends ViewFlipper {

    // ==================== 方向常量 ====================

    public static final int DIRECTION_VERTICAL = 0;
    public static final int DIRECTION_HORIZONTAL = 1;

    /** 内部使用，表示"方向尚未锁定" */
    private static final int DIRECTION_NONE = -1;

    /** 对外暴露的方向限制 */
    @IntDef({DIRECTION_VERTICAL, DIRECTION_HORIZONTAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction {
    }

    /** 内部手势锁定方向限制 */
    @IntDef({DIRECTION_NONE, DIRECTION_VERTICAL, DIRECTION_HORIZONTAL})
    @Retention(RetentionPolicy.SOURCE)
    private @interface LockedDirection {
    }

    // ==================== 手势参数 ====================

    /** 触发翻页的最小滑动距离（dp） */
    private static final float SWIPE_THRESHOLD_DP = 50f;
    /** 判定点击的最大位移（dp） */
    private static final float CLICK_SLOP_DP = 8f;
    /** 判定点击的最大时长（ms） */
    private static final long CLICK_MAX_DURATION = 300L;
    /** 主方向系数：主方向位移需大于副方向的该倍数才锁定 */
    private static final float DOMINANT_RATIO = 1.2f;
    /** 跟手阻尼系数（0~1，越小越"重"） */
    private static final float DRAG_DAMPING = 0.6f;

    // ==================== 动画参数 ====================

    /** 翻页补完动画的最大时长（ms） */
    private static final long FLIP_DURATION = 220L;
    /** 未触发翻页时的回弹动画时长（ms） */
    private static final long RESET_DURATION = 150L;
    /** 翻页补完动画的最小时长（ms） */
    private static final long MIN_FLIP_DURATION = 120L;

    // ==================== 对外配置 ====================

    private boolean supportGesture = true;
    @Direction
    private int direction = DIRECTION_HORIZONTAL;

    // ==================== 回调 ====================

    @Nullable
    private ChangeListener changeListener;
    @Nullable
    private ItemClickListener clickListener;

    // ==================== 尺寸参数（构造时一次性计算） ====================

    private final float swipeThresholdPx;
    private final float clickSlopPx;
    private final int touchSlop;
    private final int minFlingVelocity;

    // ==================== 手势状态 ====================

    private float downX;
    private float downY;
    private long downTime;
    private boolean isDragging;
    private boolean isClickCandidate;
    @LockedDirection
    private int lockedDirection = DIRECTION_NONE;
    @Nullable
    private VelocityTracker velocityTracker;

    // ==================== 动画状态 ====================

    private boolean isFlipping;

    // ==================== 构造 ====================

    public BaseViewFlipper(Context context) {
        this(context, null);
    }

    public BaseViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        swipeThresholdPx = SWIPE_THRESHOLD_DP * density;
        clickSlopPx = CLICK_SLOP_DP * density;
        ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledTouchSlop();
        minFlingVelocity = vc.getScaledMinimumFlingVelocity();
    }

    // ==================== 对外 setter / getter ====================

    public void setChangeListener(@Nullable ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void setClickListener(@Nullable ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setDirection(@Direction int direction) {
        this.direction = direction;
        resetAllTranslation();
    }

    @Direction
    public int getDirection() {
        return direction;
    }

    public void setSupportGesture(boolean supportGesture) {
        this.supportGesture = supportGesture;
    }

    public boolean isSupportGesture() {
        return supportGesture;
    }

    // ==================== 程序化翻页（保留补间动画） ====================

    @Override
    public void showNext() {
        if (getChildCount() == 0) {
            return;
        }
        finishFlipImmediately();
        resetAllTranslation();
        applyFlipAnimation(true);
        super.showNext();
        notifyChanged(getDisplayedChild());
    }

    @Override
    public void showPrevious() {
        if (getChildCount() == 0) {
            return;
        }
        finishFlipImmediately();
        resetAllTranslation();
        applyFlipAnimation(false);
        super.showPrevious();
        notifyChanged(getDisplayedChild());
    }

    private void applyFlipAnimation(boolean isNext) {
        if (direction == DIRECTION_VERTICAL) {
            if (isNext) {
                setInAnimation(getContext(), R.anim.slide_bottom_in);
                setOutAnimation(getContext(), R.anim.slide_top_out);
            } else {
                setInAnimation(getContext(), R.anim.slide_top_in);
                setOutAnimation(getContext(), R.anim.slide_bottom_out);
            }
        } else {
            if (isNext) {
                setInAnimation(getContext(), R.anim.slide_right_in);
                setOutAnimation(getContext(), R.anim.slide_left_out);
            } else {
                setInAnimation(getContext(), R.anim.slide_left_in);
                setOutAnimation(getContext(), R.anim.slide_right_out);
            }
        }
    }

    // ==================== 事件拦截 ====================

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!supportGesture) {
            return super.onInterceptTouchEvent(ev);
        }

        // 单 item：不拦截，让子 View 消费点击、让父容器正常滚动
        if (getChildCount() <= 1) {
            return false;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                finishFlipImmediately();
                resetGesture(ev);
                return false;

            case MotionEvent.ACTION_MOVE: {
                if (isDragging) {
                    return true;
                }
                float dx = ev.getX() - downX;
                float dy = ev.getY() - downY;
                if (!tryLockDirection(dx, dy)) {
                    // 方向不对 / 未达阈值 / 父容器还能滚 —— 不拦截
                    return false;
                }
                isDragging = true;
                disallowParentIntercept(true);
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                disallowParentIntercept(false);
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    // ==================== 事件处理 ====================

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!supportGesture) {
            return super.onTouchEvent(ev);
        }

        if (getChildCount() <= 1) {
            return handleSingleItemTouch(ev);
        }

        ensureVelocityTracker();
        //noinspection ConstantConditions
        velocityTracker.addMovement(ev);

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                finishFlipImmediately();
                resetGesture(ev);
                // 手势期间关闭补间动画，避免与属性动画冲突
                setInAnimation(null);
                setOutAnimation(null);
                stopFlipping();
                return true;

            case MotionEvent.ACTION_MOVE: {
                float dx = ev.getX() - downX;
                float dy = ev.getY() - downY;

                if (!isDragging) {
                    if (!tryLockDirection(dx, dy)) {
                        // 已消费 DOWN，但方向不对：继续消费避免父容器"抢走"后状态错乱
                        return true;
                    }
                    isDragging = true;
                    disallowParentIntercept(true);
                }

                applyDrag(dx, dy);
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleTouchEnd(ev);
                return true;
        }
        return true;
    }

    /**
     * 单 item 场景：与普通 View 行为一致，支持点击 / 让父容器滚动。
     */
    private boolean handleSingleItemTouch(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                resetGesture(ev);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(ev.getX() - downX) > clickSlopPx
                        || Math.abs(ev.getY() - downY) > clickSlopPx) {
                    isClickCandidate = false;
                }
                return true;

            case MotionEvent.ACTION_UP: {
                if (isClickCandidate
                        && Math.abs(ev.getX() - downX) < clickSlopPx
                        && Math.abs(ev.getY() - downY) < clickSlopPx
                        && SystemClock.uptimeMillis() - downTime < CLICK_MAX_DURATION) {
                    performClickListener(getDisplayedChild());
                }
                resetGestureState();
                return true;
            }

            case MotionEvent.ACTION_CANCEL:
                resetGestureState();
                return true;
        }
        return true;
    }

    /**
     * 多 item 场景的手势收尾：点击判定 / 翻页判定 / 回弹。
     */
    private void handleTouchEnd(MotionEvent ev) {
        float dx = ev.getX() - downX;
        float dy = ev.getY() - downY;
        long duration = SystemClock.uptimeMillis() - downTime;

        float vx = 0f;
        float vy = 0f;
        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
            velocityTracker.computeCurrentVelocity(1000);
            vx = velocityTracker.getXVelocity();
            vy = velocityTracker.getYVelocity();
            recycleVelocityTracker();
        }

        // ---- 点击判定 ----
        if (isClickCandidate
                && !isDragging
                && Math.abs(dx) < clickSlopPx
                && Math.abs(dy) < clickSlopPx
                && duration < CLICK_MAX_DURATION) {
            resetAllTranslation();
            performClickListener(getDisplayedChild());
            disallowParentIntercept(false);
            resumeAutoPlayIfNeeded();
            resetGestureState();
            return;
        }

        // ---- 翻页判定 ----
        boolean flipped = false;
        if (isDragging) {
            if (direction == DIRECTION_HORIZONTAL) {
                boolean farEnough = Math.abs(dx) > swipeThresholdPx;
                boolean fastEnough = Math.abs(vx) > minFlingVelocity;
                if (farEnough || fastEnough) {
                    flipWithAnimation(dx < 0);
                    flipped = true;
                }
            } else {
                boolean farEnough = Math.abs(dy) > swipeThresholdPx;
                boolean fastEnough = Math.abs(vy) > minFlingVelocity;
                if (farEnough || fastEnough) {
                    flipWithAnimation(dy < 0);
                    flipped = true;
                }
            }
        }

        // ---- 未翻页：回弹 + 恢复播放 ----
        if (!flipped) {
            animateResetTranslation();
            resumeAutoPlayIfNeeded();
        }

        disallowParentIntercept(false);
        resetGestureState();
    }

    // ==================== 手势工具方法 ====================

    private void resetGesture(MotionEvent ev) {
        downX = ev.getX();
        downY = ev.getY();
        downTime = SystemClock.uptimeMillis();
        isDragging = false;
        isClickCandidate = true;
        lockedDirection = DIRECTION_NONE;
    }

    private void resetGestureState() {
        isDragging = false;
        isClickCandidate = false;
        lockedDirection = DIRECTION_NONE;
    }

    private void ensureVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    /**
     * 尝试锁定主方向。
     *
     * @return true 表示手势属于本组件的处理范围，可以开始拖动；
     *         false 表示继续观察 / 让出
     */
    private boolean tryLockDirection(float dx, float dy) {
        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);

        if (absDx < touchSlop && absDy < touchSlop) {
            return false;
        }

        if (lockedDirection == DIRECTION_NONE) {
            if (absDx > absDy * DOMINANT_RATIO) {
                lockedDirection = DIRECTION_HORIZONTAL;
            } else if (absDy > absDx * DOMINANT_RATIO) {
                lockedDirection = DIRECTION_VERTICAL;
            } else {
                return false;
            }
        }

        isClickCandidate = false;

        // 手势方向与配置的翻页方向不一致：不接管
        if (lockedDirection != direction) {
            return false;
        }

        // 纵向翻页时，父容器还能继续滚的话优先让父容器处理
        if (direction == DIRECTION_VERTICAL && canParentScrollFurther(dy)) {
            return false;
        }

        return true;
    }

    private void performClickListener(int position) {
        if (clickListener != null) {
            clickListener.onItemClickListener(position, null);
        }
    }

    // ==================== 跟手 / 回弹 ====================

    private void applyDrag(float dx, float dy) {
        View current = getCurrentView();
        if (current == null) {
            return;
        }
        if (direction == DIRECTION_HORIZONTAL) {
            current.setTranslationX(dx * DRAG_DAMPING);
        } else {
            current.setTranslationY(dy * DRAG_DAMPING);
        }
    }

    private void animateResetTranslation() {
        View current = getCurrentView();
        if (current == null) {
            return;
        }
        current.animate()
                .translationX(0)
                .translationY(0)
                .setDuration(RESET_DURATION)
                .start();
    }

    private void resetAllTranslation() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.animate().cancel();
            child.setTranslationX(0);
            child.setTranslationY(0);
        }
    }

    // ==================== 翻页动画 ====================

    /**
     * 从当前跟手位移处，用属性动画平滑补完翻页。
     *
     * @param isNext true=下一页，false=上一页
     */
    private void flipWithAnimation(final boolean isNext) {
        if (isFlipping || getChildCount() <= 1) {
            return;
        }

        final int childCount = getChildCount();
        final int currentIndex = getDisplayedChild();
        final int targetIndex = isNext
                ? (currentIndex + 1) % childCount
                : (currentIndex - 1 + childCount) % childCount;

        final View currentView = getChildAt(currentIndex);
        final View targetView = getChildAt(targetIndex);
        if (currentView == null || targetView == null) {
            return;
        }

        final float width = getWidth();
        final float height = getHeight();
        if (width <= 0 || height <= 0) {
            // 未完成 layout：直接切换，避免动画计算异常
            setDisplayedChild(targetIndex);
            resetAllTranslation();
            notifyChanged(targetIndex);
            resumeAutoPlayIfNeeded();
            return;
        }

        isFlipping = true;
        setInAnimation(null);
        setOutAnimation(null);

        if (direction == DIRECTION_HORIZONTAL) {
            float startCur = currentView.getTranslationX();
            float endCur = isNext ? -width : width;
            float startTar = isNext ? width : -width;

            targetView.setVisibility(VISIBLE);
            targetView.setTranslationX(startTar);
            targetView.setTranslationY(0);

            long duration = calcDuration(Math.abs(endCur - startCur), width);

            currentView.animate()
                    .translationX(endCur)
                    .setDuration(duration)
                    .start();
            targetView.animate()
                    .translationX(0)
                    .setDuration(duration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            targetView.animate().setListener(null);
                            finishFlip(targetIndex);
                        }
                    })
                    .start();
        } else {
            float startCur = currentView.getTranslationY();
            float endCur = isNext ? -height : height;
            float startTar = isNext ? height : -height;

            targetView.setVisibility(VISIBLE);
            targetView.setTranslationY(startTar);
            targetView.setTranslationX(0);

            long duration = calcDuration(Math.abs(endCur - startCur), height);

            currentView.animate()
                    .translationY(endCur)
                    .setDuration(duration)
                    .start();
            targetView.animate()
                    .translationY(0)
                    .setDuration(duration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            targetView.animate().setListener(null);
                            finishFlip(targetIndex);
                        }
                    })
                    .start();
        }
    }

    /**
     * 根据剩余距离计算动画时长，范围 [MIN_FLIP_DURATION, FLIP_DURATION]。
     */
    private long calcDuration(float remainDistance, float totalSize) {
        if (totalSize <= 0) {
            return FLIP_DURATION;
        }
        float ratio = Math.min(1f, remainDistance / totalSize);
        long d = (long) (FLIP_DURATION * ratio);
        return Math.max(MIN_FLIP_DURATION, Math.min(FLIP_DURATION, d));
    }

    /**
     * 翻页动画结束：切换 displayed child，复位 translation，通知监听者。
     */
    private void finishFlip(int targetIndex) {
        setDisplayedChild(targetIndex);
        resetAllTranslation();
        isFlipping = false;
        notifyChanged(targetIndex);
        resumeAutoPlayIfNeeded();
    }

    /**
     * 立即终止正在进行的翻页动画。
     * <p>不改 {@code displayedChild}（页面保持原样），仅复位视觉状态。
     */
    private void finishFlipImmediately() {
        if (!isFlipping) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).animate().cancel();
        }
        resetAllTranslation();
        isFlipping = false;
    }

    // ==================== 自动播放 ====================

    /**
     * 手势结束后恢复自动播放。
     * <p>仅在 {@link #isAutoStart()} 为 true 且存在多页时恢复，
     * 避免业务方显式 {@code setAutoStart(false)} 后被强制重启。
     */
    private void resumeAutoPlayIfNeeded() {
        if (isAutoStart() && getChildCount() > 1) {
            startFlipping();
        }
    }

    // ==================== 父容器协作 ====================

    private boolean canParentScrollFurther(float dy) {
        ViewParent parent = getParent();
        while (parent != null && !(parent instanceof View)) {
            parent = parent.getParent();
        }
        if (parent == null) {
            return false;
        }
        View pv = (View) parent;
        return pv.canScrollVertically(dy > 0 ? 1 : -1);
    }

    private void disallowParentIntercept(boolean disallow) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
        }
    }

    // ==================== 回调 ====================

    private void notifyChanged(int position) {
        if (changeListener != null) {
            changeListener.onChanged(position);
        }
    }

    // ==================== 生命周期 ====================

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 视图脱离时停止播放，避免 Handler 持续占用资源
        stopFlipping();
        recycleVelocityTracker();
        finishFlipImmediately();
    }

    // ==================== 接口 ====================

    public interface ChangeListener {
        void onChanged(int position);
    }
}