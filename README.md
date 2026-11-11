---

# 1. core 模块 (核心基础库)

### 1.1 `LcaiManager`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `getInstance()` | 无 | `LcaiManager` | 获取单例。 |
| `permissionReq` | `LcaiPermissionRequestBulider` | `void` | 发起权限申请，内部调用 `LcaiPermissionRequest`。 |
| `openPhotoOrCamera` | `LcaiCameraPhotoBuilder` | `void` | 发起拍照/选图，内部调用 `LcaiCameraPhoto`。 |
| `clearPermissionReferences()` | 无 | `void` | 清空当前权限申请的 builder 引用。 |
| `clearPhotoReferences()` | 无 | `void` | 清空当前拍照选图的 builder 引用。 |
| `Internal.getPermissionResult()` | 无 | `LcaiPermissionActivityResult` | 获取权限申请回调。 |
| `Internal.getPhotoCameraResult()` | 无 | `LcaiGalleryResult` | 获取拍照相册回调。 |

### 1.2 `GlobalAppUtil`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `init` | `Application` | `void` | 初始化全局 Application。 |
| `getApplication()` | 无 | `Application` | 获取 Application。 |
| `getApplicationContext()` | 无 | `Context` | 获取 Application Context。 |
| `getActivity()` | 无 | `Activity` | 获取当前栈顶 Activity。 |
| `isRunning` | `Activity` | `boolean` | Activity 是否存活。 |
| `isForeground()` / `isBackground()` | 无 | `boolean` | 应用是否在前台/后台。 |
| `isMainThread()` | 无 | `boolean` | 是否在主线程。 |
| `runOnUiThread` / `runOnUiThreadDelayed` | `Runnable`, `long` | `void` | 切换到主线程执行。 |
| `removeUiRunnable` | `Runnable` | `void` | 移除主线程任务。 |
| `getCacheFile()` / `getCacheFile2()` | 无 | `File` | 获取缓存目录。 |
| `getInternalCacheDir()` / `getFilesDir()` | 无 | `File` | 获取内部缓存/文件目录。 |
| `setSaveLog` / `isSaveLog` | `boolean` | `void`/`boolean` | 设置/获取日志保存开关。 |
| `setIsDebug` / `isDebug` | `boolean` | `void`/`boolean` | 设置/获取调试模式。 |
| `globalSetObject` / `globalGetObject` | `String`, `Object` | `void`/`T` | 全局运行时 Map 存取。 |
| `globalContainsKey` / `globalRemoveObject` / `globalClearObject` | `String` | `boolean`/`void` | 全局 Map 查/删/清空。 |
| `getDensity()` / `dp2px()` / `px2dp()` / `sp2px()` | `float` | `float`/`int` | 屏幕密度与单位换算。 |
| `getScreenWidth()` / `getScreenHeight()` | 无 | `int` | 屏幕宽高。 |
| `isLandscape()` / `isTablet()` / `isNightMode()` | 无 | `boolean` | 横屏/平板/夜间模式判断。 |
| `getStatusBarHeight` / `getNavigationBarHeight` | 无 / `Activity`, `ComponentInterface` | `int`/`void` | 获取状态栏/导航栏高度。 |
| `getSystemFontScale` / `setSystemFontScale` | 无 / `float` | `float`/`void` | 获取/设置字体缩放。 |
| `toast` | `CharSequence` | `void` | 主线程弹 Toast。 |
| `copyText` | `CharSequence` | `void` | 复制文本。 |
| `hideKeyboard` / `showKeyboard` | `Activity` / `View` | `void` | 隐藏/显示软键盘。 |
| `resolveString` / `resolveColor` / `resolveDrawable` | `Context`, `int` | `String`/`int`/`Drawable` | 安全获取资源。 |
| `keepScreenOn` / `setSecure` | `Activity`, `boolean` | `void` | 屏幕常亮 / 防截屏。 |
| `getPackageName` / `getAppName` / `getDataDir` | 无 | `String` | 获取应用信息。 |
| `getAppVersionName` / `getAppVersionCode` | 无 | `String`/`int`/`long` | 获取版本号。 |
| `isAppInstalled` | `String` | `boolean` | 是否安装指定应用。 |
| `isFirstLaunch()` | 无 | `boolean` | 是否首次启动。 |
| `getProcessName()` / `isMainProcess()` | 无 | `String`/`boolean` | 进程名 / 是否主进程。 |
| `getSystemModel` / `getDeviceBrand` 等 | 无 | `String` | 获取各种设备信息（Model, Brand, Manufacturer, Product...）。 |
| `getDeviceSDK` / `getDeviceAndroidVersion` / `getDeviceDefaultLanguage` | 无 | `int`/`String` | SDK版本 / 系统版本 / 默认语言。 |
| `isNetworkConnected` / `isWifiConnected` / `isMobileConnected` | 无 | `boolean` | 网络状态判断。 |
| `registerNetworkCallback` / `unregisterNetworkCallback` | `NetworkCallback` | `void` | 注册/注销网络监听。 |
| `isEmulator()` | 无 | `boolean` | 是否模拟器。 |
| `openAppSettings` / `openNetworkSettings` / `openNotificationSettings` | `Activity` | `void` | 跳转各种系统设置页。 |
| `getAvailableMemory()` | 无 | `long` | 获取可用内存。 |
| `exitApp()` / `restartApp()` | 无 | `void` | 退出 / 重启应用。 |

