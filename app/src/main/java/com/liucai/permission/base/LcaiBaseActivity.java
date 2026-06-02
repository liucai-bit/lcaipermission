package com.liucai.permission.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @author liucai
 * @program lcpermission
 * @description BaseActivity
 * @Date 2026/5/26
 */
public abstract class LcaiBaseActivity extends Activity {

    public abstract int getLayout();

    public abstract void initView();

    public void initData() {

    }

    public void onClick() {

    }

    private View view;

    public Bundle bundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.bundle = savedInstanceState;
        view = LayoutInflater.from(this).inflate(getLayout(), null);
        setContentView(view);
        initData();
        initView();
        onClick();
    }

    public View getView() {
        if (view != null) {
            return view;
        }
        return null;
    }
}

