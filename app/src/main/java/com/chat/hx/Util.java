package com.chat.hx;

import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * com.video.hx
 * 2019/1/16 12:13
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class Util {
    private static final String TAG = "HxUtil";

    /**
     * 2019/1/16 12:33
     * annotation：发送消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static void sendMessage(EMMessage message, String userName, String groupId, EMCallBack callBack) {
        if (message == null)
            return;
        if (groupId == null || groupId.isEmpty())
            message.setTo(userName);
        else {
            message.setTo(groupId);
            message.setChatType(EMMessage.ChatType.GroupChat);
        }
        if (callBack != null)
            message.setMessageStatusCallback(callBack);
        else
            message.setMessageStatusCallback(new EMCallBack() {
                @Override
                public void onSuccess() {
                    Log.e(TAG, "发送成功");
                }

                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "发送失败：" + s);
                }

                @Override
                public void onProgress(int i, String s) {
                    Log.e(TAG, "进度" + i);
                }
            });
        Log.e(TAG, "发送消息");
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    /**
     * 2019/1/17 12:23
     * annotation：发送消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static void sendMessage(EMMessage message, String userName, String groupId) {
        sendMessage(message, userName, groupId, null);
    }

    /**
     * 2019/1/16 12:35
     * annotation：发送消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static void sendMessage(EMMessage message, String userName) {
        sendMessage(message, userName, null);
    }

    /**
     * 2019/1/16 12:35
     * annotation：发送群聊消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static void sendMessageToGroup(EMMessage message, String groupId) {
        sendMessage(message, null, groupId);
    }

    /**
     * 2019/1/16 12:20
     * annotation：创建文本消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static EMMessage createTxtMessage(String content) {
        return EMMessage.createTxtSendMessage(content == null || content.isEmpty() ? "null" : content, "");
    }

    /**
     * 2019/1/16 12:22
     * annotation：创建语音消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static EMMessage createVoice(String filePath, int length) {
        //filePath为语音文件路径，length为录音时间(秒)
        return EMMessage.createVoiceSendMessage(filePath, length, "");
    }

    /**
     * 2019/1/16 12:25
     * annotation：创建视频消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static EMMessage createVideo(String videoPath, String thumbPath, int videoLength) {
        //videoPath为视频本地路径，thumbPath为视频预览图路径，videoLength为视频时间长度
        return EMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, "");
    }

    /**
     * 2019/1/16 12:30
     * annotation：创建图文消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static EMMessage createImage(String imagePath, boolean original) {
        //imagePath为图片本地路径，false为不发送原图（默认超过100k的图片会压缩后发给对方），需要发送原图传true
        return EMMessage.createImageSendMessage(imagePath, original, "");
    }

    /**
     * 2019/1/16 12:30
     * annotation：创建地理位置消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static EMMessage createLocation(double latitude, double longitude, String locationAddress) {
        //latitude为纬度，longitude为经度，locationAddress为具体位置内容
        return EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, "");
    }

    /**
     * 2019/1/16 12:39
     * annotation：创建文件消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static EMMessage createFile(String filePath) {
        return EMMessage.createFileSendMessage(filePath, "");
    }

    /**
     * 2019/1/16 12:42
     * annotation：创建透传消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static EMMessage createPassthrough() {
        return EMMessage.createSendMessage(EMMessage.Type.CMD);
    }

    /**
     * 2019/1/16 12:53
     * annotation：添加拓展消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static EMMessage addExpand(EMMessage message, Map<String, Object> expands) {
        if (expands == null || expands.isEmpty() || message == null)
            return message;
        Set<Map.Entry<String, Object>> iterator = expands.entrySet();
        for (Map.Entry<String, Object> entry : iterator) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                message.setAttribute(key, (String) value);
            } else if (value instanceof Integer) {
                message.setAttribute(key, (Integer) value);
            } else if (value instanceof Long) {
                message.setAttribute(key, (Long) value);
            } else if (value instanceof Boolean) {
                message.setAttribute(key, (Boolean) value);
            } else if (value instanceof JSONObject) {
                message.setAttribute(key, (JSONObject) value);
            } else if (value instanceof JSONArray) {
                message.setAttribute(key, (JSONArray) value);
            }
        }
        return message;
    }

    /**
     * 2019/1/16 12:58
     * annotation：删除聊天
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static void delete(String username) {
//删除和某个user会话，如果需要保留聊天记录，传false
        EMClient.getInstance().chatManager().deleteConversation(username, true);
    }

    /**
     * 2019/1/16 12:58
     * annotation：删除某条消息
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static void delete(String username, String msgId) {
        //删除当前会话的某条聊天记录
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        conversation.removeMessage(msgId);
    }

    /**
     * 2019/1/16 13:01
     * annotation：获取消息数量
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static int count(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
//获取此会话在本地的所有的消息数量
        return conversation.getAllMsgCount();
//如果只是获取当前在内存的消息数量，调用
//       conversation.getAllMessages().size();
    }

    public static List<EMMessage> getAllMsg(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
//获取此会话在本地的所有的消息数量
        if (conversation == null)
            return null;
        return conversation.getAllMessages();
//如果只是获取当前在内存的消息数量，调用
    }

    public static EMMessage lastMsg(String username) {
        List<EMMessage> messages = getAllMsg(username);
        if (messages != null && messages.size() > 0)
            return messages.get(messages.size() - 1);
        return null;
    }


    /**
     * 2019/1/16 13:02
     * annotation：所有消息标记为已读
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static void read() {
        //所有未读消息数清零
        EMClient.getInstance().chatManager().markAllConversationsAsRead();
    }

    /**
     * 2019/1/16 14:40
     * annotation：指定会话消息未读数清零
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static void readAll(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        //指定会话消息未读数清零
        if (conversation == null)
            return;
        conversation.markAllMessagesAsRead();
    }

    /**
     * 2019/1/16 14:41
     * annotation：指定消息标记为已读
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public static void read(String username, String msgId) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
//把一条消息置为已读
        conversation.markMessageAsRead(msgId);
    }

    public static int unRead(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        if (conversation == null)
            return 0;
        return conversation.getUnreadMsgCount();
    }

    public static String getString(EMMessage message, String key) {
        try {
            return message.getStringAttribute(key);
        } catch (Exception e) {
            Log.e(TAG, "没有这个字段");
            return null;
        }
    }

}
