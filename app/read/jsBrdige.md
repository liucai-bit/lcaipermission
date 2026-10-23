这是一份基于你提供的 `LcaiJsBridge` 源码编写的使用文档。该框架实现了一套基于 `WebViewJavascriptBridge` 协议的 Android-H5 双向通信桥梁，并高度封装了专用的 `WebActivity` 容器，开箱即用。

---

# 📚 LcaiJsBridge WebView 容器与双向通信框架使用文档

## 1. 简介
`LcaiJsBridge` 是一个集成了 **WebView 生命周期管理**、**本地 JS 自动注入**、**双向通信协议** 和 **H5 媒体权限桥接** 的完整 H5 容器框架。
**核心特性：**
*   **零配置使用**：内建 `WebActivity`，只需传入 URL 即可直接打开网页，自带标题栏、返回键控制。
*   **自动重试注入**：底层 `WebViewClient` 在页面加载完成时会自动检测并注入 `WebViewJavascriptBridge.js`，且具备防重复注入机制。
*   **双向通信**：支持 H5 调原生（带回调）和原生调 H5（带回调）。
*   **内存安全**：高度封装了 `safeDestroy`，彻底解决 WebView 内存泄漏及 `webview.destroy()` 崩溃问题。
*   **H5 权限桥接**：无缝衔接 `Lcpermission` 库，自动处理 H5 前端发起的录音、摄像头等媒体权限请求。

---

## 2. 快速上手：一行代码打开网页

如果你的业务只需要简单地展示一个网页，直接使用 `LcaiWebActivityRouter` 发起路由即可：

```java
// 最简单的调用方式
LcaiWebActivityRouter.start(
    context, 
    "https://www.example.com", // 网页地址
    "活动详情",                  // 标题
    null                        // 业务回调 (可不传)
);
```

如果你想对页面进行精细控制（例如隐藏标题栏、指定 H5 交互方法等），请使用 `WebActivityConfig.Builder`：

```java
WebActivityConfig config = new WebActivityConfig.Builder()
    .url("https://www.example.com")
    .title("购物车")
    .showTitleBar(true)         // 是否显示标题栏
    .showBackButton(true)       // 是否显示返回按钮
    .progressBarColor(Color.RED) // 进度条颜色
    .build();

LcaiWebActivityRouter.start(context, config);
```

---

## 3. 进阶：原生与 H5 双向通信

通信的核心在于 `WebActivityConfig.Builder` 的 `.methods()` 和 `.callback()` 以及原生的 `send()` 方法。

### 3.1 H5 调用原生 (带回调)
如果你需要允许 H5 调用 App 的原生能力（如扫码、获取定位），需要先在配置中注册方法名：

```java
WebActivityConfig config = new WebActivityConfig.Builder()
    .url("https://www.example.com")
    // 注册允许 H5 调用的原生方法名
    .methods("openScan", "getLocation") 
    .callback(new WebActivityCallback() {
        @Override
        public boolean onMethodBack(LcaiCallbackFunction jsCallback, JSONObject result, Context context) {
            String method = result.getString("method");
            String data = result.getString("data");

            if ("openScan".equals(method)) {
                // 1. 处理原生逻辑（如打开扫码页面）
                // 2. 将结果回传给 H5
                if (jsCallback != null) {
                    JSONObject resp = new JSONObject();
                    resp.put("code", 0);
                    resp.put("msg", "扫码成功");
                    jsCallback.onCallback(resp.toJSONString());
                }
                
                // 返回 true 则自动关闭当前 WebActivity，返回 false 则停留在当前页
                return false; 
            }
            return false;
        }
    })
    .build();

LcaiWebActivityRouter.start(context, config);
```

**H5 端的调用方式：**
```javascript
window.WebViewJavascriptBridge.callHandler(
    'openScan', 
    { 'scanType': 'qrcode' }, 
    function(responseData) {
        console.log("原生返回的数据：", responseData);
    }
);
```

### 3.2 原生调用 H5 (带回调)
当你的 App 需要主动通知网页（如：支付完成、用户登录状态改变），可以通过 `WebActivity` 的 `getWebView()` 发送消息：

```java
public class MyWebActivity extends WebActivity {
    public void notifyH5PaymentSuccess() {
        LcaiBridgeWebview webView = getWebView();
        if (webView != null) {
            JSONObject data = new JSONObject();
            data.put("orderId", "10086");
            
            // 第一个参数：H5 注册的方法名
            // 第二个参数：传递的数据
            // 第三个参数：H5 处理完毕后回调
            webView.callHandler("onPaySuccess", data.toJSONString(), new LcaiCallbackFunction() {
                @Override
                public void onCallback(String data) {
                    // H5 处理完毕后返回的数据
                    LcaiLogUtils.i("H5 已收到通知并返回：" + data);
                }
            });
        }
    }
}
```

