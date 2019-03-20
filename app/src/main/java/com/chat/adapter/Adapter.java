package com.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.chat.adapter.tag.BaseTag;

import java.util.ArrayList;
import java.util.List;

public abstract class Adapter<T> extends BaseAdapter {
    private List<T> objects;
    protected Context context;

    public Adapter(AdapterView adapterView) {
        // TODO Auto-generated constructor stub
        objects = new ArrayList<T>();
        context = adapterView.getContext();
        adapterView.setAdapter(this);
    }

    public void addItem(T t) {
        if (t == null)
            return;
        objects.add(t);
        notifyDataSetChanged();
    }

    public void addItem(List<T> ts) {
        if (ts == null || ts.size() <= 0)
            return;
        objects.addAll(ts);
        notifyDataSetChanged();
    }

    public void remove(T t) {
        if (t == null)
            return;
        if (objects.remove(t))
            notifyDataSetChanged();
    }

    public void remove(List<T> ts) {
        if (ts == null || ts.size() <= 0)
            return;
        if (objects.removeAll(ts))
            notifyDataSetChanged();
    }

    public void clean() {
        objects.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return objects.size();
    }

    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        // TODO Auto-generated method stub
        return super.getViewTypeCount();
    }

    @Override
    public T getItem(int arg0) {
        // TODO Auto-generated method stub
        return objects.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return getItem(arg0).hashCode();
    }

    @Override
    public View getView(int position, View converView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        int itemType = getItemViewType(position);
        if (converView == null)
            converView = getView(itemType);
        if (converView == null)
            return null;
        BaseTag<T> baseTag = (BaseTag<T>) converView.getTag();
        if (baseTag == null) {
            baseTag = getViewHolder(itemType);
            baseTag.setAdapter(this);
            baseTag.setView(converView);
            converView.setTag(baseTag);
        }
        baseTag.setContext(getItem(position));
        return converView;
    }

    public abstract BaseTag<T> getViewHolder(int itemType);

    public abstract View getView(int itemType);

}
