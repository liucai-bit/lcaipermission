LcaiCameraPhotoBulider Android 相机相册构建器使用文档

📖 概述

`LcaiCameraPhotoBulider` 是一个采用 Builder 设计模式的 Android 相机和相册功能构建器类。它提供了简洁、灵活的链式调用 API，用于配置和启动相机拍照或相册选择功能，支持多种返回格式和自定义选项。

🎯 核心特性

- 链式调用：采用 Builder 模式，配置灵活，代码简洁易读
- 双模式支持：支持相机拍照和相册选择两种模式
- 多格式返回：支持返回文件路径 URL 和 Base64 编码两种格式
- 自定义配置：可配置是否保存照片、是否添加 Base64 头部信息
- 结果回调：提供统一的结果回调接口，处理成功和失败情况

📦 集成方式

1. 添加依赖
   implementation "com.github.liucai-bit:lcaipermission:vx.x.x"

2. 权限配置
在 `AndroidManifest.xml` 中添加必要的权限：

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- 相机权限 -->
    <uses-permission android:name="android.permission.CAMERA" />
    
    <!-- 存储权限（根据 Android 版本选择） -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
                     android:maxSdkVersion="32" />
    
    <!-- Android 13+ 媒体权限 -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    
    <!-- 如果需要保存照片到相册 -->
    <uses-permission android:name="android.permission.ACCESS_MEDIA_LOCATION" 
                     android:maxSdkVersion="32" />

</manifest>
```

🛠️ 使用方法

基本使用示例

```java
// 创建构建器实例
LcaiCameraPhotoBulider builder = new LcaiCameraPhotoBulider()
    .with(MainActivity.this)  // 设置上下文
    .setCheckType(LcaiPhotoLib.CHECK_PHOTO)  // 设置选择类型
    .setResultType(LcaiPhotoLib.BACK_URL)    // 设置返回类型
    .setResult(new LcaiPhotoResult() {       // 设置结果回调
        @Override
        public void onUrl(String url) {
            // 使用图片路径
            Glide.with(MainActivity.this)
                    .load(new File(url))
                    .into(imageView);
        }

        @Override
        public void onBase64(String base64) {
            // 处理成功结果
            // Base64 格式返回
            Log.d("Photo", "Base64数据: " + base64);
            // 显示 Base64 图片
            String imageHtml = "<img src=\"" + base64 + "\"/>";
            webView.loadData(imageHtml, "text/html", "UTF-8");
        }
        
        @Override
        public void onError(String error) {
            // 处理错误
            Log.e("Photo", "错误: " + error);
            Toast.makeText(MainActivity.this, "操作失败: " + error, Toast.LENGTH_SHORT).show();
        }
    });

