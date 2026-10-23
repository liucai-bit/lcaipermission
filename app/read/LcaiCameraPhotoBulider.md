# LcaiCameraPhoto 相机/相册选择模块 使用文档

> 模块包名：`com.liucai.camera_photo`
> 统一入口：**`LcaiManager.getInstance().openPhotoOrCamera(builder)`**
> 回调方式：**`builder.setResult(...)`**

---

## 一、概述

`LcaiCameraPhoto` 是一个**拍照 / 从相册选图**的轻量封装，内部用 `LcaiCamearPhtoActivity` 承载 `ActivityResultContracts.TakePicture` / `GetContent`，最终通过 `LcaiGalleryResult` 回调把结果交回业务。

**调用链**（务必理解，否则会踩 7.1 的坑）：

```
业务方
  └─ LcaiManager.getInstance().openPhotoOrCamera(builder)
       ├─ 保存 currentPhotoBuilder（强引用，防止 GC）
       └─ new LcaiCameraPhoto(builder).start()
            └─ 跳转 LcaiCamearPhtoActivity

LcaiCamearPhtoActivity 拿结果
  └─ LcaiManager.Internal.getPhotoCameraResult()（内部固定中转）
       └─ 转发到 currentPhotoBuilder.result
            └─ 业务方的 LcaiGalleryResult 回调
```

**结论：业务方必须通过 `LcaiManager.getInstance().openPhotoOrCamera(...)` 启动，并且回调必须通过 `builder.setResult(...)` 设置。** 直接 `new LcaiCameraPhoto(b).start()` 会导致 `currentPhotoBuilder == null`，`LcaiCamearPhtoActivity` 拿不到结果。

能力清单：

| 能力 | 说明 |
|---|---|
| 拍照 | 调用系统相机，自动生成临时文件，支持保存到相册 |
| 相册选图 | 调用系统选图器（`GetContent`），无需权限 |
| 结果格式 | `URL`（文件路径 JSON）或 `BASE64`（可选 data-uri 头） |
| 保存到相册 | 拍照场景可自动保存到系统相册 |
| 取消 / 错误 | 统一走 `onCancel` / `onError` |

---

## 二、快速开始

### 2.1 拍照（返回文件路径 + 保存到相册）

```java
LcaiCameraPhotoBulider builder = new LcaiCameraPhotoBulider()
        .addCheck(LcaiGalleryCheckType.CAMERA)
        .addResult(LcaiGalleryResultType.URL)
        .setSave(true)
        .setResult(new LcaiGalleryResult() {
            @Override public void onUrl(String json) {
                // json 形如：{"fileType":"image","fileSize":234,"content":"/storage/.../xxx.jpg"}
                String content = JSON.parseObject(json).getString("content");
                imageView.setImageURI(Uri.fromFile(new File(content)));
            }
            @Override public void onError(String error) {
                GlobalAppUtil.toast(error);
            }
        });

LcaiManager.getInstance().openPhotoOrCamera(builder);
```

### 2.2 相册选图（返回 Base64 带 data-uri 头）

```java
LcaiCameraPhotoBulider builder = new LcaiCameraPhotoBulider()
        .addCheck(LcaiGalleryCheckType.PHOTO)
        .addResult(LcaiGalleryResultType.BASE64)
        .setAddHeader(true)   // 带 "data:image/jpeg;base64,..." 前缀
        .setResult(new LcaiGalleryResult() {
            @Override public void onBase64(String json) {
                // json 形如：{"content":"data:image/jpeg;base64,/9j/4AAQ...","fileType":"image"}
            }
            @Override public void onError(String error) { }
        });

LcaiManager.getInstance().openPhotoOrCamera(builder);
```

### 2.3 完整的最小可用模板

