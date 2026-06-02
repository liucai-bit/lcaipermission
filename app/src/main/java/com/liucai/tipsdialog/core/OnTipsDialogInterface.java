package com.liucai.tipsdialog.core;

/**
 * @author HUAWEI
 * @program lctipsdialog
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
}
