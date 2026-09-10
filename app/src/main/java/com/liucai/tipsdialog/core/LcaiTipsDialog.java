package com.liucai.tipsdialog.core;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.liucai.core.util.text.TextUtils;
import com.liucai.image.ImageUtils;
import com.liucai.jsbridge.web.LcaiBridgeWebview;
import com.liucai.permission.R;
import com.liucai.tipsdialog.bulider.LcaiTipsDialogBuilder;
import com.liucai.tipsdialog.module.SegDisplayModule;

import java.util.List;


/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/27
 */
public class LcaiTipsDialog extends Dialog {

    private LinearLayout defaultRoot;
    private TextView tvTitle, tvContent, tvCancel, tvConfirm;
    private ImageView ivImage, ivClose;
    private TextView tvRichTitle, tvRichCancel, tvRichConfirm;
    private LcaiBridgeWebview wbContent;

    public LcaiTipsDialogBuilder builder;

    public LcaiTipsDialog(LcaiTipsDialogBuilder builder) {
        super(builder.mContext, R.style.LcaiDialogTheme);
        this.builder = builder;
        setContentView(getLayout());
        setCanceledOnTouchOutside(builder.outSideCancal);
        initWindow();
        bindViewsAndData();
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

    public int getLayout() {
        if (builder.mode == LcaiTipsMode.IMAGE_MODE) {
            return R.layout.lcai_tips_dialg_image_layout;
        } else if (builder.mode == LcaiTipsMode.RICH_TEXT_MODE) {
            return R.layout.lcai_tips_dialg_richtext_layout;
        }
        return R.layout.lcai_tips_dialg_default_layout;
    }

    private void bindViewsAndData() {
        if (builder.mode == LcaiTipsMode.DEFALUT_MODE || builder.mode == LcaiTipsMode.CONTENT_CLICK_MODE) {
            bindDefaultMode();
        } else if (builder.mode == LcaiTipsMode.IMAGE_MODE) {
            bindImageMode();
        } else if (builder.mode == LcaiTipsMode.RICH_TEXT_MODE) {
            bindRichTextMode();
        }
    }

    private void bindDefaultMode() {
        defaultRoot = findViewById(R.id.lcai_tips_dialog_default_l1);
        tvTitle = findViewById(R.id.lcai_tips_dialog_default_t1);
        tvContent = findViewById(R.id.lcai_tips_dialog_default_t2);
        tvCancel = findViewById(R.id.lcai_tips_dialog_default_t3);
        tvConfirm = findViewById(R.id.lcai_tips_dialog_default_t4);
        if (builder.popupBg != null) defaultRoot.setBackground(builder.popupBg);
        if (!TextUtils.isEmpty(builder.title)) {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(builder.title);
            tvTitle.setTextColor(builder.titleColor);
            tvTitle.setTextSize(builder.titleSize);
        } else {
            tvTitle.setVisibility(View.GONE);
        }
        if (builder.mode == LcaiTipsMode.DEFALUT_MODE) {
            handleSimpleContent();
        } else {
            handleClickableContent();
        }
        setupButton(tvCancel, builder.cancelText, builder.cancelColor, builder.cancelSize, builder.cancelBg,
                v -> {
                    if (builder.dialogInterface != null) builder.dialogInterface.onCancelListener();
                    dismiss();
                });
        setupButton(tvConfirm, builder.confirmText, builder.confirmColor, builder.confirmSize, builder.confirmBg,
                v -> {
                    if (builder.dialogInterface != null)
                        builder.dialogInterface.onConfirmListener();
                    dismiss();
                });
    }

    private void bindImageMode() {
        ivImage = findViewById(R.id.lcai_tips_dialog_image_m1);
        ivClose = findViewById(R.id.lcai_tips_dialog_image_m2);
        ImageUtils.loadImage(builder.mContext, builder.content, ivImage);
        ivClose.setOnClickListener(v -> {
            if (builder.dialogInterface != null) builder.dialogInterface.onConfirmListener();
            dismiss();
        });
    }

    private void bindRichTextMode() {
        tvRichTitle = findViewById(R.id.lcai_tips_dialog_richtext_t1);
        wbContent = findViewById(R.id.lcai_tips_dialog_richtext_w1);
        tvRichCancel = findViewById(R.id.lcai_tips_dialog_richtext_t2);
        tvRichConfirm = findViewById(R.id.lcai_tips_dialog_richtext_t3);

        if (!TextUtils.isEmpty(builder.title)) {
            tvRichTitle.setVisibility(View.VISIBLE);
            tvRichTitle.setText(builder.title);
            tvRichTitle.setTextColor(builder.titleColor);
        }

        if (!TextUtils.isEmpty(builder.content)) {
            wbContent.loadDataWithBaseURL(null, builder.content, "text/html", "UTF-8", null);
        }

        setupButton(tvRichCancel, builder.cancelText, builder.cancelColor, builder.cancelSize, builder.cancelBg,
                v -> {
                    if (builder.dialogInterface != null) builder.dialogInterface.onCancelListener();
                    dismiss();
                });

        setupButton(tvRichConfirm, builder.confirmText, builder.confirmColor, builder.confirmSize, builder.confirmBg,
                v -> {
                    if (builder.dialogInterface != null) builder.dialogInterface.onConfirmListener();
                    dismiss();
                });
    }

    private void handleSimpleContent() {
        if (!TextUtils.isEmpty(builder.content)) {
            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText(builder.content);
            tvContent.setTextColor(builder.contentColor);
            tvContent.setTextSize(builder.contentSize);
            tvContent.setGravity(builder.contentGravity);
        }
    }
    private void handleClickableContent() {
        List<SegDisplayModule> list = builder.moduleList;
        if (list != null && !list.isEmpty()) {
            tvContent.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            for (SegDisplayModule m : list) sb.append(m.text);

            SpannableString spanStr = new SpannableString(sb.toString());
            int startIndex = 0;

            for (SegDisplayModule module : list) {
                int endIndex = startIndex + module.text.length();
                if (module.clickEnabel) {
                    spanStr.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            if (builder.contentClickDismiss) dismiss();
                            if (builder.dialogInterface != null)
                                builder.dialogInterface.onContentListener(module.clickIndex);
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor(module.textColor));
                            ds.setUnderlineText(false);
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
                startIndex = endIndex;
            }
            tvContent.setText(spanStr);
            tvContent.setMovementMethod(LinkMovementMethod.getInstance());
            tvContent.setHighlightColor(Color.TRANSPARENT); // 去除点击背景色
        }
    }

    private void setupButton(TextView btn, String text, Integer color, Integer size, Drawable bg, View.OnClickListener clickListener) {
        if (btn == null) return;
        if (!TextUtils.isEmpty(text)) {
            btn.setVisibility(View.VISIBLE);
            btn.setText(text);
            if (color != null) btn.setTextColor(color);
            if (size != null) btn.setTextSize(size);
            if (bg != null) btn.setBackground(bg);
            btn.setOnClickListener(clickListener);
        } else {
            btn.setVisibility(View.GONE);
        }
    }
}
