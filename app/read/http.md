这是一份基于你提供的 `LcaiHttp` 源码编写的使用文档。该框架是一个完全基于原生 `HttpURLConnection` 封装的高效网络请求库，具有零第三方网络依赖（仅需 Fastjson 处理 JSON）、支持文件上传/下载、支持 XML 解析以及随时取消等特性。

---

# 📚 LcaiHttp 轻量级网络请求框架使用文档

## 1. 简介
`LcaiHttp` 是一个零依赖（底层基于 Java 的 `HttpURLConnection`）、支持多线程并发的 Android 网络请求框架。它采用了经典的门面模式 + Builder 设计模式，内部自带网络线程池 `GlobalThreadPool` 和 Gzip 解压支持，能够满足日常开发中 99% 的网络通信需求。

**核心特性：**
*   **简单易用**：通过链式调用极速构建网络请求。
*   **多格式支持**：支持 JSON、Form 表单、Multipart 文件上传。
*   **多样化响应**：支持返回 String、InputStream、XML 解析或直接下载文件。
*   **随时取消**：支持在请求过程中通过状态位随时中止请求，避免资源浪费。
*   **进度监听**：内置文件下载进度回调。

## 2. 快速上手

### 2.1 最简单的 GET 请求
> **注意**：框架默认请求方法为 `POST`，如果是 GET 请求，请手动指定 `LcaiHttpMethod.GET`。

```java
new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/user/info")
    .setMethod(LcaiHttpMethod.GET)
    .addMapParam("id", "1001") // GET 参数会自动拼接到 URL 中
    .setResponseResult(new LcaiBaseResponseResult() {
        @Override
        public void onSuccess(@NonNull String result) {
            // 请求成功，result 为服务端返回的 JSON 字符串
        }

        @Override
        public void onFailure(int code, @NonNull String msg) {
            // 请求失败，code 见附录，msg 为错误信息
        }
    })
    .execute(); // 发起请求
```

### 2.2 发送 JSON 格式的 POST 请求
框架默认的请求体格式为 JSON。

```java
new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/login")
    .setMethod(LcaiHttpMethod.POST)
    .addMapParam("username", "admin")
    .addMapParam("password", "123456")
    // 或者直接传 JSON 字符串：
    // .addJsonParams("{\"username\":\"admin\",\"password\":\"123456\"}")
    .setResponseResult(new LcaiBaseResponseResult() {
        @Override
        public void onSuccess(@NonNull String result) {
            // 处理登录成功逻辑
        }
    })
    .execute();
```

---

## 3. 进阶用法

### 3.1 发送表单与文件上传
通过 `setHttpSend` 指定请求体格式，使用 `addFileParam` 添加文件。

```java
new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/upload/avatar")
    .setHttpSend(LcaiHttpSend.FORM_DATA) // 指定为 multipart/form-data 格式
    .addMapParam("userId", "1001")       // 附加普通文本参数
    .addFileParam("avatar", new File("/sdcard/avatar.jpg")) // 附加文件
    .setResponseResult(new LcaiBaseResponseResult() {
        @Override
        public void onSuccess(@NonNull String result) {
            // 上传成功
        }
    })
    .execute();
```

### 3.2 文件下载与进度监听
通过设置 `LcaiHttpCall.DOWNLOAD` 自动开启文件下载，并在回调中监听进度。

```java
new LcaiHttpRequestBulider()
    .setUrl("https://www.example.com/app.apk")
    .setHttpCall(LcaiHttpCall.DOWNLOAD) // 指定响应模式为下载
    .setSavePath(Environment.getExternalStorageDirectory() + "/Download") // 设置保存目录
    .setFileName("my_app.apk") // 设置保存文件名
    .setResponseResult(new LcaiBaseResponseResult() {
        @Override
        public void onProgress(int progress, boolean finish) {
            // progress: 0-100 进度
            // finish: 是否下载完成
            if (finish) {
                // 下载完成
            }
        }

        @Override
        public void onFailure(int code, @NonNull String msg) {
            // 下载失败
        }
    })
    .execute();
```
> **提示**：如果下载中途被取消，框架会自动删除未下载完的临时文件。

### 3.3 获取流或解析 XML
*   **流式获取 (`RESPONSE`)**：适合大文件或非文本数据。**重要：调用方必须自行关闭流！**
*   **解析 XML**：通过 `backXML()` 和 `setQName()` 快速将 XML 属性解析为 JSON。

