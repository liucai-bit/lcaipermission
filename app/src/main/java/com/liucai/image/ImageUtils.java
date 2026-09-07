package com.liucai.image;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/9/7
 */
public class ImageUtils {
    public static void loadImage(Context mContext, String imageUrl, ImageView mImage) {
        if (GlobalAppUtil.isRuning((Activity) mContext) && mImage != null && !TextUtils.isEmpty(imageUrl)) {
            Glide.with(mContext).load(imageUrl).into(mImage);
            return;
        }
        LcaiLogUtils.e("activity以及被销毁，或图片地址为空，或ImageView组件为空",
                "activity是否为空:"+mContext==null,
                "图片地址是否为空:"+imageUrl,
                "ImageView是否为空:"+mImage==null);
    }
}
