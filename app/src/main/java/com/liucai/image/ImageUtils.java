package com.liucai.image;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Glide 图片加载工具。
 *
 * <p>特性：
 * <ul>
 *   <li>兼容 Activity / Application / ContextWrapper 等所有 Context</li>
 *   <li>Activity 已 finishing/destroyed 时安全跳过，不抛异常</li>
 *   <li>自动切主线程，调用方无需关心线程</li>
 *   <li>参数校验日志只在 debug 模式输出，无额外运行时开销</li>
 * </ul>
 *
 * <p>使用前请确保 Glide 已随 Application 正常初始化。
 *
 * @author liucai
 */
@SuppressWarnings("unused")
public final class ImageUtils {

    /** 默认圆角半径（dp）。 */
    private static final int DEFAULT_ROUND_RADIUS_DP = 8;

    private ImageUtils() {
    }

    // ==================== 基础加载 ====================

    /**
     * 加载图片（支持 String / Uri / File / Integer resId / byte[] 等任意 model）。
     */
    public static void loadImage(Context context, Object model, ImageView imageView) {
        load(context, model, imageView, null);
    }

    /**
     * 加载图片，指定占位图 / 错误图。
     *
     * @param placeholderRes 占位图资源，0 表示不设置
     * @param errorRes       错误图资源，0 表示不设置
     */
    public static void loadImage(Context context, Object model, ImageView imageView,
                                 int placeholderRes, int errorRes) {
        RequestOptions options = new RequestOptions();
        if (placeholderRes != 0) {
            options = options.placeholder(placeholderRes);
        }
        if (errorRes != 0) {
            options = options.error(errorRes);
        }
        load(context, model, imageView, options);
    }

    /**
     * 加载图片，使用自定义 RequestOptions。
     */
    public static void loadImage(Context context, Object model, ImageView imageView,
                                 @Nullable RequestOptions options) {
        load(context, model, imageView, options);
    }

    // ==================== 圆形 ====================

    public static void loadCircleImage(Context context, Object model, ImageView imageView) {
        loadCircleImage(context, model, imageView, 0);
    }

    public static void loadCircleImage(Context context, Object model, ImageView imageView,
                                       int placeholderRes) {
        RequestOptions options = new RequestOptions().transform(new CircleCrop());
        if (placeholderRes != 0) {
            options = options.placeholder(placeholderRes);
        }
        load(context, model, imageView, options);
    }

    // ==================== 圆角 ====================

    public static void loadRoundImage(Context context, Object model, ImageView imageView) {
        loadRoundImage(context, model, imageView, DEFAULT_ROUND_RADIUS_DP, 0);
    }

    public static void loadRoundImage(Context context, Object model, ImageView imageView,
                                      int radiusDp) {
        loadRoundImage(context, model, imageView, radiusDp, 0);
    }

    public static void loadRoundImage(Context context, Object model, ImageView imageView,
                                      int radiusDp, int placeholderRes) {
        if (context == null) {
            return;
        }
        int radiusPx = dp2px(context, radiusDp);
        RequestOptions options = new RequestOptions()
                .transform(new RoundedCorners(radiusPx));
        if (placeholderRes != 0) {
            options = options.placeholder(placeholderRes);
        }
        load(context, model, imageView, options);
    }

    // ==================== Gif ====================

    public static void loadGif(Context context, Object model, ImageView imageView) {
        if (checkBaseArgs(context, model, imageView)) {
            Context host = resolveContext(context);
            if (host == null) {
                return;
            }
            runOnMain(() -> {
                try {
                    Glide.with(host).asGif().load(model).into(imageView);
                } catch (Throwable t) {
                    logError("loadGif", t);
                }
            });
        }

    }

    // ==================== Bitmap 回调 ====================

