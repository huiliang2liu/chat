package com.chat.result;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.chat.result.activity.ResultCallback;
import com.chat.result.permission.PermissionCallback;
import com.chat.utils.IntentUtil;

import java.io.File;


/**
 * com.util.result
 * 2018/9/27 17:51
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class AndroidFragment extends Fragment implements Result {
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
    private ResultBack result = new ResultBack();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        result.activityResult(requestCode, resultCode, data);
    }

    @Override
    public void startActivityForResult(int requsetCode, Intent intent, ResultCallback... callbacks) {
        result.registerActivityResult(requsetCode, callbacks);
        startActivityForResult(intent, requsetCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        result.permissionsResult(getActivity(), requestCode, permissions);

    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @Override
    public void requestPermissions(int requsetCode, PermissionCallback callback, String... permissions) {
        result.registerPermissionsResult(requsetCode, callback);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            Log.e("permission", "权限申请");
            requestPermissions(permissions, requsetCode);
        } else {
            result.permissionsResult(getActivity(), requsetCode, permissions);
        }

    }

    public String[] check(String... permissions) {
        return result.check(getActivity(), permissions);
    }

    @Override
    public void photo2PhotoAlbum(ResultCallback... callback) {
        Intent intent = IntentUtil.photo2PhotoAlbum();
        startActivityForResult(PHOTO_ALBUM, intent, callback);
    }

    @Override
    public void openCamera(@NonNull File file, String authority, ResultCallback... callback) {
        Intent intent = IntentUtil.openCamera(getActivity(), file, authority);
        startActivityForResult(PHOTO_CAMERA, intent, callback);
    }

    @Override
    public void screenshots(@NonNull File imageFile, String authorityImage, @NonNull File saveFile, String authoritySave, int aspectX, int aspectY, int outputX, int outputY, ResultCallback... callback) {
        Intent intent = IntentUtil.screenshots(getActivity(), imageFile, authorityImage, saveFile, authoritySave, aspectX, aspectY, outputX, outputY);
        startActivityForResult(SCREENSHOT, intent, callback);
    }

    @Override
    public void screenshots(@NonNull Uri imageUri, @NonNull Uri saveUri, int aspectX, int aspectY, int outputX, int outputY, ResultCallback... callback) {
        Intent intent = IntentUtil.screenshots(getActivity(), imageUri, saveUri, aspectX, aspectY, outputX, outputY);
        startActivityForResult(SCREENSHOT, intent, callback);
    }
}
