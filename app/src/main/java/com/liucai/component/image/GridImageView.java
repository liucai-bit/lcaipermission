package com.liucai.component.image;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * 带有网格参考线的 ImageView
 */
@SuppressWarnings("unused")
public class GridImageView extends AppCompatImageView {

    private final Paint linePaint;
    private int rows = 0;
    private int cols = 0;

    public GridImageView(@NonNull Context context) {
        this(context, null);
    }

    public GridImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.FIT_CENTER);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xFF2F5597);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setPathEffect(new DashPathEffect(new float[]{15, 15}, 0));
    }

    public void setGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (rows <= 0 && cols <= 0) return;
        if (getDrawable() == null) return;

        RectF drawRect = getImageDrawRect();
        if (drawRect == null || drawRect.isEmpty()) return;

        float left = drawRect.left;
        float top = drawRect.top;
        float right = drawRect.right;
        float bottom = drawRect.bottom;
        float width = right - left;
        float height = bottom - top;

        // 画横线
        for (int i = 1; i < rows; i++) {
            float y = top + (height / rows) * i;
            canvas.drawLine(left, y, right, y, linePaint);
        }

        // 画竖线
        for (int i = 1; i < cols; i++) {
            float x = left + (width / cols) * i;
            canvas.drawLine(x, top, x, bottom, linePaint);
        }
    }

    private RectF getImageDrawRect() {
        if (getDrawable() == null) return null;
        Matrix matrix = getImageMatrix();
        float[] values = new float[9];
        matrix.getValues(values);

        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];

        int drawableWidth = getDrawable().getIntrinsicWidth();
        int drawableHeight = getDrawable().getIntrinsicHeight();

        float right = transX + drawableWidth * scaleX;
        float bottom = transY + drawableHeight * scaleY;

        return new RectF(transX, transY, right, bottom);
    }
}