### 1.3 `LcaiActivityManager`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `init` | `Application` | `void` | 注册生命周期监听。 |
| `currentActivity()` | 无 | `Activity` | 获取栈顶 Activity（哪怕是 Paused）。 |
| `getAllActivities()` | 无 | `List<Activity>` | 获取所有 Activity。 |
| `hasActivity()` | 无 | `boolean` | 栈是否为空。 |
| `getResumedActivity()` | 无 | `Activity` | 获取可见 Activity。 |
| `isResumed` | `Activity` | `boolean` | 传入的 Activity 是否 Resumed。 |
| `isAppForeground()` | 无 | `boolean` | 应用是否在前台。 |
| `showLoading` / `dismissLoading` | `LoadingAction` | `void` | 安全的 Loading 控制。 |
| `isLoadingShowing()` | 无 | `boolean` | Loading 是否展示中。 |
| `finishFirstActivity` | `Activity` | `void` | 安全结束 Activity。 |
| `finishAllActivities` / `finishAllOtherActivities` | `Class<?>` | `void` | 结束所有/其他 Activity。 |
| `exitApp()` | 无 | `void` | 结束所有 Activity 并杀进程。 |
| `startActivity` | `Context`, `Class<?>`, `Bundle` | `void` | 安全的 Activity 跳转。 |

### 1.4 `LcaiFileUtils`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `createImageFile` / `createCameraImageFile` | `Activity` | `File` | 创建临时图片文件。 |
| `clearFile` | `File` | `void` | 删除文件。 |
| `converImagePathToBase64` / `convertImageToBase64` / `cameraImageToBase64` | `String`/`Context, Uri, boolean`/`Intent, boolean` | `JSONObject` | 图片转 Base64（带压缩）。 |
| `bitmapToBase64` | `Bitmap`, `boolean` | `String` | Bitmap 转 Base64。 |
| `getPath` / `uriToCacheFile` | `Context`, `Uri` | `String`/`File` | Uri 转路径/沙盒文件。 |
| `saveImageToGallery` | `Context`, `File`, `LcaiFileInterface` | `void` | 保存至相册。 |
| `isExternalStorageDocument` 等 | `Uri` | `boolean` | 判断 Uri 类型。 |

*(注：`LcaiLogUtils`、`TextUtils`、`CommonUtils` 等极简工具类方法在此略过，因功能一目了然)*

---

# 2. permission 模块 (动态权限)

### 2.1 `LcaiPermissionRequestBulider`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `with` | `Activity` | `Builder` | 设置上下文。 |
| `check` | `boolean` | `Builder` | 仅检查不申请。 |
| `addNotification` | 无 | `Builder` | 添加通知权限（13+适配）。 |
| `showDialog` / `addDialogBulider` | 无 / `LcaiTipsDialogBuilder` | `Builder` | 配置申请前弹窗。 |
| `addPermission` | `String` / `String...` | `Builder` | 添加权限。 |
| `toSystem` / `addToSystemBuilder` | 无 / `LcaiTipsDialogBuilder` | `Builder` | 永久拒绝时跳转系统设置的配置。 |
| `addResult` | `LcaiReqPermissionResult` | `Builder` | 设置回调。 |

