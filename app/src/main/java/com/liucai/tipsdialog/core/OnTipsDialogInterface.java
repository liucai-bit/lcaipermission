package com.liucai.tipsdialog.core;

import android.view.View;

import androidx.annotation.Nullable;

import com.liucai.tipsdialog.bulider.LcaiTipsDialogBuilder;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/27
 */
public interface OnTipsDialogInterface {
    default void onCancelListener() {
    }

    default void onConfirmListener() {

    }

    default void onContentListener(String result) {

    }

    default void onBindView(View bindView, LcaiTipsDialogBuilder builder) {

    }

    /**
     * {@link LcaiTipsDialogBuilder#buildAsync} 任务完成回调（主线程）。
     *
     * @param builder 发起本次任务的 Builder（loading 已被框架 dismiss）
     * @param error   {@code null} 表示成功；非 null 表示 task 抛出的异常
     */
    default void onBuildAsync(LcaiTipsDialogBuilder builder,@Nullable Throwable error) {

    }
}