// 执行图片选择或拍照
LcaiManager.getInstance().openPhotoOrCamera(builder);
```

拍照功能（返回 Base64 并保存到相册）
拍照后需要返回图片地址必须调用.save()方法

```java
new LcaiCameraPhotoBulider()
    .with(MainActivity.this)
    .setCheckType(LcaiPhotoLib.CHECK_CAMERA)  // 设置为相机模式
    .setResultType(LcaiPhotoLib.BACK_BASE64)  // 返回 Base64 格式
    .addHeader()      // 添加 Base64 头部信息
    .save()           // 保存照片到系统相册
    .setResult(new LcaiPhotoResult() {
        @Override
        public void onBase64(String base64Data) {
            // Base64 数据已包含头部信息，可直接使用
            // 例如：data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...
            imageView.setImageBitmap(decodeBase64(base64Data));
            Toast.makeText(MainActivity.this, "拍照成功并已保存到相册", Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onError(String error) {
            Toast.makeText(MainActivity.this, "拍照失败: " + error, Toast.LENGTH_SHORT).show();
        }
    });
```

相册选择功能（返回文件路径）

```java
new LcaiCameraPhotoBulider()
    .with(MainActivity.this)
    .setCheckType(LcaiPhotoLib.CHECK_PHOTO)  // 设置为相册模式（默认）
    .setResultType(LcaiPhotoLib.BACK_URL)    // 返回文件路径
    .setResult(new LcaiPhotoResult() {
        @Override
        public void onUrl(String filePath) {
            // 使用文件路径加载图片
            File imageFile = new File(filePath);
            if (imageFile.exists()) {
                // 使用 Glide 加载图片
                Glide.with(MainActivity.this)
                     .load(imageFile)
                     .into(imageView);
                
                // 或者使用 BitmapFactory
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                imageView.setImageBitmap(bitmap);
            }
        }
        
        @Override
        public void onError(String error) {
            Toast.makeText(MainActivity.this, "选择失败: " + error, Toast.LENGTH_SHORT).show();
        }
    });
```

拍照但不保存到相册

```java
new LcaiCameraPhotoBulider()
    .with(MainActivity.this)
    .setCheckType(LcaiPhotoLib.CHECK_CAMERA)
    .setResultType(LcaiPhotoLib.BACK_URL)  // 返回临时文件路径
    // 不调用 save() 方法，照片将保存在应用缓存目录
    .setResult(new LcaiPhotoResult() {
        @Override
        public void onUrl(String filePath) {
            // 文件保存在应用缓存目录，应用卸载或清理缓存时会删除
            // 如果需要永久保存，请自行处理文件移动
            Log.d("Photo", "临时文件路径: " + filePath);
        }
        
        @Override
        public void onError(String error) {
            Log.e("Photo", "错误: " + error);
        }
    });
```

📋 配置选项详解

必需配置项

| 方法 | 说明 | 参数类型 | 默认值 | 示例 |
|------|------|----------|--------|------|
| `with(Context mContext)` | 设置上下文对象 | `Context` | 无 | `.with(MainActivity.this)` |
| `setResult(LcaiPhotoResult result)` | 设置结果回调接口 | `LcaiPhotoResult` | 无 | `.setResult(new LcaiPhotoResult() { ... })` |

功能配置项

| 方法 | 说明 | 参数类型 | 默认值 | 可选值 |
|------|------|----------|--------|--------|
| `setCheckType(String checkType)` | 设置选择类型 | `String` | `LcaiPhotoLib.CHECK_PHOTO` | `LcaiPhotoLib.CHECK_PHOTO`（相册）<br>`LcaiPhotoLib.CHECK_CAMERA`（相机） |
| `setResultType(String resultTyp)` | 设置返回类型 | `String` | `LcaiPhotoLib.BACK_URL` | `LcaiPhotoLib.BACK_URL`（文件路径）<br>`LcaiPhotoLib.BACK_BASE64`（Base64编码） |
| `addHeader()` | 添加 Base64 头部信息 | 无 | `false` | 调用此方法启用 |
| `save()` | 保存照片到系统相册 | 无 | `false` | 调用此方法启用 |

常量定义（LcaiPhotoLib）

```java
public interface LcaiPhotoLib {
    //选择路径常量
    String CHECK_PHOTO = "CHECK_PHOTO"; //选择相册

    String OPEN_CAMERA = "OPEN_CAMERA"; //选择相机

    //返回数据常量
    String BACK_URL = "BACK_URL"; //返回文件地址

    String BACK_BASE64 = "BACK_BASE64"; //返回BASE64数据

    String HEADER = "data:image/%s;base64,";
}
```

回调接口（LcaiPhotoResult）

```java
public interface LcaiPhotoResult {
    /**
     * 返回文件地址
     * @param url 地址信息
     */
    default void onUrl(String url) {

    }

    /**
     * 返回文件BASE64数据
     * @param base64
     */
    default void onBase64(String base64) {

    }

    /**
     * 错误回调
     * @param error 错误信息
     */
    void onError(String error);
}
```

🔧 核心功能说明

1. 相机拍照功能
- 调用系统相机进行拍照
- 支持保存照片到系统相册（调用 `save()` 方法）
- 返回文件路径或 Base64 编码
- 自动处理文件路径和 Uri 转换

2. 相册选择功能
- 调用系统相册选择图片
- 支持单选图片
- 返回选中图片的文件路径或 Base64 编码
- 自动处理 ContentResolver 和文件路径转换

3. Base64 编码功能
- 当 `resultType` 设置为 `BACK_BASE64` 时，返回 Base64 编码的图片数据
- 调用 `addHeader()` 方法可在 Base64 字符串前添加数据 URI 头部（如 `data:image/jpeg;base64,`）
- 适用于需要直接在前端显示或网络传输的场景

4. 文件保存功能
- 调用 `save()` 方法可将拍摄的照片保存到系统相册
- 使用 `MediaStore` API 保存，兼容 Android 各版本
- 保存后的照片可在系统图库中查看

⚠️ 注意事项

1. 权限处理
在使用相机或相册功能前，需要确保已获取相应权限：

参考《LcaiPermission.md》 处理

2. Android 版本适配

Android 10+（Scoped Storage）
- 使用 `MediaStore` API 访问媒体文件
- 无需 `WRITE_EXTERNAL_STORAGE` 权限即可保存到公共目录
- 相册选择返回的是 `content://` URI，需要使用 `ContentResolver` 读取

Android 13+（细化媒体权限）
- 需要 `READ_MEDIA_IMAGES` 权限代替 `READ_EXTERNAL_STORAGE`
- 相机权限保持不变

3. 文件路径处理
- 返回的文件路径可能是 `file://` 或 `content://` 格式
- 建议使用 `ContentResolver` 或第三方库（如 Glide）处理 URI
- Base64 数据可能较大，注意内存使用

4. 生命周期管理
- 确保在 `Activity` 或 `Fragment` 的生命周期内使用
- 处理配置变更（如屏幕旋转）导致的数据丢失
- 及时释放不再使用的资源

🐛 常见问题

Q1：拍照后返回空路径或空 Base64？
可能原因：
- 存储权限未授予
- 相机应用返回了空数据

解决方案：
1.确保已授予相机和存储权限
2.在 `onError` 回调中处理错误信息

Q2：Base64 数据无法显示？
可能原因：
- 没有调用 `addHeader()` 方法添加数据 URI 头部
- Base64 数据格式不正确

解决方案：
1. 确保调用了 `addHeader()` 方法
2. 检查 Base64 字符串是否完整
3. 使用 `android.util.Base64` 类进行解码验证

Q3：保存到相册失败？
可能原因：
- Android 10+ 没有使用 `MediaStore` API
- 存储权限不足
- 文件路径无效

解决方案：
1. Android 10+ 使用 `MediaStore.Images.Media.insertImage()`
2. 确保有存储权限（Android 9 及以下需要 `WRITE_EXTERNAL_STORAGE`）
3. 检查文件路径是否有效

Q4：在 Fragment 中如何使用？
解决方案：
传入 Fragment 的上下文：

```java
.with(getActivity())
```

Q5：如何处理大图片？
建议：
1. 使用采样率压缩
2. 异步加载图片
3. 及时回收 Bitmap 内存
4. 考虑使用第三方图片加载库（如 Glide、Picasso）

```java
// 使用 Glide 加载大图片
Glide.with(context)
     .load(filePath)
     .override(1024, 1024)  // 限制尺寸
     .into(imageView);
```

📱 兼容性

- 最低 Android 版本：API 23 (Android 5.0)
- 目标 Android 版本：建议 API 34 (Android 13) 或更高
- 存储策略：兼容 Scoped Storage（Android 10+）
- 权限模型：支持运行时权限（Android 6.0+）

🔄 更新日志

最新版本：1.1.2

📄 许可证

本项目采用 MIT 许可证。详情请查看项目 LICENSE 文件。

🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个库。在提交前请确保：
1. 代码符合 Android 开发规范
2. 添加必要的注释和文档
3. 测试覆盖主要功能

📧 技术支持

如有问题或建议，请通过以下方式联系：
- 作者：liucai
- 项目名称：lcpermission
- 创建日期：2026年6月4日

---

最佳实践建议：
1. 权限请求时机：在用户真正需要功能时请求权限，并提供清晰的解释
2. 错误处理：始终实现 `onError` 回调，提供友好的错误提示
3. 内存管理：及时回收图片资源，避免内存泄漏
4. 用户体验：提供加载指示器，避免界面卡顿
5. 测试覆盖：在不同 Android 版本和设备上测试功能

通过遵循本指南，您可以轻松集成和使用 `LcaiCameraPhotoBulider` 类，实现强大的相机和相册功能，同时确保良好的用户体验和代码可维护性。