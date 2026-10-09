package com.liucai.jsbridge.web;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.text.TextUtils;

import java.util.Arrays;
import java.util.List;

/**
 * WebActivity 配置对象。
 *
 * <p>设计：
 * <ul>
 *     <li>通过 {@link Builder} 构建，字段可读写但推荐 Builder</li>
 *     <li>实现 {@link Parcelable}，可通过 Intent 传递，避免多实例串台</li>
 *     <li>callback / pageListener 含 Activity 引用，不参与序列化，走静态暂存桥接</li>
 * </ul>
 *
 * @author liucai
 * @Date 2026/7/7
 */
public class WebActivityConfig implements Parcelable {

    @Nullable
    public String url;
    @Nullable
    public String title;
    @Nullable
    public String[] methodArrays;
    @Nullable
    public String[] permissionArray;

    public boolean showTitleBar = true;
    public boolean showBackButton = true;
    public int progressBarColor = 0;
    @Nullable
    public String errorPageUrl;

    /** 业务回调，含 Activity 引用，不参与序列化 */
    @Nullable
    private transient WebActivityCallback callback;

    /** 页面监听，含 Activity 引用，不参与序列化 */
    @Nullable
    private transient OnPageListener pageListener;

    /** 静态暂存 callback / pageListener，用于跨 Parcelable 传递 */
    @Nullable
    private static volatile WebActivityCallback sPendingCallback;
    @Nullable
    private static volatile OnPageListener sPendingPageListener;

    // ======================== 构造 ========================

    public WebActivityConfig() {
    }

    protected WebActivityConfig(@NonNull Parcel in) {
        url = in.readString();
        title = in.readString();
        methodArrays = in.createStringArray();
        permissionArray = in.createStringArray();
        showTitleBar = in.readByte() != 0;
        showBackButton = in.readByte() != 0;
        progressBarColor = in.readInt();
        errorPageUrl = in.readString();
    }

    // ======================== Getter ========================

    @Nullable
    public WebActivityCallback getCallback() {
        return callback;
    }

    @Nullable
    public OnPageListener getPageListener() {
        return pageListener;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String[] getMethodArrays() {
        return methodArrays;
    }

    @Nullable
    public String[] getPermissionArray() {
        return permissionArray;
    }

    public boolean isShowTitleBar() {
        return showTitleBar;
    }

    public boolean isShowBackButton() {
        return showBackButton;
    }

    public int getProgressBarColor() {
        return progressBarColor;
    }

    @Nullable
    public String getErrorPageUrl() {
        return errorPageUrl;
    }


    /** 由 Router 在 start 时调用，暂存 callback / pageListener */
    static void stashCallback(@Nullable WebActivityCallback callback,
                              @Nullable OnPageListener listener) {
        sPendingCallback = callback;
        sPendingPageListener = listener;
    }

    /** 由 WebActivity 在 onCreate 时取回并绑定 */
    void attachCallback() {
        this.callback = sPendingCallback;
        this.pageListener = sPendingPageListener;
        sPendingCallback = null;
        sPendingPageListener = null;
    }

    public void clear() {
        callback = null;
        pageListener = null;
        if (methodArrays != null) {
            Arrays.fill(methodArrays, null);
            methodArrays = null;
        }
        if (permissionArray != null) {
            Arrays.fill(permissionArray, null);
            permissionArray = null;
        }
        url = null;
        title = null;
        errorPageUrl = null;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(url);
    }

    // ======================== Parcelable ========================

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(title);
        dest.writeStringArray(methodArrays);
        dest.writeStringArray(permissionArray);
        dest.writeByte((byte) (showTitleBar ? 1 : 0));
        dest.writeByte((byte) (showBackButton ? 1 : 0));
        dest.writeInt(progressBarColor);
        dest.writeString(errorPageUrl);
    }

    public static final Creator<WebActivityConfig> CREATOR = new Creator<WebActivityConfig>() {
        @Override
        public WebActivityConfig createFromParcel(Parcel in) {
            return new WebActivityConfig(in);
        }

        @Override
        public WebActivityConfig[] newArray(int size) {
            return new WebActivityConfig[size];
        }
    };

    // ======================== Builder ========================

    public static class Builder {
        private final WebActivityConfig config = new WebActivityConfig();

        public Builder url(@Nullable String url) {
            config.url = url;
            return this;
        }

        public Builder title(@Nullable String title) {
            config.title = title;
            return this;
        }

        public Builder methods(@Nullable String... methods) {
            config.methodArrays = methods;
            return this;
        }

        public Builder methods(@Nullable List<String> methods) {
            config.methodArrays = methods == null ? null : methods.toArray(new String[0]);
            return this;
        }

        public Builder permissions(@Nullable String... permissions) {
            config.permissionArray = permissions;
            return this;
        }

        public Builder showTitleBar(boolean show) {
            config.showTitleBar = show;
            return this;
        }

        public Builder showBackButton(boolean show) {
            config.showBackButton = show;
            return this;
        }

        public Builder progressBarColor(int color) {
            config.progressBarColor = color;
            return this;
        }

        public Builder errorPageUrl(@Nullable String url) {
            config.errorPageUrl = url;
            return this;
        }

        public Builder pageListener(@Nullable OnPageListener listener) {
            config.pageListener = listener;
            return this;
        }

        public Builder callback(@Nullable WebActivityCallback callback) {
            config.callback = callback;
            return this;
        }

        public WebActivityConfig build() {
            return config;
        }
    }
    public interface OnPageListener {
        default void onPageStarted(String url) {
        }

        default void onPageFinished(String url) {
        }

        default void onReceivedError(int errorCode, String description, String failingUrl) {
        }

        default void onProgressChanged(int newProgress) {
        }

        default void onTitleReceived(String title) {
        }
    }
}