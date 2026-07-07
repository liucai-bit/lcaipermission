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

    /**
     * 读取权限
     */
    String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    /**
     * 写入权限
     */
    String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    /**
     * 存储权限
     *  31以上
     */
    String MANAGE_EXTERNAL_STORAGE = Manifest.permission.MANAGE_EXTERNAL_STORAGE;
    /**
     * 存储
     */
    String STORAGE[] = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    /**
     * 定位权限 粗略定位
     */
    String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    /**
     * 定位权限 精准定位
     */
    String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    /**
     * 后台定位
     */
    String ACCESS_LOCATION_EXTRA_COMMANDS = Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS;
    /**
     * 定位
     */
    String LOCATION[] = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};

    /**
     * API29 以上定位权限
     */
    String ACCESS_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
    /**
     * 相机
     */
    String CAMERA = Manifest.permission.CAMERA;
    /**
     * 相机+存储
     */
    String CAMERA_STORAGE[] = {CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE};
    /**
     * 网络状态
     */
    String ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    /**
     * WIFI状态
     */
    String ACCESS_WIFI_STATE = Manifest.permission.ACCESS_WIFI_STATE;
    /**
     * 麦克风
     */
    String MICROPHONE = Manifest.permission.RECORD_AUDIO;
    /**
     * 电话状态
     */
    String PHONE = Manifest.permission.READ_PHONE_STATE;
    /**
     * 蓝牙权限
     * 蓝牙连接（已配对）
     */
    String BLUETOOTH = Manifest.permission.BLUETOOTH;
    /**
     * 蓝牙权限
     * 扫描蓝牙
     */
    String BLUETOOTH_SCAN = Manifest.permission.BLUETOOTH_SCAN;
    /**
     * 蓝牙权限
     * 蓝牙连接
     */
    String BLUETOOTH_CONNECT = Manifest.permission.BLUETOOTH_CONNECT;
    /**
     * 蓝牙权限
     * 发现蓝牙、连接蓝牙（未连接）
     */
    String BLUETOOTH_ADMIN = Manifest.permission.BLUETOOTH_ADMIN;
    /**
     * 打电话
     */
    String CALL_PHONE = Manifest.permission.CALL_PHONE;
    /**
     * 安装应用
     */
    String INSTALL_PACKAGES = Manifest.permission.INSTALL_PACKAGES;
    /**
     * 网络权限
     */
    String INTERNET = Manifest.permission.INTERNET;
    /**
     * 读取日历
     */
    String READ_CALENDAR = Manifest.permission.READ_CALENDAR;
    /**
     * 读取联系人
     */
    String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    /**
     * 读取短信
     */
    String READ_SMS = Manifest.permission.READ_SMS;
    /**
     * 设置壁纸
     */
    String SET_WALLPAPER = Manifest.permission.SET_WALLPAPER;
    /**
     * 震动
     */
    String VIBRATE = Manifest.permission.VIBRATE;

}