```java
// 1. 构造 + 设置回调（一次完成）
LcaiCameraPhotoBulider builder = new LcaiCameraPhotoBulider()
        .addCheck(LcaiGalleryCheckType.PHOTO)
        .addResult(LcaiGalleryResultType.URL)
        .setResult(new LcaiGalleryResult() {
            @Override public void onUrl(String json)    { /* 成功 */ }
            @Override public void onBase64(String json) { /* 成功 */ }
            @Override public void onCancel()            { /* 用户取消 */ }
            @Override public void onError(String error) { /* 出错 */ }
        });

// 2. 通过 LcaiManager 启动
LcaiManager.getInstance().openPhotoOrCamera(builder);
```

---

## 三、类与 API

### 3.1 `LcaiManager`（真正入口）

```java
public class LcaiManager {
    public static LcaiManager getInstance();

    /** 打开拍照/相册选择 */
    public void openPhotoOrCamera(@NonNull LcaiCameraPhotoBulider builder);

    /** 清理当前拍照 Builder 引用（框架内部使用，业务方勿主动调用） */
    public void clearPhotoReferences();

    /** 内部回调中转，LcaiCamearPhtoActivity 用，业务方不要直接使用 */
    public static class Internal {
        public static LcaiGalleryResult getPhotoCameraResult();
    }
}
```

**`openPhotoOrCamera(builder)` 内部行为：**

1. `clearPhotoReferences()`——清掉上一次残留的 builder（⚠️ 意味着同时只能有一个进行中的调用）；
2. `currentPhotoBuilder = builder`——强引用保存，防止业务方传匿名对象时被 GC；
3. `new LcaiCameraPhoto(builder).start()`——跳转 `LcaiCamearPhtoActivity`。

### 3.2 `LcaiCameraPhoto`

框架内部启动器。**业务方不应直接调用**（见 7.1）。

```java
public class LcaiCameraPhoto {
    public LcaiCameraPhoto(LcaiCameraPhotoBulider builder);
    public void start();
}
```

`start()` 内部行为：

1. 从 `LcaiActivityManager.getInstance().getResumedActivity()` 取当前前台 Activity；
2. 为 `null` 或已 `isFinishing`/`isDestroyed` → 打印日志并**静默返回**（不抛异常，也不回调 `onError`）；
3. 构造 `Intent` 跳转 `LcaiCamearPhtoActivity`，携带 4 个参数。

### 3.3 `LcaiCameraPhotoBulider`

参数容器 + 回调载体。所有 `addXxx / setXxx` 都返回 `this`，可链式配置。

| 字段 | 类型 | 默认值 | 配置方法 | 说明 |
|---|---|---|---|---|
| `type` | `LcaiGalleryCheckType` | `PHOTO` | `addCheck(type)` | 拍照还是相册 |
| `resultTyp` | `LcaiGalleryResultType` | `URL` | `addResult(type)` | 结果格式 |
| `addHeader` | `boolean` | `false` | `setAddHeader(boolean)` / `addHeader()` | Base64 是否带 data-uri 头 |
| `save` | `boolean` | `false` | `setSave(boolean)` / `save()` | 拍照后是否保存到系统相册 |
| **`result`** | **`LcaiGalleryResult`** | **`null`** | **`setResult(result)`** | **✅ 有效，回调必经之路** |

> 每个字段都是 `public`，也可以直接赋值：
>
> ```java
> LcaiCameraPhotoBulider b = new LcaiCameraPhotoBulider();
> b.type = LcaiGalleryCheckType.CAMERA;
> b.resultTyp = LcaiGalleryResultType.BASE64;
> b.save = true;
> b.addHeader = true;
> b.result = myResultCallback;
> ```

### 3.4 `LcaiGalleryCheckType`

```java
public enum LcaiGalleryCheckType {
    CAMERA,  // 调起系统相机拍照
    PHOTO    // 调起系统相册选图（GetContent，无需权限）
}
```

### 3.5 `LcaiGalleryResultType`

```java
public enum LcaiGalleryResultType {
    URL,      // 回调 onUrl(jsonString)
    BASE64    // 回调 onBase64(jsonString)
}
```

### 3.6 `LcaiGalleryResult`