    /**
     * 只取 Bitmap（不绑定 ImageView），适合做分享、裁剪、保存等业务。
     */
    public static void loadBitmap(Context context, Object model,
                                  @Nullable LcaiImageCallback callback) {
        if (context == null || model == null) {
            if (callback != null) {
                callback.onLoadFailed(null);
            }
            return;
        }
        if (model instanceof CharSequence
                && TextUtils.isEmpty(model.toString())) {
            if (callback != null) {
                callback.onLoadFailed(null);
            }
            return;
        }
        Context host = resolveContext(context);
        if (host == null) {
            if (callback != null) {
                callback.onLoadFailed(null);
            }
            return;
        }
        runOnMain(() -> {
            try {
                Glide.with(host)
                        .asBitmap()
                        .load(model)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource,
                                                        @Nullable Transition<? super Bitmap> transition) {
                                if (callback != null) {
                                    callback.onResourceReady(resource);
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // 资源被清理不属于加载失败
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                if (callback != null) {
                                    callback.onLoadFailed(errorDrawable);
                                }
                            }
                        });
            } catch (Throwable t) {
                logError("loadBitmap", t);
                if (callback != null) {
                    callback.onLoadFailed(null);
                }
            }
        });
    }

    // ==================== 预加载 ====================

    public static void preload(Context context, Object model) {
        if (context == null || model == null) {
            return;
        }
        Context host = resolveContext(context);
        if (host == null) {
            return;
        }
        runOnMain(() -> {
            try {
                Glide.with(host).load(model).preload();
            } catch (Throwable t) {
                logError("preload", t);
            }
        });
    }

    // ==================== 清理 / 请求控制 ====================

    /**
     * 清除 ImageView 上正在进行的请求（RecyclerView 复用场景常用）。
     */
    public static void clear(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        try {
            Glide.with(imageView.getContext()).clear(imageView);
        } catch (Throwable t) {
            logError("clear", t);
        }
    }

    /**
     * 清空内存缓存（必须在主线程，内部已自动切主线程）
     */
    public static void clearMemoryCache(Context context) {
        if (context == null) {
            return;
        }
        final Context app = applicationContext(context);

        if (GlobalAppUtil.isMainThread()) {
            try {
                Glide.get(app).clearMemory();
            } catch (Throwable t) {
                logError("clearMemory", t);
            }
        } else {
            GlobalAppUtil.runOnUiThread(() -> {
                try {
                    Glide.get(app).clearMemory();
                } catch (Throwable t) {
                    logError("clearMemory", t);
                }
            });
        }
    }

    /**
     * 清空磁盘缓存（必须在子线程，内部已自动开后台线程）
     */
    public static void clearDiskCache(Context context) {
        if (context == null) {
            return;
        }
        final Context app = applicationContext(context);

        new Thread(() -> {
            try {
                Glide.get(app).clearDiskCache();
            } catch (Throwable t) {
                logError("clearDiskCache", t);
            }
        }, "glide-clear-disk").start();
    }

    /**
     * 暂停当前 Context 的所有请求（列表快速滑动时使用）。
     */
    public static void pauseRequests(Context context) {
        if (context == null) {
            return;
        }
        try {
            Glide.with(context).pauseRequests();
        } catch (Throwable t) {
            logError("pauseRequests", t);
        }
    }

    /**
     * 恢复当前 Context 的所有请求。
     */
    public static void resumeRequests(Context context) {
        if (context == null) {
            return;
        }
        try {
            Glide.with(context).resumeRequests();
        } catch (Throwable t) {
            logError("resumeRequests", t);
        }
    }

    /**
     * 保存到 App 私有目录 (Android/data/包名/files/Pictures)
     */
    public static File saveToPrivateDir(Context context, Bitmap bitmap, String fileName) {
        if (context == null || bitmap == null || TextUtils.isEmpty(fileName)) return null;
        try {
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir == null) {
                dir = context.getFilesDir();
            }
            File file = new File(dir, fileName);
            LcaiImageCore.write(bitmap, file);
            return file;
        } catch (Throwable t) {
            logError("saveToPrivateDir", t);
            return null;
        }
    }

    /**
     * 保存到系统相册 (相册中的 LcaiApp 文件夹)
     * API 29+ 无需权限；API < 29 需要 WRITE_EXTERNAL_STORAGE。
     * @param floder 指定文件夹
     *
     */
    public static String saveToGallery(Context context, Bitmap bitmap, String fileName,String floder) {
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
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/"+floder);

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
            File appDir = new File(dir, floder);
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

    /**
     * 先裁剪 → 对整块正方形应用形状遮罩 → 再按 rows×cols 切分。
     * 用 clipPath 替代 PorterDuff.DST_IN，规避部分机型/位图 alpha 通道导致的失效问题。
     */
    public static List<Bitmap> split(Bitmap source, Rect cropRect, int rows, int cols,
                                     LcaiShape shape) {
        List<Bitmap> result = new ArrayList<>();
        if (source == null || source.isRecycled() || cropRect == null
                || cropRect.width() <= 0 || cropRect.height() <= 0
                || rows <= 0 || cols <= 0) {
            return result;
        }

        // 0. hardware bitmap 会拒绝 createBitmap，先转成软件位图
        Bitmap safeSource = source;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && source.getConfig() == Bitmap.Config.HARDWARE) {
            safeSource = source.copy(Bitmap.Config.ARGB_8888, false);
        }

        // 1. 裁剪
        Bitmap cropped = Bitmap.createBitmap(safeSource,
                cropRect.left, cropRect.top, cropRect.width(), cropRect.height());

        // 2. 应用形状遮罩（clipPath 方式）
        if (shape != null && shape != LcaiShape.NONE) {
            Bitmap masked = Bitmap.createBitmap(cropped.getWidth(), cropped.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(masked);

            Path path = new Path();
            LcaiShapePathFactory.build(path, shape, 0f, 0f,
                    (float) cropped.getWidth(), (float) cropped.getHeight());
            c.clipPath(path);          // 只有形状内的绘制会被保留
            c.drawBitmap(cropped, 0f, 0f, null);

            if (masked != cropped) cropped.recycle();
            cropped = masked;
        }

        // 3. 切块
        int cellW = cropped.getWidth() / cols;
        int cellH = cropped.getHeight() / rows;
        for (int r = 0; r < rows; r++) {
            for (int col = 0; col < cols; col++) {
                int x = col * cellW;
                int y = r * cellH;
                int w = (col == cols - 1) ? cropped.getWidth() - x : cellW;
                int h = (r == rows - 1) ? cropped.getHeight() - y : cellH;
                result.add(Bitmap.createBitmap(cropped, x, y, w, h));
            }
        }
        return result;
    }

    // ==================== 内部实现 ====================

    private static void load(Context context, Object model, ImageView imageView, @Nullable RequestOptions options) {
        if (checkBaseArgs(context, model, imageView)) {
            Context host = resolveContext(context);
            if (host == null) {
                return;
            }
            runOnMain(() -> {
                try {
                    RequestBuilder<Drawable> builder = Glide.with(host).load(model);
                    if (options != null) {
                        builder = builder.apply(options);
                    }
                    builder.into(imageView);
                } catch (Throwable t) {
                    logError("load", t);
                }
            });
        }
    }

    private static boolean checkBaseArgs(Context context, Object model, ImageView imageView) {
        if (context == null || model == null || imageView == null) {
            if (GlobalAppUtil.isDebug()) {
                LcaiLogUtils.e("图片加载参数为空",
                        "context == null: " + (context == null),
                        "model == null: " + (model == null),
                        "imageView == null: " + (imageView == null));
            }
            return false;
        }
        if (model instanceof CharSequence
                && TextUtils.isEmpty(model.toString())) {
            if (GlobalAppUtil.isDebug()) {
                LcaiLogUtils.e("图片加载地址为空: " + model);
            }
            return false;
        }
        return true;
    }

    /**
     * 解析安全的宿主 Context：
     * <ul>
     *   <li>Activity：必须存活，否则返回 null 放弃加载</li>
     *   <li>其他 Context：回退到 ApplicationContext，避免强转崩溃</li>
     * </ul>
     */
    @Nullable
    private static Context resolveContext(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!GlobalAppUtil.isRunning(activity)) {
                if (GlobalAppUtil.isDebug()) {
                    LcaiLogUtils.e("Activity 已销毁，放弃加载图片: "
                            + activity.getClass().getSimpleName());
                }
                return null;
            }
            return activity;
        }
        return applicationContext(context);
    }

    private static Context applicationContext(Context context) {
        Context app = context.getApplicationContext();
        return app != null ? app : context;
    }

    private static void runOnMain(Runnable action) {
        if (GlobalAppUtil.isMainThread()) {
            action.run();
        } else {
            GlobalAppUtil.runOnUiThread(action);
        }
    }

    private static int dp2px(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static void logError(String action, Throwable t) {
        if (GlobalAppUtil.isDebug()) {
            LcaiLogUtils.e("Glide " + action + " failed: " + t.getMessage());
        }
    }

    /**
     * Bitmap 加载回调。
     */
    public interface LcaiImageCallback {
        void onResourceReady(@NonNull Bitmap bitmap);

        void onLoadFailed(@Nullable Drawable errorDrawable);
    }
}