package com.chat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chat.R;


/**
 * com.chat.fragment
 * 2019/3/20 16:16
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class MyFragment extends Fragment {
    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_my, null);
        }
        return mView;
    }
}