### 2.2 `LcaiPermissionUtils`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `with` / `addResult` / `addVerifyPermission` | `Activity` / `LcaiPermissionResult` / `String...` | `LcaiPermissionUtils` | 链式配置。 |
| `verify()` | 无 | `void` | 校验权限状态，回调 `PermissionsState`。 |
| `isRequest()` | 无 | `void` | 查询历史申请记录，回调 `LcaiPermissionEntity` 列表。 |

### 2.3 `LcaiReqPermissionResult` (接口)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `onReqPermissionPass()` | 无 | `void` | 全部授权通过。 |
| `onReqPermissionNoPass` | `Map<String, Boolean>` | `void` | 有权限未通过（Map<权限名, 是否永久拒绝>）。 |

*(其余 `LcaiPermissionString` 为常量接口，`PermissionsState` 为枚举类，`LcaiPermissionEntity` 为实体类)*

---

# 3. camera_photo 模块 (相机与相册)

### 3.1 `LcaiCameraPhotoBuilder`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `addCheck` | `LcaiGalleryCheckType` | `Builder` | `CAMERA`(拍照) 或 `PHOTO`(相册)。 |
| `addResult` | `LcaiGalleryResultType` | `Builder` | `URL` 或 `BASE64`。 |
| `setAddHeader` / `addHeader` | `boolean` / 无 | `Builder` | Base64 是否带前缀。 |
| `setSave` / `save` | `boolean` / 无 | `Builder` | 是否存入相册。 |
| `setResult` | `LcaiGalleryResult` | `Builder` | 设置回调。 |

### 3.2 `LcaiGalleryResult` (接口)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `onUrl` | `String` | `void` | 成功回调（JSON：文件路径、大小等）。 |
| `onBase64` | `String` | `void` | 成功回调（JSON：Base64内容）。 |
| `onCancel()` | 无 | `void` | 取消回调。 |
| `onError` | `String` | `void` | 错误回调。 |

### 3.3 `LcaiCamearPhtoActivity` (内部透明页)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `onCreate` | `Bundle` | `void` | 注册 `ActivityResultLauncher` 并开启流程。 |
| `launchPicker()` | 无 | `void` | 启动系统相册或相机。 |
| `photoResult` / `cameraResult` | `Uri` / `boolean` | `void` | 处理结果（转 Base64 / 获取路径）。 |
| `sendUrl` / `sendBase64` / `sendError` / `sendCancel` | `String` | `void` | 通过 `LcaiManager` 分发回调。 |

---

# 4. http 模块 (网络请求)

### 4.1 `LcaiHttpRequestBulider`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `setUrl` / `setMethod` | `String` / `LcaiHttpMethod` | `Builder` | 设置地址和方法。 |
| `addHeader` / `addHeaders` | `String, Object` / `Map` | `Builder` | 添加请求头。 |
| `addMapParam` / `addMapParams` | `String, Object` / `Map` | `Builder` | 添加 Map 参数。 |
| `addFileParam` / `addFileParams` | `String, File` / `Map` | `Builder` | 添加文件参数。 |
| `addJsonParams` | `String` | `Builder` | 添加 JSON 字符串。 |
| `setConnectTimeout` / `setReadTimeout` | `int` | `Builder` | 设置超时（默认10s/30s）。 |
| `setHttpCall` | `LcaiHttpCall` | `Builder` | `JSON` / `RESPONSE` / `DOWNLOAD`。 |
| `setHttpSend` | `LcaiHttpSend` | `Builder` | `JSON` / `MAP` / `FORM_DATA`。 |
| `setEncode` | `String` | `Builder` | 设置编码（默认 UTF-8）。 |
| `backXML` / `setQName` | 无 / `String` | `Builder` | 开启 XML 解析。 |
| `setSavePath` / `setFileName` | `String` | `Builder` | 文件下载配置。 |
| `withHeader()` | 无 | `Builder` | 回调带回 Response Header。 |
| `setResponseResult` | `LcaiBaseResponseResult` | `Builder` | 设置回调。 |
| `execute()` | 无 | `LcaiHttpRequest` | 执行请求。 |

