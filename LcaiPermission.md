LcaiPermissionRequestBulider Android 权限请求构建器使用文档

📖 概述

`LcaiPermissionRequestBulider` 是一个采用 Builder 设计模式的 Android 权限请求构建器类。它提供了一个链式调用的 API，用于简化 Android 运行时权限的申请流程，支持自定义权限申请弹窗、权限检查、跳转系统设置等功能。

🎯 核心特性

- 链式调用：采用 Builder 模式，配置灵活，代码简洁易读
- 自定义弹窗：支持在系统权限弹窗显示前展示自定义解释性弹窗，提高用户授权率
- 权限检查：支持仅检查权限而不发起系统申请的功能
- 系统设置跳转：当权限被永久拒绝时，可引导用户跳转到系统设置页面
- 样式自定义：支持弹窗的标题、内容、按钮文字、颜色、大小等全方位自定义配置
- 多权限支持：支持一次性申请多个权限，系统会依次弹出申请对话框

📦 集成方式

implementation "com.github.liucai-bit:lcaipermission:v1.1.2"

1.权限声明
   在 `AndroidManifest.xml` 中声明所需的权限，例如：

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- 相机权限 -->
    <uses-permission android:name="android.permission.CAMERA" />
    
    <!-- 存储权限（根据 Android 版本选择） -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    
    <!-- Android 13+ 媒体权限 -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    
    <!-- 位置权限 -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <!-- 录音权限 -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
</manifest>
```

🛠️ 使用方法

基本使用示例

```java
// 创建构建器实例
LcaiPermissionRequestBulider builder = new LcaiPermissionRequestBulider()
    .with(MainActivity.this)  // 设置 Activity 上下文
    .addPermission(LcaiPermissionString.CAMERA)  // 添加相机权限
    .addPermission(LcaiPermissionString.READ_EXTERNAL_STORAGE)  // 添加存储权限
    .addResult(new LcaiReqPermissionResult() {  // 设置回调
        @Override
        public void onReqPermissionPass() {
            // 权限申请成功
            Toast.makeText(MainActivity.this, "权限申请成功", Toast.LENGTH_SHORT).show();
            // 执行需要权限的操作
            openCamera();
        }
        
        @Override
        public void onReqPermissionNoPass() {
            // 权限申请失败
            Toast.makeText(MainActivity.this, "权限被拒绝", Toast.LENGTH_SHORT).show();
        }
    });

// 执行权限请求
LcaiManager.getInstance().permissionReq(builder);
```

带自定义解释弹窗的权限申请

```java
LcaiPermissionRequestBulider builder=new LcaiPermissionRequestBulider()
    .with(MainActivity.this)
    .addPermission(LcaiPermissionString.CAMERA, LcaiPermissionString.READ_EXTERNAL_STORAGE)
    .showDialog()  // 显示自定义弹窗
    .addTitle("需要权限授权")  // 弹窗标题
    .addContent("此功能需要相机和存储权限，用于拍照和保存图片")  // 弹窗内容
    .addContentNever("您已永久拒绝权限，请到设置中手动开启")  // 永久拒绝时的提示内容
    .addLeftString("取消")  // 左侧按钮文字
    .addRightStirng("去开启")  // 右侧按钮文字
    .addTitleColor(Color.parseColor("333333"))  // 标题颜色
    .addContentColor(Color.parseColor("666666"))  // 内容颜色
    .addLeftColor(Color.parseColor("66676B"))  // 左侧按钮颜色
    .addRightColor(Color.parseColor("FF9800"))  // 右侧按钮颜色
    .addTitleSize(20)  // 标题文字大小
    .addContentSize(16)  // 内容文字大小
    .addBtnSize(18)  // 按钮文字大小
    .addResult(new LcaiReqPermissionResult() {
        @Override
        public void onReqPermissionPass() {
            // 用户点击"去开启"并授权成功
            startCamera();
        }
        
        @Override
        public void onReqPermissionNoPass() {
            // 用户点击"取消"或拒绝授权
            Toast.makeText(MainActivity.this, "权限被拒绝", Toast.LENGTH_SHORT).show();
        }
    });
