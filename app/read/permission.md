这是一份基于你提供的 `Lcpermission` 核心源码编写的使用文档。该框架封装了 Android 6.0 到 13+ 的动态权限申请，支持自定义引导弹窗、永久拒绝检测、跳转系统设置，并且结合了 `ActivityResultLauncher` 实现了更稳定的生命周期管理。

---

# 📚 Lcpermission 权限申请框架使用文档

## 1. 简介
`Lcpermission` 是一个轻量、易用、高兼容性的 Android 动态权限申请框架。它通过**透明 Activity 代理**和**ActivityResult API** 完成权限申请，自动处理了 Android 13 通知权限适配、永久拒绝判断（勾选“不再询问”）、权限申请历史记录等复杂场景。

**核心特性：**
*   **链式调用**：极致简洁的 Builder 设计模式。
*   **自定义引导**：支持在拉起系统弹窗前，展示自定义的业务解释弹窗。
*   **状态精准**：自动区分“暂时拒绝”和“永久拒绝”，并提供全局状态枚举。
*   **通知适配**：自动兼容 Android 13 及以上与 13 以下的系统通知权限申请。
*   **历史记录**：内置持久化存储，可随时查询某个权限的历史申请记录。

## 2. 初始化与配置

### 2.1 AndroidManifest 配置
在项目的 `AndroidManifest.xml` 中注册透明代理 Activity：
```xml
<application>
    <!-- 注册权限申请透明页 -->
    <activity
        android:name=".permission.view.LcaiPermissionActivity"
        android:theme="@style/Theme.Transparent"
        android:exported="false" />
</application>
```
> **注意**：`LcaiPermissionActivity` 继承自 `LcaiBasePermissionActivity`，内部已经做了全屏透明和状态栏透明处理，确保用户无感知。

### 2.2 权限声明
在 `AndroidManifest.xml` 中声明你需要申请的权限，例如：
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

## 3. 基础用法：申请权限

所有的权限申请都通过 `LcaiManager` 发起。

### 3.1 最简单的申请
```java
LcaiManager.getInstance().permissionReq(
    new LcaiPermissionRequestBulider()
        .with(activity) // 必须传入当前存活的 Activity
        .addPermission(LcaiPermissionString.CAMERA)
        .addResult(new LcaiReqPermissionResult() {
            @Override
            public void onReqPermissionPass() {
                // 权限全部通过，可以执行相机操作
            }

            @Override
            public void onReqPermissionNoPass(Map<String, Boolean> permissions) {
                // 权限未通过
                // permissions 格式为：Map<权限名, 是否被永久拒绝>
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    if (entry.getValue()) {
                        // 用户勾选了"不再询问"，需引导去系统设置
                    }
                }
            }
        })
);
```

### 3.2 申请通知权限（自动适配 Android 13）
框架自动处理了通知权限的版本差异：
```java
LcaiManager.getInstance().permissionReq(
    new LcaiPermissionRequestBulider()
        .with(activity)
        .addNotification() // 自动判断 Android 13 还是低版本
        .addResult(new LcaiReqPermissionResult() {
            @Override
            public void onReqPermissionPass() {
                // 通知权限已获取
            }
            // ...
        })
);
```

### 3.3 自定义引导弹窗
在申请敏感权限（如相机、定位）时，通常需要先向用户解释用途。框架内置了弹窗拦截：
```java
LcaiManager.getInstance().permissionReq(
    new LcaiPermissionRequestBulider()
        .with(activity)
        .addPermission(LcaiPermissionString.LOCATION)
        .showDialog() // 开启弹窗拦截
        .addDialogBulider(new LcaiTipsDialogBuilder()
            .setTitle("位置权限说明")
            .setMessage("我们需要位置权限为您推荐附近的商家")
            .setConfirmText("去授权")
            // 弹窗的确定和取消事件已被框架接管，无需额外写回调
        )
        .addResult(new LcaiReqPermissionResult() {
            @Override
            public void onReqPermissionPass() {
                // 用户看完弹窗并成功授权
            }

            @Override
            public void onReqPermissionNoPass(Map<String, Boolean> permissions) {
                // 用户拒绝了弹窗，或在系统申请页拒绝了权限
            }
        })
);
```

### 3.4 永久拒绝后跳转系统设置
如果用户勾选了“不再询问”，单纯调用 API 已经无法再弹窗，需要引导用户去应用设置页手动开启：
```java
LcaiManager.getInstance().permissionReq(
    new LcaiPermissionRequestBulider()
        .with(activity)
        .addPermission(LcaiPermissionString.CAMERA)
        .toSystem() // 开启跳转系统设置功能（当发生永久拒绝时，自动跳转到应用设置页）
        .addToSystemBuilder(new LcaiTipsDialogBuilder()
            .setTitle("权限被禁用")
            .setMessage("请去系统设置中开启相机权限")
        )
        .addResult(new LcaiReqPermissionResult() {
            @Override
            public void onReqPermissionPass() {
                // 用户从系统设置回来，并授予了权限
            }

            @Override
            public void onReqPermissionNoPass(Map<String, Boolean> permissions) {
                // 用户从系统设置回来，依然没有授权
            }
        })
);
```