回调接口。**只有 `onError` 是必须实现的**，其余都有默认空实现。

```java
public interface LcaiGalleryResult {
    default void onUrl(String url)      {}  // 注意：参数是 JSON 字符串，不是裸 URL
    default void onBase64(String base64){}  // 注意：参数是 JSON 字符串
    default void onCancel()             {}
    void onError(String error);
}
```

**回调时机：**

| 场景 | 触发的回调 |
|---|---|
| 相册选图成功 | `onUrl` 或 `onBase64` |
| 相册选图取消 | `onCancel` |
| 拍照成功 | `onUrl` 或 `onBase64` |
| 拍照取消 | `onCancel` |
| 参数缺失 / 创建文件失败 | `onError` |

每个回调最多被调用**一次**。若 `start()` 静默失败（见 7.3），则**一次都不会调用**。

---

## 四、参数组合矩阵

### 4.1 `PHOTO` + `URL`

- 通过 `LcaiFileUtils.getPath(activity, uri)` 把 `content://` 转成真实路径；
- 回调 `onUrl(jsonString)`；
- 不修改源文件；
- `save` 参数在此模式下**无意义**。

### 4.2 `PHOTO` + `BASE64`

- 通过 `LcaiFileUtils.convertImageToBase64(activity, uri, addHeader)` 转 Base64；
- 回调 `onBase64(jsonString)`；
- `save` 参数在此模式下**无意义**。

### 4.3 `CAMERA` + `URL` + `save=false`

- `LcaiFileUtils.createCameraImageFile(this)` 创建临时文件；
- 拍照成功后生成 JSON，`content` 是**临时文件绝对路径**；
- **临时文件不会被自动删除**，业务方拿到路径后需自行管理。

### 4.4 `CAMERA` + `URL` + `save=true`

- 拍照成功后先 `saveImageToGallery()` 保存到系统相册；
- 保存成功后再生成 JSON，`content` 是**相册文件路径**；
- 保存后**临时文件会被清理**。

### 4.5 `CAMERA` + `BASE64` + `save=false`

- 直接读取临时文件 → Base64 → `onBase64`；
- **临时文件会被清理**。

### 4.6 `CAMERA` + `BASE64` + `save=true`

- 先读临时文件转 Base64 并发送；
- 再 `saveImageToGallery()` 保存到相册；
- 保存后清理临时文件；
- ⚠️ 顺序是「**先 Base64 回调，后保存相册**」，业务方若依赖"相册保存完再继续"的场景需注意这个异步性。

---

## 五、结果数据格式

### 5.1 `URL` 结果的 JSON

`onUrl(String)` 的参数是 **JSON 字符串**，不是裸路径：

```json
{
  "fileType": "image",
  "fileSize": 234,
  "content": "/storage/emulated/0/DCIM/LcaiCamera/xxx.jpg"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `fileType` | String | 固定 `"image"` |
| `fileSize` | long | 文件大小，单位 **KB**（`length() / 1024`），文件不存在时**字段可能缺失** |
| `content` | String | 文件绝对路径 |

解析示例：

```java
@Override public void onUrl(String json) {
    JSONObject obj = JSON.parseObject(json);
    String path = obj.getString("content");
    long sizeKb = obj.getLongValue("fileSize");
}
```

### 5.2 `BASE64` 结果的 JSON

`onBase64(String)` 的参数同样是 JSON 字符串：

```json
{
  "fileType": "image",
  "content": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ..." // 或裸 base64
}
```

- `addHeader=true` 时 `content` 带 `data:image/jpeg;base64,` 前缀；
- `addHeader=false` 时 `content` 是纯 base64 字符串；
- 具体字段以 `LcaiFileUtils` 实现为准，**建议业务方只取 `content` 字段**。

---

## 六、前置条件

### 6.1 初始化

```java
public class MyApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        GlobalAppUtil.init(this); // 内部会 init LcaiActivityManager
    }
}
```

`LcaiManager` 本身是单例，无需 `init`，但它依赖 `LcaiActivityManager.getResumedActivity()`，所以**必须先 `GlobalAppUtil.init(...)`**，否则 `start()` 静默失败。

### 6.2 FileProvider 配置

拍照必须配置 `FileProvider`，authority 固定为 `${applicationId}.fileprovider`：

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/lcai_file_paths" />
</provider>
```

