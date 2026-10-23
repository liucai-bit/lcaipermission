# LcaiAppCore 使用文档

> 适用版本：`GlobalAppUtil` + `LcaiActivityManager`
> 适用对象：Android 业务开发、基础框架维护者

---

## 一、概述

框架核心由两个类组成，职责严格分离：

| 类 | 角色 | 职责 |
|---|---|---|
| **`LcaiActivityManager`** | 状态持有者（唯一真相源） | 维护 Activity 栈、当前前台宿主、Loading 宿主、页面跳转、退出 App |
| **`GlobalAppUtil`** | 工具门面 + Application 持有 | 提供 Application/Context、全局 KV、尺寸/设备/网络/包信息、UI 小工具 |

**核心约定**：所有"当前处于前台的 Activity"相关能力，都从 `LcaiActivityManager` 获取，`GlobalAppUtil.getActivity()` 只是转发，二者数据一致。

```
┌─────────────────────────────────────────────┐
│ LcaiActivityManager（状态）                  │
│  activityList / mResumedActivity            │
│  isAppForeground / Loading / finishAll      │
└──────────────┬──────────────────────────────┘
               │ 委托
┌──────────────▼──────────────────────────────┐
│ GlobalAppUtil（工具门面）                    │
│  getActivity() / isForeground() + 各类工具   │
└─────────────────────────────────────────────┘
```

---

## 二、初始化

在 `Application.onCreate()` 中调用**一次**：

```java
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GlobalAppUtil.init(this);
    }
}
```

`init()` 内部会完成：

1. 保存 `Application` 实例；
2. 缓存外部缓存目录；
3. 根据 `FLAG_DEBUGGABLE` 自动设置 `isDebug()`；
4. 初始化 `LcaiPreferenceUtils`；
5. 初始化 `LcaiActivityManager`（注册 Activity 生命周期回调）。

**未初始化时**调用 `getApplication()` / `getApplicationContext()` 会抛 `IllegalStateException`，便于尽早暴露问题。

> 多进程场景：每个进程都会执行 `Application.onCreate()`，`init()` 天然幂等，无需额外处理。

---

## 三、快速开始

```java
// 显示 Loading
LcaiActivityManager.getInstance().showLoading(new MyLoadingDialog());
// ……网络请求……
LcaiActivityManager.getInstance().dismissLoading();

// 页面跳转
LcaiActivityManager.getInstance().startActivity(this, DetailActivity.class);

// 弹 Toast
GlobalAppUtil.toast("保存成功");

// 判断前台
if (GlobalAppUtil.isBackground()) {
    // 后台时不弹框，改为发通知
}
```

---

## 四、GlobalAppUtil 使用说明

### 4.1 上下文获取

| 方法 | 说明 |
|---|---|
| `getApplication()` | 返回 `Application`，未初始化抛异常 |
| `getApplicationContext()` | 返回 Application Context，**推荐**作为长生命周期 Context |
| `getActivity()` | 当前 resumed 的 Activity，可能为 `null` |

```java
Context ctx = GlobalAppUtil.getApplicationContext(); // 不要持有 Activity 引用
```

> **不要**用 `getActivity()` 做网络请求的 Context，它随时可能为 `null`。它只适合"此刻需要弹 UI"的场景。

### 4.2 前后台判断

```java
if (GlobalAppUtil.isForeground()) {
    // 有可见 Activity
}
if (GlobalAppUtil.isBackground()) {
    // 切到后台（如发通知、暂停轮询）
}
```

判断依据是 `LcaiActivityManager.isAppForeground()`，即"存在 resumed 且未销毁的 Activity"。

### 4.3 Activity 存活判断

```java
// 任意 Activity 能否继续接收 UI 操作（不要求是当前 resumed 的那个）
if (GlobalAppUtil.isRunning(activity)) {
    activity.doSomething();
}

// 是否就是当前前台 Activity
if (LcaiActivityManager.getInstance().isResumed(activity)) {
    // 弹 Dialog 安全
}
```

两者区别：

