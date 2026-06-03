1.1.0版本 权限申请，权限检查 弹窗配置
权限申请属性
    /**
    * 上下文对象
    */
    public Activity mActivity;

    /**
     * 检查权限
     */
    public boolean checkPermission;

    /**
     * 权限申请码
     */
    public int req_code;

    /**
     * 是否显示自定义索权弹窗
     * 默认不显示
     */
    public boolean asDialog = false;

    /**
     * 权限申请标题
     */
    public String title;

    /**
     * 权限申请内容
     */
    public String content;

    /**
     * 拒绝后显示内容
     */
    public String neverContent;

    /**
     * 左侧按钮内容
     */
    public String leftString;

    /**
     * 左侧按钮背景
     * 默认无背景
     */
    public Drawable leftBg;

    /**
     * 按钮文字大小
     * 默认16
     */
    public int btnSize = 16;

    /**
     * 左侧文字颜色
     * 默认#66676B
     */
    public int leftColor = Color.parseColor("#66676B");

    /**
     * 右侧按钮内容
     */
    public String rightString;

    /**
     * 右侧文字背景
     * 默认无背景
     */
    public Drawable rightBg;

    /**
     * 右侧文字颜色
     * 默认#FF9800
     */
    public int rightColor = Color.parseColor("#FF9800");

    /**
     * 内容文字颜色
     * 默认#66676B
     */
    public int contentColor = Color.parseColor("#66676B");

    /**
     * 内容文字大小
     * 默认14
     */
    public int contentSize = 14;

    /**
     * 标题文字颜色
     * 默认#333333
     */
    public int titleColor = Color.parseColor("#333333");

    /**
     * 标题文字大小
     * 默认18
     */
    public int titleSize = 18;


    /**
     * 权限集合
     */
    public List<String> permissions;

    /**
     * 是否跳转系统设置
     */
    public boolean system;

    /**
     * 申请回调
     */
    public LcaiReqPermissionResult result;
