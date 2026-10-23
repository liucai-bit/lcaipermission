package com.liucai.core.util.file;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.core.os.EnvironmentCompat;

import com.alibaba.fastjson.JSONObject;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.http.thread.GlobalThreadPool;
import com.liucai.http.thread.LcaiRunnableUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author liucai
 * @description 文件工具
 */
public class LcaiFileUtils {

    private static final String HEADER = "data:image/%s;base64,";
    private static final int BUFFER_SIZE = 8 * 1024;

    /**
     * 创建保存图片的文件。
     */
    public static File createImageFile(Activity activity) {
        if (activity == null) {
            return null;
        }

        String imageName = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = activity.getCacheDir();
        }
        if (storageDir == null) {
            return null;
        }

        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return null;
        }

        File tempFile = new File(storageDir, "JPEG" + imageName + ".jpg");

        if (EnvironmentCompat.getStorageState(tempFile)
                != Environment.MEDIA_MOUNTED) {
            return null;
        }

        return tempFile;
    }

    public static File createCameraImageFile(Activity activity) {
        if (activity == null) {
            return null;
        }

        File imageFile = null;
        try {
            String imageName = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

            File storageDir = activity.getExternalCacheDir();
            if (storageDir == null) {
                storageDir = activity.getCacheDir();
            }
            if (storageDir == null) {
                return null;
            }

            if (!storageDir.exists() && !storageDir.mkdirs()) {
                return null;
            }

            imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        } catch (IOException e) {
            LcaiLogUtils.e("创建文件失败: " + e.getMessage());
        }
        return imageFile;
    }

    public static void clearFile(File file) {
        if (file != null && file.exists()) {
            String path = file.getAbsolutePath();
            boolean delete = file.delete();
            LcaiLogUtils.d("删除图片:" + path, delete ? "SUCCESS" : "FAILURE");
        }
    }

    public static JSONObject converImagePathToBase64(String path, boolean addHeader) {
        JSONObject jsonObject = new JSONObject();

        if (path == null) {
            return jsonObject;
        }

        File file = new File(path);
        if (!file.exists()) {
            return jsonObject;
        }

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path);
            if (bitmap == null) {
                return jsonObject;
            }

            float sizeInMb = bitmapByteCount(bitmap) / (1024f * 1024f);
            boolean needCompress = sizeInMb > 1.0f;

            jsonObject.put("fileSize", sizeInMb);

            String base64 = bitmapToBase64(bitmap, needCompress);
            if (addHeader) {
                base64 = String.format(HEADER, "jpeg") + base64;
            }
            jsonObject.put("content", base64);
        } catch (Throwable t) {
            LcaiLogUtils.e("图片转 Base64 失败: " + t.getMessage());
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        return jsonObject;
    }

    public static JSONObject convertImageToBase64(Context context,
                                                  Uri uri,
                                                  boolean addHeader) {
        JSONObject jsonObject = new JSONObject();

        if (context == null || uri == null) {
            return jsonObject;
        }

        Cursor cursor = null;
        Bitmap bitmap = null;

        try {
            String[] projection = {MediaStore.Images.Media.SIZE};
            cursor = context.getContentResolver()
                    .query(uri, projection, null, null, null);

            float sizeInMb = 0f;
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                if (sizeIndex >= 0) {
                    long sizeInBytes = cursor.getLong(sizeIndex);
                    sizeInMb = sizeInBytes / (1024f * 1024f);
                }
            }

            bitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), uri);
            if (bitmap == null) {
                return jsonObject;
            }

            if (sizeInMb <= 0) {
                sizeInMb = bitmapByteCount(bitmap) / (1024f * 1024f);
            }

            jsonObject.put("fileSize", sizeInMb);
            jsonObject.put("fileType", "image");

            boolean needCompress = sizeInMb > 1.0f;
            String base64 = bitmapToBase64(bitmap, needCompress);

            if (addHeader) {
                base64 = String.format(HEADER, "jpeg") + base64;
            }
            jsonObject.put("content", base64);
        } catch (Throwable t) {
            LcaiLogUtils.e("图片转换失败: " + t.getMessage());
        } finally {
            closeQuietly(cursor);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        return jsonObject;
    }

    public static JSONObject cameraImageToBase64(Intent intent, boolean addHeader) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileType", "image");

        if (intent == null || intent.getExtras() == null) {
            return jsonObject;
        }

        Object data = intent.getExtras().get("data");
        if (!(data instanceof Bitmap)) {
            return jsonObject;
        }

        Bitmap bitmap = (Bitmap) data;
        try {
            double fileSizeMB = bitmapByteCount(bitmap) / (1024.0 * 1024.0);
            boolean needCompress = fileSizeMB > 1.0;

            String content = bitmapToBase64(bitmap, needCompress);
            jsonObject.put("fileSize", fileSizeMB);

            if (addHeader) {
                content = String.format(HEADER, "jpeg") + content;
            }
            jsonObject.put("content", content);
        } catch (Throwable t) {
            LcaiLogUtils.e("相机图片转 Base64 失败: " + t.getMessage());
        }

        return jsonObject;
    }

    public static String bitmapToBase64(Bitmap bitmap, boolean compress) {
        if (bitmap == null || bitmap.isRecycled()) {
            return "";
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int quality = compress ? 50 : 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Throwable t) {
            LcaiLogUtils.e("Bitmap 转 Base64 失败: " + t.getMessage());
            return "";
        }
    }

    public static String getPath(final Context context, final Uri uri) {
        if (context == null || uri == null) {
            return null;
        }

        try {
            final boolean isKitKat =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type) && split.length > 1) {
                        return Environment.getExternalStorageDirectory()
                                + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                Long.parseLong(id));
                        return getDataColumn(context, contentUri, null, null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    if (contentUri == null || split.length <= 1) {
                        return null;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }
                return getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Throwable t) {
            LcaiLogUtils.e("获取文件路径失败: " + t.getMessage());
        }

        return null;
    }

    public static String getDataColumn(Context context,
                                       Uri uri,
                                       String selection,
                                       String[] selectionArgs) {
        if (context == null || uri == null) {
            return null;
        }

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(
                    uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(column);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Throwable t) {
            LcaiLogUtils.e("读取 _data 失败: " + t.getMessage());
        } finally {
            closeQuietly(cursor);
        }

        return null;
    }

    public static void saveImageToGallery(Context context,
                                          final File sourceFile,
                                          LcaiFileInterface fileInterface) {
        if (context == null || sourceFile == null || !sourceFile.exists()) {
            clearFile(sourceFile);
            return;
        }

        final Context appContext = context.getApplicationContext() != null
                ? context.getApplicationContext()
                : context;

        GlobalThreadPool.execute(new LcaiRunnableUtils() {
            @Override
            public void run() {
                Uri savedUri = null;
                OutputStream out = null;
                InputStream in = null;
                File destFile = null;

                try {
                    String timeStamp = new SimpleDateFormat(
                            "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String imageFileName = "IMG_" + timeStamp + ".jpg";

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                    Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                                Environment.DIRECTORY_DCIM + "/Camera");
                        values.put(MediaStore.Images.Media.IS_PENDING, 1);

                        savedUri = appContext.getContentResolver()
                                .insert(collection, values);
                        if (savedUri == null) {
                            throw new IOException("insert MediaStore failed");
                        }

                        out = appContext.getContentResolver().openOutputStream(savedUri);
                        if (out == null) {
                            throw new IOException("openOutputStream failed");
                        }

                        in = new FileInputStream(sourceFile);
                        copy(in, out);
                        out.flush();

                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        appContext.getContentResolver()
                                .update(savedUri, values, null, null);
                    } else {
                        File dcim = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DCIM);
                        File cameraDir = new File(dcim, "Camera");
                        if (!cameraDir.exists() && !cameraDir.mkdirs()) {
                            throw new IOException("create camera dir failed");
                        }

                        destFile = new File(cameraDir, imageFileName);

                        in = new FileInputStream(sourceFile);
                        out = new FileOutputStream(destFile);
                        copy(in, out);
                        out.flush();

                        values.put(MediaStore.Images.Media.DATA,
                                destFile.getAbsolutePath());

                        savedUri = appContext.getContentResolver()
                                .insert(collection, values);

                        Uri scanUri = savedUri != null
                                ? savedUri
                                : Uri.fromFile(destFile);

                        appContext.sendBroadcast(new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, scanUri));
                    }

                    LcaiLogUtils.d("图片插入图库成功！");

                    String path;
                    if (savedUri != null) {
                        path = getPath(appContext, savedUri);
                    } else {
                        path = destFile != null ? destFile.getAbsolutePath() : null;
                    }

                    if (fileInterface != null) {
                        fileInterface.onUriPath(path);
                    }
                } catch (Throwable t) {
                    if (savedUri != null) {
                        try {
                            appContext.getContentResolver()
                                    .delete(savedUri, null, null);
                        } catch (Throwable ignored) {
                        }
                    }
                    if (destFile != null) {
                        //noinspection ResultOfMethodCallIgnored
                        destFile.delete();
                    }
                    LcaiLogUtils.d("图片插入图库失败: " + t.getMessage());
                } finally {
                    closeQuietly(out);
                    closeQuietly(in);
                    clearFile(sourceFile);
                }
            }
        });
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return uri != null
                && "com.android.externalstorage.documents"
                .equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return uri != null
                && "com.android.providers.downloads.documents"
                .equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return uri != null
                && "com.android.providers.media.documents"
                .equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return uri != null
                && "com.google.android.apps.photos.content"
                .equals(uri.getAuthority());
    }

    private static long bitmapByteCount(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return 0L;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        }
        return bitmap.getByteCount();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
    }

    private static void closeQuietly(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private static void closeQuietly(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private static void closeQuietly(OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (Throwable ignored) {
            }
        }
    }
}