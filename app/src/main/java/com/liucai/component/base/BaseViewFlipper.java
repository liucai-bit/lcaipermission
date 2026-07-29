package com.liucai.component.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.permission.R;


/**
 * @author liucai
 * @program lctipsdialog
 * @description
 * @Date 2026/7/24
 */
public class BaseViewFlipper extends ViewFlipper {

    private static final int SWIPE_THRESHOLD = 50;

    public boolean isSupportGesture;
    public int direction;

    public ChangeListener changeListener;

    public ItemClickListener clickListener;

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

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isSupportGesture) {
            if (getChildCount() <= 1) {
                return true;
            }
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = ev.getX();
                    startY = ev.getY();
                    stopFlipping();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float endX = ev.getX();
                    float endY = ev.getY();
                    if (direction == 0) {
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
                        } else {
                            if (clickListener != null) {
                                clickListener.onItemClickListener(getDisplayedChild(),null);
                            }
                        }
                    }else{
                        if (startX - endX > SWIPE_THRESHOLD) {
                            if (getDisplayedChild() < getChildCount() -1) {
                                showNext();
                            } else {
                                setDisplayedChild(0);
                                if (changeListener != null) {
                                    changeListener.onChanged(0);
                                }
                            }
                        } else if (endX - startX > SWIPE_THRESHOLD) {
                            if (getDisplayedChild() > 0) {
                                showPrevious();
                            } else {
                                int position = getChildCount() - 1;
                                setDisplayedChild(position);
                                if (changeListener != null) {
                                    changeListener.onChanged(position);
                                }
                            }
                        }else {
                            if (clickListener != null) {
                                clickListener.onItemClickListener(getDisplayedChild(),null);
                            }
                        }
                    }
                    startFlipping();
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    startFlipping();
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isSupportGesture;
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
