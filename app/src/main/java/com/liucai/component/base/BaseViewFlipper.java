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

import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.permission.R;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/24
 */
public class BaseViewFlipper extends ViewFlipper {

    public static final int DIRECTION_VERTICAL = 0;
    public static final int DIRECTION_HORIZONTAL = 1;

    /**
     * 触发翻页的最小滑动距离（dp）
     */
    private static final float SWIPE_THRESHOLD_DP = 50f;
    /**
     * 判定点击的最大位移（dp）
     */
    private static final float CLICK_SLOP_DP = 8f;
    /**
     * 判定点击的最大时长（ms）
     */
    private static final long CLICK_MAX_DURATION = 300L;
    /**
     * 主方向系数：主方向位移需大于副方向的该倍数
     */
    private static final float DOMINANT_RATIO = 1.2f;
    /**
     * 跟手阻尼系数（0~1，越小越"重"）
     */
    private static final float DRAG_DAMPING = 0.6f;
    /**
     * 翻页补完动画时长（ms）
     */
    private static final long FLIP_DURATION = 220L;
    /**
     * 未触发翻页时的回弹动画时长（ms）
     */
    private static final long RESET_DURATION = 150L;

    public boolean isSupportGesture = true;
    public int direction = DIRECTION_HORIZONTAL;

    public ChangeListener changeListener;
    public ItemClickListener clickListener;

    private final float density;
    private final float swipeThresholdPx;
    private final float clickSlopPx;
    private final int touchSlop;
    private final int minFlingVelocity;

    // ---- 手势状态 ----
    private float downX, downY;
    private long downTime;
    private boolean isDragging;
    private boolean isClickCandidate;
    private int lockedDirection = -1; // -1 未定, 0 纵向, 1 横向
    private VelocityTracker velocityTracker;

    // ---- 翻页动画状态 ----
    private boolean isFlipping = false;

    public BaseViewFlipper(Context context) {
        this(context, null);
    }