| 方法 | 要求 | 典型用途 |
|---|---|---|
| `isRunning(a)` | 未 finishing、未 destroyed | 从回调里拿到一个 Activity，判断能否操作 |
| `isResumed(a)` | 上面 + 必须等于当前 resumed | 弹 Dialog / 做动画 |

### 4.4 线程切换

```java
GlobalAppUtil.runOnUiThread(() -> textView.setText("done"));

GlobalAppUtil.runOnUiThreadDelayed(() -> hideSplash(), 1500);

GlobalAppUtil.removeUiRunnable(runnable); // 取消延迟任务

if (GlobalAppUtil.isMainThread()) { ... }
```

### 4.5 全局 KV 存储

进程内内存级存储，进程退出即消失。**不要**用它替代 SharedPreferences。

```java
GlobalAppUtil.globalSetObject("token", token);
String token = GlobalAppUtil.globalGetObject("token", "");

GlobalAppUtil.globalContainsKey("token");
GlobalAppUtil.globalRemoveObject("token");
GlobalAppUtil.globalClearObject(); // 慎用，会清空所有
```

行为说明：

- `globalSetObject(key, null)` **等价于** `globalRemoveObject(key)`；
- 内部 `ConcurrentHashMap`，任意线程安全；
- 值是引用类型，取出来的是同一对象，修改对象内容会影响存储。

### 4.6 尺寸换算

```java
int px = GlobalAppUtil.dp2px(16);
int dp = GlobalAppUtil.px2dp(48);
int sp = GlobalAppUtil.sp2px(14);

float density = GlobalAppUtil.getDensity();
```

### 4.7 屏幕信息

```java
int w = GlobalAppUtil.getScreenWidth();
int h = GlobalAppUtil.getScreenHeight();

if (GlobalAppUtil.isLandscape()) { ... }
if (GlobalAppUtil.isTablet()) { ... }     // smallestScreenWidthDp >= 600
if (GlobalAppUtil.isNightMode()) { ... }  // 深色模式
```

> 屏幕尺寸取的是 `Resources.displayMetrics`。多窗口/折叠屏场景建议业务侧改用 `WindowManager.getCurrentWindowMetrics()`。

### 4.8 状态栏 / 导航栏高度

**同步获取**（推荐，简单场景）：

```java
int statusBarH = GlobalAppUtil.getStatusBarHeight();      // 兜底 24dp
int navBarH    = GlobalAppUtil.getNavigationBarHeight();  // 兜底 48dp，横屏自动切换资源
```

**异步获取精确值**（刘海屏 / 需要 `WindowInsets`）：

```java
GlobalAppUtil.getStatusBarHeight(activity, height -> {
    // height 为 Integer
    applyPadding(height);
});

GlobalAppUtil.getNavigationBarHeight(activity, height -> {
    applyBottomPadding(height);
});
```

回调保证**恰好回调一次**：

- Activity 不在可用状态 → 立即返回资源兜底值；
- 能读到 `WindowInsets` → 返回精确值；
- 读不到 → 走 `post` + `getWindowVisibleDisplayFrame` 兜底。

> 注意：异步回调里若持有 Activity 引用，务必自行检查 `isRunning(activity)`，因为 `post` 执行时 Activity 可能已被销毁。

### 4.9 字体缩放

```java
float scale = GlobalAppUtil.getSystemFontScale(); // 默认 1.0f

GlobalAppUtil.setSystemFontScale(1.3f);
```

`setSystemFontScale` 会：

1. 写入 `LcaiPreferenceUtils`；
2. 与旧值相同则直接返回；
3. 否则 `recreate()` 当前 resumed 的 Activity。

> 栈中其它页面需要在 `attachBaseContext` 中统一 `applyOverrideConfiguration` 才能同步生效，这是 BaseActivity 的职责，不在本工具类内。

### 4.10 UI 小工具

```java
GlobalAppUtil.toast("提示");              // 自动切主线程
GlobalAppUtil.copyText("待复制内容");      // 系统剪贴板

GlobalAppUtil.hideKeyboard(activity);
GlobalAppUtil.showKeyboard(editText);

GlobalAppUtil.keepScreenOn(activity, true);  // 屏幕常亮
GlobalAppUtil.setSecure(activity, true);     // 防截屏（FLAG_SECURE）
```

