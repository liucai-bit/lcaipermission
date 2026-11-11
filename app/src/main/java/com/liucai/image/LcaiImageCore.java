package com.liucai.image;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.http.exception.LcaiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public final class LcaiImageCore {

    private LcaiImageCore() {
    }

    public static Bitmap read(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new LcaiException("路径不能为空");
        }
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) {
            throw new LcaiException("无法解析图片: " + path);
        }
        return bitmap;
    }

    public static Bitmap read(InputStream in) {
        Objects.requireNonNull(in, "输入流不能为空");
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        if (bitmap == null) {
            throw new LcaiException("无法解析图片数据");
        }
        return bitmap;
    }

    public static void write(Bitmap bitmap, File file) throws IOException {
        Objects.requireNonNull(bitmap, "图片不能为空");
        File parent = file.getParentFile();
        Bitmap.CompressFormat format = getCompressFormat(file, parent);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            boolean success = bitmap.compress(format, 100, fos);
            if (!success) {
                throw new LcaiException("图片保存失败");
            }
        }
    }

    private static Bitmap.CompressFormat getCompressFormat(File file, File parent) {
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new LcaiException("无法创建目录: " + parent);
        }
        String name = file.getName().toLowerCase();
        Bitmap.CompressFormat format;
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            format = Bitmap.CompressFormat.JPEG;
        } else if (name.endsWith(".webp")) {
            format = Bitmap.CompressFormat.WEBP;
        } else {
            format = Bitmap.CompressFormat.PNG;
        }
        return format;
    }

    /**
     * 保存到系统相册 (相册中的 LcaiApp 文件夹)
     */
    public static String saveToGallery(Context context, Bitmap bitmap, String fileName) {
        if (context == null || bitmap == null || TextUtils.isEmpty(fileName)) return null;

        String mimeType = "image/png";
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
            format = Bitmap.CompressFormat.JPEG;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LcaiApp");

            Uri uri = null;
            try {
                uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                        if (os != null && bitmap.compress(format, 100, os)) {
                            return uri.toString();
                        }
                    }
                }
            } catch (Exception e) {
                logError("saveToGallery_Q", e);
                if (uri != null) context.getContentResolver().delete(uri, null, null);
            }
            return null;
        } else {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appDir = new File(dir, "LcaiApp");
            if (!appDir.exists() && !appDir.mkdirs()) return null;
            File file = new File(appDir, fileName);
            try {
                LcaiImageCore.write(bitmap, file);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                context.sendBroadcast(intent);
                return file.getAbsolutePath();
            } catch (Exception e) {
                logError("saveToGallery_Legacy", e);
                return null;
            }
        }
    }

    public static Bitmap crop(Bitmap src, int x, int y, int width, int height) {
        checkSrc(src);
        if (width <= 0 || height <= 0) {
            throw new LcaiException("宽高必须大于 0");
        }
        int x1 = Math.max(0, x);
        int y1 = Math.max(0, y);
        int x2 = Math.min(src.getWidth(), x + width);
        int y2 = Math.min(src.getHeight(), y + height);

        int w = x2 - x1;
        int h = y2 - y1;
        if (w <= 0 || h <= 0) {
            throw new LcaiException("裁剪区域完全超出图片范围");
        }
        return Bitmap.createBitmap(src, x1, y1, w, h);
    }

    public static Bitmap cropCenterSquare(Bitmap src) {
        return cropToAspectRatio(src, 1.0);
    }

    public static Bitmap cropToAspectRatio(Bitmap src, double targetRatio) {
        checkSrc(src);
        int w = src.getWidth();
        int h = src.getHeight();
        int cw, ch;
        if ((double) w / h > targetRatio) {
            ch = h;
            cw = (int) Math.round(h * targetRatio);
        } else {
            cw = w;
            ch = (int) Math.round(w / targetRatio);
        }
        cw = Math.min(cw, w);
        ch = Math.min(ch, h);
        return Bitmap.createBitmap(src, (w - cw) / 2, (h - ch) / 2, cw, ch);
    }

    public static List<Bitmap> split4(Bitmap src) {
        return splitSquareGrid(src, 2, 2);
    }

    public static List<Bitmap> split6(Bitmap src) {
        return splitSquareGrid(src, 2, 3);
    }

    public static List<Bitmap> split9(Bitmap src) {
        return splitSquareGrid(src, 3, 3);
    }

    public static List<Bitmap> splitSquareGrid(Bitmap src, int rows, int cols) {
        checkSrc(src);
        Bitmap base = cropToAspectRatio(src, (double) cols / rows);
        return splitGrid(base, rows, cols);
    }

    public static List<Bitmap> splitGrid(Bitmap src, int rows, int cols) {
        checkSrc(src);
        if (rows <= 0 || cols <= 0) {
            throw new LcaiException("行列数必须大于 0");
        }
        int w = src.getWidth();
        int h = src.getHeight();
        if (cols > w || rows > h) {
            throw new LcaiException("图片尺寸过小，无法切分");
        }
        List<Bitmap> result = new ArrayList<>(rows * cols);
        for (int r = 0; r < rows; r++) {
            int y1 = (int) Math.round((double) r * h / rows);
            int y2 = (int) Math.round((double) (r + 1) * h / rows);
            for (int c = 0; c < cols; c++) {
                int x1 = (int) Math.round((double) c * w / cols);
                int x2 = (int) Math.round((double) (c + 1) * w / cols);
                result.add(Bitmap.createBitmap(src, x1, y1, x2 - x1, y2 - y1));
            }
        }
        return result;
    }

    public static Bitmap flip(Bitmap src, FlipMode mode) {
        checkSrc(src);
        Matrix matrix = new Matrix();
        switch (mode) {
            case HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case VERTICAL:
                matrix.postScale(1, -1);
                break;
            case BOTH:
                matrix.postScale(-1, -1);
                break;
        }
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static Bitmap flipHorizontal(Bitmap src) {
        return flip(src, FlipMode.HORIZONTAL);
    }

    public static Bitmap flipVertical(Bitmap src) {
        return flip(src, FlipMode.VERTICAL);
    }

    public static Bitmap rotate(Bitmap src, float degrees) {
        checkSrc(src);
        if (degrees % 360 == 0) return src;
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static Bitmap rotateRight(Bitmap src) {
        return rotate(src, 90);
    }

    public static Bitmap rotateLeft(Bitmap src) {
        return rotate(src, -90);
    }

    private static void checkSrc(Bitmap src) {
        Objects.requireNonNull(src, "源图片不能为空");
        if (src.isRecycled()) {
            throw new LcaiException("源图片已被回收(Recycled)");
        }
    }

    private static void logError(String action, Throwable t) {
        if (GlobalAppUtil.isDebug()) {
            LcaiLogUtils.e("ImageUtils " + action + " failed: " + t.getMessage());
        }
    }
}