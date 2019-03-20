package com.chat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.List;


/**
 * com.tvblack.lamp.adapter
 * 2019/2/28 12:54
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class PagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public PagerAdapter(FragmentActivity activity, List<Fragment> fragments, ViewPager viewPager) {
        // TODO Auto-generated constructor stub
        super(activity.getSupportFragmentManager());
        this.fragments = fragments;
        viewPager.setAdapter(this);
    }

    public PagerAdapter(Fragment fragment, List<Fragment> fragments) {
        // TODO Auto-generated constructor stub
        super(fragment.getChildFragmentManager());
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int arg0) {
        // TODO Auto-generated method stub
        return fragments.get(arg0);
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return fragments == null ? 0 : fragments.size();
    }
}