所有方法内部均做了 `Activity` 存活校验与 `try/catch`，无需额外判空。

### 4.11 应用信息

```java
String pkg     = GlobalAppUtil.getPackageName();
String appName = GlobalAppUtil.getAppName();
String dataDir = GlobalAppUtil.getDataDir();

String versionName = GlobalAppUtil.getAppVersionName();   // 已缓存
int    versionCode = GlobalAppUtil.getAppVersionCode();
long   versionCodeL = GlobalAppUtil.getAppVersionCodeLong(); // 推荐

if (GlobalAppUtil.isAppInstalled("com.xxx.yyy")) { ... }
```

版本号在首次调用后缓存到静态字段，后续无 IO 开销。

### 4.12 首次启动

```java
if (GlobalAppUtil.isFirstLaunch()) {
    showGuide();
}
```

首次调用返回 `true` 并立即写入偏好，**第二次起返回 `false`**。用于引导页、隐私弹窗等场景。

### 4.13 进程信息

```java
String processName = GlobalAppUtil.getProcessName();

if (GlobalAppUtil.isMainProcess()) {
    // 主进程才初始化推送、统计等
}
```

### 4.14 设备信息

```java
GlobalAppUtil.getSystemModel();          // Build.MODEL
GlobalAppUtil.getDeviceBrand();          // Build.BRAND
GlobalAppUtil.getDeviceManufacturer();   // Build.MANUFACTURER
GlobalAppUtil.getDeviceSDK();            // SDK_INT
GlobalAppUtil.getDeviceAndroidVersion(); // RELEASE
GlobalAppUtil.getDeviceDefaultLanguage();// 如 "zh"
```

### 4.15 网络状态

```java
if (!GlobalAppUtil.isNetworkConnected()) {
    GlobalAppUtil.toast("网络不可用");
    return;
}

if (GlobalAppUtil.isWifiConnected()) { ... }
if (GlobalAppUtil.isMobileConnected()) { ... }
```

**监听网络变化**（必须成对调用）：

```java
private final ConnectivityManager.NetworkCallback callback =
        new ConnectivityManager.NetworkCallback() {
            @Override public void onAvailable(Network network) { /* 网络恢复 */ }
            @Override public void onLost(Network network)       { /* 网络断开 */ }
        };

@Override protected void onStart() {
    super.onStart();
    GlobalAppUtil.registerNetworkCallback(callback);
}

@Override protected void onStop() {
    super.onStop();
    GlobalAppUtil.unregisterNetworkCallback(callback);
}
```

> 底层使用 `registerNetworkCallback(NetworkRequest, callback)`，需要 `ACCESS_NETWORK_STATE` 权限。反复注册同一 callback 会抛异常，务必成对使用。

### 4.16 系统设置跳转

```java
GlobalAppUtil.openAppSettings(activity);          // 应用详情页（权限引导必用）
GlobalAppUtil.openNetworkSettings(activity);      // 无线网络设置
GlobalAppUtil.openNotificationSettings(activity); // 通知设置（API 26+）
```

### 4.17 内存与退出

```java
long avail = GlobalAppUtil.getAvailableMemory(); // 字节

GlobalAppUtil.exitApp();     // 结束所有 Activity + 杀进程
GlobalAppUtil.restartApp();  // 拉起新进程 + 延迟 200ms 结束当前进程
```

> 退出 App 属于产品决策，很多应用禁止主动退出。`restartApp` 延迟 200ms 是为避免部分 ROM "新进程未起就被杀"。

---

## 五、LcaiActivityManager 使用说明

### 5.1 栈查询

```java
Activity top = LcaiActivityManager.getInstance().currentActivity();
// 栈顶 Activity，不保证可见（如被 Dialog Activity 覆盖时可能不是 resumed）

List<Activity> all = LcaiActivityManager.getInstance().getAllActivities();
// 只读快照，外部修改不影响内部

boolean has = LcaiActivityManager.getInstance().hasActivity();
```

