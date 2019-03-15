package com.chat.result;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;

import com.chat.result.activity.ResultCallback;
import com.chat.result.permission.PermissionCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * com.result
 * 2018/10/30 11:05
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
class ResultBack {
    private Map<Integer, ResultCallback[]> callbackMap;
    private Map<Integer, PermissionCallback> permissionCallbackMap;

    {
        callbackMap = new HashMap<>();
        permissionCallbackMap = new HashMap<>();
    }

    public void registerActivityResult(int requsetCode, ResultCallback... callbacks) {
        if (callbacks == null || callbacks.length <= 0)
            return;
        callbackMap.put(requsetCode, callbacks);
    }

    public void activityResult(int requsetCode,int resultCode, Intent intent) {
        if (callbackMap.containsKey(requsetCode)) {
            for (ResultCallback callback : callbackMap.get(requsetCode))
                callback.onActivityResult(requsetCode,resultCode, intent);
            callbackMap.remove(requsetCode);
        }
    }

    public void registerPermissionsResult(int requsetCode, PermissionCallback callback) {
        if (callback == null)
            return;
        permissionCallbackMap.put(requsetCode, callback);
    }

    public void permissionsResult(Context context, int requestCode, @NonNull String[] permissions) {
        if (permissionCallbackMap.containsKey(requestCode)) {
            PermissionCallback callback = permissionCallbackMap.get(requestCode);
            callback.result(check(context, permissions));
            permissionCallbackMap.remove(requestCode);
        }
    }

    public String[] check(Context context, String... permissions) {
        if (context == null || permissions == null || permissions.length <= 0)
            return permissions;
        List<String> no = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : permissions) {
                if (context.checkSelfPermission(permission) != PermissionChecker.PERMISSION_GRANTED)
                    no.add(permission);
            }
        } else {
            try {
                String[] manifestPermissions = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
                for (String permission : permissions) {
                    boolean is = false;
                    for (String manifestPermission : manifestPermissions) {
                        if (manifestPermission.equals(permission)) {
                            is = true;
                            break;
                        }
                    }
                    if (!is)
                        no.add(permission);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return no.toArray(new String[no.size()]);
    }
}
