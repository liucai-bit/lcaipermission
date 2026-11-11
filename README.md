# Lcai 项目架构与开发指南

## 1. 项目整体架构与设计理念

本项目采用高度模块化、组件化的架构设计。底层由无第三方依赖的 `http`、`lcaidb`、`core` 支撑；中层封装了 `permission`、`camera_photo`、`jsbridge` 等具备系统交互能力的业务模块；顶层由 `component` 和 `tipsdialog` 提供丰富的 UI 组件。

### 1.1 全局架构与核心调度中心 (LcaiManager)
为了保证跨模块调用的解耦以及回调的准确分发，本项目引入了 **`LcaiManager`** 作为单例门面（Facade）和调度中心。
**特别注意：** 涉及 `permission` (权限申请)、`camera_photo` (拍照/相册选取) 等存在生命周期和异步回调的模块，**业务层不可直接 new 出对象调用**，必须通过 `LcaiManager.getInstance()` 发起。其内部会使用 `LcaiManager.Internal` 维护回调引用，保证 Activity 销毁重建或跨模块调用时，回调依然能够精准下发。

```mermaid
graph TD
    subgraph 业务层
        App[业务代码]
    end

    subgraph 统一调度门面
        Manager[LcaiManager]
    end

    subgraph 基础与功能模块
        Core[core (基础核心)]
        Http[http (网络请求)]
        DB[lcaidb (数据库)]
        Image[image (图片处理)]
        Event[eventBus (事件总线)]
        Js[javascriptbridge (混合开发)]
        Comp[component (通用UI)]
        Dialog[tipsdialog (通用弹窗)]
        Perm[permission (动态权限)]
        Camera[camera_photo (相机相册)]
    end

    App -->|权限申请| Manager
    App -->|拍照/选图| Manager
    Manager --> Perm
    Manager --> Camera

    App --> Comp
    App --> Dialog
    App --> Http
    App --> DB
    App --> Image
    App --> Event
    App --> Js
    App --> Core