### 5.2 可见性查询（弹 UI 必用）

```java
Activity host = LcaiActivityManager.getInstance().getResumedActivity();
if (host != null) {
    new AlertDialog.Builder(host).show();
}

boolean fg = LcaiActivityManager.getInstance().isAppForeground();
boolean r  = LcaiActivityManager.getInstance().isResumed(activity);
```

**弹 Dialog / Loading / PopupWindow 请一律用 `getResumedActivity()`**，不要用 `currentActivity()`。

### 5.3 Loading 管理

定义 Loading 实现：

```java
public class LoadingDialog implements LcaiActivityManager.LoadingAction {
    private Dialog dialog;

    @Override public void show(@NonNull Activity host) {
        dialog = new Dialog(host, R.style.LoadingDialog);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
    }
}
```

使用：

```java
LoadingDialog loading = new LoadingDialog();

// 可跨线程调用
LcaiActivityManager.getInstance().showLoading(loading);

// 关闭（自动清理 pending）
LcaiActivityManager.getInstance().dismissLoading();

// 是否正在显示
LcaiActivityManager.getInstance().isLoadingShowing();
```

**Pending 机制**：若 `showLoading` 时无可用宿主（跳转空窗期、启动页等），请求会暂存，待下一个 Activity `resumed` 时自动补弹。

行为约定：

| 场景 | 行为 |
|---|---|
| `showLoading` 时无 resumed Activity | 暂存，下一个 resumed 补弹 |
| `showLoading` 时有宿主，且已有 active | 先关旧 loading，再弹新的 |
| `dismissLoading` | 同时清理 pending 和 active |
| 宿主 `onDestroy` | 自动 dismiss active，防止窗口泄漏 |
| 跨线程调用 | 内部自动切主线程 |

> **不要**用 `Loading` 承载业务成功/失败提示，它只表达"等待中"。

### 5.4 关闭 Activity

```java
// 关闭指定 Activity
LcaiActivityManager.getInstance().finishFirstActivity(activity);

// 关闭某类 Activity（cls 为 null 表示关闭全部）
LcaiActivityManager.getInstance().finishAllActivities(LoginActivity.class);

// 关闭除了某类以外的所有 Activity（如：回到首页后清栈）
LcaiActivityManager.getInstance().finishAllOtherActivities(MainActivity.class);

// 关闭所有 Activity
LcaiActivityManager.getInstance().finishAllActivities();
```

内部遍历使用快照，`finish()` 触发的 `onDestroyed` 不会引起并发修改异常。

### 5.5 页面跳转

```java
LcaiActivityManager.getInstance().startActivity(context, DetailActivity.class);

// 带参数
Bundle b = new Bundle();
b.putString("id", "123");
LcaiActivityManager.getInstance().startActivity(context, DetailActivity.class, b);
```

相比原生 `startActivity` 的增强：

- `context` 为已销毁/正在结束的 Activity 时，**直接丢弃**而非崩溃；
- `context` 非 Activity 时自动补 `FLAG_ACTIVITY_NEW_TASK`。

### 5.6 退出 App

```java
LcaiActivityManager.getInstance().exitApp();
// 结束所有 Activity 后 killProcess

// 或使用 GlobalAppUtil 版本（额外 System.exit）
GlobalAppUtil.exitApp();
```

---

## 六、典型场景

### 场景 1：网络请求前检查网络 + 弹 Loading

```java
if (!GlobalAppUtil.isNetworkConnected()) {
    GlobalAppUtil.toast("网络不可用，请检查网络");
    return;
}
LcaiActivityManager.getInstance().showLoading(loading);
api.fetch(new Callback() {
    @Override public void onSuccess(Data d) {
        LcaiActivityManager.getInstance().dismissLoading();
        bind(d);
    }
    @Override public void onError(Throwable t) {
        LcaiActivityManager.getInstance().dismissLoading();
        GlobalAppUtil.toast(t.getMessage());
    }
});
```

### 场景 2：权限引导