`lcai_file_paths.xml` 至少需要包含相机文件目录（以 `LcaiFileUtils.createCameraImageFile` 的落盘位置为准）。

### 6.3 权限

| 场景 | 需要的权限 |
|---|---|
| `PHOTO` | 无（走 `GetContent`，系统选择器自带权限） |
| `CAMERA` | `CAMERA`（运行时权限，由 `LcaiBasePermissionActivity` 处理） |
| `CAMERA` + `save=true`（Android 10+） | 使用 `MediaStore` 写入相册，通常无需额外权限；Android 9 及以下需要 `WRITE_EXTERNAL_STORAGE` |
| 读取 `content` 指向的相册路径 | `READ_EXTERNAL_STORAGE` / `READ_MEDIA_IMAGES`（Android 13+） |

> `LcaiCamearPhtoActivity` 继承 `LcaiBasePermissionActivity`，相机运行时权限由基类统一申请，业务方无需手写。

### 6.4 Activity 声明

```xml
<activity
    android:name="com.liucai.camera_photo.view.LcaiCamearPhtoActivity"
    android:exported="false"
    android:theme="@style/Theme.Transparent" />
```

> 建议透明主题，避免从业务页面跳相机时闪黑屏。

---

## 七、注意事项与坑

### 7.1 ⚠️ 必须走 `LcaiManager.openPhotoOrCamera`，回调必须通过 `builder.setResult`

这是**最常见、也最致命的坑**。

`LcaiCamearPhtoActivity` 拿到结果后，调用的是 `LcaiManager.Internal.getPhotoCameraResult()`，它内部再转发到 `currentPhotoBuilder.result`。

- ✅ 正确：`LcaiManager.getInstance().openPhotoOrCamera(builder)` → `currentPhotoBuilder` 被赋值 → 回调能到达；
- ❌ 错误：`new LcaiCameraPhoto(builder).start()` → `currentPhotoBuilder` 为 `null` → **回调永远不触发**。

另外，**不能**用 `LcaiManager.Internal.setPhotoCameraResult(...)` 设置回调（旧文档有过错误示例）。`Internal` 里的 `photoCameraActivityResult` 是固定中转器，业务方的回调必须挂在 `builder.result` 上。

```java
// ✅ 唯一正确写法
LcaiCameraPhotoBulider b = new LcaiCameraPhotoBulider()
        .setResult(myCallback);
LcaiManager.getInstance().openPhotoOrCamera(b);

// ❌ 错误 1：直接 new，回调失效
new LcaiCameraPhoto(b).start();

// ❌ 错误 2：没有 setResult，回调丢失
LcaiManager.getInstance().openPhotoOrCamera(new LcaiCameraPhotoBulider());
```

### 7.2 全局只有一个 builder 槽

`LcaiManager.currentPhotoBuilder` 是**全局单例字段**，`openPhotoOrCamera()` 开头会先 `clearPhotoReferences()` 再赋值。

**后果：**

- 同时发起两次调用，第二次会覆盖第一次，第一次的结果**全部丢失**（连 `onCancel` / `onError` 都不会触发）；
- `LcaiCamearPhtoActivity` 因为是独立 Activity，会**并排存在两个实例**，用户可能在两个选择器之间看到奇怪的叠层。

**结论：一个页面只应发起一次 `openPhotoOrCamera`，等回调回来后再发起下一次。**

### 7.3 `start()` 静默失败

以下情况 `start()` 只打日志、**不回调 `onError`**，业务方感知不到：

