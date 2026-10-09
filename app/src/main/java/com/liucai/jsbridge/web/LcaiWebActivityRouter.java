package com.liucai.jsbridge.web;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * WebActivity 统一启动入口。
 *
 * @author liucai
 */
public final class LcaiWebActivityRouter {

    public static final String EXTRA_CONFIG = "extra_web_activity_config";

    private LcaiWebActivityRouter() {
    }

    public static void start(@NonNull Context context, @NonNull WebActivityConfig config) {
        // 1. 暂存 callback / pageListener（transient，无法序列化）
        WebActivityConfig.stashCallback(config.getCallback(), config.getPageListener());

        // 2. 通过 Intent 传 config（不含 callback）
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(EXTRA_CONFIG, config);

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void start(@NonNull Context context, @NonNull Class<? extends WebActivity> activityClass, @NonNull WebActivityConfig config) {
        // 1. 暂存 callback / pageListener（transient，无法序列化）
        WebActivityConfig.stashCallback(config.getCallback(), config.getPageListener());

        // 2. 通过 Intent 传 config（不含 callback）
        Intent intent = new Intent(context, activityClass);
        intent.putExtra(EXTRA_CONFIG, config);

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void start(@NonNull Context context,
                             @Nullable String url,
                             @Nullable String title,
                             @Nullable WebActivityCallback callback) {
        WebActivityConfig config = new WebActivityConfig.Builder()
                .url(url)
                .title(title)
                .callback(callback)
                .build();
        start(context, config);
    }

    public static void start(@NonNull Context context,
                             @NonNull WebActivityConfig.Builder builder) {
        start(context, builder.build());
    }
}