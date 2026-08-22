package com.liucai.component.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
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

    private static final float SWIPE_THRESHOLD = 100;
    private static final float CLICK_MAX_DURATION = 200;
    // 新增：主方向判定系数，偏移量满足1.5倍才判定为有效主方向滑动，避免斜向滑动误判
    private static final float SWIPE_DOMINANT_RATIO = 1.0f;

    public boolean isSupportGesture;
    public int direction;

    public ChangeListener changeListener;

    public ItemClickListener clickListener;

    public long touchDownTime;

    public BaseViewFlipper(Context context) {
        super(context);
    }

    public BaseViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void showNext() {
        int currentIndex = getDisplayedChild();
        if (direction == 0) {
            setInAnimation(getContext(), R.anim.slide_bottom_in);
            setOutAnimation(getContext(), R.anim.slide_top_out);
        } else {
            setInAnimation(getContext(), R.anim.slide_right_in);
            setOutAnimation(getContext(), R.anim.slide_left_out);
        }
        super.showNext();
        int newIndex;
        if (currentIndex < getChildCount() - 1) {
            newIndex = currentIndex + 1;
        } else {
            newIndex = 0;
            setDisplayedChild(newIndex);
        }

        if (changeListener != null) {
            changeListener.onChanged(newIndex);
        }
    }

    @Override
    public void showPrevious() {
        int currentIndex = getDisplayedChild();
        if (direction == 0) {
            setInAnimation(getContext(), R.anim.slide_top_in);
            setOutAnimation(getContext(), R.anim.slide_bottom_out);
        } else {
            setInAnimation(getContext(), R.anim.slide_left_in);
            setOutAnimation(getContext(), R.anim.slide_right_out);
        }
        super.showPrevious();
        int newIndex;
        if (currentIndex > 0) {
            newIndex = currentIndex - 1;
        } else {
            newIndex = getChildCount() - 1;
            setDisplayedChild(newIndex);
        }

        if (changeListener != null) {
            changeListener.onChanged(newIndex);
        }
    }

    private float startX;
    private float startY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isSupportGesture && getChildCount()>1) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setInAnimation(null);
                    setOutAnimation(null);
                    startX = ev.getX();
                    startY = ev.getY();
                    touchDownTime = System.currentTimeMillis();
                    stopFlipping();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float endX = ev.getX();
                    float endY = ev.getY();
                    long touchDuration = System.currentTimeMillis() - touchDownTime;
                    float xDiff = Math.abs(startX - endX);
                    float yDiff = Math.abs(startY - endY);
                    // 核心判定1：同时满足 滑动距离极小 + 按下时长极短 才判定为点击
                    boolean isPureClick = xDiff < SWIPE_THRESHOLD / 3 && yDiff < SWIPE_THRESHOLD / 3 && touchDuration < CLICK_MAX_DURATION;
                    LcaiLogUtils.d("点击还是切换：" + (isPureClick ? "点击" : "切换"));
                    if (isPureClick) {
                        setInAnimation(null);
                        setOutAnimation(null);
                        // 纯点击逻辑，仅回调点击事件，绝对不触发任何切换动画
                        if (clickListener != null) {
                            clickListener.onItemClickListener(getDisplayedChild(),null);
                        }
                        startFlipping();
                        break;
                    }
                    // 核心判定2：判断滑动主方向
                    boolean isVerticalSwipe = yDiff > xDiff * SWIPE_DOMINANT_RATIO;
                    boolean isHorizontalSwipe = xDiff > yDiff * SWIPE_DOMINANT_RATIO;
                    LcaiLogUtils.d("当前模式==",(direction==0) ? "纵向模式" : "横向模式");
                    LcaiLogUtils.d("滑动方向",isVerticalSwipe ? "竖向滑动" : isHorizontalSwipe ? "横向滑动" :"不知道咋滑动");
                    if (direction == 0) {
                        // 纵向模式下 仅响应主方向为纵向的滑动，完全屏蔽横向滑动
                        if (!isVerticalSwipe) {
                            setInAnimation(null);
                            setOutAnimation(null);
                            startFlipping();
                            break;
                        }
                        if (startY - endY > SWIPE_THRESHOLD) {
                            if (getDisplayedChild() < getChildCount() -1) {
                                showNext();
                            } else {
                                setDisplayedChild(0);
                                if (changeListener != null) {
                                    changeListener.onChanged(0);
                                }
                            }
                        } else if (endY - startY > SWIPE_THRESHOLD) {
                            if (getDisplayedChild() > 0) {
                                showPrevious();
                            } else {
                                int position = getChildCount() - 1;
                                setDisplayedChild(position);
                                if (changeListener != null) {
                                    changeListener.onChanged(position);
                                }
                            }
                        }
                    }else if (direction == 1){
                        // 横向模式下 仅响应主方向为横向的滑动，完全屏蔽纵向滑动
                        if (!isHorizontalSwipe) {
                            setInAnimation(null);
                            setOutAnimation(null);
                            startFlipping();
                            break;
                        }
                        if (startX - endX > SWIPE_THRESHOLD) {
                            if (getDisplayedChild() < getChildCount() -1) {
                                showNext();
                            } else {
                                setInAnimation(getContext(), R.anim.slide_right_in);
                                setOutAnimation(getContext(),R.anim.slide_left_out);
                                setDisplayedChild(0);
                                if (changeListener != null) {
                                    changeListener.onChanged(0);
                                }
                            }
                        } else if (endX - startX > SWIPE_THRESHOLD) {
                            if (getDisplayedChild() > 0) {
                                showPrevious();
                            } else {
                                setInAnimation(getContext(), R.anim.slide_left_in);
                                setOutAnimation(getContext(),R.anim.slide_right_out);
                                int position = getChildCount() - 1;
                                setDisplayedChild(position);
                                if (changeListener != null) {
                                    changeListener.onChanged(position);
                                }
                            }
                        }
                    }
                    startFlipping();
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    startFlipping();
                    setInAnimation(null);
                    setOutAnimation(null);
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isSupportGesture && getChildCount() > 1) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public interface ChangeListener{
        void onChanged(int position);
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setSupportGesture(boolean supportGesture) {
        isSupportGesture = supportGesture;
    }
}
