package com.chat.result.permission;

/**
 * com.util.result
 * 2018/9/27 17:05
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public interface IResult {
    /**
     * 2019/1/8 10:49
     * annotation：请求权限
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    void requestPermissions(int requsetCode, PermissionCallback callback, String... permissions);

    /**
     * 2019/1/8 10:49
     * annotation：检查权限
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    String[] check(String... permissions);
}
