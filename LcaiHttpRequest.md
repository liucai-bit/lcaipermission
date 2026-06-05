LcaiHttpRequest Android HTTP 请求框架使用文档

📖 概述

`LcaiHttpRequest` 是一个基于 Java 原生 `HttpURLConnection` 实现的 Android HTTP 请求框架，采用 Builder 设计模式 提供链式调用的 API。该框架封装了常见的 HTTP 请求功能，支持 GET/POST/PUT 请求、JSON/表单数据提交、文件上传、文件下载、XML 解析等，同时提供了完善的错误处理和回调机制。

🎯 核心特性

- 链式调用：采用 Builder 模式，配置灵活，代码简洁易读
- 多请求方式：支持 GET、POST、PUT、DOWNLOAD 等多种 HTTP 方法
- 多数据格式：支持 JSON、表单数据、文件上传（multipart/form-data）
- 多响应格式：支持 JSON、XML、原始流、文件下载等多种响应处理
- 异步执行：内置线程池管理，自动异步执行网络请求
- 完整回调：提供成功、失败、进度等多种回调接口
- 错误处理：完善的异常分类和错误码机制

📦 文件结构

```
com.liucai.http
├── base
│   └── LcaiBaseResponseResult.java    响应回调接口
├── bulider
│   └── LcaiHttpRequestBulider.java    请求构建器
├── core
│   ├── LcaiHttpCall.java              响应类型枚举
│   ├── LcaiHttpMethod.java            请求方法常量
│   ├── LcaiHttpRequest.java           核心请求类
│   └── LcaiHttpSend.java              发送数据类型枚举
```

🛠️ 快速开始

📦 集成方式

   添加依赖
   implementation "com.github.liucai-bit:lcaipermission:v1.1.2"

1. 基本 GET 请求

```java
new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/data")
    .setMethod(LcaiHttpMethod.GET)
    .addMapParam("page", "1")
    .addMapParam("size", "10")
    .setResponseResult(new LcaiBaseResponseResult() {
    @Override
    public void success(String result) {
        // 处理成功响应
        Log.d("HTTP", "响应数据: " + result);
    }

    @Override
    public void error(int code, String msg) {
        // 处理错误
        Log.e("HTTP", "请求失败: " + code + ", " + msg);
    }
})
        .excute();
```

2. POST JSON 请求

```java
String jsonBody = "{\"username\":\"admin\",\"password\":\"123456\"}";

new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/login")
    .setMethod(LcaiHttpMethod.POST)
    .setHttpSend(LcaiHttpSend.JSON)
    .addJsonParams(jsonBody)
    .setResponseResult(new LcaiBaseResponseResult() {
    @Override
    public void success(String result) {
        // 处理登录响应
    }
})
        .excute();
```

3. POST 表单请求

```java
new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/submit")
    .setMethod(LcaiHttpMethod.POST)
    .setHttpSend(LcaiHttpSend.MAP)
    .addMapParam("name", "张三")
    .addMapParam("age", "25")
    .addMapParam("email", "zhangsan@example.com")
    .setResponseResult(new LcaiBaseResponseResult() {
    @Override
    public void success(String result) {
        // 处理表单提交响应
    }
})
        .excute();
```

4. 文件上传

```java
File imageFile = new File("/sdcard/image.jpg");

new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/upload")
    .setMethod(LcaiHttpMethod.POST)
    .setHttpSend(LcaiHttpSend.FORM_DATA)
    .addMapParam("description", "用户头像")
    .addFileParam("file", imageFile)
    .setResponseResult(new LcaiBaseResponseResult() {
    @Override
    public void success(String result) {
        // 处理上传成功响应
    }
})
        .excute();
```

5. 文件下载

```java
new LcaiHttpRequestBulider()
    .setUrl("https://example.com/file.zip")
    .setMethod(LcaiHttpMethod.DOWNLOAD)
    .setHttpCall(LcaiHttpCall.DOWNLOAD)
    .setSavePath("/sdcard/Download")
    .setFileName("app_update.zip")
    .setResponseResult(new LcaiBaseResponseResult() {
    @Override
    public void success(int progress, boolean finish, LcaiHttpRequestBulider bulider) {
        // 下载进度回调
        if (finish) {
            Log.d("DOWNLOAD", "下载完成");
        } else {
            Log.d("DOWNLOAD", "下载进度: " + progress + "%");
        }
    }

    @Override
    public void error(int code, String msg) {
        // 下载错误处理
        Log.e("DOWNLOAD", "下载失败: " + msg);
    }
})
        .excute();
```

📋 API 详细说明

LcaiHttpRequestBulider 构建器

基本配置方法