### 4.2 `LcaiBaseResponseResult` (接口)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `onFailure` | `int`, `String` | `void` | 失败回调。 |
| `onSuccess` | `String` / `Map, String` | `void` | 字符串结果。 |
| `onSuccess` | `InputStream` / `Map, InputStream` | `void` | 流结果。 |
| `onProgress` | `int`, `boolean` | `void` | 下载进度。 |
| `isCancelled()` | 无 | `boolean` | 是否取消请求。 |

### 4.3 `GlobalThreadPool`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `getInstance()` | 无 | `ThreadPoolExecutor` | 获取线程池。 |
| `execute` / `submit` | `Runnable` | `void`/`Future<?>` | 提交任务。 |
| `remove` | `Runnable` | `boolean` | 移除任务。 |
| `shutdown` / `shutdownNow` | 无 | `void` | 关闭线程池。 |

---

# 5. eventBus 模块 (事件总线)

### 5.1 `LcaiEventBus`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `getInstance()` | 无 | `LcaiEventBus` | 获取单例。 |
| `register` | `String eventId`, `Object subscriber` | `void` | 注册订阅（反射遍历父类）。 |
| `unRegister` | `Object subscriber` | `void` | 取消订阅（遍历清理）。 |
| `post` | `MessageEvent` | `void` | 发送普通事件（遍历时清理 GC 对象）。 |
| `postSticky` / `removeStickyEvent` | `MessageEvent` / `String` | `void` | 发送/移除粘性事件。 |
| `purgeDeadSubscriptions()` | 无 | `void` | 主动清理失效订阅。 |
| `clearAll()` | 无 | `void` | 清空所有订阅和粘性事件。 |

### 5.2 `MessageEvent` / `Subscribe` / `ThreadMode`
*   `MessageEvent`: 包含 `msgId` 与 `msgContent` 属性。
*   `Subscribe`: 注解，方法必须仅带一个 `String` 参数，可指定 `mode()`。
*   `ThreadMode`: 枚举 `POSTING` (当前线程) / `MAIN` (主线程)。

---

# 6. image 模块 (图片处理)

### 6.1 `ImageUtils` (Glide封装)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `loadImage` | `Context, Object, ImageView` / `+ 占位图` / `+ RequestOptions` | `void` | 基础加载。 |
| `loadCircleImage` | `Context, Object, ImageView` | `void` | 圆形加载。 |
| `loadRoundImage` | `Context, Object, ImageView, radiusDp` | `void` | 圆角加载。 |
| `loadGif` | `Context, Object, ImageView` | `void` | 加载 GIF。 |
| `loadBitmap` | `Context, Object, LcaiImageCallback` | `void` | 获取 Bitmap。 |
| `preload` | `Context, Object` | `void` | 预加载。 |
| `clear` | `ImageView` | `void` | 清除 ImageView 请求。 |
| `clearMemoryCache` / `clearDiskCache` | `Context` | `void` | 清理缓存。 |
| `pauseRequests` / `resumeRequests` | `Context` | `void` | 暂停/恢复请求。 |
| `saveToPrivateDir` | `Context, Bitmap, fileName` | `File` | 存至私有目录。 |
| `saveToGallery` | `Context, Bitmap, fileName, folder` | `String` | 存至相册。 |
| `split` | `Bitmap, Rect, rows, cols, LcaiShape` | `List<Bitmap>` | 异形裁剪+切分。 |

