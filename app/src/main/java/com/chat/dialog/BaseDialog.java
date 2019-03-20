package com.chat.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


/**
 * com.chat.dialog
 * 2019/3/20 11:55
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public abstract class BaseDialog extends Dialog {
    private Window window;
    private WindowManager.LayoutParams layoutParams;

    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public BaseDialog(@NonNull Context context) {
        super(context);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected BaseDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window = getWindow();
        layoutParams = window.getAttributes();
        window.getDecorView().setPadding(0, 0, 0, 0);
        setContentView(layoutId());
        if (!focusable())
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        layoutParams.width = width();
        layoutParams.height = height();
        layoutParams.x = x();
        layoutParams.y = y();
        layoutParams.gravity = gravity();
        window.setAttributes(layoutParams);
    }

    /**
     * 2019/3/20 12:14
     * annotation：宽度
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    protected int width() {
        return layoutParams.width;
    }

    /**
     * 2019/3/20 12:14
     * annotation：高度
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    protected int height() {
        return layoutParams.height;
    }

    /**
     * 2019/3/20 12:15
     * annotation：位置 Gravity BOTTOM CENTER CENTER_HORIZONTAL CENTER_VERTICAL LEFT RIGHT TOP
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    protected int gravity() {
        return layoutParams.gravity;
    }

    /**
     * 2019/3/20 12:19
     * annotation：距离屏幕左边的距离
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    protected int x() {
        return layoutParams.x;
    }

    /**
     * 2019/3/20 12:19
     * annotation：距离屏幕上边的距离
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    protected int y() {
        return layoutParams.y;
    }

    /**
     * 2019/3/20 12:21
     * annotation：设置距离屏幕左边的距离
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public void setX(int x) {
        setXY(x, y());
    }

    /**
     * 2019/3/20 12:22
     * annotation：设置距离屏幕上边的距离
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public void setY(int y) {
        setXY(x(), y);
    }

    /**
     * 2019/3/20 12:22
     * annotation：设置位置
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public void setXY(int x, int y) {
        Log.e("setXY","x:"+x+",y:"+y);
        layoutParams.x = x;
        layoutParams.y = y;
        window.setAttributes(layoutParams);
    }

    public void setXY(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        setXY(location[0], location[1]);
    }

    /**
     * 2019/3/20 12:09
     * annotation：布局
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    protected abstract int layoutId();

    /**
     * 2019/3/20 12:09
     * annotation：放回是否取消
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    protected boolean cancelable() {
        return true;
    }

    /**
     * 2019/3/20 12:09
     * annotation：点击外部是否取消
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    protected boolean canceledOnTouch() {
        return true;
    }

    /**
     * 2019/3/20 12:09
     * annotation：是否获取焦点
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    protected boolean focusable() {
        return true;
    }

    /**
     * 2019/3/20 12:09
     * annotation：背后activity变暗程度
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    protected float dimAmount() {
        return 0.5f;
    }

    @Override
    public void show() {
        setCanceledOnTouchOutside(canceledOnTouch());
        setCancelable(cancelable());
        super.show();
        layoutParams.dimAmount = dimAmount();
        window.setAttributes(layoutParams);
    }
}