| 方法 | 参数类型 | 说明 | 默认值 |
|------|----------|------|--------|
| `setUrl(String url)` | `String` | 设置请求地址 | 必需 |
| `setMethod(String method)` | `String` | 设置请求方法（GET/POST/PUT/DOWNLOAD） | `LcaiHttpMethod.POST` |
| `setConnectTimeout(int timeout)` | `int` | 设置连接超时时间（毫秒） | `10000` (10秒) |
| `setReadTimeout(int timeout)` | `int` | 设置读取超时时间（毫秒） | `30000` (30秒) |
| `setEncode(String encode)` | `String` | 设置编码格式 | `"utf-8"` |

请求头配置

| 方法 | 说明 |
|------|------|
| `addHeader(String key, Object value)` | 添加单个请求头 |
| `addHeaders(Map<String, Object> headers)` | 添加多个请求头 |

参数配置

| 方法 | 说明 |
|------|------|
| `addMapParam(String key, String value)` | 添加表单参数（键值对） |
| `addMapParams(Map<String, Object> mapParams)` | 添加多个表单参数 |
| `addJsonParams(String jsonParams)` | 设置 JSON 参数 |
| `addFileParam(String key, File file)` | 添加文件参数 |
| `addFileParams(Map<String, File> fileParams)` | 添加多个文件参数 |

响应配置

| 方法 | 说明 |
|------|------|
| `setHttpCall(LcaiHttpCall httpCall)` | 设置响应类型（JSON/RESPONSE/DOWNLOAD） | `LcaiHttpCall.JSON` |
| `setHttpSend(LcaiHttpSend httpSend)` | 设置发送数据类型（JSON/MAP/FORM_DATA） | `LcaiHttpSend.JSON` |
| `backXML()` | 设置返回 XML 格式（需配合 `setQName` 使用） |
| `setQName(String qName)` | 设置 XML 标签名 |
| `withHeader()` | 返回结果包含响应头 |
| `setSavePath(String savePath)` | 设置文件下载保存路径 |
| `setFileName(String fileName)` | 设置文件下载保存名称 |
| `stopDownload()` | 停止下载（删除已下载文件） |

回调设置

| 方法 | 说明 |
|------|------|
| `setResponseResult(LcaiBaseResponseResult responseResult)` | 设置响应回调接口 |

LcaiBaseResponseResult 回调接口

```java
public interface LcaiBaseResponseResult {
    // 请求失败回调
    default void error(int code, String msg) {}

    // 请求成功回调（字符串结果）
    default void success(String result) {}

    // 请求成功回调（带响应头）
    default void success(Map<String, List<String>> headers, String result) {}

    // 请求成功回调（输入流）
    default void success(InputStream inputStream) {}

    // 请求成功回调（带响应头的输入流）
    default void success(Map<String, List<String>> headers, InputStream inputStream) {}

    // 下载进度回调
    default void success(int progress, boolean finish, LcaiHttpRequestBulider bulider) {}

    // 取消请求（可选实现）
    default boolean cancelRequest() {
        return false;
    }
}
```

枚举类型说明

LcaiHttpMethod（请求方法）
- `GET` - GET 请求
- `POST` - POST 请求
- `PUT` - PUT 请求
- `DOWNLOAD` - 下载请求（内部转换为 GET）

LcaiHttpSend（发送数据类型）
- `JSON` - 发送 JSON 格式数据
- `MAP` - 发送表单格式数据（application/x-www-form-urlencoded）
- `FORM_DATA` - 发送 multipart/form-data 格式（文件上传）

LcaiHttpCall（响应类型）
- `JSON` - 返回 JSON 格式字符串
- `RESPONSE` - 返回原始 InputStream
- `DOWNLOAD` - 文件下载模式

🔧 高级用法

1. 获取带响应头的 JSON 数据

```java
new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/data")
    .setMethod(LcaiHttpMethod.GET)
    .withHeader()  // 启用响应头
    .setResponseResult(new LcaiBaseResponseResult() {
        @Override
        public void success(Map<String, List<String>> headers, String result) {
            // headers 包含所有响应头信息
            // result 是响应体 JSON 字符串
            List<String> contentType = headers.get("Content-Type");
            if (contentType != null && !contentType.isEmpty()) {
                Log.d("HEADER", "Content-Type: " + contentType.get(0));
            }
        }
    })
    .excute();
```

2. 解析 XML 响应

```java
new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/xml-data")
    .setMethod(LcaiHttpMethod.GET)
    .backXML()  // 启用 XML 解析
    .setQName("item")  // 设置要解析的 XML 标签名
    .setResponseResult(new LcaiBaseResponseResult() {
        @Override
        public void success(String result) {
            // result 是解析后的 Map 列表字符串
            // 格式: [{key1=value1, key2=value2}, ...]
            Log.d("XML", "解析结果: " + result);
        }
    })
    .excute();
```

3. 获取原始响应流