    public BaseViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        swipeThresholdPx = SWIPE_THRESHOLD_DP * density;
        clickSlopPx = CLICK_SLOP_DP * density;
        ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledTouchSlop();
        minFlingVelocity = vc.getScaledMinimumFlingVelocity();
    }

    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setDirection(int direction) {
        this.direction = direction;
        resetAllTranslation();
    }

    public void setSupportGesture(boolean supportGesture) {
        this.isSupportGesture = supportGesture;
    }

    // ==================== 翻页（外部调用，保留补间动画） ====================

    @Override
    public void showNext() {
        int currentIndex = getDisplayedChild();
        int nextIndex = (currentIndex + 1) % getChildCount();
        resetAllTranslation();
        applyFlipAnimation(true);
        super.showNext();
        if (nextIndex != currentIndex + 1) {
            setDisplayedChild(nextIndex);
        }
        notifyChanged(nextIndex);
    }

    @Override
    public void showPrevious() {
        int currentIndex = getDisplayedChild();
        int prevIndex = currentIndex - 1;
        if (prevIndex < 0) prevIndex = getChildCount() - 1;
        resetAllTranslation();
        applyFlipAnimation(false);
        super.showPrevious();
        if (prevIndex != currentIndex - 1) {
            setDisplayedChild(prevIndex);
        }
        notifyChanged(prevIndex);
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

    private void notifyChanged(int position) {
        if (changeListener != null) {
            changeListener.onChanged(position);
        }
    }

    // ==================== 拦截判定 ====================

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isSupportGesture) {
            return super.onInterceptTouchEvent(ev);
        }

        // ===== 单 item：DOWN 不拦截（保证 onTouchEvent 收到 DOWN 记录起点），MOVE/UP 拦截 =====
        if (getChildCount() <= 1) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    finishFlipImmediately();
                    resetGesture(ev);
                    return false;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    return true;
            }
            return false;
        }

        // ===== 多 item =====
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                finishFlipImmediately();
                resetGesture(ev);
                return false; // 先不拦截，给子 View 机会

            case MotionEvent.ACTION_MOVE: {
                if (!isDragging) {
                    float dx = ev.getX() - downX;
                    float dy = ev.getY() - downY;
                    float absDx = Math.abs(dx);
                    float absDy = Math.abs(dy);

                    if (absDx < touchSlop && absDy < touchSlop) {
                        return false;
                    }

                    if (lockedDirection == -1) {
                        if (absDx > absDy * DOMINANT_RATIO) {
                            lockedDirection = DIRECTION_HORIZONTAL;
                        } else if (absDy > absDx * DOMINANT_RATIO) {
                            lockedDirection = DIRECTION_VERTICAL;
                        } else {
                            return false;
                        }
                    }

                    isClickCandidate = false;

                    if (lockedDirection != direction) {
                        requestDisallowParentIntercept(false);
                        return false;
                    }

                    if (direction == DIRECTION_VERTICAL && canParentScrollFurther(dy)) {
                        requestDisallowParentIntercept(false);
                        return false;
                    }

                    isDragging = true;
                    requestDisallowParentIntercept(true);
                    return true;
                }
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                requestDisallowParentIntercept(false);
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isSupportGesture) {
            return super.onTouchEvent(ev);
        }

        if (getChildCount() <= 1) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    resetGesture(ev);
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    float dx = Math.abs(ev.getX() - downX);
                    float dy = Math.abs(ev.getY() - downY);
                    if (dx > clickSlopPx || dy > clickSlopPx) {
                        isClickCandidate = false;
                    }
                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    float dx = Math.abs(ev.getX() - downX);
                    float dy = Math.abs(ev.getY() - downY);
                    long duration = SystemClock.uptimeMillis() - downTime;
                    if (isClickCandidate
                            && dx < clickSlopPx
                            && dy < clickSlopPx
                            && duration < CLICK_MAX_DURATION) {
                        if (clickListener != null) {
                            clickListener.onItemClickListener(getDisplayedChild(), null);
                        }
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

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                finishFlipImmediately();
                resetGesture(ev);
                setInAnimation(null);
                setOutAnimation(null);
                stopFlipping();
                return true;

            case MotionEvent.ACTION_MOVE: {
                float dx = ev.getX() - downX;
                float dy = ev.getY() - downY;
                float absDx = Math.abs(dx);
                float absDy = Math.abs(dy);

                if (!isDragging) {
                    if (absDx < touchSlop && absDy < touchSlop) return true;

                    if (lockedDirection == -1) {
                        if (absDx > absDy * DOMINANT_RATIO) {
                            lockedDirection = DIRECTION_HORIZONTAL;
                        } else if (absDy > absDx * DOMINANT_RATIO) {
                            lockedDirection = DIRECTION_VERTICAL;
                        } else {
                            return true;
                        }
                    }
                    isClickCandidate = false;

                    if (lockedDirection != direction) {
                        requestDisallowParentIntercept(false);
                        return false;
                    }

                    if (direction == DIRECTION_VERTICAL && canParentScrollFurther(dy)) {
                        requestDisallowParentIntercept(false);
                        return false;
                    }

                    isDragging = true;
                    requestDisallowParentIntercept(true);
                }

                applyDrag(dx, dy);
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                float dx = ev.getX() - downX;
                float dy = ev.getY() - downY;
                long duration = SystemClock.uptimeMillis() - downTime;

                float vx = 0, vy = 0;
                if (velocityTracker != null) {
                    velocityTracker.addMovement(ev);
                    velocityTracker.computeCurrentVelocity(1000);
                    vx = velocityTracker.getXVelocity();
                    vy = velocityTracker.getYVelocity();
                    velocityTracker.recycle();
                    velocityTracker = null;
                }

                // ---- 点击判定 ----
                if (isClickCandidate
                        && Math.abs(dx) < clickSlopPx
                        && Math.abs(dy) < clickSlopPx
                        && duration < CLICK_MAX_DURATION) {
                    resetAllTranslation();
                    if (clickListener != null) {
                        clickListener.onItemClickListener(getDisplayedChild(), null);
                    }
                    startFlipping();
                    requestDisallowParentIntercept(false);
                    return true;
                }

                // ---- 翻页判定 ----
                boolean handled = false;
                if (isDragging && lockedDirection == direction) {
                    if (direction == DIRECTION_HORIZONTAL) {
                        boolean farEnough = Math.abs(dx) > swipeThresholdPx;
                        boolean fastEnough = Math.abs(vx) > minFlingVelocity;
                        if (farEnough || fastEnough) {
                            flipWithAnimation(dx < 0);
                            handled = true;
                        }
                    } else {
                        boolean farEnough = Math.abs(dy) > swipeThresholdPx;
                        boolean fastEnough = Math.abs(vy) > minFlingVelocity;
                        if (farEnough || fastEnough) {
                            flipWithAnimation(dy < 0);
                            handled = true;
                        }
                    }
                }

                if (!handled) {
                    animateResetTranslation();
                }

                resetGestureState();
                requestDisallowParentIntercept(false);
                startFlipping();
                return true;
            }

            case MotionEvent.ACTION_OUTSIDE:
                resetAllTranslation();
                resetGestureState();
                requestDisallowParentIntercept(false);
                startFlipping();
                return true;
        }
        return true;
    }

    private void resetGesture(MotionEvent ev) {
        downX = ev.getX();
        downY = ev.getY();
        downTime = SystemClock.uptimeMillis();
        isDragging = false;
        isClickCandidate = true;
        lockedDirection = -1;
    }

    private void resetGestureState() {
        isDragging = false;
        isClickCandidate = false;
        lockedDirection = -1;
    }

    /**
     * 跟手位移（带阻尼）
     */
    private void applyDrag(float dx, float dy) {
        View current = getCurrentView();
        if (current == null) return;
        if (direction == DIRECTION_HORIZONTAL) {
            current.setTranslationX(dx * DRAG_DAMPING);
        } else {
            current.setTranslationY(dy * DRAG_DAMPING);
        }
    }

    /**
     * 未触发翻页，平滑回弹到 0
     */
    private void animateResetTranslation() {
        View current = getCurrentView();
        if (current == null) return;
        current.animate()
                .translationX(0)
                .translationY(0)
                .setDuration(RESET_DURATION)
                .start();
    }

    /**
     * 复位所有子 View 的 translation
     */
    private void resetAllTranslation() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.animate().cancel();
            child.setTranslationX(0);
            child.setTranslationY(0);
        }
    }

    /**
     * 父容器（ScrollView）在 dy 方向上是否还能滚
     */
    private boolean canParentScrollFurther(float dy) {
        ViewParent parent = getParent();
        while (parent != null && !(parent instanceof View)) {
            parent = parent.getParent();
        }
        if (parent == null) return false;
        View pv = (View) parent;
        return pv.canScrollVertically(dy > 0 ? 1 : -1);
    }

    private void requestDisallowParentIntercept(boolean disallow) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
        }
    }


    /**
     * 从当前跟手位移处，用属性动画平滑补完翻页。
     * @param isNext true=下一页，false=上一页
     */
    private void flipWithAnimation(final boolean isNext) {
        if (isFlipping || getChildCount() <= 1) return;

        final int currentIndex = getDisplayedChild();
        final int targetIndex;
        if (isNext) {
            targetIndex = (currentIndex + 1) % getChildCount();
        } else {
            targetIndex = (currentIndex - 1 + getChildCount()) % getChildCount();
        }

        final View currentView = getChildAt(currentIndex);
        final View targetView = getChildAt(targetIndex);
        if (currentView == null || targetView == null) return;

        isFlipping = true;
        setInAnimation(null);
        setOutAnimation(null);

        final float width = getWidth();
        final float height = getHeight();

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
     * 根据剩余距离计算动画时长，范围 [120, FLIP_DURATION]
     */
    private long calcDuration(float remainDistance, float totalSize) {
        if (totalSize <= 0) return FLIP_DURATION;
        float ratio = Math.min(1f, remainDistance / totalSize);
        long d = (long) (FLIP_DURATION * ratio);
        return Math.max(120L, Math.min(FLIP_DURATION, d));
    }

    /**
     * 翻页动画结束：切换 displayed child，复位所有 translation。
     */
    private void finishFlip(int targetIndex) {
        setDisplayedChild(targetIndex);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.setTranslationX(0);
            child.setTranslationY(0);
        }
        isFlipping = false;
        notifyChanged(targetIndex);
    }

    /**
     * 若动画进行中被再次触摸，立即结束归位，避免状态错乱。
     */
    private void finishFlipImmediately() {
        if (!isFlipping) return;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).animate().cancel();
        }
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.setTranslationX(0);
            child.setTranslationY(0);
        }
        isFlipping = false;
    }

    public interface ChangeListener {
        void onChanged(int position);
    }
}