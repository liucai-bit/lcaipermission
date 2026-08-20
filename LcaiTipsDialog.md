LcaiTipsDialogBulider Android 弹窗构建器使用文档

📖 概述

`LcaiTipsDialogBulider` 是一个采用 Builder 设计模式的 Android 自定义弹窗构建器类。它提供了链式调用的 API，用于快速创建和配置各种样式的提示对话框，支持丰富的自定义选项，包括标题、内容、分段显示、按钮样式等。

🎯 核心特性

- 链式调用：采用 Builder 模式，配置灵活，代码简洁易读
- 高度可定制：支持标题、内容、按钮文字、颜色、大小、背景等全方位自定义
- 分段显示：支持将内容分段显示，每段可独立配置样式和点击事件
- 事件回调：提供点击事件回调接口，处理按钮点击和内容点击
- 样式丰富：支持自定义弹窗背景、按钮背景等视觉效果

📦 集成方式

1. 添加依赖
   implementation "com.github.liucai-bit:lcaipermission:v1.1.2"

2. 添加资源文件（可选）
如果需要使用自定义背景，在 `res/drawable` 目录下添加相应的 drawable 资源文件。

🛠️ 使用方法

基本使用示例

```java
// 创建构建器实例并显示弹窗
new LcaiTipsDialogBulider()
    .with(MainActivity.this)  // 设置上下文
    .addTitle("提示")  // 设置标题
    .addContent("这是一个简单的提示弹窗")  // 设置内容
    .addCancelText("取消")  // 左侧按钮文字
    .addConfirmText("确定")  // 右侧按钮文字
    .addDialogInterface(new OnTipsDialogInterface() {  // 设置回调
        @Override
        public void onCancelListener() {
            // 左侧按钮点击事件
            Toast.makeText(MainActivity.this, "点击了取消", Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onConfirmListener() {
            // 右侧按钮点击事件
            Toast.makeText(MainActivity.this, "点击了确定", Toast.LENGTH_SHORT).show();
        }
    })
    .bulid()  // 构建弹窗
    .show();  // 显示弹窗
```

带样式自定义的弹窗

```java
new LcaiTipsDialogBulider()
    .with(MainActivity.this)
    .addTitle("操作确认")  // 标题
    .addTitleColor(Color.parseColor("FF5722"))  // 标题颜色
    .addTitleSize(20)  // 标题大小（单位：sp）
    .addContent("确定要删除这条记录吗？删除后无法恢复。")  // 内容
    .addContentColor(Color.parseColor("666666"))  // 内容颜色
    .addContentSize(16)  // 内容大小
    .addCancelText("取消")  // 左侧按钮文字
    .addCancelColor(Color.parseColor("999999"))  // 左侧按钮文字颜色
    .addCancelSize(16)  // 左侧按钮文字大小
    .addCancelBackground(R.drawable.btn_cancel_bg)  // 左侧按钮背景
    .addConfirmText("删除")  // 右侧按钮文字
    .addConfirmColor(Color.parseColor("FFFFFF"))  // 右侧按钮文字颜色
    .addConfirmSize(16)  // 右侧按钮文字大小
    .addConfirmBackground(R.drawable.btn_delete_bg)  // 右侧按钮背景
    .addTipsBackground(R.drawable.dialog_bg)  // 弹窗背景
    .addDialogInterface(new OnTipsDialogInterface() {
        @Override
        public void onCancelListener() {
            // 用户点击取消
            Log.d("Dialog", "用户取消了删除操作");
        }
        
        @Override
        public void onConfirmListener() {
            // 用户点击确定删除
            deleteRecord();
        }
    })
    .bulid()
    .show();
```

支持内容点击关闭的弹窗

```java
new LcaiTipsDialogBulider()
    .with(MainActivity.this)
    .addTitle("温馨提示")
    .addContent("点击弹窗内容区域可以关闭此提示")
    .isContentClickDismiss()  // 启用内容点击关闭功能
    .addConfirmText("知道了")
    .addDialogInterface(new OnTipsDialogInterface() {
        @Override
        public void onCancelListener() {
            // 左侧按钮点击（未设置左侧按钮时不会调用）
        }
        
        @Override
        public void onConfirmListener() {
            // 右侧按钮点击
            Toast.makeText(MainActivity.this, "已确认", Toast.LENGTH_SHORT).show();
        }
    })
    .bulid()
    .show();
```

分段显示内容的弹窗

