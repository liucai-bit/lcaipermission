package com.liucai.permission.core;

import android.Manifest;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public interface LcaiPermissionString {
    String PERMISSION_KEY = "LCAI_PERMISSION_KEY";
    String HAS_NOTIFICATION = "hasNotification";
    String TO_SYSTEM = "to_system_permission";

    /* ==================== 存储（抽象标识，框架按 SDK_INT 解析） ==================== */
    /**
     * 存储权限（旧抽象标识，兼容保留）。
     *
     * @deprecated 语义太粗，建议改用细化的 STORAGE_READ_IMAGE / STORAGE_READ_VIDEO /
     * STORAGE_READ_AUDIO / STORAGE_WRITE，避免 Android 13+ 上一次弹出多个权限框。
     * 当前等价于同时申请三种读取权限（不含写入）。
     * <p>
     * 版本映射：
     * API >= 33 (Android 13+)：READ_MEDIA_IMAGES + READ_MEDIA_VIDEO + READ_MEDIA_AUDIO
     * API 29 ~ 32 (Android 10 ~ 12L)：READ_EXTERNAL_STORAGE
     * API <= 28 (Android 9 及以下)：READ_EXTERNAL_STORAGE
     */
    @Deprecated
    String STORAGE = "DYNAMIC_STORAGE";

    /**
     * 读取图片（抽象标识）。
     * 版本映射：
     * API >= 33：READ_MEDIA_IMAGES
     * API 29 ~ 32：READ_EXTERNAL_STORAGE
     * API <= 28：READ_EXTERNAL_STORAGE
     * 注意：如果业务使用系统照片选择器（Photo Picker），通常不需要此权限。
     */
    String STORAGE_READ_IMAGE = "DYNAMIC_STORAGE_READ_IMAGE";

    /**
     * 读取视频（抽象标识）。
     * 版本映射：
     * API >= 33：READ_MEDIA_VIDEO
     * API 29 ~ 32：READ_EXTERNAL_STORAGE
     * API <= 28：READ_EXTERNAL_STORAGE
     */
    String STORAGE_READ_VIDEO = "DYNAMIC_STORAGE_READ_VIDEO";

    /**
     * 读取音频（抽象标识）。
     * 版本映射：
     * API >= 33：READ_MEDIA_AUDIO
     * API 29 ~ 32：READ_EXTERNAL_STORAGE
     * API <= 28：READ_EXTERNAL_STORAGE
     */
    String STORAGE_READ_AUDIO = "DYNAMIC_STORAGE_READ_AUDIO";

    /**
     * 写入公共存储（抽象标识），例如保存图片/视频到相册。
     * 版本映射：
     * API >= 29：无需任何权限，使用 MediaStore 写入即可，框架直接判定通过。
     * API <= 28：WRITE_EXTERNAL_STORAGE
     * 注意：如果只是写入应用私有目录，任何版本都不需要此标识。
     */
    String STORAGE_WRITE = "DYNAMIC_STORAGE_WRITE";

    /* ==================== 通知 ==================== */
    /**
     * 通知权限，Android 13（API 33）及以上。
     * 注意：这是运行时权限，新安装应用在 Android 13+ 上默认关闭通知；
     * targetSdk >= 33 时由应用控制弹窗时机，建议在用户触发通知相关功能时再申请。
     */
    String NOTIFICATIONS_13 = Manifest.permission.POST_NOTIFICATIONS;

    /**
     * 通知权限标识，Android 13 以下。
     * 注意：这不是真实系统权限，仅作为低版本通知开关的业务标识使用；
     * 低版本没有 POST_NOTIFICATIONS 运行时权限，通知默认开启。
     */
    String NOTIFICATIONS = "NOTIFICATION";

    /* ==================== 存储 / 媒体（真实权限） ==================== */
    /**
     * 读取外部存储（旧）。
     * 注意：API 33+ 已完全失效；API 29~32 仍可用于读取媒体文件；
     * 在清单中建议加 android:maxSdkVersion="32"。
     */
    String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;

    /**
     * 写入外部存储（旧）。
     * 注意：API 29+ 对公共目录基本无效，应使用 MediaStore；
     * 仅 API <= 28 有意义，清单建议加 android:maxSdkVersion="28"。
     */
    String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    /**
     * 所有文件访问权限（特殊权限）。
     * 注意：仅适合文件管理器、备份等核心场景；
     * Google Play 审核严格，普通应用不建议声明；
     * 申请方式不是普通运行时弹窗，需跳转“所有文件访问权限”设置页。
     */
    String MANAGE_EXTERNAL_STORAGE = Manifest.permission.MANAGE_EXTERNAL_STORAGE;

    /**
     * 读取图片媒体，Android 13（API 33）及以上。
     * 注意：替代旧 READ_EXTERNAL_STORAGE 的细粒度权限；
     * 仅在需要读取其他应用创建的图片时声明，最小权限原则；
     * 如果使用系统照片选择器（Photo Picker），通常不需要此权限。
     */
    String READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES;

    /**
     * 读取视频媒体，Android 13（API 33）及以上。
     * 注意：同上，按需声明；仅图片业务不需要 VIDEO。
     */
    String READ_MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO;

    /**
     * 读取音频媒体，Android 13（API 33）及以上。
     * 注意：仅音频播放/扫描业务声明；不要为了“通用存储”而全量声明。
     */
    String READ_MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO;

    /* ==================== 定位 ==================== */
    /** 粗略定位。 */
    String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    /*
     * 注意：粗略定位足以满足大多数场景（如天气、附近推荐），
     * 优先申请 COARSE，不要默认申请 FINE。
     */

    /**
     * 精准定位。
     * 注意：必须与 ACCESS_COARSE_LOCATION 在同一个运行时请求中一起申请；
     * 单独请求 FINE 在部分 Android 12 版本上会被系统忽略。
     */
    String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    /**
     * 后台定位，Android 10（API 29）及以上需要。
     * 注意：Google Play 对后台定位审核非常严格，必须证明是核心功能；
     * Android 11+ 不能随前台定位一起申请，通常需要先有前台定位，
     * 再引导用户到设置页选择“始终允许”；
     * 如果不需要后台持续定位，应从清单中移除。
     */
    String ACCESS_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;

    /* 注意：ACCESS_LOCATION_EXTRA_COMMANDS 是普通权限，一般无需运行时申请。 */
    String ACCESS_LOCATION_EXTRA_COMMANDS = Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS;

    /* ==================== 相机 / 麦克风 ==================== */
    /**
     * 相机。
     * 注意：运行时权限；如果使用系统相机 Intent 拍照，通常不需要自己申请 CAMERA；
     * 只有直接调用 Camera API 时才需要。
     */
    String CAMERA = Manifest.permission.CAMERA;

    /**
     * 麦克风。
     * 注意：运行时权限；录音、语音识别等场景申请；
     * 后台使用麦克风在 Android 11+ 有额外限制。
     */
    String MICROPHONE = Manifest.permission.RECORD_AUDIO;

    /* ==================== 网络 / WiFi ==================== */
    /** 注意：普通权限，安装时自动授予，用于监听网络状态变化。 */
    String ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;

    /** 注意：普通权限，安装时自动授予，用于读取 WiFi 状态。 */
    String ACCESS_WIFI_STATE = Manifest.permission.ACCESS_WIFI_STATE;

    /** 注意：普通权限，安装时自动授予；没有它应用无法发起网络请求。 */
    String INTERNET = Manifest.permission.INTERNET;

    /* ==================== 电话 ==================== */
    /**
     * 读取电话状态。
     * 注意：敏感权限，Google Play 审核严格；
     * 如果只需要设备标识，优先使用 ANDROID_ID 或其他非敏感标识，
     * 不要为了“设备唯一 ID”而申请 READ_PHONE_STATE。
     */
    String PHONE = Manifest.permission.READ_PHONE_STATE;

    /**
     * 拨打电话。
     * 注意：直接拨号需要 CALL_PHONE；
     * 如果只是跳转系统拨号盘（ACTION_DIAL），不需要此权限。
     */
    String CALL_PHONE = Manifest.permission.CALL_PHONE;

    /* ==================== 蓝牙 ==================== */
    /**
     * 注意：旧版蓝牙权限，Android 12（API 31）起在新系统中基本被
     * BLUETOOTH_SCAN / BLUETOOTH_CONNECT 取代；
     * 清单中建议加 android:maxSdkVersion="30"。
     */
    String BLUETOOTH = Manifest.permission.BLUETOOTH;

    /**
     * 扫描蓝牙设备，Android 12（API 31）及以上。
     * 注意：运行时权限，系统弹窗显示为“附近设备”；
     * 如果扫描结果不用于推导物理位置，可以设置 android:usesPermissionFlags="neverForLocation"；
     * 否则可能需要同时声明 ACCESS_FINE_LOCATION。
     */
    String BLUETOOTH_SCAN = Manifest.permission.BLUETOOTH_SCAN;

    /**
     * 连接已配对蓝牙设备，Android 12（API 31）及以上。
     * 注意：运行时权限；与已配对设备通信时声明；
     * 不要和 BLUETOOTH（旧版）混淆，API 31+ 应使用此权限。
     */
    String BLUETOOTH_CONNECT = Manifest.permission.BLUETOOTH_CONNECT;

    /**
     * 旧版蓝牙管理权限（发现/配对等）。
     * 注意：API 31+ 已被 BLUETOOTH_SCAN / BLUETOOTH_CONNECT 取代；
     * 清单中建议加 android:maxSdkVersion="30"。
     */
    String BLUETOOTH_ADMIN = Manifest.permission.BLUETOOTH_ADMIN;

    /* ==================== 应用安装 ==================== */
    /**
     * 注意：系统级权限，普通第三方应用无法获得；
     * 如果业务是“引导用户安装 APK”，应使用 ACTION_VIEW + FileProvider 或 PackageInstaller，
     * 不需要在清单声明 INSTALL_PACKAGES。
     */
    String INSTALL_PACKAGES = Manifest.permission.INSTALL_PACKAGES;

    /* ==================== 日历 / 联系人 ==================== */
    /**
     * 注意：敏感运行时权限；仅日历类核心功能申请；
     * 如果只是创建日历事件 Intent，可能不需要 READ_CALENDAR。
     */
    String READ_CALENDAR = Manifest.permission.READ_CALENDAR;

    /**
     * 注意：敏感运行时权限；读取通讯录必须让用户明确知晓用途；
     * Google Play 对通讯录权限有额外审核要求，非核心功能不要申请。
     */
    String READ_CONTACTS = Manifest.permission.READ_CONTACTS;

    /* ==================== 短信 ==================== */
    /**
     * 注意：受限制权限；
     * 如果应用要在 Google Play 分发，READ_SMS 通常只允许默认短信处理程序申请，
     * 且必须先请求成为默认短信应用，再申请权限；
     * 普通应用读取验证码应使用 SMS Retriever API 或 SMS User Consent API，不要直接申请 READ_SMS。
     */
    String READ_SMS = Manifest.permission.READ_SMS;

    /* ==================== 壁纸 / 震动 ==================== */
    /** 注意：普通权限，安装时自动授予，用于设置壁纸。 */
    String SET_WALLPAPER = Manifest.permission.SET_WALLPAPER;

    /** 注意：普通权限，安装时自动授予，用于震动反馈。 */
    String VIBRATE = Manifest.permission.VIBRATE;

    /* ==================== 前台服务（Android 14 重点） ==================== */
    /**
     * 前台服务基础权限。
     * 注意：Android 14（API 34）起，targetSdk >= 34 的应用
     * 必须为每个前台服务声明 android:foregroundServiceType，
     * 并根据类型申请对应的 FOREGROUND_SERVICE_* 权限，
     * 否则会抛出 SecurityException。
     */
    String FOREGROUND_SERVICE = "android.permission.FOREGROUND_SERVICE";

    /* ==================== 精确闹钟（Android 14 重点） ==================== */
    /**
     * 精确闹钟权限，Android 12（API 31）引入。
     * 注意：从 Android 14 起，targetSdk >= 33 的新安装应用默认不预授予该权限，
     * 默认状态为“拒绝”；
     * 需要使用 AlarmManager.setExact / setExactAndAllowWhileIdle 时，
     * 应先检查 canScheduleExactAlarms()，必要时引导用户到设置页授权。
     */
    String SCHEDULE_EXACT_ALARM = "android.permission.SCHEDULE_EXACT_ALARM";
}