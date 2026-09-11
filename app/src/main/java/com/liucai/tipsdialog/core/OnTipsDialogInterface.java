package com.liucai.tipsdialog.core;

import android.view.View;

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
}