```java
// 创建分段显示数据
List<SegDisplayModule> moduleList = new ArrayList<>();

// 第一段
SegDisplayModule module1 = new SegDisplayModule()
    .setText("功能特点：\n1. 支持链式调用\n2. 高度可定制\n3. 分段显示内容")
    .setTextColor("2196F3")  // 蓝色
    .isClickEnabel()  // 启用点击
    .setClickIndex("feature");  // 点击标识

// 第二段
SegDisplayModule module2 = new SegDisplayModule()
    .setText("使用说明：\n• 调用 with() 方法设置上下文\n• 配置各项参数\n• 调用 bulid() 构建\n• 调用 show() 显示")
    .setTextColor("4CAF50");  // 绿色

moduleList.add(module1);
moduleList.add(module2);

// 创建弹窗
new LcaiTipsDialogBulider()
    .with(MainActivity.this)
    .addTitle("功能介绍")
    .addSegdisplays(moduleList)  // 添加分段显示数据
    .addConfirmText("开始使用")
    .addDialogInterface(new OnTipsDialogInterface() {
        @Override
        public void onConfirmListener() {
            startUsing();
        }
        
        @Override
        public void onContentListener(String result) {
            // 处理分段内容点击事件
            if ("feature".equals(result)) {
                Toast.makeText(MainActivity.this, "点击了功能特点", Toast.LENGTH_SHORT).show();
            }
        }
    })
    .bulid()
    .show();
```

单段分段显示

```java
// 添加单个分段
new LcaiTipsDialogBulider()
    .with(MainActivity.this)
    .addTitle("更新日志")
    .addSegdisplay(new SegDisplayModule()
        .setText("版本 1.0.0\n• 初始版本发布\n• 支持基本弹窗功能\n• 支持样式自定义")
        .setTextColor("2196F3"))
    .addConfirmText("确定")
    .bulid()
    .show();
```

📋 配置选项详解

必需配置项

| 方法 | 说明 | 参数类型 | 默认值 | 示例 |
|------|------|----------|--------|------|
| `with(Context mContext)` | 设置上下文对象 | `Context` | 无 | `.with(MainActivity.this)` |

标题配置项

| 方法 | 说明 | 参数类型 | 默认值 |
|------|------|----------|--------|
| `addTitle(String title)` | 设置弹窗标题文本 | `String` | 无 |
| `addTitleColor(int titleColor)` | 设置标题文字颜色 | `int` (颜色值) | `1c1c1c` |
| `addTitleSize(int titleSize)` | 设置标题文字大小 | `int` (sp) | 无 |

内容配置项

| 方法 | 说明 | 参数类型 | 默认值 |
|------|------|----------|--------|
| `addContent(String content)` | 设置弹窗内容文本 | `String` | 无 |
| `addContentColor(int contentColor)` | 设置内容文字颜色 | `int` (颜色值) | `1C1C1C` |
| `addContentSize(int contentSize)` | 设置内容文字大小 | `int` (sp) | 无 |
| `isContentClickDismiss()` | 启用点击内容关闭弹窗 | 无 | `false` |

分段显示配置项

| 方法 | 说明 | 参数类型 | 默认值 |
|------|------|----------|--------|
| `addSegdisplays(List<SegDisplayModule> moduleList)` | 添加分段显示数据列表 | `List<SegDisplayModule>` | 无 |
| `addSegdisplay(SegDisplayModule module)` | 添加单个分段显示数据 | `SegDisplayModule` | 无 |

按钮配置项

左侧按钮（取消按钮）

| 方法 | 说明 | 参数类型 | 默认值 |
|------|------|----------|--------|
| `addCancelText(String cancelText)` | 设置左侧按钮文字 | `String` | 无 |
| `addCancelColor(int cancelColor)` | 设置左侧按钮文字颜色 | `int` (颜色值) | 无 |
| `addCancelSize(int cancelSize)` | 设置左侧按钮文字大小 | `int` (sp) | 无 |
| `addCancelBackground(int drawable)` | 设置左侧按钮背景 | `int` (drawable资源ID) | 无 |

右侧按钮（确认按钮）

| 方法 | 说明 | 参数类型 | 默认值 |
|------|------|----------|--------|
| `addConfirmText(String confirmText)` | 设置右侧按钮文字 | `String` | 无 |
| `addConfirmColor(int confirmColor)` | 设置右侧按钮文字颜色 | `int` (颜色值) | 无 |
| `addConfirmSize(int contentSize)` | 设置右侧按钮文字大小 | `int` (sp) | 无 |
| `addConfirmBackground(int drawable)` | 设置右侧按钮背景 | `int` (drawable资源ID) | 无 |

弹窗样式配置项

| 方法 | 说明 | 参数类型 | 默认值 |
|------|------|----------|--------|
| `addTipsBackground(int drawable)` | 设置弹窗背景 | `int` (drawable资源ID) | 无 |

