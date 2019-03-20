package com.chat;

import android.os.Build;
import android.os.Process;

import com.chat.utils.FontsUtils;
import com.chat.utils.LogUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * com.chat
 * 2019/3/12 11:41
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class Application extends android.app.Application implements Thread.UncaughtExceptionHandler{
    private static final String TAG="Application";
    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        //设置全局默认字体样式
        FontsUtils.setDefaultFont(this, "SERIF", "fonts/handwritten.ttf");
        defaultUncaughtExceptionHandler=Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String throwa = throwable2string(throwable);
        saveErr(throwa);
        if (defaultUncaughtExceptionHandler != null) {
            defaultUncaughtExceptionHandler.uncaughtException(thread, throwable);
        } else {
            System.exit(0);
            Process.killProcess(Process.myPid());
        }
    }
    protected void saveErr(String err) {
        LogUtil.e(TAG, err);
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     * @throws Throwable
     */
    private String throwable2string(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        sb.append("model:");
        sb.append(Build.MODEL);
        sb.append("\nmake:");
        sb.append(Build.MANUFACTURER);
        sb.append("\nbrand:");
        sb.append(Build.BRAND);
        sb.append("\nsystem_version:");
        sb.append(Build.VERSION.RELEASE);
        sb.append("\n");
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        return sb.toString();
    }
}
