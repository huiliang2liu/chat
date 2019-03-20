package com.chat.hx;

import android.os.Handler;

import com.chat.Application;
import com.chat.net.NetworkUtil;
import com.chat.utils.LogUtil;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMGroupOptions;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMucSharedFile;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.exceptions.HyphenateException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * com.chat.hx
 * 2019/3/12 12:03
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class HxManager implements EMConnectionListener, EMMessageListener, EMGroupChangeListener {
    private static final String TAG = "HxManager";
    private EMClient mEmClient;
    private Application mApplication;
    private Handler handler = new Handler();
    private List<HxStatusListener> mStatusListeners = new ArrayList<>();
    private Map<String, HxMessageListener> messageListenerMap = new HashMap<>();
    private String mUserName;

    public HxManager(Application application) {
        mApplication = application;
        EMOptions options = new EMOptions();
// 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
// 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载，如果设为 false，需要开发者自己处理附件消息的上传和下载
        options.setAutoTransferMessageAttachments(true);
// 是否自动下载附件类消息的缩略图等，默认为 true 这里和上边这个参数相关联
        options.setAutoDownloadThumbnail(true);
        options.setAutoLogin(true);
//初始化
        EMClient.getInstance().init(application, options);
//在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
        mEmClient.addConnectionListener(this);
        mEmClient.chatManager().removeMessageListener(this);
    }

    public void registerHxStatusListener(HxStatusListener statusListener) {
        if (statusListener == null)
            return;
        mStatusListeners.add(statusListener);
    }

    public void unRegisterHxStatusListener(HxStatusListener statusListener) {
        if (statusListener == null)
            return;
        mStatusListeners.remove(statusListener);
    }

    public void registerHxMessageListener(String name, HxMessageListener messageListener) {
        if (name == null || name.isEmpty() || messageListener == null)
            return;
        messageListenerMap.put(name, messageListener);
    }

    public void unRegisterHxMessageListener(String name, HxMessageListener messageListener) {
        if (name == null || name.isEmpty() || messageListener == null)
            return;
        messageListenerMap.remove(name);
    }


    //连接状态
    @Override
    public void onConnected() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (HxStatusListener statusListener : mStatusListeners)
                    statusListener.connected();
            }
        });
    }

    @Override
    public void onDisconnected(final int error) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (error == EMError.USER_REMOVED) {
                    LogUtil.e(TAG, "显示帐号已经被移除");
                    for (HxStatusListener statusListener : mStatusListeners)
                        statusListener.userDelete();
                } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    LogUtil.e(TAG, "显示帐号在其他设备登录");
                    for (HxStatusListener statusListener : mStatusListeners)
                        statusListener.anotherPlaceLogin();
                } else {
                    for (HxStatusListener statusListener : mStatusListeners)
                        statusListener.lostConnection();
                    if (NetworkUtil.isNetConnected(mApplication))
                        LogUtil.e(TAG, "连接不到聊天服务器");
                    else
                        LogUtil.e(TAG, "当前网络不可用，请检查网络设置");
                }
            }
        });
    }

    //消息监听
    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        LogUtil.e(TAG, "收到消息");
//        List<EMMessage> msgs=new ArrayList<>();
        for (final EMMessage message : messages) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String key = "";
                    if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                        key = message.getTo();
                    } else {
                        key = message.getFrom();
                    }
                    if (messageListenerMap.containsValue(key)) {
                        HxMessageListener listener = messageListenerMap.get(key);
                        listener.messageReceived(message);
                    }
                }
            });