事件回调配置项

| 方法 | 说明 | 参数类型 | 默认值 |
|------|------|----------|--------|
| `addDialogInterface(OnTipsDialogInterface dialogInterface)` | 设置弹窗点击事件回调 | `OnTipsDialogInterface` | 无 |

🔧 回调接口详解

OnTipsDialogInterface 接口

```java
public interface OnTipsDialogInterface {
    /**
     * 左侧按钮（取消按钮）点击回调
     * 如果未设置左侧按钮，则不会触发此回调
     */
    default void onCancelListener() {
    }

    /**
     * 右侧按钮（确认按钮）点击回调
     * 必须设置右侧按钮才会触发此回调
     */
    default void onConfirmListener() {

    }

    /**
     * 内容点击回调
     * @param result 点击的内容标识，通过 SegDisplayModule.setClickIndex() 设置
     */
    default void onContentListener(String result) {

    }
}
```

SegDisplayModule 类（分段显示模块）

`SegDisplayModule` 类用于配置分段显示的内容，支持链式调用：

```java
public class SegDisplayModule {
    public String text;           // 显示字符串
    public boolean clickEnabel;   // 是否可以点击
    public String clickIndex;     // 点击标识
    public String textColor="1c1c1c";  // 显示文字颜色，默认 1c1c1c

    // 设置显示文本
    public SegDisplayModule setText(String text) {
        this.text = text;
        return this;
    }

    // 启用点击功能
    public SegDisplayModule isClickEnabel() {
        this.clickEnabel = true;
        return this;
    }

    // 设置点击标识
    public SegDisplayModule setClickIndex(String clickIndex) {
        this.clickIndex = clickIndex;
        return this;
    }

    // 设置文字颜色
    public SegDisplayModule setTextColor(String textColor) {
        this.textColor = textColor;
        return this;
    }
}
```

使用示例：
```java
SegDisplayModule module = new SegDisplayModule()
    .setText("这是一个可点击的段落")
    .setTextColor("FF5722")  // 设置橙色文字
    .isClickEnabel()          // 启用点击
    .setClickIndex("section1"); // 设置点击标识
```

⚠️ 注意事项

1. 上下文有效性
- 传入的 Context 必须是有效的 Activity 上下文
- 避免使用 Application Context，否则可能导致 WindowManager$BadTokenException
- 建议在 Activity 的 `onCreate()` 或按钮点击事件等生命周期稳定的地方调用

2. 资源管理
- 自定义背景图片应放在 `res/drawable` 目录下
- 颜色值可以使用 `Color.parseColor("RRGGBB")` 或 `getResources().getColor(R.color.color_name)`
- 文字大小单位为 sp（缩放像素），确保在不同屏幕密度下的可读性

3. 内存管理
- 避免在循环中频繁创建弹窗，可能导致内存泄漏
- 及时取消弹窗引用，避免持有 Activity 引用导致内存泄漏
- 在 Activity 销毁时确保弹窗已关闭

4. 样式兼容性
- 在不同 Android 版本上测试弹窗样式
- 考虑深色模式下的颜色适配
- 确保文字颜色与背景有足够的对比度

5. 分段显示注意事项
- 分段内容支持富文本显示，可以使用 `\n` 进行换行
- 通过 `setClickIndex()` 设置点击标识，在 `onContentListener()` 中根据标识处理点击事件
- 文字颜色支持十六进制格式（如 `"FF5722"`）

🐛 常见问题

Q1：弹窗显示时崩溃，报错 WindowManager$BadTokenException？
可能原因：
- 使用了错误的 Context（如 Application Context）
- Activity 已销毁但仍在显示弹窗

解决方案：
```java
// 正确：使用 Activity Context
.with(MainActivity.this)

// 错误：不要使用 Application Context
.with(getApplicationContext())
```

Q2：自定义背景不显示？
可能原因：
- drawable 资源文件不存在或路径错误
- drawable 资源格式不正确

解决方案：
1. 检查 drawable 资源文件是否存在于 `res/drawable` 目录
2. 确保资源文件名正确，没有拼写错误
3. 验证 drawable 资源是否有效

Q3：按钮点击事件不触发？
可能原因：
- 未设置 `addDialogInterface()` 回调
- 回调接口实现不正确
- 弹窗已被销毁

解决方案：
```java
// 确保正确设置回调接口
.addDialogInterface(new OnTipsDialogInterface() {
    @Override
    public void onCancelListener() {
        // 处理取消点击
    }
    
    @Override
    public void onConfirmListener() {
        // 处理确认点击
    }
    
    @Override
    public void onContentListener(String result) {
        // 处理内容点击
        if ("section1".equals(result)) {
            // 处理第一个分段的点击
        }
    }
})
```

