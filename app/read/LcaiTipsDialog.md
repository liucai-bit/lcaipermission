这是一份基于你提供的 `LcaiTipsDialog` 源码编写的使用文档。该框架是一个高度封装、支持多模式渲染、自动管理生命周期及防崩溃（防 BadTokenException）的通用弹窗库。

---

# 📚 LcaiTipsDialog 通用弹窗框架使用文档

## 1. 简介
`LcaiTipsDialog` 是一个功能强大的 Dialog 封装库，采用 Builder 设计模式，支持 6 种不同的弹窗模式（纯文本、富文本、图片、列表、自定义等）。
**核心特性：**
*   **自动绑定宿主**：内部通过 `LcaiActivityManager` 自动获取当前栈顶可见的 Activity，彻底告别 `BadTokenException`。
*   **线程安全**：所有方法（包括 `build()`、`dismiss()`）均可在子线程调用，内部自动切回主线程执行。
*   **防内存泄漏**：内部持有了宿主和界面的引用，但在弹窗关闭 (`onDismiss`) 时会自动调用 `release()` 释放所有引用。
*   **高度可定制**：支持动态修改标题、内容、按钮的文字大小、颜色、背景及边距。

---

## 2. 快速上手

### 2.1 基础调用 (默认模式)
最简单的弹窗，包含标题、内容和两个按钮。

```java
new LcaiTipsDialogBuilder()
    .setTitle("温馨提示")
    .setContent("确认要删除这条数据吗？")
    .setCancelText("取消")
    .setConfirmText("删除")
    .setOnTipsDialogInterface(new OnTipsDialogInterface() {
        @Override
        public void onCancelListener() {
            // 点击取消的回调
        }

        @Override
        public void onConfirmListener() {
            // 点击确认的回调
        }
    })
    .build(); // 显示弹窗
```
> **提示**：默认情况下，框架会自动寻找前台的 Activity 作为宿主。如果你在非 Activity 环境（如 Service 中）调用，或者想指定宿主，可以调用 `.setContext(activity)`。

---

## 3. 六大弹窗模式详解

通过 `.setMode(LcaiTipsMode.XXX)` 切换弹窗样式。

### 3.1 纯文本模式 (`DEFALUT_MODE`)
即上面的快速上手示例。如果传入的 `cancelText` 或 `confirmText` 为空，对应的按钮会自动隐藏（单按钮弹窗）。

### 3.2 富文本点击模式 (`CONTENT_CLICK_MODE`)
常用于协议弹窗（如《用户协议》《隐私政策》），支持文本分段、着色、点击拦截。
需配合 `SegDisplayModule` 使用：

```java
SegDisplayModule titleSeg = new SegDisplayModule().setText("请阅读并同意");
SegDisplayModule linkSeg = new SegDisplayModule()
        .setText("《用户协议》")
        .isClickEnabel()
        .setTextColor("#007AFF")
        .setClickIndex("agreement"); // 用作点击标识

new LcaiTipsDialogBuilder()
    .setMode(LcaiTipsMode.CONTENT_CLICK_MODE)
    .addModule(titleSeg)
    .addModule(linkSeg)
    .setContentClickDismiss(false) // 点击文字是否关闭弹窗
    .setOnTipsDialogInterface(new OnTipsDialogInterface() {
        @Override
        public void onContentListener(String result) {
            // result 就是你在 setClickIndex 里传的值 ("agreement")
            if ("agreement".equals(result)) {
                // 跳转到协议页面
            }
        }
    })
    .build();
```

### 3.3 图片模式 (`IMAGE_MODE`)
常用于展示广告图、活动海报或大图。无需设置标题和按钮，自带右上角关闭按钮。

```java
new LcaiTipsDialogBuilder()
    .setMode(LcaiTipsMode.IMAGE_MODE)
    .setContent("https://example.com/banner.png") // 这里传入图片 URL 或本地路径
    .setOnTipsDialogInterface(new OnTipsDialogInterface() {
        @Override
        public void onConfirmListener() {
            // 点击右上角关闭按钮的回调
        }
    })
    .build();
```

### 3.4 富文本/HTML模式 (`RICH_TEXT_MODE`)
底层使用 `LcaiBridgeWebview` 渲染，适合展示包含复杂排版或图文混排的 HTML 字符串。

```java
String htmlContent = "<h3>活动规则</h3><p>1. 活动时间...</p>";

new LcaiTipsDialogBuilder()
    .setMode(LcaiTipsMode.RICH_TEXT_MODE)
    .setTitle("活动详情")
    .setContent(htmlContent)
    .build();
```

### 3.5 列表模式 (`LIST_MODE`)
适合需要展示多选项或滚动列表的场景，需传入一个继承自 `BaseRecycleAdapter` 的适配器。

