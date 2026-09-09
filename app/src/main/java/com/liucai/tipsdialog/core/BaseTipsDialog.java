package com.liucai.tipsdialog.core;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.liucai.permission.R;
import com.liucai.tipsdialog.bulider.LcaiTipsDialogBuilder;

public abstract class BaseTipsDialog extends Dialog {

    protected LcaiTipsDialogBuilder builder;
    protected View rootView;

    public BaseTipsDialog(@NonNull LcaiTipsDialogBuilder builder) {
        super(builder.mContext, R.style.LcaiDialogTheme);
        this.builder = builder;
        setCancelable(builder.outSideCancal);
        setCanceledOnTouchOutside(builder.outSideCancal);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(getLayoutId(), null);
        setContentView(rootView);
        initWindow();
        bindViewsAndData(rootView);
        show();
    }

    private void initWindow() {
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    protected abstract @LayoutRes int getLayoutId();

    protected abstract void bindViewsAndData(View rootView);
}