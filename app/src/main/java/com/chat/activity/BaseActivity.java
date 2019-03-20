package com.chat.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.chat.R;
import com.chat.net.NetworkUtil;
import com.chat.result.ResultImpl;


/**
 * com.chat.activity
 * 2019/3/12 11:47
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class BaseActivity extends FragmentActivity {
    private static final String BASE_TAG = "BaseActivity";
    public ResultImpl mResult;
    protected ViewGroup parentView;
    private View whiteboardView;
    public boolean net = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager
                .LayoutParams.FLAG_HARDWARE_ACCELERATED);
        mResult = new ResultImpl(this);
        net = NetworkUtil.isNetConnected(this);
        parentView = (ViewGroup) getWindow().getDecorView();
        parentView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }


    @Override
    protected void onResume() {
        setStatusBar(statusBar());
        super.onResume();
        setWhiteboard(whiteboard());
    }


    public int statusBar() {
        return R.color.colorAccent;
    }

    public int whiteboard() {
        return -1;
    }

    void removeWhiteboard() {
        if (whiteboardView != null)
            parentView.removeView(whiteboardView);
    }

    private void setWhiteboard(int resId) {
        if (resId <= 0)
            return;
        whiteboardView = new View(this);
        parentView.addView(whiteboardView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void setStatusBar(int resId) {
        if (resId <= 0)
            return;
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        int resourceId = getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        int statusBarHeight = getResources()
                .getDimensionPixelSize(resourceId);
        // 绘制一个和状态栏一样高的矩形
        View statusView = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
        statusView.setLayoutParams(params);
        statusView.setBackgroundResource(resId);
        // 添加 statusView 到布局中
        ViewGroup decorView = (ViewGroup) getWindow()
                .getDecorView();
        decorView.addView(statusView);
        ViewGroup rootView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        if (rootView == null)
            return;
        rootView.setFitsSystemWindows(true);
        rootView.setClipToPadding(true);
    }
}
