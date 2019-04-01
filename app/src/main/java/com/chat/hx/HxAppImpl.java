package com.chat.hx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMucSharedFile;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.util.NetUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * com.video.hx
 * 2019/1/16 15:24
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class HxAppImpl implements IVideo, EMMessageListener, EMCallBack, EMConnectionListener, EMCallStateChangeListener, EMGroupChangeListener {
    private static final String TAG = "HxAppImpl";
    private static final String LOGIN = "HxAppImpl_login";
    private Handler mHandler = new Handler();
    private Context mContext;
    private boolean login = false;
    private String mConfwewnceId;
    private EMConferenceStream mStream;
    private ViewGroup mInput;
    private ViewGroup mOutput;
    private boolean video = true;
    private boolean audio = true;
    private String mPassword;
    private String mJid;
    private List<MessageListener> listeners;
    private List<GroupListener> groupListeners;
    private List<CameraListener> cameraListeners;
    private SharedPreferences sharedPreferences;
    private MeetRoom meetRoom;
    private List<String> ids = new ArrayList<>();
    private MeetRoom.MeetRoomSubscribeListener listener;
    private MeetRoom.MeetRoomListener meetRoomListener;

    public synchronized void registerListener(MessageListener listener) {
        if (listener == null)
            return;
        this.listeners.add(listener);
    }

    public synchronized void unRegisterListener(MessageListener listener) {
        if (listener == null)
            return;
        this.listeners.remove(listener);
    }

    public synchronized void registerGroupListener(GroupListener listener) {
        if (listener == null)
            return;
        this.groupListeners.add(listener);
    }

    public synchronized void unRegisterGroupListener(GroupListener listener) {
        if (listener == null)
            return;
        this.groupListeners.remove(listener);
    }

    public synchronized void registerCameraListener(CameraListener listener) {
        if (listener == null)
            return;
        this.cameraListeners.add(listener);
    }

    public synchronized void unRegisterCameraListener(CameraListener listener) {
        if (listener == null)
            return;
        this.cameraListeners.remove(listener);
    }

    public HxAppImpl(Context context) {
        mContext = context;
        sharedPreferences = context.getSharedPreferences("lamp", Context.MODE_PRIVATE);
        login = sharedPreferences.getBoolean(LOGIN, false);
        listeners = new ArrayList<>();
        groupListeners = new ArrayList<>();
        cameraListeners = new ArrayList<>();
        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
// 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载，如果设为 false，需要开发者自己处理附件消息的上传和下载
        options.setAutoTransferMessageAttachments(true);
// 是否自动下载附件类消息的缩略图等，默认为 true 这里和上边这个参数相关联
        options.setAutoDownloadThumbnail(true);
        options.setAutoLogin(false);//取消自动登录
//初始化
        EMClient.getInstance().init(context.getApplicationContext(), options);
//在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
        //注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(this);
        //注册消息监听
        EMClient.getInstance().chatManager().addMessageListener(this);
        //监听呼入通话
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        context.registerReceiver(new CallReceiver(), callFilter);
        //注册通话状态监听
        EMClient.getInstance().callManager().addCallStateChangeListener(this);
        //注册全在监听
        EMClient.getInstance().groupManager().addGroupChangeListener(this);
    }

    public synchronized void login(String userName, String password) {
        if (login)
            return;
        Log.e(TAG, "userName:" + userName + ",password:" + password);
        EMClient.getInstance().login(userName, password, this);
//        EMClient.getInstance().login("liuhuiliang", "liuhuiliang", this);
    }

    private class CallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 拨打方username
            String from = intent.getStringExtra("from");
            // call type
            String type = intent.getStringExtra("type");
            //跳转到通话页面
            try {
                EMClient.getInstance().callManager().answerCall();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    //监听群组
    @Override
    public synchronized void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
        //接收到群组加入邀请
        Log.e(TAG, "onInvitationReceived");
    }

    @Override
    public synchronized void onRequestToJoinReceived(String groupId, String groupName, String applyer, String reason) {
        //用户申请加入群
        Log.e(TAG, "onRequestToJoinReceived");
    }

    @Override
    public synchronized void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {
        //加群申请被同意
        Log.e(TAG, "onRequestToJoinAccepted");
    }

    @Override
    public synchronized void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {
        //加群申请被拒绝
        Log.e(TAG, "onRequestToJoinDeclined");
    }

    @Override
    public synchronized void onInvitationAccepted(String groupId, String inviter, String reason) {
        //群组邀请被同意
        Log.e(TAG, "onInvitationAccepted");
    }

    @Override
    public synchronized void onInvitationDeclined(String groupId, String invitee, String reason) {
        //群组邀请被拒绝
        Log.e(TAG, "onInvitationDeclined");
    }

    @Override
    public synchronized void onAutoAcceptInvitationFromGroup(final String groupId, final String inviter, final String inviteMessage) {
        //接收邀请时自动加入到群组的通知
        Log.e(TAG, "onAutoAcceptInvitationFromGroup groupId：" + groupId);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (GroupListener listener : groupListeners)
                    listener.onAutoAcceptInvitationFromGroup(groupId, inviter, inviteMessage);
            }
        });

    }

    @Override
    public synchronized void onMuteListAdded(String groupId, List<String> mutes, long muteExpire) {
        //成员禁言的通知
        Log.e(TAG, "onMuteListAdded");
    }

    @Override
    public synchronized void onMuteListRemoved(String groupId, List<String> mutes) {
        //成员从禁言列表里移除通知
        Log.e(TAG, "onMuteListRemoved");
    }

    @Override
    public synchronized void onAdminAdded(final String groupId, final String administrator) {
        //增加管理员的通知
        Log.e(TAG, "onAdminAdded");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (GroupListener listener : groupListeners)
                    listener.onAdminAdded(groupId, administrator);
            }
        });
    }

    @Override
    public synchronized void onAdminRemoved(final String groupId, final String administrator) {
        //管理员移除的通知
        Log.e(TAG, "onAdminAdded");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (GroupListener listener : groupListeners)
                    listener.onAdminRemoved(groupId, administrator);
            }
        });
    }

    @Override
    public synchronized void onOwnerChanged(String groupId, String newOwner, String oldOwner) {
        //群所有者变动通知
        Log.e(TAG, "onOwnerChanged");
    }

    @Override
    public synchronized void onMemberJoined(final String groupId, final String member) {
        //群组加入新成员通知
        Log.e(TAG, "onMemberJoined");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (GroupListener listener : groupListeners)
                    listener.onMemberJoined(groupId, member);
            }
        });

    }

    @Override
    public synchronized void onMemberExited(final String groupId, final String member) {
        //群成员退出通知
        Log.e(TAG, "onMemberExited groupId：" + groupId);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (GroupListener listener : groupListeners)
                    listener.onMemberExited(groupId, member);
            }
        });
    }

    @Override
    public synchronized void onAnnouncementChanged(String groupId, String announcement) {
        //群公告变动通知
        Log.e(TAG, "onAnnouncementChanged");
    }

    @Override
    public synchronized void onSharedFileAdded(String groupId, EMMucSharedFile sharedFile) {
        //增加共享文件的通知
        Log.e(TAG, "onSharedFileAdded");
    }

    @Override
    public synchronized void onSharedFileDeleted(String groupId, String fileId) {
        //群共享文件删除通知
        Log.e(TAG, "onSharedFileDeleted");
    }

    @Override
    public synchronized void onUserRemoved(final String groupId, final String groupIdNickName) {
        Log.e(TAG, "自己被删除群组" + groupId + groupIdNickName);
        Log.e(TAG, "onUserRemoved");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (GroupListener listener : groupListeners)
                    listener.onUserRemoved(groupId, groupIdNickName);
            }
        });
    }

    @Override
    public synchronized void onGroupDestroyed(final String s, final String s1) {
        Log.e(TAG, "onGroupDestroyed");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (GroupListener listener : groupListeners)
                    listener.onGroupDestroyed(s, s1);
            }
        });
    }

    //监听通话状态
    @Override
    public synchronized void onCallStateChanged(CallState callState, CallError error) {
        switch (callState) {
            case CONNECTING: // 正在连接对方

                break;
            case CONNECTED: // 双方已经建立连接

                break;

            case ACCEPTED: // 电话接通成功

                break;
            case DISCONNECTED: // 电话断了

                break;
            case NETWORK_UNSTABLE: //网络不稳定
                if (error == CallError.ERROR_NO_DATA) {
                    //无通话数据
                } else {
                }
                break;
            case NETWORK_NORMAL: //网络恢复正常

                break;
            default:
                break;
        }

    }

    //连接状态回调
    @Override
    public void onConnected() {
//        login = true;
    }

    @Override
    public synchronized void onDisconnected(final int error) {
        close();
        login = false;
        if (error == EMError.USER_REMOVED) {
            Log.e(TAG, "显示帐号已经被移除");
        } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
            Log.e(TAG, "显示帐号在其他设备登录");
        } else {
            if (NetUtils.hasNetwork(mContext)) {
                Log.e(TAG, "连接不到聊天服务器");
            } else
                Log.e(TAG, "当前网络不可用，请检查网络设置");
        }

    }

    //登录回调
    @Override
    public synchronized void onSuccess() {
        EMClient.getInstance().groupManager().loadAllGroups();
        EMClient.getInstance().chatManager().loadAllConversations();
        Log.e(TAG, "登录聊天服务器成功！");
        login = true;
    }

    @Override
    public synchronized void onProgress(int progress, String status) {
        Log.e(TAG, "status=" + status + ",progress=" + progress);
    }

    @Override
    public synchronized void onError(int code, String message) {
        Log.e(TAG, "登录聊天服务器失败！==" + message);
    }

    //消息监听
    @Override
    public synchronized void onMessageReceived(List<EMMessage> messages) {
        //收到消息
        Log.e(TAG, "收到消息");
        if (messages == null || messages.isEmpty())
            return;
        List<EMMessage> msgs = new ArrayList<>();
        for (final EMMessage message : messages) {
            if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                String switchCamera = Util.getString(message, Constants.SWITCH_CAMERA);
                if (switchCamera != null) {
                    if (Boolean.valueOf(switchCamera))
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                for (CameraListener cameraListener : cameraListeners)
                                    cameraListener.switchCamera();
                            }
                        });
                    Util.delete(message.getTo(), message.getMsgId());
                    continue;
                }
            }
            String meetId = Util.getString(message, Constants.MEET_ID);
            if (meetId != null) {
                mConfwewnceId = meetId;
                String pass = Util.getString(message, Constants.MEET_PASS);
                mPassword = pass;
                if (meetRoom != null)
                    meetRoom.destroy();
                meetRoom = new MeetRoom(mInput, mOutput, mContext, ids, mConfwewnceId, mPassword, listener, meetRoomListener);
                meetRoom.joinMeet();
                Util.delete(message.getFrom(), message.getMsgId());
                continue;
            }
            String refresh = Util.getString(message, Constants.MACHINE_REFRESH);

            if (refresh != null && !refresh.isEmpty()) {
                Log.e(TAG, "刷新");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (GroupListener listener : groupListeners)
                            listener.machineRefresh();
                    }
                });
                if (message.getChatType() != EMMessage.ChatType.GroupChat)
                    Util.delete(message.getFrom(), message.getMsgId());
                else
                    Util.delete(message.getTo(), message.getMsgId());
                continue;
            }
            String roleChanged = Util.getString(message, "roleChanged");
            if (roleChanged != null) {
                try {
                    int r = Integer.valueOf(roleChanged);
                    if (meetRoom != null)
                        meetRoom.onRoleChanged(r == 1 ? EMConferenceManager.EMConferenceRole.Audience : r == 3 ? EMConferenceManager.EMConferenceRole.Talker : EMConferenceManager.EMConferenceRole.Admin);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (message.getChatType() != EMMessage.ChatType.GroupChat)
                    Util.delete(message.getFrom(), message.getMsgId());
                else
                    Util.delete(message.getTo(), message.getMsgId());
            }
            if (!listeners.isEmpty())
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (MessageListener listener : listeners)
                            listener.message(message);
                    }
                });
            msgs.add(message);
        }
        if (msgs.size() > 0)
            EMClient.getInstance().chatManager().importMessages(msgs);
    }

    @Override
    public synchronized void onCmdMessageReceived(List<EMMessage> messages) {
        //收到透传消息
        Log.e(TAG, "收到透传消息");
        for (EMMessage message : messages) {

        }
    }

    @Override
    public synchronized void onMessageRead(List<EMMessage> messages) {
        //收到已读回执
        Log.e(TAG, "收到已读回执");
    }

    @Override
    public synchronized void onMessageDelivered(List<EMMessage> message) {
        //收到已送达回执
        Log.e(TAG, "收到已送达回执");
    }

    @Override
    public synchronized void onMessageRecalled(List<EMMessage> messages) {
        //消息被撤回
        Log.e(TAG, "消息被撤回");
    }

    @Override
    public synchronized void onMessageChanged(EMMessage message, Object change) {
        //消息状态变动
        Log.e(TAG, "消息状态变动");
    }

    @Override
    public synchronized void close() {
        // 关闭音频传输
        if (meetRoom != null)
            meetRoom.destroy();
        meetRoom = null;
        EMClient.getInstance().conferenceManager().closeVoiceTransfer();
        // 关闭视频传输
        EMClient.getInstance().conferenceManager().closeVideoTransfer();
    }


    public synchronized void logout() {
        login = false;
        close();
        EMClient.getInstance().logout(true);
    }

    public synchronized void destory() {
        EMClient.getInstance().chatManager().removeMessageListener(this);
        sharedPreferences.edit().putBoolean(LOGIN, login);
    }

    @Override
    public synchronized void closeOrOpenVideo() {
        try {
            if (video) {
                video = false;
                EMClient.getInstance().conferenceManager().closeVideoTransfer();
            } else {
                video = true;
                // 开启视频传输
                EMClient.getInstance().conferenceManager().openVideoTransfer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void closeOrOpenAudio() {
        try {
            if (audio) {
                audio = false;
                // 关闭音频传输
                EMClient.getInstance().conferenceManager().closeVoiceTransfer();
            } else {
                audio = true;
                // 开启音频传输
                EMClient.getInstance().conferenceManager().openVoiceTransfer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendCreateMeet(ViewGroup inputGroup, ViewGroup outputGroup, boolean monitor, String id, MeetRoom.MeetRoomSubscribeListener listener, MeetRoom.MeetRoomListener meetRoomListener) {
        mInput = inputGroup;
        mOutput = outputGroup;
        ids.clear();
        ids.add(id);
        this.listener = listener;
        this.meetRoomListener = meetRoomListener;
        Log.e(TAG, "monitor:" + monitor);
        if (monitor)// 关闭音频传输
            EMClient.getInstance().conferenceManager().closeVoiceTransfer();
        else  // 开启音频传输
            EMClient.getInstance().conferenceManager().openVoiceTransfer();
    }

    public synchronized void switchCamera() {
        // 切换摄像头
        EMClient.getInstance().conferenceManager().switchCamera();
    }


    /**
     * 2019/1/17 12:14
     * annotation：是否登录
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    public synchronized boolean isLogin() {
        return login;
    }


    public interface MessageListener {
        void message(EMMessage message);
    }

    public interface CameraListener {
        void switchCamera();
    }


    public interface GroupListener {
        public void onUserRemoved(String groupId, String groupIdNickName);

        public void onGroupDestroyed(String groupId, String groupIdNickName);

        public void onMemberJoined(final String groupId, final String member);

        public void onMemberExited(final String groupId, final String member);

        public void onAdminAdded(String groupId, String administrator);

        public void onAdminRemoved(String groupId, String administrator);

        public void machineRefresh();

        public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage);
    }
}