- `LcaiActivityManager.getResumedActivity()` 返回 `null`（如在 `onCreate` 里立刻调用、App 在后台）；
- 宿主 Activity 已 `isFinishing` 或 `isDestroyed`。

**建议调用前先判断：**

```java
Activity host = LcaiActivityManager.getInstance().getResumedActivity();
if (host == null || host.isFinishing() || host.isDestroyed()) {
    GlobalAppUtil.toast("请稍后再试");
    return;
}
LcaiManager.getInstance().openPhotoOrCamera(builder);
```

### 7.4 `onUrl` / `onBase64` 参数都是 JSON 字符串

见第 5 节。**不要**把 `onUrl(json)` 的参数当作纯路径直接 `new File(json)`，必须先解析 `content` 字段。

### 7.5 `save=true` 时，`URL` 结果的 `content` 是相册路径

若业务方用这个路径做后续上传，且 App 没有 `READ_MEDIA_IMAGES` 权限，直接 `new File(path).length()` 可能为 0。要读取相册文件请通过 `ContentResolver` 或申请相应权限。

### 7.6 `CAMERA + URL + save=false` 时临时文件不会自动删除

框架只在以下场景清理临时文件：

| 场景 | 是否清理 |
|---|---|
| 拍照取消 | ✅ 清理 |
| `save=true` 保存相册成功 | ✅ 清理 |
| `BASE64`（无论 `save` 与否） | ✅ 清理 |
| `URL + save=false` | ❌ **不清理** |

**业务方需要**：拿到 `content` 后自行转存或上传，然后调用 `LcaiFileUtils.clearFile(new File(content))` 清理，否则会持续占用外部缓存。

### 7.7 回调线程

`onUrl` / `onBase64` / `onCancel` / `onError` **全部在主线程**（Activity 生命周期 + 同步工具方法）。如果 `LcaiFileUtils.convertImageToBase64` 是同步读大文件，会导致主线程卡顿——超大图片请业务方在收到回调后再切子线程上传。

### 7.8 `onDestroy` 会清空 builder 引用

```java
// LcaiCamearPhtoActivity
@Override protected void onDestroy() {
    super.onDestroy();
    LcaiManager.getInstance().clearPhotoReferences();
}
```

`LcaiCamearPhtoActivity` 销毁时清空 `currentPhotoBuilder`，因此：

- 业务页面在**回调到达之前**被销毁（旋转屏幕、被系统回收等），结果会丢失；
- 若结果需要跨 Activity 重建保留，业务方应把结果存到 `ViewModel` 或 `GlobalAppUtil.globalSetObject`，**不要**依赖回调在 Activity 内直接使用。

### 7.9 业务方不要主动调 `clearPhotoReferences()`

虽然方法是 `public` 的（方便 Activity 内部调用），但业务方**不要主动调用**：

```java
// ❌ 不要这样做，会让进行中的回调失效
LcaiManager.getInstance().clearPhotoReferences();
```

想取消当前选择器，请让用户自行 `onCancel`，或 `finish` 承载它的 `LcaiCamearPhtoActivity`。

### 7.10 重复 `launch` 保护

`LcaiCamearPhtoActivity` 内部有 `isLaunched` 标记，屏幕旋转（`savedInstanceState != null`）时**不会再次调起选择器**，避免用户转屏后拍照界面被重新拉起。业务方无需关心。

### 7.11 `photoResult` 里 `getPath` 可能返回 null

`LcaiFileUtils.getPath(activity, uri)` 在部分 ROM 或特殊 provider 上可能返回 `null`，此时 `new File(path)` 会 NPE。业务方对稳定性要求高时，可与框架维护者沟通在此处加判空兜底。

---

## 八、典型场景

### 场景 1：用户头像上传（相册选图 + Base64 直传）