//            msgs.add(message);
        }
        EMClient.getInstance().chatManager().importMessages(messages);
    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {
        LogUtil.e(TAG, "收到透传消息");
    }

    @Override
    public void onMessageRead(List<EMMessage> messages) {
        LogUtil.e(TAG, "收到已读回执");
        for (final EMMessage message : messages) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    message.setUnread(false);
                    String key;
                    key = message.getTo();
                    if (messageListenerMap.containsKey(key)) {
                        HxMessageListener listener = messageListenerMap.get(key);
                        listener.messageChanged();
                    }
                }
            });
        }

    }

    @Override
    public void onMessageDelivered(List<EMMessage> messages) {
        LogUtil.e(TAG, "收到已送达回执");
        for (final EMMessage message : messages) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    message.setDelivered(true);
                    String key;
                    key = message.getTo();
                    if (messageListenerMap.containsKey(key)) {
                        HxMessageListener listener = messageListenerMap.get(key);
                        listener.messageChanged();
                    }
                }
            });
        }

    }

    @Override
    public void onMessageRecalled(List<EMMessage> messages) {
        LogUtil.e(TAG, "消息被撤回");
        //删除当前会话的某条聊天记录
        for (EMMessage message : messages) {
            String name = message.getFrom().equals(mUserName) ? message.getFrom() : message.getTo();
            EMConversation conversation = mEmClient.chatManager().getConversation(name);
            conversation.removeMessage(message.getMsgId());
        }
    }

    @Override
    public void onMessageChanged(final EMMessage message, Object change) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                LogUtil.e(TAG, "消息状态变动");
                String key;
                if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                    key = message.getTo();
                } else {
                    key = message.getFrom();
                }
                if (messageListenerMap.containsKey(key)) {
                    HxMessageListener listener = messageListenerMap.get(key);
                    listener.messageChanged();
                }
            }
        });
    }

    //群组监听
    @Override
    public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
        //接收到群组加入邀请
    }

    @Override
    public void onRequestToJoinReceived(String groupId, String groupName, String applyer, String reason) {
        //用户申请加入群
    }

    @Override
    public void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {
        //加群申请被同意
    }

    @Override
    public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {
        //加群申请被拒绝
    }

    @Override
    public void onInvitationAccepted(String groupId, String inviter, String reason) {
        //群组邀请被同意
    }

    @Override
    public void onInvitationDeclined(String groupId, String invitee, String reason) {
        //群组邀请被拒绝
    }

    @Override
    public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {
        //接收邀请时自动加入到群组的通知
    }

    @Override
    public void onMuteListAdded(String groupId, final List<String> mutes, final long muteExpire) {
        //成员禁言的通知
    }

    @Override
    public void onMuteListRemoved(String groupId, final List<String> mutes) {
        //成员从禁言列表里移除通知
    }

    @Override
    public void onAdminAdded(String groupId, String administrator) {
        //增加管理员的通知
    }

    @Override
    public void onAdminRemoved(String groupId, String administrator) {
        //管理员移除的通知
    }

    @Override
    public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {
        //群所有者变动通知
    }

    @Override
    public void onMemberJoined(final String groupId, final String member) {
        //群组加入新成员通知
    }

    @Override
    public void onMemberExited(final String groupId, final String member) {
        //群成员退出通知
    }

    @Override
    public void onAnnouncementChanged(String groupId, String announcement) {
        //群公告变动通知
    }

    @Override
    public void onSharedFileAdded(String groupId, EMMucSharedFile sharedFile) {
        //增加共享文件的通知
    }

    @Override
    public void onSharedFileDeleted(String groupId, String fileId) {
        //群共享文件删除通知
    }

    @Override
    public void onUserRemoved(final String groupId, final String groupIdNickName) {
        //用户被移除
    }

    @Override
    public void onGroupDestroyed(final String s, final String s1) {
        //群组被销毁
    }

    /**
     * 发送消息
     *
     * @param message
     * @param name
     * @param group
     * @param callBack
     */
    public void sendMessage(EMMessage message, String name, boolean group, EMCallBack callBack) {
        message.setMessageStatusCallback(callBack);
        message.setTo(name);
        if (group)
            message.setChatType(EMMessage.ChatType.GroupChat);
        mEmClient.chatManager().sendMessage(message);
    }

    public void sendMessage(EMMessage message, String name, EMCallBack callBack) {
        sendMessage(message, name, false, callBack);
    }

    public void sendMessage(EMMessage message, String name) {
        sendMessage(message, name, new EMCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    public void sendGroupMessage(EMMessage message, String groupId, EMCallBack callBack) {
        sendMessage(message, groupId, true, callBack);
    }

    public void sendGroupMessage(EMMessage message, String groupId) {
        sendGroupMessage(message, groupId, new EMCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    /**
     * 创建一条文本消息
     *
     * @param content
     * @return
     */
    public EMMessage createTxtSendMessage(String content) {
        return EMMessage.createTxtSendMessage(content, "");
    }

    /**
     * 创建一条语音消息
     *
     * @param filePath
     * @param length   录音时间(秒)
     * @return
     */
    public EMMessage createVoiceSendMessage(String filePath, int length) {
        return EMMessage.createVoiceSendMessage(filePath, length, "");
    }

    /**
     * 创建一条视频消息
     *
     * @param videoPath   为视频本地路径
     * @param thumbPath   为视频预览图路径
     * @param videoLength 为视频时间长度
     * @return
     */
    public EMMessage createVideoSendMessage(String videoPath, String thumbPath, int videoLength) {
        return EMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, "");
    }

    /**
     * 创建一条图片消息
     *
     * @param imagePath 为图片本地路径
     * @param original  是否发送原图
     * @return
     */
    public EMMessage createImageSendMessage(String imagePath, boolean original) {
        return EMMessage.createImageSendMessage(imagePath, original, "");
    }

    /**
     * 创建一条地理位置消息
     *
     * @param latitude        纬度
     * @param longitude       经度
     * @param locationAddress 具体位置
     * @return
     */
    public EMMessage createLocationSendMessage(double latitude, double longitude, String locationAddress) {
        return EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, "");
    }

    /**
     * 创建一条文件消息
     *
     * @param filePath
     * @return
     */
    public EMMessage createFileSendMessage(String filePath) {
        return EMMessage.createFileSendMessage(filePath, "");
    }

    /**
     * 创建一条拓展消息
     *
     * @param expands
     * @return
     */
    public EMMessage createExpand(Map<String, Object> expands) {
        return addExpand(createTxtSendMessage("aaa"), expands);
    }

    /**
     * 创建一条透传消息
     *
     * @param action
     * @return
     */
    public EMMessage createCmdMessage(String action) {
        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.addBody(cmdBody);
        return cmdMsg;
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
     * 获取未读消息数量
     *
     * @param username
     * @return
     */
    public int getUnreadMsgCount(String username) {
        EMConversation conversation = mEmClient.chatManager().getConversation(username);
        if (conversation == null)
            return 0;
        return conversation.getUnreadMsgCount();
    }


    /**
     * 指定会话消息未读数清零
     *
     * @param username
     */
    public void markAllMessagesAsRead(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        if (conversation == null)
            return;
        conversation.markAllMessagesAsRead();
    }

    /**
     * 把一条消息置为已读
     *
     * @param username
     * @param messageId
     */
    public void markMessageAsRead(String username, String messageId) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        if (conversation == null)
            return;
        conversation.markMessageAsRead(messageId);
    }

    /**
     * 把一条消息置为已读
     *
     * @param username
     * @param message
     */
    public void markMessageAsRead(String username, EMMessage message) {
        markMessageAsRead(username, message.getMsgId());
    }

    /**
     * 所有未读消息数清零
     */
    public void markAllConversationsAsRead() {
        mEmClient.chatManager().markAllConversationsAsRead();
    }

    /**
     * 删除和某个user会话
     *
     * @param username
     * @param save     是否保留聊天记录
     */
    public void deleteConversation(String username, boolean save) {
        mEmClient.chatManager().deleteConversation(username, save);
    }

    /**
     * 删除当前会话的某条聊天记录
     *
     * @param username
     * @param msgId
     */
    public void removeMessage(String username, String msgId) {
        EMConversation conversation = mEmClient.chatManager().getConversation(username);
        if (conversation == null)
            return;
        conversation.removeMessage(msgId);
    }

    /**
     * 删除当前会话的某条聊天记录
     *
     * @param username
     * @param message
     */
    public void removeMessage(String username, EMMessage message) {
        removeMessage(username, message.getMsgId());
    }

    /**
     * 2019/3/20 15:06
     * annotation：获取所有会话
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public Map<String, EMConversation> getAllConversations() {
        return EMClient.getInstance().chatManager().getAllConversations();
    }

    /**
     * 创建群组
     *
     * @param groupName  群组名称
     * @param desc       群组简介
     * @param allMembers 群组初始成员，如果只有自己传空数组即可
     * @param reason     邀请成员加入的reason
     */
    public void createGroup(String groupName, String desc, String[] allMembers, String reason) {
        try {
            EMGroupOptions option = new EMGroupOptions();
            option.maxUsers = 200;
            option.style = EMGroupManager.EMGroupStyle.EMGroupStylePrivateMemberCanInvite;
            mEmClient.groupManager().createGroup(groupName, desc, allMembers, reason, option);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加群组管理员，需要owner权限
     *
     * @param groupId
     * @param admin
     */
    public void addGroupAdmin(String groupId, String admin) {
        try {
            mEmClient.groupManager().addGroupAdmin(groupId, admin);//需异部处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除群组管理员，需要owner权限
     *
     * @param groupId
     * @param admin
     * @return
     */
    public void removeGroupAdmin(String groupId, String admin) {
        try {
            mEmClient.groupManager().removeGroupAdmin(groupId, admin);//需异部处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 群组所有权给他人
     *
     * @param groupId
     * @param newOwner
     * @return
     */
    public void changeOwner(String groupId, String newOwner) {
        try {
            mEmClient.groupManager().changeOwner(groupId, newOwner);//需异部处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 群主加人调用此方法
     *
     * @param groupId
     * @param newmembers
     */
    public void addUsersToGroup(String groupId, String[] newmembers) {
        try {
            mEmClient.groupManager().addUsersToGroup(groupId, newmembers);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 私有群里，如果开放了群成员邀请，群成员邀请调用下面方法
     *
     * @param groupId
     * @param newmembers
     */
    public void inviteUser(String groupId, String[] newmembers) {
        try {
            mEmClient.groupManager().inviteUser(groupId, newmembers, null);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把username从群组里删除
     *
     * @param groupId
     * @param username
     */
    public void removeUserFromGroup(String groupId, String username) {
        try {
            mEmClient.groupManager().removeUserFromGroup(groupId, username);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果群开群是自由加入的，即group.isMembersOnly()为false，直接join
     *
     * @param groupid
     */
    public void joinGroup(String groupid) {
        try {
            mEmClient.groupManager().joinGroup(groupid);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 需要申请和验证才能加入的，即group.isMembersOnly()为true，调用下面方法
     *
     * @param groupid
     * @param reason
     */
    public void applyJoinToGroup(String groupid, String reason) {
        try {
            mEmClient.groupManager().applyJoinToGroup(groupid, reason);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出群组
     *
     * @param groupId
     */
    public void leaveGroup(String groupId) {
        try {
            mEmClient.groupManager().leaveGroup(groupId);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解散群组
     *
     * @param groupId
     */
    public void destroyGroup(String groupId) {
        try {
            mEmClient.groupManager().destroyGroup(groupId);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改群名称
     *
     * @param groupId
     * @param changedGroupName
     */
    public void changeGroupName(String groupId, String changedGroupName) {
        try {
            mEmClient.groupManager().changeGroupName(groupId, changedGroupName);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }


    /**
     * 修改群描述
     *
     * @param groupId
     * @param description
     */
    public void changeGroupDescription(String groupId, String description) {
        try {
            mEmClient.groupManager().changeGroupDescription(groupId, description);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 屏蔽群消息后，就不能接收到此群的消息（还是群里面的成员，但不再接收消息）
     *
     * @param groupId， 群ID
     */
    public void blockGroupMessage(String groupId) {
        try {
            mEmClient.groupManager().blockGroupMessage(groupId);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消屏蔽群消息，就可以正常收到群的所有消息
     *
     * @param groupId
     */
    public void unblockGroupMessage(String groupId) {
        try {
            mEmClient.groupManager().unblockGroupMessage(groupId);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新群公告
     *
     * @param groupId      群id
     * @param announcement 公告内容
     */
    public void updateGroupAnnouncement(String groupId, String announcement) {
        try {
            mEmClient.groupManager().updateGroupAnnouncement(groupId, announcement);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取群公告
     *
     * @param groupId
     */
    public void fetchGroupAnnouncement(String groupId) {
        try {
            mEmClient.groupManager().fetchGroupAnnouncement(groupId);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传共享文件至群组，注意callback只做进度回调用
     *
     * @param groupId  群id
     * @param filePath 文件本地路径
     * @param callBack 回调
     */
    public void uploadGroupSharedFile(String groupId, String filePath, EMCallBack callBack) {
        if (callBack == null)
            callBack = new EMCallBack() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(int i, String s) {

                }

                @Override
                public void onProgress(int i, String s) {

                }
            };
        try {
            mEmClient.groupManager().uploadGroupSharedFile(groupId, filePath, callBack);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从群组里删除这个共享文件
     *
     * @param groupId 群id
     * @param fileId  文件id
     */
    public void deleteGroupSharedFile(String groupId, String fileId) {
        try {
            mEmClient.groupManager().deleteGroupSharedFile(groupId, fileId);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从服务器获取群组的共享文件列表
     *
     * @param groupId  群id
     * @param pageNum  分页号
     * @param pageSize 分页大小
     */
    public void fetchGroupSharedFileList(String groupId, int pageNum, int pageSize) {
        try {
            mEmClient.groupManager().fetchGroupSharedFileList(groupId, pageNum, pageSize);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载群里的某个共享文件，注意callback只做进度回调用
     *
     * @param groupId  群id
     * @param fileId   文件id
     * @param savePath 文件保存路径
     * @param callBack 回调
     */
    public void downloadGroupSharedFile(String groupId, String fileId, String savePath, EMCallBack callBack) {
        if (callBack == null)
            callBack = new EMCallBack() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(int i, String s) {

                }

                @Override
                public void onProgress(int i, String s) {

                }
            };
        try {
            EMClient.getInstance().groupManager().downloadGroupSharedFile(groupId, fileId, savePath, callBack);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * annotation:注册
     *
     * @param userName
     * @param pass
     * @return
     */
    public boolean register(String userName, String pass) {
        try {
            mEmClient.createAccount(userName, pass);//同步方法
            return true;
        } catch (HyphenateException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void login(String userName, String password) {
        mUserName = userName;
        mEmClient.login(userName, password, new EMCallBack() {//回调
            @Override
            public void onSuccess() {
                mEmClient.groupManager().loadAllGroups();
                mEmClient.chatManager().loadAllConversations();
                LogUtil.e(TAG, "登录聊天服务器成功！");
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                LogUtil.e(TAG, "登录聊天服务器失败！");
            }
        });
    }

    public void logout() {
        mEmClient.logout(true);
    }

    public void destory() {
//        mEmClient.
        mStatusListeners.clear();
        messageListenerMap.clear();
    }

    public interface HxStatusListener {
        /**
         * 2019/3/12 12:42
         * annotation：和服务器失去连接
         * author：liuhuiliang
         * email ：825378291@qq.com
         */
        void lostConnection();

        /**
         * 2019/3/12 12:42
         * annotation：用户被删除了
         * author：liuhuiliang
         * email ：825378291@qq.com
         */
        void userDelete();

        /**
         * 2019/3/12 12:42
         * annotation：异地登录
         * author：liuhuiliang
         * email ：825378291@qq.com
         */
        void anotherPlaceLogin();

        /**
         * 2019/3/12 12:46
         * annotation：连接上服务器
         * author：liuhuiliang
         * email ：825378291@qq.com
         */
        void connected();
    }

    public interface HxCmdMessageListener {
        /**
         * 2019/3/12 12:53
         * annotation：收到透传消息
         * author：liuhuiliang
         * email ：825378291@qq.com
         */
        void cmdMessageReceived(EMMessage message);
    }

    public interface HxMessageListener {
        /**
         * 2019/3/12 12:52
         * annotation：收到消息
         * author：liuhuiliang
         * email ：825378291@qq.com
         */
        void messageReceived(EMMessage message);

        /**
         * 2019/3/12 13:59
         * annotation：消息状态变动
         * author：liuhuiliang
         * email ：825378291@qq.com
         */
        void messageChanged();
    }
}
