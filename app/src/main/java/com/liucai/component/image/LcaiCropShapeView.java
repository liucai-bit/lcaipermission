package com.liucai.imageslice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.liucai.image.LcaiShape;
import com.liucai.image.LcaiShapePathFactory;

public class LcaiCropShapeView extends View {

    public interface OnCropChangedListener {
        void onCropChanged();
    }

    private Bitmap bitmap;
    private float cropX, cropY, cropSize;

    private int gridRows = 0;
    private int gridCols = 0;
    private LcaiShape shape = LcaiShape.NONE;

    private final Paint imagePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint darkPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shapeStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shapeMask   = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path maskPath  = new Path();
    private final Path shapePath = new Path();
    private final RectF imageRect = new RectF();

    private boolean dragging;
    private float downX, downY, startCropX, startCropY;

    private OnCropChangedListener listener;

    public LcaiCropShapeView(Context c) { this(c, null); }
    public LcaiCropShapeView(Context c, AttributeSet a) {
        super(c, a);
        init();
    }

    private void init() {
        darkPaint.setColor(Color.argb(115, 0, 0, 0));

        gridPaint.setColor(Color.argb(242, 33, 150, 243));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1.5f));
        gridPaint.setPathEffect(new DashPathEffect(new float[]{dp(8), dp(6)}, 0));

        borderPaint.setColor(Color.rgb(33, 150, 243));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(2f));

        shapeStroke.setColor(Color.argb(242, 255, 152, 0));
        shapeStroke.setStyle(Paint.Style.STROKE);
        shapeStroke.setStrokeWidth(dp(2.5f));

        shapeMask.setColor(Color.argb(140, 0, 0, 0));
        shapeMask.setStyle(Paint.Style.FILL);

        // DashPathEffect / Path 蒙版在软件层更稳
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    public void setImage(Bitmap bmp) {
        this.bitmap = bmp;
        resetCrop();
        requestLayout();
        invalidate();
    }

    public void resetCrop() {
        if (bitmap == null) return;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        cropSize = Math.min(w, h);
        cropX = (w - cropSize) / 2f;
        cropY = (h - cropSize) / 2f;
    }

    public void setCrop(int x, int y, int size) {
        cropX = x; cropY = y; cropSize = size;
        invalidate();
    }

    public void setGrid(int rows, int cols) {
        gridRows = rows; gridCols = cols;
        invalidate();
    }

    public void setShape(LcaiShape s) {
        shape = s;
        invalidate();
    }

    public void setOnCropChangedListener(OnCropChangedListener l) { listener = l; }

    public int getCropX()    { return Math.round(cropX); }
    public int getCropY()    { return Math.round(cropY); }
    public int getCropSize() { return Math.round(cropSize); }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int width = (MeasureSpec.getMode(wSpec) == MeasureSpec.UNSPECIFIED)
                ? getResources().getDisplayMetrics().widthPixels
                : MeasureSpec.getSize(wSpec);

        if (bitmap == null) {
            setMeasuredDimension(width, width);
            return;
        }
        int bmpW = bitmap.getWidth();
        int bmpH = bitmap.getHeight();
        float ratio = (float) bmpH / bmpW;
        int targetW = width;
        int targetH = Math.round(targetW * ratio);

        int maxH = Math.round(getResources().getDisplayMetrics().heightPixels * 0.48f);
        if (MeasureSpec.getMode(hSpec) == MeasureSpec.EXACTLY) {
            maxH = Math.min(maxH, MeasureSpec.getSize(hSpec));
        }
        if (targetH > maxH) {
            targetH = maxH;
            targetW = Math.round(targetH / ratio);
        }
        setMeasuredDimension(targetW, targetH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap == null) return;

        float scale = getWidth() / (float) bitmap.getWidth();
        float dcx = cropX * scale;
        float dcy = cropY * scale;
        float dcs = cropSize * scale;

        // 1. 画图
        imageRect.set(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(bitmap, null, imageRect, imagePaint);

        // 2. 裁剪框外暗化
        if (dcy > 0) canvas.drawRect(0, 0, getWidth(), dcy, darkPaint);
        if (dcy + dcs < getHeight())
            canvas.drawRect(0, dcy + dcs, getWidth(), getHeight(), darkPaint);
        if (dcx > 0) canvas.drawRect(0, dcy, dcx, dcy + dcs, darkPaint);
        if (dcx + dcs < getWidth())
            canvas.drawRect(dcx + dcs, dcy, getWidth(), dcy + dcs, darkPaint);

        // 3. 形状外暗化（用 EVEN_ODD 得到"裁剪框内但形状外"的区域）
        if (shape != null && shape != LcaiShape.NONE) {
            maskPath.reset();
            maskPath.setFillType(Path.FillType.EVEN_ODD);
            maskPath.addRect(dcx, dcy, dcx + dcs, dcy + dcs, Path.Direction.CW);
            LcaiShapePathFactory.build(shapePath, shape, dcx, dcy, dcs, dcs);
            maskPath.addPath(shapePath);
            canvas.drawPath(maskPath, shapeMask);
        }

        // 4. 网格
        if (gridRows > 0 && gridCols > 0) {
            canvas.save();
            canvas.clipRect(dcx, dcy, dcx + dcs, dcy + dcs);
            for (int i = 1; i < gridRows; i++) {
                float y = dcy + dcs * i / gridRows;
                canvas.drawLine(dcx, y, dcx + dcs, y, gridPaint);
            }
            for (int j = 1; j < gridCols; j++) {
                float x = dcx + dcs * j / gridCols;
                canvas.drawLine(x, dcy, x, dcy + dcs, gridPaint);
            }
            canvas.restore();
        }

        // 5. 裁剪框描边
        canvas.drawRect(dcx + 1, dcy + 1, dcx + dcs - 1, dcy + dcs - 1, borderPaint);

        // 6. 形状轮廓
        if (shape != null && shape != LcaiShape.NONE) {
            canvas.save();
            canvas.clipRect(dcx, dcy, dcx + dcs, dcy + dcs);
            LcaiShapePathFactory.build(shapePath, shape, dcx, dcy, dcs, dcs);
            canvas.drawPath(shapePath, shapeStroke);
            canvas.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (bitmap == null) return false;
        float x = e.getX(), y = e.getY();
        float scale = getWidth() / (float) bitmap.getWidth();
        float dcx = cropX * scale;
        float dcy = cropY * scale;
        float dcs = cropSize * scale;

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (x >= dcx && x <= dcx + dcs && y >= dcy && y <= dcy + dcs) {
                    dragging = true;
                    downX = x; downY = y;
                    startCropX = cropX; startCropY = cropY;
                    if (getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    float dx = (x - downX) / scale;
                    float dy = (y - downY) / scale;
                    float nx = startCropX + dx;
                    float ny = startCropY + dy;
                    nx = Math.max(0, Math.min(bitmap.getWidth()  - cropSize, nx));
                    ny = Math.max(0, Math.min(bitmap.getHeight() - cropSize, ny));
                    cropX = nx;
                    cropY = ny;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragging) {
                    dragging = false;
                    if (getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(false);
                    if (listener != null) listener.onCropChanged();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(e);
    }
}