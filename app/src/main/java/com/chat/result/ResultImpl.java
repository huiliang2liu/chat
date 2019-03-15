package com.chat.result;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.chat.result.activity.ActivityResult;
import com.chat.result.activity.ResultCallback;
import com.chat.result.permission.PermissionCallback;

import java.io.File;

/**
 * com.result
 * 2018/10/26 16:06
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public final class ResultImpl implements Result {
    /**
     * 截图使用的返回码
     */
    private final static int SCREENSHOT = 1000;
    /**
     * 从相册选图片的返回码
     */
    private final static int PHOTO_ALBUM = 1001;
    /**
     * 拍照返回码
     */
    private final static int PHOTO_CAMERA = 1002;
    private final static String TAG = ActivityResult.class.getName();
    private Result result;

    public ResultImpl(Activity activity) {
        if (activity == null)
            throw new NullPointerException("activity is null");
        if (activity instanceof FragmentActivity) {
            addFragment(((FragmentActivity) activity).getSupportFragmentManager());
        } else {
            addFragment(activity.getFragmentManager());
        }
    }

    public ResultImpl(Fragment fragment) {
        if (fragment == null)
            throw new NullPointerException("fragment is null");
        if (android.os.Build.VERSION.SDK_INT >= 17)
            addFragment(fragment.getChildFragmentManager());
        else
            addFragment(fragment.getFragmentManager());
    }

    public ResultImpl(android.support.v4.app.Fragment fragment) {
        if (fragment == null)
            throw new NullPointerException("fragment is null");
        addFragment(fragment.getChildFragmentManager());
    }

    private void addFragment(FragmentManager manager) {
        Fragment fragment = manager.findFragmentByTag(TAG);
        boolean isNewInstance = fragment == null;
        if (isNewInstance) {
            fragment = new AndroidFragment();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(fragment, TAG);
            if (android.os.Build.VERSION.SDK_INT >= 24)
                transaction.commitNow();
            else {
                transaction.commitAllowingStateLoss();
                manager.executePendingTransactions();
            }
        }
        result = (Result) fragment;
    }

    private void addFragment(android.support.v4.app.FragmentManager manager) {
        android.support.v4.app.Fragment fragment = manager.findFragmentByTag(TAG);
        boolean isNewInstance = fragment == null;
        if (isNewInstance) {
            fragment = new V4Fragment();
            android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(fragment, TAG);
            if (android.os.Build.VERSION.SDK_INT >= 24)
                transaction.commitNow();
            else {
                transaction.commitAllowingStateLoss();
                manager.executePendingTransactions();
            }
        }
        result = (Result) fragment;
    }

    @Override
    public void startActivityForResult(int requsetCode, Intent intent, ResultCallback... callbacks) {
            result.startActivityForResult(requsetCode, intent, callbacks);
    }

    @Override
    public void requestPermissions(int requsetCode, PermissionCallback callback, String... permissions) {
            result.requestPermissions(requsetCode, callback, permissions);
    }

    @Override
    public String[] check(String... permissions) {
        return result.check(permissions);
    }

    @Override
    public void startActivity(Intent intent) {
            result.startActivity(intent);
    }

    @Override
    public void photo2PhotoAlbum(ResultCallback... callback) {
            result.photo2PhotoAlbum(callback);
    }

    @Override
    public void openCamera(@NonNull File file, String authority, ResultCallback... callback) {
            result.openCamera(file, authority, callback);
    }

    @Override
    public void screenshots(@NonNull File imageFile, String authorityImage, @NonNull File saveFile, String authoritySave, int aspectX, int aspectY, int outputX, int outputY, ResultCallback... callback) {
            result.screenshots(imageFile, authorityImage, saveFile, authoritySave, aspectX, aspectY, outputX, outputY, callback);
    }

    @Override
    public void screenshots(@NonNull Uri imageUri, @NonNull Uri saveUri, int aspectX, int aspectY, int outputX, int outputY, ResultCallback... callback) {
            result.screenshots(imageUri, saveUri, aspectX, aspectY, outputX, outputY, callback);
    }
}
