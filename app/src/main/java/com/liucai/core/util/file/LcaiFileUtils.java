package com.liucai.core.util.file;

import android.annotation.SuppressLint;
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
import com.liucai.camera_photo.core.LcaiPhotoLib;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.http.thread.GlobalThreadPool;
import com.liucai.http.thread.LcaiRunnableUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * @author liucai
 * @program lcpermission
 * @description 文件
 * @Date 2025-08-28
 */
public class LcaiFileUtils {

    /**
     * 创建保存图片的文件
     *
     * @return
     * @throws IOException
     */
    public static File createImageFile(Activity mActivity) {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, "JPEG" + imageName + ".jpg");
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }

    public static File createCameraImageFile(Activity activity) {
        File imageFile=null;
        try {
            String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File storageDir = activity.getExternalCacheDir();
            imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        } catch (IOException e) {
            LcaiLogUtils.e("创建文件失败");
        }
        return imageFile;
    }

    public static void clearFile(File file) {
        if (file != null && file.exists()) {
            String path = file.getAbsolutePath();
            boolean delete = file.delete();
            LcaiLogUtils.d("删除图片:"+path,delete ? "SUCCESS":"FAILURE");
        }
    }

    public static JSONObject converImagePathToBase64(String path, boolean addHeader) {
        JSONObject jsonObject = new JSONObject();
        float sizeInMb = 0;
        File file = new File(path);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            long byteCount = bitmap.getAllocationByteCount();
            sizeInMb = byteCount / (1024f * 1024f);
            boolean needCompress = sizeInMb > 1.0;
            jsonObject.put("fileSize", sizeInMb);
            String base64 = bitmapToBase64(bitmap, needCompress);
            if (addHeader) {
                base64 = String.format(LcaiPhotoLib.HEADER, "jpeg") + base64;
            }
            jsonObject.put("content", base64);
        }
        return jsonObject;
    }

    /**
     * 将图片转换成BASE64
     *
     * @param mContext
     * @param uri
     * @param addHeader
     * @return
     */
    public static JSONObject convertImageToBase64(Context mContext, Uri uri, boolean addHeader) {
        String base64 = "";
        float sizeInMb = 0;
        JSONObject jsonObject = new JSONObject();
        try {
            String[] filePathColumn = {MediaStore.Images.Media.SIZE, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT};
            Cursor cursor = mContext.getContentResolver().query(uri, filePathColumn, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") long sizeInBytes = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                sizeInMb = sizeInBytes / (1024f * 1024f);
                cursor.close();
            }

            // 1. 获取Bitmap并计算大小
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            jsonObject.put("fileSize", sizeInMb);
            boolean needCompress = sizeInMb > 1.0; // 超过1MB压缩
            base64 = bitmapToBase64(bitmap, needCompress);
            jsonObject.put("fileType", "image");

            if (addHeader) {
                base64 = String.format(LcaiPhotoLib.HEADER, "jpeg") + base64;
            }
            jsonObject.put("content", base64);
        } catch (IOException e) {
            LcaiLogUtils.e("图片转换失败");
        }
        return jsonObject;
    }

    /**
     * 将图片转换成BASE64
     *
     * @param intent
     * @param addHeader
     * @return
     */
    public static JSONObject cameraImageToBase64(Intent intent, boolean addHeader) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileType", "image");
        if (intent != null) {
            Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
            long byteCount = bitmap.getAllocationByteCount();
            double fileSizeMB = byteCount / (1024.0 * 1024.0);
            boolean needCompress = fileSizeMB > 1.0; // 超过1MB压缩
            String content = bitmapToBase64(bitmap, needCompress);
            jsonObject.put("fileSize", fileSizeMB);
            if (addHeader) {
                content = String.format(LcaiPhotoLib.HEADER, "jpeg") + content;
            }
            jsonObject.put("content", content);
        }

        return jsonObject;
    }

    /**
     * 将bitmap转换成BASE64
     *
     * @param bitmap
     * @param ya
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap, boolean ya) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (ya) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    /**
     * 获取实际地址
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
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

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static void saveImageToGallery(Context mContext,final File sourceFile,LcaiFileInterface fileInterface) {
        GlobalThreadPool.execute(new LcaiRunnableUtils() {
            @Override
            public void run() {
                if (sourceFile != null && sourceFile.exists()) {
                    OutputStream out = null;
                    FileInputStream in = null;
                    Uri savedUri = null;
                    try {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String imageFileName = "IMG_" + timeStamp + ".jpg";
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/Camera");
                            values.put(MediaStore.Images.Media.IS_PENDING, 1);
                        } else {
                            values.put(MediaStore.Images.Media.DATA, sourceFile.getAbsolutePath());
                        }
                        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        savedUri = mContext.getContentResolver().insert(collection, values);
                        if (savedUri != null) {
                            out = mContext.getContentResolver().openOutputStream(savedUri);
                            in = new FileInputStream(sourceFile);
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                            out.flush();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                values.clear();
                                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                                mContext.getContentResolver().update(savedUri, values, null, null);
                            } else {
                                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, savedUri));
                            }
                            LcaiLogUtils.d("图片插入图库成功！");

                            String path = getPath(mContext, savedUri);
                            if (fileInterface != null) {
                                fileInterface.onUriPath(path);
                            }
                        }
                    } catch (Exception e) {
                        if (savedUri != null) {
                            mContext.getContentResolver().delete(savedUri, null, null);
                        }
                        LcaiLogUtils.d("图片插入图库失败");
                    }finally {
                        try {
                            if (out != null) out.close();
                            if (in != null) in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //清除临时文件
                        clearFile(sourceFile);
                    }
                }
            }
        });

    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