---

## 4. 进阶用法：权限状态与历史记录

针对复杂的业务场景，框架提供了 `LcaiPermissionUtils` 进行状态校验和历史查询。

### 4.1 仅检查权限状态（不发起弹窗）
如果你只是想知道权限当前是否被授予，不想弹系统框：
```java
LcaiPermissionUtils.getInstance()
    .with(activity)
    .addVerifyPermission(LcaiPermissionString.CAMERA, LcaiPermissionString.MICROPHONE)
    .addResult(new LcaiPermissionResult() {
        @Override
        public void onPermissionsState(PermissionsState state) {
            switch (state) {
                case ALL_GRANTED:
                    // 全部已授权，开始业务逻辑
                    break;
                case NEVER_REQUESTED:
                case PARTIALLY_REQUESTED:
                    // 部分或全部从未申请，可调用 LcaiManager 发起申请
                    break;
                case ALL_DENIED_PERMANENT:
                case MIXED:
                    // 被永久拒绝或混合状态，提示去设置页
                    break;
            }
        }
    })
    .verify();
```

**PermissionsState 枚举说明：**
| 状态 | 含义 |
| :--- | :--- |
| `ALL_GRANTED` | 组内全部权限已授权 |
| `NEVER_REQUESTED` | 组内所有权限都从未申请过 |
| `PARTIALLY_REQUESTED` | 组内部分申请过（或暂未拒绝），部分没申请过 |
| `ALL_DENIED_PERMANENT` | 组内全部被永久拒绝（系统弹窗无法再拉起） |
| `MIXED` | 混合状态（有的授权，有的拒绝，有的永久拒绝） |

### 4.2 查询权限申请历史记录
框架利用 `SharedPreferences` 持久化了每一次的申请记录。你可以查询某个权限是否被申请过：
```java
LcaiPermissionUtils.getInstance()
    .with(activity)
    .addVerifyPermission(LcaiPermissionString.CAMERA)
    .addResult(new LcaiPermissionResult() {
        @Override
        public void onPermissionRequest(boolean isRequest, List<LcaiPermissionEntity> entities) {
            if (isRequest) {
                // 曾经申请过
                for (LcaiPermissionEntity entity : entities) {
                    // entity.granted：最近一次是否授权
                    // entity.permanentlyDenied：是否被永久拒绝
                }
            } else {
                // 历史上从未申请过
            }
        }
    })
    .isRequest();
```

---

## 5. 常用权限常量 (`LcaiPermissionString`)
为了避免手动敲错权限字符串，框架内置了大量的常量：
```java
// 单个权限
LcaiPermissionString.CAMERA           // 相机
LcaiPermissionString.MICROPHONE       // 麦克风 (录音)
LcaiPermissionString.ACCESS_FINE_LOCATION // 精准定位
LcaiPermissionString.READ_CONTACTS    // 读取联系人

// 权限数组 (一次申请多个)
LcaiPermissionString.LOCATION         // 粗略 + 精准定位
LcaiPermissionString.STORAGE          // 读写外部存储
LcaiPermissionString.CAMERA_STORAGE   // 相机 + 存储
```

---

## 6. 防混淆配置 (ProGuard)
如果你的项目开启了代码混淆，请在 `proguard-rules.pro` 中添加以下规则，以保证权限记录序列化正常：

```proguard
# 保留权限实体类及其属性，防止 JSON 解析失败
-keep class com.liucai.permission.bean.LcaiPermissionEntity { *; }
```

---

## 7. 开发者避坑指南（重要）
1. **单例模式的状态陷阱**：`LcaiPermissionUtils` 采用了单例实现（`getInstance()`），这意味着如果在多个 Activity 或并发异步任务中同时调用 `.verify()` 或 `.isRequest()`，可能会因为内部字段 `permissionList` 或 `result` 被覆盖而导致回调错乱。**请确保同一时刻只在一个页面中进行校验操作**。如果是频繁调用的场景，建议在 `onDestroy` 或回调完成后清理状态。
2. **Activity 存活检查**：`with(activity)` 传入的 Activity 必须处于存活状态，如果在权限申请过程中 Activity 被销毁，框架底层虽然做了防护（`GlobalAppUtil.isRunning`），但回调可能无法正常执行。
3. **Notification 通知权限**：Android 13 以下的通知权限实际上是“无弹窗”的，框架底层已经处理（直接检查 `NotificationManager.areNotificationsEnabled()`），无需特殊处理。