```java
new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/stream")
    .setMethod(LcaiHttpMethod.GET)
    .setHttpCall(LcaiHttpCall.RESPONSE)
    .setResponseResult(new LcaiBaseResponseResult() {
        @Override
        public void success(InputStream inputStream) {
            // 自行处理输入流
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // 处理每一行数据
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    })
    .excute();
```

4. 自定义请求头

```java
Map<String, Object> headers = new HashMap<>();
headers.put("Authorization", "Bearer token123");
headers.put("User-Agent", "MyApp/1.0");
headers.put("Accept", "application/json");

new LcaiHttpRequestBulider()
    .setUrl("https://api.example.com/protected")
    .setMethod(LcaiHttpMethod.GET)
    .addHeaders(headers)
    .addHeader("X-Custom-Header", "custom-value")
    .setResponseResult(new LcaiBaseResponseResult() {
        @Override
        public void success(String result) {
            // 处理响应
        }
    })
    .excute();
```

⚠️ 注意事项

1. 线程安全
- 所有请求都在 `GlobalThreadPool` 中异步执行，不会阻塞 UI 线程
- 回调方法在后台线程执行，如需更新 UI 请使用 Handler 或 runOnUiThread

2. 文件上传
- 使用 `FORM_DATA` 格式时，会自动识别文件 MIME 类型
- 支持同时上传多个文件和普通表单字段
- 大文件上传建议分片处理

3. 文件下载
- 下载前需确保有存储权限
- 下载路径必须存在，否则会自动创建目录
- 支持进度回调，可实时更新 UI
- 可通过 `stopDownload()` 中断下载并删除临时文件

4. 错误处理
   框架定义了以下错误码：
- `-1`：通用错误（请求地址为空、构建请求失败等）
- `-2`：网络连接错误（IOException）
- `-3`：HTTP 处理错误（LcaiHttpException）
- `200-300`：HTTP 状态码（服务器返回的错误码）

5. 编码设置
- 默认使用 UTF-8 编码
- 可通过 `setEncode()` 方法修改编码
- 文件上传时使用指定的编码格式

🔍 内部实现原理

1. 请求流程
```
构建请求参数 → 创建 HttpURLConnection → 设置请求头 → 写入请求体 → 
发送请求 → 接收响应 → 解析响应 → 回调结果
```

2. 数据格式处理
- JSON 格式：使用 FastJSON 库处理 JSON 序列化
- 表单格式：自动进行 URL 编码
- 文件上传：生成符合 RFC 2388 标准的 multipart/form-data 格式
- XML 解析：使用 SAX 解析器，性能高效

3. 连接管理
- 支持连接池复用
- 自动处理 Keep-Alive 连接
- 合理的超时设置防止资源浪费

🐛 常见问题

Q1：请求超时怎么办？
A：调整超时时间：
```java
.setConnectTimeout(15000)  // 连接超时 15秒
.setReadTimeout(60000)     // 读取超时 60秒
```

Q2：如何取消请求？
A：目前框架支持以下取消方式：
- 文件下载：调用 `stopDownload()` 方法
- 其他请求：在回调中忽略结果或停止处理
- 可通过实现 `cancelRequest()` 方法自定义取消逻辑

Q3：HTTPS 证书问题？
A：框架使用系统默认的证书验证机制。如需自定义证书验证，需要扩展 `LcaiHttpRequest` 类，重写 SSL 相关配置。

Q4：如何调试请求？
A：框架内置了日志输出，可通过 `LcaiLogUtils` 查看：
- 请求 URL
- 请求头
- 请求体
- 异常信息

Q5：如何处理大文件上传？
A：建议分片上传或使用流式上传。当前实现适合中小文件上传，大文件上传可能会占用较多内存。

📄 许可证

本项目采用 MIT 许可证。详情请查看项目 LICENSE 文件。

🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个框架。在提交前请确保：
1. 代码符合 Android 开发规范
2. 添加必要的注释和文档
3. 测试覆盖主要功能
4. 保持向后兼容性

📧 技术支持

如有问题或建议，请通过以下方式联系：
- 作者：liucai
- 项目名称：lcpermission
- 创建日期：2026年6月5日

---

最佳实践建议：

1. 统一配置：创建统一的请求配置类，管理公共参数（如超时时间、编码格式）
2. 错误处理：实现统一的错误处理逻辑，如网络错误重试、Token 过期刷新等
3. 进度显示：文件下载时使用进度回调更新 UI
4. 内存管理：及时关闭 InputStream，避免内存泄漏
5. 测试覆盖：编写单元测试覆盖各种请求场景

通过本框架，您可以快速、简洁地实现 Android 应用中的各种 HTTP 请求需求，同时保持良好的代码结构和可维护性。<br><br>百度AI生成，内容仅供参考