### 6.2 `LcaiImageCore` (纯算法)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `read` | `String` / `InputStream` | `Bitmap` | 读取图片。 |
| `write` | `Bitmap, File` | `void` | 写入图片。 |
| `saveToGallery` | `Context, Bitmap, fileName` | `String` | 存相册。 |
| `crop` / `cropCenterSquare` / `cropToAspectRatio` | `Bitmap, ...` | `Bitmap` | 裁剪。 |
| `split4` / `split6` / `split9` / `splitGrid` | `Bitmap`, `rows`, `cols` | `List<Bitmap>` | 网格切分。 |
| `flip` / `flipHorizontal` / `flipVertical` | `Bitmap`, `FlipMode` | `Bitmap` | 翻转。 |
| `rotate` / `rotateRight` / `rotateLeft` | `Bitmap`, `float` | `Bitmap` | 旋转。 |

### 6.3 `LcaiShapePathFactory`
*   `build`: `Path, LcaiShape, ox, oy, w, h`。支持心形、五角星、六边形等生成。
* 
# 7. jsbridge 模块 (Hybrid 通信与 WebView 容器)

### 7.1 `WebActivity` (WebView 容器页)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `getLayout()` | 无 | `int` | 返回布局 ID，默认 `web_activity_layout`。 |
| `findWebView()` | 无 | `LcaiBridgeWebview` | 查找 WebView 实例（子类可重写换 ID）。 |
| `findTitleView()` / `findBackView()` | 无 | `View` | 查找标题/返回按钮。 |
| `initView()` | 无 | `void` | 页面初始化入口（解析配置、初始化组件、加载 URL）。 |
| `prepareConfig()` | 无 | `boolean` | 校验传入的 Config 是否有效。 |
| `setupWebView` / `createChromeClient` / `setupCallbackAdapter` / `setupJsBridge` / `registerJavascriptInterfaces` / `setupTitle` / `setupBackButton` | 无 | `void` | 各种初始化子步骤，子类可重写。 |
| `onBeforeLoadUrl` | `String url` | `String` | 加载 URL 前的钩子，可修改 URL。 |
| `onWebViewReady` / `onPageLoaded` | `LcaiBridgeWebview` / `String` | `void` | 生命周期钩子。 |
| `verifyMethod` | `LcaiCallbackFunction`, `String`, `String` | `void` | 触发 JS 方法回调。 |
| `safeFinish()` / `destroyWebView()` | 无 | `void` | 安全关闭页面 / 销毁 WebView。 |
| `getWebView()` / `getConfig()` | 无 | `LcaiBridgeWebview` / `WebActivityConfig` | 获取实例。 |

### 7.2 `WebActivityConfig` (页面配置类)
| 方法名 / 属性 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `Builder` 内部类 | 无 | `Builder` | 提供 `url()`, `title()`, `methods()`, `permissions()`, `showTitleBar()`, `showBackButton()`, `callback()` 等链式配置。 |
| `getUrl` / `getTitle` / `getMethodArrays` / `getPermissionArray` | 无 | `String` / `String[]` | 获取各项配置。 |
| `isShowTitleBar` / `isShowBackButton` | 无 | `boolean` | 是否显示标题栏 / 返回键。 |
| `getCallback` / `getPageListener` | 无 | `WebActivityCallback` / `OnPageListener` | 获取回调。 |
| `isValid()` / `clear()` | 无 | `boolean` / `void` | 校验 / 清理内存引用。 |
| `OnPageListener` 接口 | 无 | 无 | 监听页面加载，含 `onPageStarted`, `onPageFinished`, `onProgressChanged` 等。 |

### 7.3 `LcaiWebActivityRouter`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `start` | `Context`, `WebActivityConfig` / `Context`, `Class`, `Config` / `Context`, `String`, `String`, `Callback` / `Context`, `Builder` | `void` | 提供四种重载的页面启动方式。 |

### 7.4 `LcaiBridgeWebview` (核心 WebView)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `setDefaultHandler` | `LcaiBridgeHandler` | `void` | 设置默认处理器。 |
| `setPrintLog` / `getPrintLog` | `boolean` | `void` / `boolean` | 打印 JS 交互日志。 |
| `isBridgeInjected` / `setBridgeInjected` | `boolean` | `boolean` / `void` | JS Bridge 是否已注入。 |
| `flushStartupMessages()` | 无 | `void` | 发送暂存的原生调用消息。 |
| `send` | `String` / `String, LcaiCallbackFunction` | `void` | 向 JS 发送数据。 |
| `registerHandler` | `String`, `LcaiBridgeHandler` | `void` | 注册供 JS 调用的 Native 方法。 |
| `callHandler` | `String`, `String`, `LcaiCallbackFunction` | `void` | 调用 JS 方法。 |