```java
// 1. 获取流
new LcaiHttpRequestBulider()
    .setUrl("...")
    .setHttpCall(LcaiHttpCall.RESPONSE)
    .setResponseResult(new LcaiBaseResponseResult() {
        @Override
        public void onSuccess(@NonNull InputStream inputStream) {
            try {
                // 读取流
            } finally {
                try { inputStream.close(); } catch (IOException ignored) {}
            }
        }
    })
    .execute();

// 2. 解析 XML
new LcaiHttpRequestBulider()
    .setUrl("...")
    .backXML() // 开启 XML 解析
    .setQName("item") // 指定要解析的 XML 标签名（如 <item name="abc" />）
    .setResponseResult(new LcaiBaseResponseResult() {
        @Override
        public void onSuccess(@NonNull String result) {
            // 解析成功后，框架自动将属性转成了 JSON 数组格式返回
            // 例如： [{"name":"abc"}]
        }
    })
    .execute();
```

### 3.4 携带请求头与超时设置
```java
new LcaiHttpRequestBulider()
    .setUrl("...")
    .addHeader("Authorization", "Bearer token_123456") // 添加单个请求头
    .addHeaders(someHeadersMap) // 批量添加请求头
    .setConnectTimeout(15000) // 连接超时：15 秒
    .setReadTimeout(60000)    // 读取超时：60 秒
    .setResponseResult(...)
    .execute();
```

---

## 4. 核心难点：如何取消请求？
由于框架使用的是 `HttpURLConnection`，它的取消机制并不是直接调用 `.cancel()` 方法，而是**通过回调接口轮询状态位**来实现的。

你需要持有一个标识位（例如 `volatile boolean`），并重写回调的 `isCancelled()` 方法：

```java
public class MyActivity extends AppCompatActivity {
    private volatile boolean isRequestCancelled = false;

    private void doRequest() {
        isRequestCancelled = false;

        new LcaiHttpRequestBulider()
            .setUrl("...")
            .setResponseResult(new LcaiBaseResponseResult() {
                @Override
                public boolean isCancelled() {
                    // 框架会在建立连接前、读取网络流时不断调用这个方法
                    // 如果返回 true，框架会中止请求，并触发 onFailure(-4, "请求已被取消")
                    return isRequestCancelled;
                }

                @Override
                public void onSuccess(@NonNull String result) {}

                @Override
                public void onFailure(int code, @NonNull String msg) {
                    if (code == -4) {
                        // 区分是正常错误，还是被主动取消
                    }
                }
            })
            .execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Activity 销毁时，标记为取消
        isRequestCancelled = true;
    }
}
```

---

## 5. 附录：错误码与常见问题

### 5.1 错误码 (`onFailure` 中的 `code`)
框架对异常情况进行了统一拦截，并映射为以下错误码：

| 错误码 | 说明 | 发生场景 |
| :--- | :--- | :--- |
| `-1` | 参数或未知异常 | 请求地址为空、下载路径为空、其他未捕获异常 |
| `-2` | 网络连接错误 | 断网、DNS 解析失败、`IOException` |
| `-3` | HTTP 处理错误 | 响应体解析异常、XML 格式错误等 |
| `-4` | 请求被取消 | 客户端主动取消 (`isCancelled` 返回了 true) |
| `2xx` | 成功 | 仅返回 200-299 区间视为成功，走 `onSuccess` |
| `4xx/5xx` | 服务端错误 | HTTP 状态码，错误体 (ErrorStream) 会作为 msg 返回 |

### 5.2 开发者避坑指南
1. **回调线程**：所有的 `onSuccess`、`onFailure`、`onProgress` 都在 **子线程（后台线程池）** 中执行。如果你需要更新 UI，请务必使用 `runOnUiThread()` 或 `GlobalAppUtil.runOnUiThread()` 切回主线程。
2. **流式响应的内存泄漏**：如果使用了 `LcaiHttpCall.RESPONSE` 或回调了 `onSuccess(InputStream)`，**必须**由调用方在业务结束时手动 `close()` 掉流，否则会引发严重的内存泄漏和连接池阻塞。
3. **Builder 不可复用**：`LcaiHttpRequestBulider` 内部维护了 `boundary`（用于文件上传）等状态，**每次请求都必须 `new` 一个全新的 Builder**，绝不要复用。
4. **JSON 依赖**：框架底层的 JSON 序列化与 XML 转 JSON 依赖了 `com.alibaba.fastjson`，请确保你的项目中已经引入了 Fastjson。