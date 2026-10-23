
本文档基于经过全面架构优化、性能提升与崩溃修复后的组件代码编写。所有组件均遵循统一的 `Base` 抽象体系，支持灵活的 XML 属性配置，同时大幅降低了代码冗余。

---

## 目录
1. [核心基础基类](#一核心基础基类)
2. [RecyclerView 体系](#二recyclerview-体系)
3. [业务组件 (BusinessNodeStatusBar)](#三业务组件)
4. [轮播组件 (ViewFlipperCarousel)](#四轮播组件)
5. [导航与标签组件](#五导航与标签组件)
6. [角标组件 (BadgeView)](#六角标组件)

---

## 一、核心基础基类

所有自定义组件均继承自以下基类。基类通过模板方法模式统一了**属性读取、资源回收与生命周期**，子类无需再关心 `TypedArray` 的回收和 `LayoutParams` 的覆盖问题。

### 1.1 统一的生命周期
子类需要实现两个核心方法：
* `init()`：用于读取 `mTa` 中的自定义属性，以及进行一些基础字段初始化。
* `initView()`：用于执行 `addView()` 等视图结构构建操作。

> **⚠️ 注意**：`mTa` 仅在 `init()` 执行期间有效。`init()` 执行完毕后，基类会自动调用 `recycle()` 将其置空，避免内存泄漏。

### 1.2 支持的基类
* `BaseLinearLayout`
* `BaseRelativeLayout`
* `BaseRecyclerView`
* `BaseTextView`
* `BaseFlexboxLayout`

**优化亮点**：基类不再强制设置 `LayoutParams(MATCH_PARENT, WRAP_CONTENT)`。你在 XML 中设置的 `android:layout_width` 和 `android:layout_height` **将会完全生效**。

---

## 二、RecyclerView 体系

RecyclerView 体系提供了极简的 Adapter、ViewHolder 以及自带上下拉防抖判定的 View。

### 2.1 BaseViewHolder
专为减少样板代码设计，采用 `SparseArray` 缓存控件，避免频繁反射查找，并支持链式调用。

```java
@Override
public void onBindView(int position, View view, BaseViewHolder holder, Bean bean) {
    // 传统写法
    TextView tv = holder.getView(R.id.title);
    if (tv != null) tv.setText(bean.title);

    // 推荐写法：链式调用
    holder.setText(R.id.title, bean.title)
          .setTextColor(R.id.title, bean.color)
          .setVisibility(R.id.badge, bean.hasBadge ? View.VISIBLE : View.GONE)
          .setOnClickListener(R.id.container, v -> {
              // 点击事件
          });
          
    // 确定控件必然存在时，用 requireView 可以尽早暴露 XML 缺控件的问题
    ViewGroup root = holder.requireView(R.id.root_view);
}
```

### 2.2 BaseRecycleAdapter
支持列表和 `JSONArray` 两种数据源，内置泛型自动解析、加载状态管理。

**基础使用**：
```java
public class MyAdapter extends BaseRecycleAdapter<BaseViewHolder, MyBean> {
    public MyAdapter(Context context) {
        super(context, R.layout.item_my);
    }

    @Override
    public void onBindView(int position, View mConvertView, BaseViewHolder holder, MyBean bean) {
        // 绑定逻辑
    }
}
```

**多状态 / 加载更多模式**：
覆写 `openMultiMode()` 返回 `true` 后，Adapter 会自动在列表底部增加一个 Footer，并支持 `LOADING`、`END`、`ERROR` 三种状态展示。
```java
@Override
public boolean openMultiMode() {
    return true; // 开启底部 Footer
}

// 外部更新加载状态
adapter.setLoadState(RecycleViewType.LOADING); // 正在加载
adapter.setLoadState(RecycleViewType.END);     // 已全部加载
adapter.setLoadState(RecycleViewType.ERROR);   // 加载失败
```

### 2.3 LcaiRecyclerView
在 `BaseRecyclerView` 的基础上，封装了**触底加载更多**和**下拉刷新**的判定，自带 300ms 防抖（Debounce），防止一帧滑动触发多次请求。

```java
LcaiRecyclerView recyclerView = findViewById(R.id.recycler);
recyclerView.setLayoutManager(new LinearLayoutManager(this));
recyclerView.setAdapter(adapter);

// 设置状态监听
recyclerView.setTypeListener(type -> {
    if (type == RecycleViewType.LOADING) {
        // 触发加载更多
        recyclerView.setViewType(RecycleViewType.LOADING); // 同步状态给 Footer
        loadMoreData();
    } else if (type == RecycleViewType.REFRESH) {
        // 触发下拉刷新
        refreshData();
    }
});
```

---

## 三、业务组件

### 3.1 BusinessNodeStatusBar (业务节点状态)
用于展示带图标、文本、角标（Badge）的网格布局。常用于金刚区、快捷入口、步骤条。

**XML 配置**：
```xml
<com.liucai.component.BusinessNodeStatusBar
    android:id="@+id/node_status_bar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:columns="4"
    app:title="我的应用"
    app:itemSpace="8"
    app:iconWidth="40"
    app:iconHeight="40" />
```

**代码调用**：
```java
BusinessNodeStatusBar statusBar = findViewById(R.id.node_status_bar);

// 设置数据
List<BusinessNodesStatusBean> datas = new ArrayList<>();
BusinessNodesStatusBean bean = new BusinessNodesStatusBean();
bean.setLabel("扫码");
bean.setIcon("https://xxx.png");
bean.setSubscript(5); // 角标数字
bean.setData(originalObject); // 附带原始数据
datas.add(bean);

statusBar.setDatas(datas);

// 设置点击
statusBar.setItemClickListener((position, data) -> {
    // data 即为上面绑定的 originalObject
    Toast.makeText(this, "点击了: " + position, Toast.LENGTH_SHORT).show();
});
```

---

## 四、轮播组件

### 4.1 ViewFlipperCarousel
支持文本、图片、自定义 View 三种模式的轮播图，支持手势滑动、自动播放、自定义指示器位置和样式。

**基础模式**：
```java
ViewFlipperCarousel carousel = findViewById(R.id.carousel);
carousel.setFlipperMode(1); // 1为图片模式，0为文本，2为自定义View

List<ViewFlipperCarouselBean> beans = new ArrayList<>();
ViewFlipperCarouselBean item = new ViewFlipperCarouselBean();
item.setLabel("https://图片地址.png");
item.setData(bannerData); 
beans.add(item);

carousel.setClickListener((position, data) -> {
    // 点击处理
});
carousel.setDatas(beans);
```

**自定义 View 模式 (`flipperMode=2`)**：
```java
// ⚠️ 注意：必须在 setDatas 之前设置渲染回调
carousel.setCarouseInterface(data -> {
    // data 为 ViewFlipperCarouselBean
    TextView textView = new TextView(this);
    textView.setText(((ViewFlipperCarouselBean)data).getLabel());
    textView.setGravity(Gravity.CENTER);
    return textView;
});
carousel.setDatas(beans);
```

### 4.2 BaseViewFlipper
无界面轮播容器，高度定制化的手势滑动组件，手势滑动极其流畅（带阻尼和惯性判定）。如果 `ViewFlipperCarousel` 不满足需求，可以直接使用它。

```java
BaseViewFlipper flipper = new BaseViewFlipper(context);
flipper.setDirection(BaseViewFlipper.DIRECTION_HORIZONTAL); // 水平滑动
flipper.setSupportGesture(true); // 开启手势

// 往里面 addView
flipper.addView(view1);
flipper.addView(view2);

flipper.setChangeListener(position -> {
    // 页面切换回调
});
```

---

## 五、导航与标签组件

### 5.1 HorizontalNavigationBar (横向导航栏)
类似于 TabLayout，支持横向滑动，点击切换及选中态高亮。
*(注：内部已修复点击事件不穿透的 Bug，以及初始化选中态失效的问题)*

```java
HorizontalNavigationBar navBar = findViewById(R.id.nav_bar);
navBar.setDatas(Arrays.asList("推荐", "热门", "关注", "最新"));

// 设置点击回调
navBar.setClickListener((position, text) -> {
    // 切换 Fragment 逻辑
});

// 设置默认选中第 2 个（不会触发 onClickListener）
navBar.setSelectedPosition(1);

// 动态修改底部指示线
navBar.setIndicatorColor(Color.RED);
navBar.setIndicatorHeight(3);
```

### 5.2 HorizonalBreadBar (面包屑)
用于展示层级路径，例如：`首页 > 企业服务 > 工商注册`。

```java
HorizonalBreadBar breadBar = findViewById(R.id.bread_bar);
List<String> paths = Arrays.asList("首页", "企业服务", "工商注册");
// 内部已修复 NPE 隐患，支持直接赋值
breadBar.setDatas(paths);
```

### 5.3 HorizonalLabelBar (标签流)
基于 `FlexboxLayout` 实现的标签列表，支持自动换行。

```java
HorizonalLabelBar labelBar = findViewById(R.id.label_bar);

List<HorizonalLabelBean> labels = new ArrayList<>();
HorizonalLabelBean l1 = new HorizonalLabelBean();
l1.setLabel("科技");
l1.setTextSize(12);
l1.setTextColor(Color.BLUE);
labels.add(l1);

// 设置点击回调
labelBar.setClickListener((position, data) -> {
    HorizonalLabelBean clicked = (HorizonalLabelBean) data;
    Toast.makeText(this, "点击标签: " + clicked.getLabel(), Toast.LENGTH_SHORT).show();
});

labelBar.setDatas(labels);
```

---

## 六、角标组件

### 6.1 BadgeView
类似微信的未读消息红点，支持数字和最大数字限制。

**XML 配置**：
```xml
<com.liucai.component.BadgeView
    android:id="@+id/badge"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:bBackground="@drawable/bg_red_circle"
    app:bTextColor="@color/white"
    app:bTextSize="10"
    app:bIsMax="true"
    app:bMaxNumber="99"
    app:bBadge="5" />
```

**代码调用**：
```java
BadgeView badge = findViewById(R.id.badge);
// 设置角标为 120，因为设置了 isMax 且 maxNumber=99，所以会显示 "99+"
badge.setEadge(120);

// 隐藏角标
badge.setEadge(0);
```
