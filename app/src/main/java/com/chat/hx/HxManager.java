package com.chat.hx;

import android.os.Handler;

import com.chat.Application;
import com.chat.net.NetworkUtil;
import com.chat.utils.LogUtil;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * com.chat.hx
 * 2019/3/12 12:03
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class HxManager implements EMConnectionListener, EMMessageListener {
    private static final String TAG = "HxManager";
    private EMClient mEmClient;
    private Application mApplication;
    private Handler handler = new Handler();
    private List<HxStatusListener> mStatusListeners = new ArrayList<>();
    private Map<String, HxMessageListener> messageListenerMap = new HashMap<>();
    private Map<String, HxMessageListener> groupMessageListenerMap = new HashMap<>();
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

    public void registerGroupHxMessageListener(String name, HxMessageListener messageListener) {
        if (name == null || name.isEmpty() || messageListener == null)
            return;
        groupMessageListenerMap.put(name, messageListener);
    }

    public void unRegisterGroupHxMessageListener(String name, HxMessageListener messageListener) {
        if (name == null || name.isEmpty() || messageListener == null)
            return;
        groupMessageListenerMap.remove(name);
    }

    //连接状态
    @Override
    public void onConnected() {
        for (HxStatusListener statusListener : mStatusListeners)
            statusListener.connected();
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
        for (EMMessage message:messages){
            if(message.getChatType()== EMMessage.ChatType.GroupChat){

            }else{

            }
        }
    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {
        LogUtil.e(TAG, "收到透传消息");
    }

    @Override
    public void onMessageRead(List<EMMessage> messages) {
        LogUtil.e(TAG, "收到已读回执");
        for (EMMessage message : messages)
            message.setUnread(false);
    }

    @Override
    public void onMessageDelivered(List<EMMessage> messages) {
        LogUtil.e(TAG, "收到已送达回执");
        for (EMMessage message : messages)
            message.setDelivered(true);
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
    public void onMessageChanged(EMMessage message, Object change) {
        LogUtil.e(TAG, "消息状态变动");
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
        groupMessageListenerMap.clear();
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