// 执行权限请求
LcaiManager.getInstance().permissionReq(builder);
```

仅检查权限状态（不发起申请）

```java
LcaiPermissionRequestBulider builder=new LcaiPermissionRequestBulider()
    .with(MainActivity.this)
    .addPermission(LcaiPermissionString.CAMERA, LcaiPermissionString.ACCESS_FINE_LOCATION)
    .check(true)  // 仅检查权限，不发起申请
    .addResult(new LcaiReqPermissionResult() {
        @Override
        public void onReqPermissionPass() {
            // 已有权限
            Log.d("Permission", "已拥有所需权限");
            proceedWithFeature();
        }
        
        @Override
        public void onReqPermissionNoPass() {
            // 缺少权限
            Log.d("Permission", "缺少所需权限");
            showPermissionNeededDialog();
        }
    });
// 执行权限请求
LcaiManager.getInstance().permissionReq(builder);
```


📋 配置选项详解

必需配置项

| 方法 | 说明 | 示例 |
|------|------|------|
| `with(Activity mActivity)` | 设置 Activity 上下文，必须调用 | `.with(MainActivity.this)` |
| `addPermission(String permission)` | 添加单个权限 | `.addPermission(Manifest.permission.CAMERA)` |
| `addPermission(String... pers)` | 添加多个权限 | `.addPermission(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)` |
| `addResult(LcaiReqPermissionResult result)` | 设置结果回调接口 | `.addResult(new LcaiReqPermissionResult() { ... })` |

弹窗配置项（需先调用 `showDialog()`）

| 方法 | 说明 | 默认值 |
|------|------|--------|
| `showDialog()` | 启用自定义弹窗显示 | `false` |
| `addTitle(String title)` | 设置弹窗标题文本 | 无 |
| `addContent(String content)` | 设置弹窗说明内容文本 | 无 |
| `addContentNever(String neverContent)` | 设置权限被永久拒绝时的提示内容 | 无 |
| `addLeftString(String leftString)` | 设置左侧按钮文字 | 无 |
| `addRightStirng(String rightString)` | 设置右侧按钮文字 | 无 |
| `addLeftBg(Drawable leftBg)` | 设置左侧按钮背景 | 无 |
| `addRightBg(Drawable rightBg)` | 设置右侧按钮背景 | 无 |
| `addLeftColor(int leftColor)` | 设置左侧按钮文字颜色 | `66676B` |
| `addRightColor(int rightColor)` | 设置右侧按钮文字颜色 | `FF9800` |
| `addTitleColor(int titleColor)` | 设置标题文字颜色 | `333333` |
| `addContentColor(int contentColor)` | 设置内容文字颜色 | `66676B` |
| `addTitleSize(int titleSize)` | 设置标题文字大小（单位：sp） | `18` |
| `addContentSize(int contentSize)` | 设置内容文字大小（单位：sp） | `14` |
| `addBtnSize(int btnSize)` | 设置按钮文字大小（单位：sp） | `16` |

功能配置项

| 方法 | 说明 | 默认值 |
|------|------|--------|
| `check(boolean checkPermission)` | 设置为 `true` 时仅检查权限，不发起系统权限申请 | `false` |
| `toSystem()` | 设置为开启状态，权限被永久拒绝后会提示跳转系统设置 | `false` |

🔧 回调接口

LcaiReqPermissionResult 接口

```java
public interface LcaiReqPermissionResult {
    /**
     * 权限申请成功回调
     * 当所有申请的权限都被用户授予时触发
     */
    void onReqPermissionPass();
    