```java
private void pickAvatar() {
    LcaiCameraPhotoBulider b = new LcaiCameraPhotoBulider()
            .addCheck(LcaiGalleryCheckType.PHOTO)
            .addResult(LcaiGalleryResultType.BASE64)
            .setAddHeader(true)
            .setResult(new LcaiGalleryResult() {
                @Override public void onBase64(String json) {
                    String content = JSON.parseObject(json).getString("content");
                    String pure = content.contains(",")
                            ? content.substring(content.indexOf(',') + 1)
                            : content;
                    api.uploadAvatar(pure, callback);
                }
                @Override public void onCancel() { /* 静默 */ }
                @Override public void onError(String e) { GlobalAppUtil.toast(e); }
            });

    LcaiManager.getInstance().openPhotoOrCamera(b);
}
```

### 场景 2：身份证拍照并保留原图（拍照 + 保存到相册 + URL）

```java
private void takeIdCard() {
    LcaiCameraPhotoBulider b = new LcaiCameraPhotoBulider()
            .addCheck(LcaiGalleryCheckType.CAMERA)
            .addResult(LcaiGalleryResultType.URL)
            .save()   // 等价 setSave(true)
            .setResult(new LcaiGalleryResult() {
                @Override public void onUrl(String json) {
                    String path = JSON.parseObject(json).getString("content");
                    // path 是相册文件路径，不会自动清理
                    showPreview(Uri.fromFile(new File(path)));
                }
                @Override public void onCancel() { GlobalAppUtil.toast("已取消拍照"); }
                @Override public void onError(String e) { GlobalAppUtil.toast(e); }
            });

    LcaiManager.getInstance().openPhotoOrCamera(b);
}
```

### 场景 3：临时拍照（不保存相册，业务侧自行清理）

```java
private void takeTempPhoto() {
    LcaiCameraPhotoBulider b = new LcaiCameraPhotoBulider()
            .addCheck(LcaiGalleryCheckType.CAMERA)
            .addResult(LcaiGalleryResultType.URL)
            .setSave(false)
            .setResult(new LcaiGalleryResult() {
                @Override public void onUrl(String json) {
                    File file = new File(JSON.parseObject(json).getString("content"));
                    upload(file, () -> LcaiFileUtils.clearFile(file)); // 用完清理
                }
                @Override public void onCancel() { }
                @Override public void onError(String e) { }
            });

    LcaiManager.getInstance().openPhotoOrCamera(b);
}
```

### 场景 4：统一封装到工具方法

```java
public final class PhotoPicker {

    public interface Callback {
        void onPath(String path);
        void onBase64(String base64);
        default void onCancel() {}
        default void onError(String e) {}
    }

    public static void pick(@NonNull Callback cb) {
        Activity host = LcaiActivityManager.getInstance().getResumedActivity();
        if (host == null || host.isFinishing()) return;

        LcaiCameraPhotoBulider b = new LcaiCameraPhotoBulider()
                .addCheck(LcaiGalleryCheckType.PHOTO)
                .addResult(LcaiGalleryResultType.URL)
                .setResult(new LcaiGalleryResult() {
                    @Override public void onUrl(String json) {
                        cb.onPath(JSON.parseObject(json).getString("content"));
                    }
                    @Override public void onBase64(String json) {
                        cb.onBase64(JSON.parseObject(json).getString("content"));
                    }
                    @Override public void onCancel() { cb.onCancel(); }
                    @Override public void onError(String e) { cb.onError(e); }
                });

        LcaiManager.getInstance().openPhotoOrCamera(b);
    }

    public static void capture(@NonNull Callback cb) {
        // 同上，只是 CheckType 换成 CAMERA，可按需 setSave(true)
    }
}
```

---

## 九、FAQ

**Q1：为什么回调一直不触发？**
A：按优先级检查：
1. 是否用了 `LcaiManager.getInstance().openPhotoOrCamera(builder)`？直接 `new LcaiCameraPhoto(b).start()` 不会触发回调（见 7.1）。
2. 是否 `builder.setResult(...)` 设置了回调？
3. 是否 `GlobalAppUtil.init(...)` 已调用？没有会导致 `start()` 静默失败。
4. 是否有另一次 `openPhotoOrCamera` 覆盖了这次？全局只有一个槽（见 7.2）。

