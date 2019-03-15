package com.chat.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.chat.result.permission.PermissionCallback;
import com.chat.utils.LogUtil;

/**
 * com.chat.activity
 * 2019/3/12 11:45
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class InitializeActivity extends BaseActivity {
    private static final String TAG = "InitializeActivity";
    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.READ_PHONE_STATE",
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
//            "android.permission.BLUETOOTH_ADMIN",
//            "android.permission.BLUETOOTH",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_WIFI_STATE",
//            "android.permission.CHANGE_NETWORK_STATE",
//            "android.permission.WRITE_SETTINGS",
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResult.requestPermissions(0, new PermissionCallback() {
            @Override
            public void result(String... failPermissions) {
                if (failPermissions.length <= 0) {
                    LogUtil.e(TAG, "申请权限成功");
                    if (Build.VERSION.SDK_INT >= 23)
                        if (!Settings.System.canWrite(InitializeActivity.this)) {
                            LogUtil.e(TAG, "申请修改权限");
                            Intent goToSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            goToSettings.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(goToSettings);
                        } else {
                            LogUtil.e(TAG, "有修改权限");
                        }

                } else {
                    LogUtil.e(TAG, "申请权限失败");
                }

            }
        }, MANDATORY_PERMISSIONS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23)
            if (Settings.System.canWrite(this))
                LogUtil.e(TAG, "有修改权限了");
            else
                LogUtil.e(TAG, "没有修改权限");
    }
}