**H5 端的注册方式：**
```javascript
window.WebViewJavascriptBridge.registerHandler("onPaySuccess", function(data, responseCallback) {
    console.log("收到原生通知：", data);
    // 处理完毕后回调原生
    responseCallback("H5 已经处理完成");
});
```

---

## 4. H5 媒体权限适配 (摄像头/麦克风)

如果 H5 需要使用 `<input type="file">` 或 `WebRTC` 开启摄像头，`WebActivity` 内部的 `WebChromeClient` 已经集成了 `LcaiPermissionBridge` 自动拦截权限申请。

你只需要在 `Builder` 中配置需要的权限：

```java
WebActivityConfig config = new WebActivityConfig.Builder()
    .url("https://www.example.com/faceAuth")
    // 声明网页可能需要的权限
    .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    .build();
```
当 H5 触发 `getUserMedia` 时，APP 会自动弹出系统权限授权框，用户同意后 H5 即可正常获取流。如果拒绝，会自动阻断 H5 的请求。

---

## 5. 深度定制：继承 WebActivity

如果标准的 UI 无法满足业务需求（比如你需要加个自定义的分享按钮），你可以继承 `WebActivity` 重写部分方法。

### 5.1 替换布局
```java
public class CustomWebActivity extends WebActivity {
    @Override
    public int getLayout() {
        return R.layout.my_custom_web_layout; // 替换为你自己的布局
    }

    @Override
    protected LcaiBridgeWebview findWebView() {
        // 如果你自定义布局里的 WebView ID 不同，需要重写这个方法
        return findViewById(R.id.my_custom_webview_id); 
    }
}
```

### 5.2 注入原生 JS 对象
如果你更偏向传统的 `addJavascriptInterface` 方式交互，可以在回调中注入对象：

```java
@Override
public boolean onMethodBack(LcaiCallbackFunction jsCallback, JSONObject result, Context context) {
    return false;
}

@Override
public List<JsInterface> createJsMethod() {
    List<JsInterface> list = new ArrayList<>();
    // 注入一个名为 "nativeObj" 的对象给 JS 调用
    list.add(new JsInterface("nativeObj")); 
    return list;
}
```
*(注意：`JsInterface` 类内部已实现了 `@JavascriptInterface` 注解的方法)*

---

## 6. 开发者避坑指南 & 最佳实践

1. **回调静态暂存机制**：
   `WebActivityConfig` 是 `Parcelable` 的，但内部的 `callback` 和 `pageListener` 声明了 `transient` 无法序列化。框架利用了 **静态变量暂存** (`stashCallback` / `attachCallback`) 来实现跨 Intent 传递。**千万不要**在同一个 Activity 内同时连续跳转多个 WebActivity，这可能会导致回调错乱（新页面的静态暂存覆盖了上一个页面的）。
2. **WebView 内存泄漏防范**：
   在 `WebActivity` 的 `onDestroy()` 中，已经调用了 `LcaiWebViewLifecycle.safeDestroy(webView)`，内部做了 `loadUrl("about:blank")`、移除父 View、置空 Client 等操作。**如果你自己实现了 H5 容器而没继承 `WebActivity`，切记一定要调用 `safeDestroy()`。**
3. **混淆配置**：
   由于框架底层使用了反射拦截并解析 URL，并且通过 JSBridge 交互，请在 `proguard-rules.pro` 中添加：
   ```proguard
   # 保留 JSBridge 的接口类
   -keep class com.liucai.jsbridge.bridge.** { *; }
   -keep interface com.liucai.jsbridge.bridge.** { *; }
   -keep class com.liucai.jsbridge.web.JsInterface { *; }
   ```
4. **JS 端代码的引入**：
   确保你的 H5 前端页面中引用了 `WebViewJavascriptBridge.js`。原生端 `LcaiBridgeWebViewClient` 会在页面加载完成时自动将其注入到 H5 环境中。
5. **防止重复注入**：
   原生端通过 `JS_CHECK_BRIDGE_EXIST` 检测了 H5 中是否已经存在 Bridge 对象。如果 H5 自己在 html 里也引入了该 JS，原生端会自动跳过注入，避免覆盖原对象导致通信断连。