    /**
     * 权限申请失败回调
     * 当有任何权限被用户拒绝时触发
     */
    void onReqPermissionNoPass();
}
```

⚠️ 注意事项

1. Android 权限系统要求
- Android 6.0 (API 23) 及以上版本需要动态申请危险权限
- 必须在 `AndroidManifest.xml` 中声明需要申请的权限
- Android 10 (API 29) 及以上版本引入了分区存储（Scoped Storage），存储权限的使用方式有所变化
- Android 13 (API 33) 开始，`READ_EXTERNAL_STORAGE` 被 `READ_MEDIA_IMAGES` 等更细粒度的权限替代

2. 上下文有效性
- 传入的 Activity 上下文必须是有效的，避免使用已销毁的 Activity
- 建议在 `onCreate()`、`onResume()` 或按钮点击事件等生命周期稳定的地方调用

3. 适配不同 Android 版本

```java
// 根据 Android 版本使用不同的权限
LcaiPermissionRequestBulider builder = new LcaiPermissionRequestBulider()
    .with(MainActivity.this);

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // Android 13+ 使用新的媒体权限
    builder.addPermission(Manifest.permission.READ_MEDIA_IMAGES);
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    // Android 10-12 只需要 READ_EXTERNAL_STORAGE（写入媒体文件无需权限）
    builder.addPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
} else {
    // Android 9 及以下需要读写权限
    builder.addPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    builder.addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
}

builder.addPermission(Manifest.permission.CAMERA);
```

4. 权限分组处理
   Android 系统会将权限分组，当请求同一组的多个权限时，系统只会显示一次请求对话框。常见的权限组包括：
- CALENDAR：日历读写权限
- CAMERA：相机权限
- CONTACTS：联系人权限
- LOCATION：位置权限
- MICROPHONE：麦克风权限
- PHONE：电话权限
- SENSORS：传感器权限
- SMS：短信权限
- STORAGE：存储权限（Android 13+ 有变化）

5. 测试建议
- 在真实设备上测试权限申请流程
- 测试权限被拒绝后的用户体验
- 测试权限被永久拒绝（勾选"不再询问"）后的处理
- 测试应用重启后的权限状态

🐛 常见问题排查

Q1：自定义弹窗不显示？
可能原因及解决方案：
- 没有调用 `showDialog()` 方法
- 弹窗的必要配置项（如标题、内容、按钮文字）没有设置
- 检查 `addTitle()`、`addContent()`、`addLeftString()`、`addRightStirng()` 是否已正确设置

Q2：权限申请后回调没有触发？
可能原因及解决方案：
- 确保实现了 `LcaiReqPermissionResult` 接口的两个方法
- 检查 Activity 上下文是否仍然有效（未销毁）
- 确认已调用 `addResult()` 设置回调
- 确保权限已在 `AndroidManifest.xml` 中正确声明

Q3：在 Fragment 中如何使用？
解决方案：
传入 Fragment 所在的 Activity 上下文：

```java
.with(getActivity())
```

Q4：如何一次申请多个权限？
解决方案：
使用 `addPermission(String... pers)` 方法：

```java
.addPermission(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.ACCESS_FINE_LOCATION
)
```

系统会自动按权限组分类，可能会显示多个权限申请对话框。

📱 兼容性要求

- 最低 Android 版本：API 23 (Android 6.0) - 运行时权限起始版本
- 目标 Android 版本：建议根据应用需求设置，最新版本可获得最佳体验
- 编译 SDK 版本：建议 API 34 (Android 14) 或更高，以支持最新的权限特性
- 支持库：纯原生实现，无需额外依赖库

🔄 更新日志

最新版本：1.1.2

📄 许可证信息

`LcaiPermissionRequestBulider` 类采用 MIT 许可证发布，允许在商业和非商业项目中自由使用、修改和分发。

🤝 贡献与反馈

如果您在使用过程中发现问题或有改进建议，欢迎通过以下方式参与：
1. 提交 Issue 报告问题
2. Fork 项目并提交 Pull Request
3. 提供使用反馈和改进建议

📧 技术支持

如有技术问题或需要进一步的使用指导，请联系：
- 项目维护者：liucai
- 项目名称：lcpermission
- 联系邮箱：[liucai@csii.com.cn]
- 项目仓库：[请在此处添加项目仓库地址]

---

重要提示：
1. 权限申请是 Android 应用开发中的重要环节，请确保充分测试各种权限申请场景
2. 对于敏感权限，建议在使用前向用户解释权限用途，提高用户授权率
3. 遵循最小权限原则，只申请应用真正需要的权限
4. 定期检查 Android 权限系统的更新，确保应用兼容最新的 Android 版本