### 7.5 `LcaiBridgeUtil` (工具类)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `parseFunctionName` / `getDataFromReturnUrl` / `getFunctionFromReturnUrl` | `String` | `String` | 解析 URL 和回调数据。 |
| `webViewLoadJs` / `webViewLoadLocalJs` | `WebView`, `String` | `void` | 注入网络/本地 JS。 |
| `assetFile2Str` | `Context`, `String` | `String` | 读取 assets 文件（去 BOM）。 |

### 7.6 其他核心类
*   **`LcaiBridgeWebViewClient`**: `shouldOverrideUrlLoading` 拦截 `yy://` 协议，`onPageFinished` 检测并注入 JS。
*   **`LcaiWebViewInitializer`**: `applyDefaults(webView)` 统一开启 DomStorage、JS。
*   **`LcaiWebViewLifecycle`**: `safeDestroy(webView)` 安全销毁。
*   **`LcaiJsBridgeDispatcher`**: `register(String[])` 批量注册方法，`unregisterAll()` 清空。
*   **`LcaiPermissionBridge`**: `handle(Activity, PermissionRequest, String[], Callback)`，桥接 H5 与原生的权限申请。

---

# 8. lcaidb 模块 (仿 MyBatis-Plus 的 SQLite 数据库)

### 8.1 `LcaidbCore<T>` (业务操作门面)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `createTableForClass` | 无 | `void` | 自动建表。 |
| `insert` / `batchInsert` | `T` / `List<T>` | `boolean` | 插入单条 / 批量插入。 |
| `deleteById` | `Collection<Serializable>` | `boolean` | 根据 ID 集合删除。 |
| `deleteByParams` / `deleteByEntity` | `Map<String, Object>` / `T` | `boolean` | 根据条件 / 实体删除。 |
| `updateById` | `T` | `boolean` | 根据 ID 更新（不更新 null 字段）。 |
| `updateByParams` | `Map`, `Map` | `boolean` | 根据条件更新指定字段。 |
| `query` | 无 | `List<T>` | 查询全部。 |
| `queryById` | `T` | `T` | 根据 ID 查询单条。 |
| `queryByParams` | `Map<String, Object>` | `List<T>` | 条件查询。 |
| `queryPage` / `queryPageByParams` | `size`, `current`, `order`, `orderType`, `condition` | `List<T>` | 分页查询。 |
| `queryCount` | 无 | `long` | 统计总条数。 |

### 8.2 `LcaidbHelper` (数据库初始化)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `init` | `Context`, `String` | `void` | 必须调用初始化 DB 名。 |
| `getInstance()` | 无 | `LcaidbHelper` | 获取单例。 |

### 8.3 核心 SQL 生成器
| 类名 | 核心方法 | 功能说明 |
| :--- | :--- | :--- |
| `Create<T>` | `createTable()` | 反射解析字段与注解，生成 `CREATE TABLE`。 |
| `Query<T>` | `getEntity()`, `queryList()`, `queryByParams()`, `queryByPage()`, `queryCount()` | 生成查询语句，将 Cursor 映射回实体。 |
| `Update<T>` | `updateById()`, `updateByParams()` | 生成更新语句，过滤 null 值。 |
| `Delete<T>` | `deleteById()`, `deleteByParams()`, `deleteByEntity()` | 生成删除语句。 |
| `BaseCRUD<T>` | `wrap()` | **核心**：防 SQL 注入转义（单引号转双）。 |

*(注：`LcaiPage` 提供分页计算，`OrderTypeEnum` 提供 ASC/DESC，`LcaiSql` 提供 SQL 模板常量)*

---

# 9. tipsdialog 模块 (通用弹窗组件)

