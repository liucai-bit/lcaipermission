package com.liucai.tipsdialog.core;

import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.R;
import com.liucai.tipsdialog.bulider.LcaiTipsDialogBulider;
import com.liucai.tipsdialog.module.SegDisplayModule;


/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/27
 */
public class LcaiTipsDialog extends Dialog {

    public LcaiTipsDialogBulider bulider;

    public LcaiTipsDialog(LcaiTipsDialogBulider bulider) {
        super(bulider.mContext,R.style.LcaiDialogTheme);
        this.bulider = bulider;
        setContentView(R.layout.lcai_tipsdialog_layout);
        init();
    }

    public void init() {
        LinearLayout mLcaiTipsDialogBg = findViewById(R.id.lcai_tips_dialog_bg);
        TextView mLcaiTipsDialogTitle = findViewById(R.id.lcai_tips_dialog_title);
        TextView mLcaiTipsDialogContent = findViewById(R.id.lcai_tips_dialog_content);
        TextView mLcaiTipsDialogCancel = findViewById(R.id.lcai_tips_dialog_cancel);
        TextView mLcaiTipsDialogConfirm = findViewById(R.id.lcai_tips_dialog_confirm);


        if (bulider.tipsBackground != null) {
            mLcaiTipsDialogBg.setBackground(bulider.tipsBackground);
        }

        if (!TextUtils.isEmpty(bulider.title)) {
            mLcaiTipsDialogTitle.setVisibility(VISIBLE);
            mLcaiTipsDialogTitle.setText(bulider.title);

            if (bulider.titleSize > 0) {
                mLcaiTipsDialogTitle.setTextSize(bulider.titleSize);
            }

            if (bulider.titleColor > 0) {
                mLcaiTipsDialogTitle.setTextColor(bulider.titleColor);
            }
        }

        if (bulider.segDisplay && bulider.moduleList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (SegDisplayModule module1 : bulider.moduleList) {
                sb.append(module1.text);
            }
            SpannableString spannableString = new SpannableString(sb.toString());
            int staerIndex = 0;
            int endIndex;
            for (SegDisplayModule module1 : bulider.moduleList) {
                staerIndex += module1.text.length();
                if (module1.clickEnabel) {
                    endIndex = staerIndex - module1.text.length();

                    spannableString.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            if (bulider.contentClickDismiss) {
                                dismiss();
                            }
                            //点击事件
                            if (bulider.dialogInterface != null) {
                                bulider.dialogInterface.onContentListener(module1.clickIndex);
                            }
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor(module1.textColor));
                            ds.setUnderlineText(false);
                        }
                    }, endIndex, staerIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(module1.textColor)),
                            endIndex, staerIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }

            mLcaiTipsDialogContent.setText(spannableString);
            mLcaiTipsDialogContent.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            mLcaiTipsDialogContent.setText(bulider.content);
        }

        if (!TextUtils.isEmpty(bulider.cancelText)) {
            mLcaiTipsDialogCancel.setVisibility(VISIBLE);
            mLcaiTipsDialogCancel.setText(bulider.cancelText);

            if (bulider.cancelBackground != null) {
                mLcaiTipsDialogCancel.setBackground(bulider.cancelBackground);
            }

            if (bulider.cancelSize > 0) {
                mLcaiTipsDialogCancel.setTextSize(bulider.cancelSize);
            }

            if (bulider.cancelColor > 0) {
                mLcaiTipsDialogCancel.setTextColor(bulider.cancelColor);
            }
        }

        if (!TextUtils.isEmpty(bulider.confirmText)) {
            mLcaiTipsDialogConfirm.setVisibility(VISIBLE);
            mLcaiTipsDialogConfirm.setText(bulider.confirmText);

            if (bulider.confirmBackground != null) {
                mLcaiTipsDialogConfirm.setBackground(bulider.confirmBackground);
            }

            if (bulider.confirmSize > 0) {
                mLcaiTipsDialogConfirm.setTextSize(bulider.confirmSize);
            }

            if (bulider.confirmColor > 0) {
                mLcaiTipsDialogConfirm.setTextColor(bulider.confirmColor);
            }
        }

        mLcaiTipsDialogCancel.setOnClickListener(v->{
            dismiss();
            if (bulider.dialogInterface != null) {
                bulider.dialogInterface.onCancelListener();
            }
        });

        mLcaiTipsDialogConfirm.setOnClickListener(v->{
            dismiss();
            if (bulider.dialogInterface != null) {
                bulider.dialogInterface.onConfirmListener();
            }
        });

        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }

        show();
    }
}
