package com.chat.adapter;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.chat.adapter.tag.BaseTag;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

/**
 * com.chat.adapter.tag
 * 2019/3/20 10:17
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class ChatAdapter extends Adapter<EMMessage> {
    private static final String TAG = "ChatAdapter";
    private EMConversation conversation;
    private String toName;

    public ChatAdapter(AdapterView adapterView, String toName) {
        super(adapterView);
        this.toName = toName;
        conversation = EMClient.getInstance().chatManager().getConversation(toName);
        if (conversation != null) {
            if (conversation.getAllMessages().size() > 0)
                conversation.loadMoreMsgFromDB(conversation.getAllMessages().get(0).getMsgId(), Integer.MAX_VALUE);
            conversation.markAllMessagesAsRead();
        }
    }

    @Override
    public int getCount() {
        if (conversation == null)
            return 0;
        Log.e(TAG, "conversation.getAllMsgCount():" + conversation.getAllMsgCount());
        return conversation.getAllMessages().size();
    }

    @Override
    public void notifyDataSetChanged() {
        conversation = EMClient.getInstance().chatManager().getConversation(toName);
        super.notifyDataSetChanged();
    }

    @Override
    public EMMessage getItem(int position) {
        return conversation.getAllMessages().get(position);
    }

    @Override
    public int getViewTypeCount() {
        return 12;
    }

    @Override
    public int getItemViewType(int position) {
        EMMessage message = getItem(position);
        if (message.getType() == EMMessage.Type.TXT) {
            if (message.direct() == EMMessage.Direct.RECEIVE)
                return 0;
            return 1;
        }
        if (message.getType() == EMMessage.Type.IMAGE) {
            if (message.direct() == EMMessage.Direct.RECEIVE)
                return 2;
            return 3;
        }
        if (message.getType() == EMMessage.Type.LOCATION) {
            if (message.direct() == EMMessage.Direct.RECEIVE)
                return 4;
            return 5;
        }
        if (message.getType() == EMMessage.Type.VOICE) {
            if (message.direct() == EMMessage.Direct.RECEIVE)
                return 6;
            return 7;
        }
        if (message.getType() == EMMessage.Type.VIDEO) {
            if (message.direct() == EMMessage.Direct.RECEIVE)
                return 8;
            return 9;
        }
        if (message.getType() == EMMessage.Type.FILE) {
            if (message.direct() == EMMessage.Direct.RECEIVE)
                return 10;
            return 11;
        }
        return 0;
    }

    @Override
    public BaseTag<EMMessage> getViewHolder(int itemType) {
        return null;
    }

    @Override
    public View getView(int itemType) {
        return null;
    }
}