### 9.1 `LcaiTipsDialogBuilder` (构建器)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `setMode` | `LcaiTipsMode` | `Builder` | 设置 7 种模式之一。 |
| `setCustomLayout` | `int` | `Builder` | 自定义 View 布局。 |
| `setPopupBg` / `setPopupBgRes` | `Drawable` / `int` | `Builder` | 弹窗背景。 |
| `setTitle` / `setTitleColor` / `setTitleSize` | `String` / `int` / `int` | `Builder` | 标题配置。 |
| `setAdapter` / `setListVertical` / `setListCloumns` | `T` / `boolean` / `int` | `Builder` | 列表模式配置。 |
| `setContent` / `setContentColor` / `setContentSize` / `setContentClickDismiss` / `setContentGravity` | `String` / `int` / `int` / `boolean` / `int` | `Builder` | 内容配置。 |
| `setSegDisplay` / `setModuleList` / `addModule` | `boolean` / `List` / `SegDisplayModule` | `Builder` | 富文本分段点击配置。 |
| `setCancelText` / `setCancelColor` / `setCancelSize` / `setCancelBg` | ... | `Builder` | 取消按钮配置。 |
| `setConfirmText` / `setConfirmColor` / `setConfirmSize` / `setConfirmBg` | ... | `Builder` | 确认按钮配置。 |
| `setOutSideCancal` | `boolean` | `Builder` | 是否允许点击外部关闭。 |
| `setOnTipsDialogInterface` | `OnTipsDialogInterface` | `Builder` | 设置回调。 |
| `setContext` | `Context` | `Builder` | 显示指定宿主（默认自动取栈顶）。 |
| `showOrThrow()` | 无 | `void` | 展示弹窗（宿主无效会抛异常，适合调试）。 |
| `build()` | 无 | `void` | 展示弹窗（宿主无效静默失败，适合生产）。 |
| `buildAsync` | `Runnable` | `void` | 自动显示 Loading 执行异步任务并回调。 |
| `dismiss()` / `isShowing()` / `release()` | 无 | `void` / `boolean` | 关闭 / 状态 / 释放引用。 |

### 9.2 `LcaiTipsDialog` (内部实现类)
*   继承自 `Dialog`，根据 `LcaiTipsMode` 加载不同 Layout 并绑定数据（`bindDefaultMode`, `bindImageMode`, `bindRichTextMode`, `bindListMode`, `bindLoadingMode` 等）。

### 9.3 `OnTipsDialogInterface` (回调接口)
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `onCancelListener` / `onConfirmListener` | 无 | `void` | 取消 / 确认回调。 |
| `onContentListener` | `String` | `void` | 富文本片段点击回调。 |
| `onBindView` | `View`, `Builder` | `void` | 自定义模式绑定 View。 |
| `onBuildAsync` | `Builder`, `Throwable` | `void` | 异步任务结束回调。 |

### 9.4 `SegDisplayModule` (富文本片段实体)
*   `setText` / `setTextRes`：设置文本。
*   `isClickEnabel`：开启可点击。
*   `setClickIndex`：设置标识。
*   `setTextColor` / `setTextColorRes`：颜色设置。

---

# 10. component 模块 (通用 UI 组件库)

### 10.1 基础 View 体系
*   **`BaseLinearLayout` / `BaseRelativeLayout` / `BaseFlexboxLayout` / `BaseRecyclerView` / `BaseTextView`**:
    *   抽象方法：`init()`（初始化变量）、`initView()`（addView）。
    *   提供 `setAttrs()` 供子类返回自定义属性。
    *   统一管理 `TypedArray` 的获取与 `recycle()`。

### 10.2 列表体系
**`BaseRecycleAdapter<VH, T>`**
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `onBindView` | `int`, `View`, `VH`, `T` | `void` | **必须实现**，绑定数据。 |
| `setData` / `appendData` | `List<T>` / `JSONArray` | `void` | 设置/追加数据。 |
| `setLoadState` | `int` | `void` | 底部状态（LOADING, END, ERROR）。 |
| `openMultiMode` / `loadOver` | 无 | `boolean` | 是否多布局 / 是否加载完毕。 |
| `setClickListener` | `ItemClickListener` | `void` | 设置点击/加载更多监听。 |
| `getLineManager` / `getGridManager` / `getHorManager` | `int` | `LayoutManager` | 快捷布局管理器。 |

