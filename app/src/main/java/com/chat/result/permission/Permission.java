package com.chat.result.permission;

import android.app.Activity;
import android.app.Fragment;

import com.chat.result.ResultImpl;

/**
 * com.util.result
 * 2018/9/27 16:52
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class Permission implements IResult {
    private final static String TAG = Permission.class.getName();
    private IResult result;

    public Permission(Activity activity) {
        result = new ResultImpl(activity);
    }

    public Permission(Fragment fragment) {
        result = new ResultImpl(fragment);
    }

    public Permission(android.support.v4.app.Fragment fragment) {
        result = new ResultImpl(fragment);
    }

    @Override
    public String[] check(String... permissions) {
        return result.check(permissions);
    }

    @Override
    public void requestPermissions(int requsetCode, PermissionCallback callback, String... permissions) {
        if (result != null)
            result.requestPermissions(requsetCode, callback, permissions);
    }
}
