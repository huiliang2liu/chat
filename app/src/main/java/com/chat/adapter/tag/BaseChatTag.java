package com.chat.adapter.tag;

import android.view.View;

import com.hyphenate.chat.EMMessage;

/**
 * com.chat.adapter.tag
 * 2019/3/20 10:46
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class BaseChatTag extends BaseTag<EMMessage> {
    @Override
    public void setView(View context) {

    }

    @Override
    public void setContext(EMMessage message) {
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            if (message.isUnread()) {
                message.setUnread(true);
            }
        }
    }
}