```java
if (!hasPermission()) {
    new AlertDialog.Builder(LcaiActivityManager.getInstance().getResumedActivity())
        .setTitle("需要相机权限")
        .setPositiveButton("去设置", (d, w) ->
                GlobalAppUtil.openAppSettings(getActivity()))
        .show();
}
```

### 场景 3：退出登录清栈

```java
LcaiActivityManager.getInstance().finishAllActivities(LoginActivity.class);
LcaiActivityManager.getInstance().startActivity(this, LoginActivity.class);
```

### 场景 4：登录页保护，回到首页清栈

```java
LcaiActivityManager.getInstance().startActivity(this, MainActivity.class);
LcaiActivityManager.getInstance().finishAllOtherActivities(MainActivity.class);
```

### 场景 5：后台推送处理

```java
public void onPushReceived(PushMsg msg) {
    if (GlobalAppUtil.isBackground()) {
        showNotification(msg);
    } else {
        new AlertDialog.Builder(getActivity()).setMessage(msg.text).show();
    }
}
```

### 场景 6：防止支付页截图

```java
@Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    GlobalAppUtil.setSecure(this, true);
}

@Override protected void onDestroy() {
    GlobalAppUtil.setSecure(this, false);
    super.onDestroy();
}
```

---

## 七、注意事项与最佳实践

### 7.1 Context 使用

- **长生命周期**（网络、缓存、单例）用 `GlobalAppUtil.getApplicationContext()`；
- **弹 UI** 用 `LcaiActivityManager.getInstance().getResumedActivity()`；
- 永远不要用 `getActivity()` 去发起异步任务并持有到回调里。

### 7.2 弹窗安全

```java
Activity host = LcaiActivityManager.getInstance().getResumedActivity();
if (host == null || host.isFinishing() || host.isDestroyed()) {
    return; // 或延迟到下次 resumed
}
```

最安全的写法就是使用 `showLoading` 的 pending 机制，把"等待下一次前台"交给框架。

### 7.3 内存泄漏红线

- `GLOBAL` 里不要存 Activity / Context；
- 网络回调、Handler Runnable 里的 Activity 引用需在 `onDestroy` 时清理；
- 组件（Dialog、PopupWindow）必须在 `LoadingAction.dismiss` 或 `onDestroy` 里释放。

### 7.4 网络监听成对调用

`registerNetworkCallback` / `unregisterNetworkCallback` 必须在同一生命周期内配对，否则会抛 `IllegalArgumentException`（重复注册）或资源泄漏。

### 7.5 版本号 API 选择

- 新代码统一用 `getAppVersionCodeLong()`；
- `getAppVersionCode()` 仅为兼容老代码保留，内部是 `(int) longVersionCode`，可能溢出。

### 7.6 屏幕高度

- `getScreenWidth/Height` 取自 `Resources`，包含状态栏/导航栏区域；
- 内容区域计算请结合 `getStatusBarHeight()` / `getNavigationBarHeight()`；
- 多窗口、折叠屏、分屏场景建议改用 `WindowManager.getCurrentWindowMetrics()`。

### 7.7 退出/重启

- 主动 `exitApp()` 在多数产品规范中不推荐；
- `restartApp()` 已加 200ms 延迟，若某 ROM 仍失败，可改为 `PendingIntent + AlarmManager` 方案。

### 7.8 多进程

- `GlobalAppUtil` 的 `GLOBAL`、版本号缓存、`isFirstLaunch` 都是**进程内**的，多进程不共享；
- `isMainProcess()` 用于区分主进程，副进程请勿重复初始化推送/统计。

### 7.9 初始化时机

- `init()` 必须在 `Application.onCreate()` 里调用，早于任何 Activity 创建；
- `LcaiPreferenceUtils.init()` 若你的框架需要传入 Application，请按实际签名调整 `GlobalAppUtil.init()` 里的调用。

---

## 八、常见问题（FAQ）

**Q1：`getActivity()` 为什么返回 `null`？**
A：说明此刻没有 `resumed` 的 Activity（启动中、跳转空窗、后台）。弹 UI 请用 `showLoading` 或先判断 `null`。

