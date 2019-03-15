package com.chat.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.chat.result.ResultImpl;


/**
 * com.chat.activity
 * 2019/3/12 11:47
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class BaseActivity extends FragmentActivity {
    protected ResultImpl mResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResult = new ResultImpl(this);
    }
}