**Q2：为什么调用了 `openPhotoOrCamera` 什么都没发生？**
A：99% 是 `LcaiActivityManager.getResumedActivity()` 返回了 `null`——常见于在 `onCreate` 里直接调用，或 App 处于后台。请 `view.post { ... }` 延迟到界面可见后再调。

**Q3：`onUrl(json)` 里 `json` 解析失败？**
A：这是 fastjson 序列化的 JSON，字段见 5.1 节。`fileSize` 缺失说明文件不存在（临时文件创建失败或已被清理）。

**Q4：`save=true` 后 `content` 是相册路径，但 App 读不到文件？**
A：相册路径需要 `READ_MEDIA_IMAGES`（Android 13+）或 `READ_EXTERNAL_STORAGE`（Android 12-）权限，或通过 `ContentResolver.openInputStream` 流式读取。

**Q5：可以同时发起两个 `openPhotoOrCamera` 吗？**
A：不行。全局只有一个 `currentPhotoBuilder`，第二次会覆盖第一次，第一次的回调全部丢失。请等第一次回调完成后再发起第二次。

**Q6：拍照后返回的路径找不到文件？**
A：`save=true` 时框架会清理临时文件，返回的是相册路径；`save=false` 时返回的是缓存目录路径，若用户清了缓存或 App 被卸载，文件会丢失。

**Q7：可以自定义相机 UI 吗？**
A：不可以。当前实现固定使用 `ActivityResultContracts.TakePicture`（系统相机）。如需自定义相机，需要新建模块。

**Q8：`BASE64` 适合传大图吗？**
A：不建议。Base64 会让体积膨胀约 33%，且是主线程同步转码。超 2MB 的图建议改用 `URL` + 子线程上传。

**Q9：想主动取消当前选择器怎么办？**
A：让用户自己返回即可。也可以 `finish` 承载的 `LcaiCamearPhtoActivity`（例如通过 `LcaiActivityManager.finishAllActivities(LcaiCamearPhtoActivity.class)`），但会触发 `onCancel`。**不要**手动调 `clearPhotoReferences()`。

---

## 十、API 索引

### LcaiManager（对外）

| 方法 | 说明 |
|---|---|
| `getInstance()` | 单例入口 |
| `openPhotoOrCamera(builder)` | **拍照/相册选择唯一入口** |
| `clearPhotoReferences()` | 框架内部使用，业务方勿主动调 |

### LcaiCameraPhotoBulider

| 字段 / 方法 | 默认 | 说明 |
|---|---|---|
| `type` / `addCheck(LcaiGalleryCheckType)` | `PHOTO` | 拍照 or 相册 |
| `resultTyp` / `addResult(LcaiGalleryResultType)` | `URL` | 结果格式 |
| `addHeader` / `setAddHeader(boolean)` / `addHeader()` | `false` | Base64 带 data-uri 头 |
| `save` / `setSave(boolean)` / `save()` | `false` | 拍照后保存相册 |
| **`result` / `setResult(LcaiGalleryResult)`** | `null` | **✅ 必填，回调载体** |

### LcaiGalleryResult

| 方法 | 说明 |
|---|---|
| `onUrl(String json)` | 成功回调（URL 模式），参数是 JSON 字符串 |
| `onBase64(String json)` | 成功回调（BASE64 模式），参数是 JSON 字符串 |
| `onCancel()` | 用户主动取消 |
| `onError(String error)` | 出错（必实现） |

### LcaiGalleryCheckType

| 枚举值 | 说明 |
|---|---|
| `CAMERA` | 系统相机 |
| `PHOTO` | 系统相册（`GetContent`） |

### LcaiGalleryResultType

| 枚举值 | 说明 |
|---|---|
| `URL` | 返回文件路径 JSON |
| `BASE64` | 返回 Base64 JSON |

---

*文档随代码同步维护。若发现与实现不一致，以代码为准并在 issue 中反馈。*