**`BaseViewHolder`**
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `getView` / `requireView` | `int` | `<T extends View>` | 获取控件（查 SparseArray 缓存）。 |
| `setText` / `setTextRes` / `setTextColor` / `setTextSize` | `int`, ... | `BaseViewHolder` | 文本相关设置。 |
| `setVisibility` / `setEnabled` / `setSelected` | `int`, `boolean` | `BaseViewHolder` | 状态设置。 |
| `setImageResource` / `setBackgroundColor` / `setBackground` | `int`, ... | `BaseViewHolder` | 背景和图片设置。 |
| `setOnClickListener` / `setOnLongClickListener` | `int`, `Listener` | `BaseViewHolder` | 事件设置。 |

### 10.3 核心高级 UI 组件
*   **`LcaiRecyclerView`**: 支持功能开关与 UI 开关分离。
    *   `stopRefresh()`, `isRefreshing()`：刷新控制。
    *   `setTypeListener()`：监听加载/刷新动作。
    *   `setRefreshHeader()` / `setShowRefreshHeader()` / `setShowLoadMoreFooter()`：UI 控制。
*   **`ViewFlipperCarousel`**: 轮播图。
    *   `setDatas()`, `setFlipperMode()` (文字/图片/自定义 View)。
    *   `setAutoPlay()`, `setSpeed()`, `setIndicatePoint()`, `setPointStyle()`。
*   **`BaseViewFlipper`**: 手势翻页容器（跟手阻尼）。
*   **`BadgeView`**: 角标。
    *   `setEadge(int)`, `setMaxNumber(int)`, `setMax(boolean)`。
*   **`BusinessNodeStatusBar`**: 业务节点网格展示。
    *   `setTitle()`, `setDatas()`, `setColumns()`, `setStatusItemConfig()`。
*   **`HorizonalLabelBar`**: Flexbox 流式标签。
    *   `setDatas()`, `setLabelWrap/Direction/Align()`, `setLabelPadding/Margin()`。
*   **`HorizonalBreadBar`**: 横向面包屑。
*   **`HorizontalNavigationBar`**: 横向导航（RecyclerView实现）。
*   **`LcaiCropShapeView`**: 异形裁剪遮罩。
    *   `setImage()`, `setCrop()`, `setGrid()`, `setShape()`。
*   **`GridImageView`**: 九宫格参考线 ImageView。

---

# 11. preference 模块 (键值存储)

### `LcaiPreferenceUtils`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `init()` | 无 | `void` | **必须调用**，初始化 SharedPreferences。 |
| `isInit()` | 无 | `boolean` | 是否已初始化。 |
| `put` | `String`, `Object` | `void` | 写入数据（异步 apply），支持 String/Boolean/Int/Long/Float/Set。 |
| `commit()` | 无 | `boolean` | 同步提交。 |
| `getString` / `getBoolean` / `getInt` / `getLong` / `getFloat` / `getStringSet` | `String`, `默认值` | 对应类型 | 读取数据。 |
| `putDouble` / `getDouble` | `String`, `double` | `void` / `double` | Double 存储（转 Long bits 防精度丢失）。 |
| `hasKey` / `removeKey` / `clear` | `String` | `boolean` / `void` / `void` | 查键 / 删键 / 清空。 |

---

# 12. json 模块 (安全 JSON 解析)

### `JSONUtils`
| 方法名 | 参数 | 返回值 | 功能说明 |
| :--- | :--- | :--- | :--- |
| `parse` | `String` | `JSONObject` | 字符串转 JSON（失败返回空对象）。 |
| `parsrArray` | `String` / `String, Class<T>` | `JSONArray` / `List<T>` | 字符串转数组/List。 |
| `isJson` | `String` | `boolean` | 判断是否为合法 JSON。 |
| `getInt` / `getString` / `getBoolean` | `JSONObject`, `String`, `默认值` | `int`/`String`/`boolean` | 安全取值（带默认值）。 |
| `getJSONArray` | `JSONObject`, `String` | `JSONArray` | 安全获取数组。 |
| `getJSONObject` | `JSONObject`, `String` / `JSONArray`, `int` | `JSONObject` | 安全获取对象。 |

---