**Q2：Loading 弹不出来？**
A：检查 ① 是否在 `init()` 之前调用；② 宿主 Activity 是否 `isFinishing/isDestroyed`；③ 是否忘了 `dismissLoading` 导致下次 `showLoading` 被覆盖。

**Q3：`dismissLoading` 后 pending 也丢了？**
A：这是设计行为。pending 代表"上次没弹成功"，dismiss 代表"整体取消等待"。

**Q4：`isForeground()` 和 `isRunning()` 的区别？**
A：前者是"应用整体是否有可见界面"，后者是"某个具体 Activity 能否继续使用"。前者不接收参数。

**Q5：`getStatusBarHeight()` 在刘海屏不准？**
A：同步版读的是资源值，刘海区域不计入。需要精确值请用异步版 `getStatusBarHeight(activity, callback)`。

**Q6：`restartApp` 后进程没重启？**
A：部分 ROM 限制了后台拉起，可改用 `AlarmManager` 调度 `PendingIntent`，或在 `finishAllActivities` 后 `postDelayed` 更长时间。

**Q7：`setSystemFontScale` 后只有当前页面变了？**
A：需要在 `BaseActivity.attachBaseContext` 中统一 `applyOverrideConfiguration` 应用 `fontScale`，本工具类只负责偏好存储与当前 Activity 重建。

---

## 九、API 索引

### GlobalAppUtil

| 分类 | 方法 |
|---|---|
| 上下文 | `getApplication` `getApplicationContext` `getActivity` |
| 前后台 | `isForeground` `isBackground` `isRunning` |
| 线程 | `isMainThread` `runOnUiThread` `runOnUiThreadDelayed` `removeUiRunnable` |
| 目录 | `getCacheFile` `getCacheFile2` `getInternalCacheDir` `getFilesDir` |
| 开关 | `isDebug` `setIsDebug` `isSaveLog` `setSaveLog` |
| KV | `globalSetObject` `globalGetObject` `globalContainsKey` `globalRemoveObject` `globalClearObject` |
| 尺寸 | `getDensity` `dp2px` `px2dp` `sp2px` |
| 屏幕 | `getScreenWidth` `getScreenHeight` `isLandscape` `isTablet` `isNightMode` |
| 高度 | `getStatusBarHeight()` `getStatusBarHeight(Activity, cb)` `getNavigationBarHeight()` `getNavigationBarHeight(Activity, cb)` |
| 字体 | `getSystemFontScale` `setSystemFontScale` |
| UI | `toast` `copyText` `hideKeyboard` `showKeyboard` `keepScreenOn` `setSecure` |
| 应用 | `getPackageName` `getAppName` `getDataDir` `getAppVersionName` `getAppVersionCode` `getAppVersionCodeLong` `isAppInstalled` `isFirstLaunch` |
| 进程 | `getProcessName` `isMainProcess` |
| 设备 | `getSystemModel` `getDeviceBrand` `getDeviceManufacturer` `getDeviceSDK` `getDeviceAndroidVersion` `getDeviceDefaultLanguage` 等 |
| 网络 | `isNetworkConnected` `isWifiConnected` `isMobileConnected` `registerNetworkCallback` `unregisterNetworkCallback` |
| 设置 | `openAppSettings` `openNetworkSettings` `openNotificationSettings` |
| 其他 | `isEmulator` `getAvailableMemory` `exitApp` `restartApp` |

### LcaiActivityManager

| 分类 | 方法 |
|---|---|
| 栈 | `currentActivity` `getAllActivities` `hasActivity` |
| 可见性 | `getResumedActivity` `isResumed` `isAppForeground` |
| Loading | `showLoading` `dismissLoading` `isLoadingShowing` |
| 关闭 | `finishFirstActivity` `finishAllActivities()` `finishAllActivities(Class)` `finishAllOtherActivities` |
| 跳转 | `startActivity(Context, Class)` `startActivity(Context, Class, Bundle)` |
| 退出 | `exitApp` |

---

*文档随代码同步维护。若发现描述与实现不一致，以代码为准并提 issue 修正。*