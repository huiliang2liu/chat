package com.chat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.chat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * com.chat.fragment
 * 2019/3/20 15:36
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class MessageFragment extends Fragment {
    @BindView(R.id.message_lv)
    ListView messageLv;
    Unbinder unbinder;
    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_message, null);
            unbinder = ButterKnife.bind(this, mView);
        }
        return mView;
    }

}