```java
MyAdapter adapter = new MyAdapter(dataList);

new LcaiTipsDialogBuilder<>()
    .setMode(LcaiTipsMode.LIST_MODE)
    .setTitle("选择城市")
    .setAdapter(adapter)
    .setListVertical(false) // 是否纵向排列，设为 false 则为网格排列
    .setListCloumns(3)      // 网格排列时的列数
    .setConfirmText("")     // 隐藏确认按钮
    .build();
```

### 3.6 完全自定义模式 (`CUSTOM_MODE`)
如果前面 5 种都不满足需求，你可以传入自己的布局文件，并在 `onBindView` 中自己处理 View 绑定。

```java
new LcaiTipsDialogBuilder()
    .setMode(LcaiTipsMode.CUSTOM_MODE)
    .setCustomLayout(R.layout.my_custom_dialog) // 你的自定义布局
    .setOnTipsDialogInterface(new OnTipsDialogInterface() {
        @Override
        public void onBindView(View bindView, LcaiTipsDialogBuilder builder) {
            // bindView 就是你自定义的布局 View
            TextView tv = bindView.findViewById(R.id.tv_custom);
            tv.setText("我是自定义内容");
            tv.setOnClickListener(v -> builder.dismiss());
        }
    })
    .build();
```
> ⚠️ **注意**：使用 `CUSTOM_MODE` 必须实现 `onBindView`，否则源码内部会直接抛出 `LcaiHttpException`。

---

## 4. 样式与属性配置

在 `LcaiTipsDialogBuilder` 中，你可以针对弹窗的任意元素进行像素级调整：

```java
new LcaiTipsDialogBuilder()
    .setPopupBg(new ColorDrawable(Color.WHITE)) // 设置弹窗背景（支持 Drawable）
    .setTitle("标题")
    .setTitleColor(Color.BLACK)                 // 标题颜色
    .setTitleSize(18)                           // 标题字号 (sp)
    .setContent("内容")
    .setContentColor(Color.GRAY)                // 内容颜色
    .setContentSize(14)                         // 内容字号 (sp)
    .setContentGravity(Gravity.CENTER)          // 内容对齐方式
    // 取消按钮配置
    .setCancelText("取消")
    .setCancelColor(Color.RED)
    .setCancelSize(16)
    .setCancelBg(...)                           // 取消按钮背景
    // 确认按钮配置
    .setConfirmText("确定")
    .setConfirmColor(Color.BLUE)
    .setConfirmSize(16)
    .setConfirmBg(...)                          // 确认按钮背景
    // 点击外部空白处是否可关闭
    .setOutSideCancal(true) 
    .build();
```

---

## 5. 生命周期与核心 API

### 5.1 `build()` vs `showOrThrow()`
*   **`build()` (推荐)**：生产环境使用。如果当前没有存活的 Activity 可以依附，它会**静默失败**（不崩溃），只在 Log 中输出一条提示。
*   **`showOrThrow()`**：开发环境使用。如果找不到宿主 Activity，会直接抛出 `IllegalStateException`，方便你在开发期快速发现“在错误的时机弹窗”的 Bug。

### 5.2 `dismiss()`
关闭弹窗。可在任意线程调用。

### 5.3 `isShowing()`
判断当前弹窗是否正在显示。

### 5.4 `release()`
源码内部在 Dialog 的 `onDismiss()` 监听中自动调用了 `release()`，用于清理 Context、Adapter、Drawable 等引用，防止内存泄漏。
**注意**：如果你在 Activity 销毁时想强行强制释放（即使弹窗未显示），可以手动调用 `builder.release()`，调用后当前 Builder 实例不可再被复用。

---

## 6. 开发者避坑指南

1. **Builder 不可复用**：每次展示弹窗，请务必 `new LcaiTipsDialogBuilder()`。因为内部持有了 Dialog 实例，复用会导致不可预知的渲染问题或空指针。
2. **多线程安全**：框架内部已经做了 `Looper.myLooper() == Looper.getMainLooper()` 的判断，因此哪怕你在网络请求成功的回调里直接 `.build()`，它也会自动去主线程弹窗。
3. **点击回调与关闭顺序**：在默认和列表模式中，点击按钮会先触发你的 `onCancelListener` / `onConfirmListener`，随后内部会自动调用 `dismiss()` 关闭弹窗。如果你在回调中有耗时的同步操作，会导致 UI 短暂卡顿。
4. **图片模式的下拉**：`IMAGE_MODE` 模式下，`builder.content` 被当作图片地址传给 `ImageUtils.loadImage`。请确保你传入的是合法的 URL 或者本地绝对路径。