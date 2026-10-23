这是一份基于你提供的 `LcaiEventBus` 及其相关核心源码编写的使用文档。该框架是一个轻量级、基于字符串事件 ID 进行事件分发、并通过弱引用防止内存泄漏的事件总线。

---

# 📚 LcaiEventBus 轻量级事件总线使用文档

## 1. 简介
`LcaiEventBus` 是一个零第三方依赖、高度轻量级的 Android 事件总线。它通过**字符串 ID (`msgId`)** 进行事件匹配，使用 `CopyOnWriteArraySet` 保证多线程并发下的安全，并通过 `WeakReference` 持有订阅者，极大地降低了开发者因忘记反注册而导致的内存泄漏问题。

**核心特性：**
*   **弱引用防泄漏**：订阅者被 GC 时，事件总线自动清理失效的订阅关系。
*   **事件 ID 分发**：不依赖具体的实体类，使用 `String` 作为事件 ID，解耦更彻底。
*   **线程调度**：支持指定回调在“当前线程”或“主线程”执行。
*   **粘性事件**：支持发送粘性事件，新注册的订阅者能立刻收到上一次发送的事件。

## 2. 快速上手

### 2.1 定义与注册订阅者
在需要接收事件的类（如 Activity、Fragment 或 Presenter）中，添加带 `@Subscribe` 注解的方法。

> ⚠️ **注意**：被注解的方法**必须有且仅有一个 `String` 类型的参数**，且不能是 `static` 方法。

```java
public class MainActivity extends AppCompatActivity {

    private static final String EVENT_UPDATE_UI = "event_update_ui";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 注册事件 (传入事件ID和当前订阅者对象)
        LcaiEventBus.getInstance().register(EVENT_UPDATE_UI, this);
    }

    // 2. 定义接收方法 (必须是 public/private/protected 非静态方法，且参数必须是 String)
    @Subscribe(mode = ThreadMode.MAIN) // 指定在主线程接收
    public void onUpdateUiEvent(String content) {
        // 处理收到的消息，content 为发送方传递的数据
        Log.d("EventBus", "收到事件: " + content);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 3. 取消注册 (虽然框架内部使用了弱引用，但主动解绑是良好的编程习惯)
        LcaiEventBus.getInstance().unRegister(this);
    }
}
```

### 2.2 发送事件
在任意位置，通过 `MessageEvent` 打包数据并发送：
```java
// 发送普通事件
LcaiEventBus.getInstance().post(new MessageEvent(EVENT_UPDATE_UI, "我是传递的数据"));
```

---

## 3. 进阶用法

### 3.1 线程模式 (`ThreadMode`)
通过 `@Subscribe(mode = ...)` 控制回调方法所在的线程：

*   **`ThreadMode.POSTING` (默认)**：在发送事件的线程直接调用回调方法。效率最高，但如果在主线程发送，回调也在主线程；如果在子线程发送，回调也在子线程。
*   **`ThreadMode.MAIN`**：无论事件在哪个线程发送，回调都会通过 `Handler` 自动切换到 Android 的主线程执行。适合用来更新 UI。

```java
@Subscribe(mode = ThreadMode.MAIN)
public void onMainThreadEvent(String data) {
    // 这里安全地更新 UI
    textView.setText(data);
}
```

### 3.2 粘性事件 (Sticky Event)
有时候事件在订阅者注册之前就已经发送了（例如：网络请求在后台完成，但 Activity 刚刚启动）。这时可以使用粘性事件：

```java
// 发送粘性事件
LcaiEventBus.getInstance().postSticky(new MessageEvent("event_sticky", "登录成功"));

// ----------------- 稍后的时刻 -----------------

// 此时新注册的订阅者会立刻收到 "登录成功"
LcaiEventBus.getInstance().register("event_sticky", this);
```

**移除粘性事件：**
如果不需要保留历史状态了，可以手动移除：
```java
LcaiEventBus.getInstance().removeStickyEvent("event_sticky");
```

### 3.3 父类事件继承
如果你有一个 `BaseActivity` 处理一些通用事件，但具体业务在 `SubActivity` 中，框架支持向上遍历父类查找 `@Subscribe` 方法。
```java
public class BaseActivity extends AppCompatActivity {
    @Subscribe
    public void onLogout(String msg) {
        finish();
    }
}

public class UserProfileActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册当前类，但也会自动把父类的 onLogout 方法注册进去
        LcaiEventBus.getInstance().register("event_logout", this);
    }
}
```

---

## 4. 内部机制与防泄漏说明

1. **如何防止内存泄漏？**
   `LcaiEventBus` 内部不使用强引用直接持有订阅者，而是使用 `WeakReference<Object>` 包裹订阅者对象。如果外部忘记调用 `unRegister`，当 Activity/Fragment 被系统回收时，垃圾回收器（GC）会清理掉该对象。下次发送事件时，框架会自动探测到该订阅者已失效并清理它。
2. **并发安全**
   事件表 `eventMap` 内部使用了 `CopyOnWriteArraySet`，这保证了在事件分发（遍历）的过程中，即使有其他线程正在注册或注销事件，也不会抛出 `ConcurrentModificationException`。

---

## 5. API 接口一览

| 方法名 | 说明 |
| :--- | :--- |
| `register(String eventId, Object subscriber)` | 注册订阅者。传入对应的 ID 和对象实例。 |
| `unRegister(Object subscriber)` | 取消该对象上的所有订阅。 |
| `post(MessageEvent event)` | 发送普通事件。 |
| `postSticky(MessageEvent event)` | 发送粘性事件。 |
| `removeStickyEvent(String eventId)` | 移除指定 ID 的粘性事件缓存。 |
| `purgeDeadSubscriptions()` | 手动清理所有已失效（被 GC）的订阅者，释放内存。 |
| `clearAll()` | 清空所有事件订阅和粘性缓存，一般在多模块卸载或退出 App 时调用。 |

---

## 6. 开发者注意事项（避坑指南）

1. **参数类型限制**：
   该框架的 `MessageEvent` 内部只保存了 `String` 类型的 `msgContent`。且反射校验时 `parameterTypes[0] != String.class` 会直接抛出异常。**因此，目前该总线只支持传递 `String` 数据**。如果需传递复杂对象，建议将对象转成 JSON 字符串发送，或在业务层使用临时单例缓存对象，总线仅传递 ID。

2. **不能使用静态内部类或匿名内部类**：
   由于框架底层基于**弱引用**，如果你将匿名内部类作为订阅者注册（例如 `new Object() { @Subscribe ... }`），它通常会在注册后很快被系统 GC 回收，导致你收不到事件。**订阅者必须是长生命周期的类（如 Activity、Fragment、Application、Manager 实例）。**

3. **Android 7.0 以下的兼容性警告**：
   在核心源码 `unRegister` 和 `purgeDeadSubscriptions` 方法中，使用了 Java 8 的 `removeIf` 接口，并做了 `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)` 版本判断。如果你的 App 需要兼容 Android 7.0（API 24）以下版本，目前框架底层的逻辑在低版本会**导致无法移除失效的订阅者**。
   *（如果项目要求极低版本兼容，请自行补齐低版本的 `Iterator.remove()` 逻辑）*

4. **ProGuard 混淆配置**：
   由于框架使用了反射来调用方法，如果你开启了代码混淆，请务必保留注解信息及订阅方法的名称，否则反射调用会失败：
   ```proguard
   # 保留 LcaiEventBus 相关注解
   -keepattributes *Annotation*
   # 保留带有 @Subscribe 的方法不被混淆
   -keepclassmembers class * {
       @com.liucai.eventBus.Subscribe <methods>;
   }
   ```