package com.chat.adapter.tag;

import android.view.View;
import android.widget.BaseAdapter;

/**
 * com.tvblack.lamp.adapter.tag
 * 2019/2/18 16:40
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public abstract class BaseTag<T> {
    protected BaseAdapter adapter;

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    public abstract void setView(View context);

    public abstract void setContext(T t);
}
