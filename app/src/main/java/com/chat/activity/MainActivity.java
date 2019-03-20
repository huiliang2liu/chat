package com.chat.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.R;
import com.chat.adapter.PagerAdapter;
import com.chat.fragment.FindFragment;
import com.chat.fragment.FriendFragment;
import com.chat.fragment.MessageFragment;
import com.chat.fragment.MyFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.main_search)
    LinearLayout mainSearch;
    @BindView(R.id.main_add)
    LinearLayout mainAdd;
    @BindView(R.id.main_vp)
    ViewPager mainVp;
    @BindView(R.id.main_page1)
    LinearLayout mainPage1;
    @BindView(R.id.main_page2)
    LinearLayout mainPage2;
    @BindView(R.id.main_page3)
    LinearLayout mainPage3;
    @BindView(R.id.main_page4)
    LinearLayout mainPage4;
    @BindView(R.id.main_tv)
    TextView mainTv;
    private MessageFragment messageFragment;
    private FriendFragment friendFragment;
    private FindFragment findFragment;
    private MyFragment myFragment;
    private boolean finish = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            finish = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mainSearch.setOnClickListener(this);
        mainAdd.setOnClickListener(this);
        mainPage1.setOnClickListener(this);
        mainPage2.setOnClickListener(this);
        mainPage3.setOnClickListener(this);
        mainPage4.setOnClickListener(this);
        messageFragment = new MessageFragment();
        friendFragment = new FriendFragment();
        findFragment = new FindFragment();
        myFragment = new MyFragment();
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(messageFragment);
        fragments.add(friendFragment);
        fragments.add(findFragment);
        fragments.add(myFragment);
        new PagerAdapter(this, fragments, mainVp);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.main_search) {

        } else if (id == R.id.main_add) {

        } else if (id == R.id.main_page1) {
            mainVp.setCurrentItem(0);
            mainTv.setText("微信");
        } else if (id == R.id.main_page2) {
            mainVp.setCurrentItem(1);
            mainTv.setText("通讯录");
        } else if (id == R.id.main_page3) {
            mainVp.setCurrentItem(2);
            mainTv.setText("发现");
        } else if (id == R.id.main_page4) {
            mainVp.setCurrentItem(3);
            mainTv.setText("我");
        }
    }

    @Override
    public void onBackPressed() {
        if (finish)
            super.onBackPressed();
        else {
            Toast.makeText(this, "再按一次退出程序 !", Toast.LENGTH_SHORT).show();
            finish = true;
            handler.sendEmptyMessageDelayed(0, 3000);
        }
    }
}