Q4：分段显示内容格式混乱？
可能原因：
- `SegDisplayModule` 属性设置不正确
- 内容包含特殊字符或换行符处理不当

解决方案：
```java
// 正确设置分段模块
SegDisplayModule module = new SegDisplayModule()
    .setText("标题\n内容行1\n内容行2")  // 使用 \n 换行
    .setTextColor("333333")
    .isClickEnabel()
    .setClickIndex("section1");
```

Q5：在 Fragment 中如何使用？
解决方案：
传入 Fragment 所在的 Activity 上下文：

```java
.with(getActivity())
```

Q6：分段内容点击事件不触发？
可能原因：
- 未调用 `isClickEnabel()` 启用点击
- 未设置 `setClickIndex()` 点击标识
- 未实现 `onContentListener()` 回调方法

解决方案：
```java
// 正确配置可点击的分段
SegDisplayModule module = new SegDisplayModule()
    .setText("可点击的内容")
    .isClickEnabel()  // 必须调用此方法启用点击
    .setClickIndex("clickable_section");  // 设置唯一标识

// 实现内容点击回调
.addDialogInterface(new OnTipsDialogInterface() {
    @Override
    public void onContentListener(String result) {
        if ("clickable_section".equals(result)) {
            // 处理点击事件
        }
    }
})
```

📱 兼容性

- 最低 Android 版本：API 16 (Android 4.1)
- 目标 Android 版本：建议 API 33 (Android 13) 或更高
- 支持库：纯原生实现，无需额外依赖库
- 屏幕适配：支持不同屏幕密度和尺寸

🔄 版本更新日志

最新版本：1.1.2

📄 许可证

`LcaiTipsDialogBulider` 及相关类采用 MIT 许可证发布，允许在商业和非商业项目中自由使用、修改和分发。

🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个库。在提交前请确保：
1. 代码符合 Android 开发规范
2. 添加必要的注释和文档
3. 测试覆盖主要功能
4. 保持向后兼容性

📧 技术支持

如有技术问题或需要进一步的使用指导，请联系：
- 作者：liucai
- 项目名称：lcpermission
- 创建日期：2026年5月27日

---

最佳实践建议：

1. 统一样式管理：将常用的弹窗样式封装成工具方法
2. 资源复用：创建样式常量类，统一管理颜色、尺寸等资源
3. 错误处理：在显示弹窗前检查上下文有效性
4. 用户体验：合理设置弹窗显示时机，避免干扰用户操作
5. 国际化：将文字内容提取到 `strings.xml` 中，支持多语言

示例：样式常量类

```java
public class DialogStyles {
    int TITLE_COLOR_PRIMARY = Color.parseColor("1c1c1c");
    int CONTENT_COLOR_PRIMARY = Color.parseColor("666666");
    int BUTTON_COLOR_CONFIRM = Color.parseColor("2196F3");
    int BUTTON_COLOR_CANCEL = Color.parseColor("9E9E9E");
    int TITLE_SIZE = 18;
    int CONTENT_SIZE = 14;
    int BUTTON_SIZE = 16;
    
    // 分段显示颜色
    String SEGMENT_COLOR_PRIMARY = "1c1c1c";
    String SEGMENT_COLOR_SECONDARY = "666666";
    String SEGMENT_COLOR_ACCENT = "2196F3";
}
```

示例：工具类封装

```java
public class DialogUtils {
    
    public static LcaiTipsDialogBulider createSuccessDialog(Context context, String message) {
        return new LcaiTipsDialogBulider()
            .with(context)
            .addTitle("操作成功")
            .addTitleColor(Color.parseColor("4CAF50"))
            .addContent(message)
            .addConfirmText("确定")
            .addConfirmColor(Color.WHITE)
            .addConfirmBackground(R.drawable.bg_success_button);
    }
    
    public static LcaiTipsDialogBulider createErrorDialog(Context context, String message) {
        return new LcaiTipsDialogBulider()
            .with(context)
            .addTitle("操作失败")
            .addTitleColor(Color.parseColor("F44336"))
            .addContent(message)
            .addConfirmText("重试")
            .addCancelText("取消")
            .addConfirmColor(Color.WHITE)
            .addConfirmBackground(R.drawable.bg_error_button);
    }
}
```

通过遵循本指南，您可以轻松集成和使用 `LcaiTipsDialogBulider` 类，创建美观、功能丰富的自定义弹窗，提升应用的用户体验。