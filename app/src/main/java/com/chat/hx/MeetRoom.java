package com.chat.hx;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.hyphenate.EMConferenceListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMStreamStatistics;
import com.hyphenate.util.EasyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * com.lamp.hx
 * 2019/3/29 10:53
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class MeetRoom implements EMConferenceListener, SubscribeStream.SubscribeStreamListener, PublishStream.PublishStreamListener {
    private static final String TAG = "MeetRoom";
    private static final int CREATE_TIMEOUT = 10 * 1000;
    private List<String> ids;
    private ViewGroup input;
    private ViewGroup output;
    private String roomId;
    private String pass;
    private boolean create = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Handler createHandler = new Handler(Looper.getMainLooper());
    private MeetRoomListener roomCreateListener;
    private Map<String, RoleTalker> roleTalkerMap = new HashMap<>();
    private Map<String, SubscribeStream> subscribeStreamMap = new HashMap<>();
    private PublishStream publishStream;
    private MeetRoomSubscribeListener meetRoomSubscribeListener;
    private Context mContext;
    private PowerManager.WakeLock mWakeLock;
    private String mJid;

    public MeetRoom(ViewGroup input, ViewGroup output, Context context, List<String> ids, String roomId, String pass, MeetRoomSubscribeListener listener, MeetRoomListener meetRoomListener) {
        if (context == null)
            throw new RuntimeException("you context null");
        this.ids = ids;
        if (input == null)
            input = new FrameLayout(context);
        this.input = input;
        if (output == null)
            output = new FrameLayout(context);
        mContext = context;
        this.output = output;
        meetRoomSubscribeListener = listener;
        roomCreateListener = meetRoomListener;
        this.roomId = roomId;
        this.pass = pass;
        EMClient.getInstance().conferenceManager().addConferenceListener(this);

    }

    public synchronized void joinMeet() {
        Log.e(TAG, "加入聊天室");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (roomCreateListener != null)
                    roomCreateListener.meetRoomJoinFailuer();
            }
        }, CREATE_TIMEOUT);
        EMClient.getInstance().conferenceManager().joinConference(roomId, pass, new
                EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(EMConference value) {
                        Log.e(TAG, "加入会议室成功");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                                    mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "WakeLock");
                                    mWakeLock.acquire();
                                    if (!create) {
                                        Log.e(TAG, "发送说话请求");
                                        EMMessage message = Util.createTxtMessage("=="+ids.get(0));
                                        Map<String, Object> params = new HashMap<>();
                                        params.put("request_speak", "request_speak");
                                        Util.addExpand(message, params);
                                        Util.sendMessage(message, ids.get(0));
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "锁屏幕时出错了");
                                }
                            }
                        });
                        mJid = EasyUtils.getMediaRequestUid(EMClient.getInstance().getOptions().getAppKey(), EMClient.getInstance().getCurrentUser());
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        // 运行在子线程中，勿直接操作UI
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (roomCreateListener != null)
                                    roomCreateListener.meetRoomJoinFailuer();
                            }
                        });
                    }
                });
    }

    public void createMeet() {
        create = true;
        createHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                synchronized (MeetRoom.this) {
                    if (roomCreateListener != null)
                        roomCreateListener.meetRoomJoinFailuer();
                }
            }
        }, CREATE_TIMEOUT);
        EMClient.getInstance().conferenceManager().createAndJoinConference(EMConferenceManager.EMConferenceType.LargeCommunication,
                pass, new EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(final EMConference value) {
                        createHandler.removeCallbacksAndMessages(null);
                        createHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (MeetRoom.this) {
                                    if (roomCreateListener != null) {
                                        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                                        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "WakeLock");
                                        mWakeLock.acquire();
                                        MeetRoom.this.roomId = value.getConferenceId();
                                        publishStream = new PublishStream(output, output.getContext(), MeetRoom.this);
                                        roomCreateListener.meetRoomJoinSuccess(value.getConferenceId(), pass);
                                        roomCreateListener = null;
                                    }
                                }
                            }
                        });

                    }

                    @Override
                    public synchronized void onError(int error, String errorMsg) {
                        createHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (MeetRoom.this) {
                                    if (roomCreateListener != null) {
                                        roomCreateListener.meetRoomJoinFailuer();
                                        roomCreateListener = null;
                                    }
                                }
                            }
                        });
                    }
                });
    }

    public synchronized void destroy() {
        // 在Activity#onDestroy()中移除监听
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    destroy();
                }
            });
            return;
        }
        try {
            if (mWakeLock != null)
                mWakeLock.release();
        } catch (Exception e) {
        }
        EMClient.getInstance().conferenceManager().removeConferenceListener(this);
        destroyConference();
        if (publishStream != null) {
            publishStream.unPublishStream();
            publishStream = null;
        }

        for (SubscribeStream subscribeStream : subscribeStreamMap.values())
            subscribeStream.unSubscribe();
        subscribeStreamMap.clear();
        synchronized (this) {
            roomCreateListener = null;
        }
        if (create)
            EMClient.getInstance().conferenceManager().destroyConference(new EMValueCallBack() {
                @Override
                public void onSuccess(Object o) {
                    Log.e(TAG, "会议室关闭成功");

                }

                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "会议室关闭失败");
                }
            });
    }

    private int destroyConference = 0;

    private void destroyConference() {
        destroyConference++;
        EMClient.getInstance().conferenceManager().destroyConference(new EMValueCallBack() {
            @Override
            public void onSuccess(Object o) {
                Log.e(TAG, "会议室关闭成功");

            }

            @Override
            public void onError(int i, String s) {

                if (destroyConference <= 0) {
                    destroyConference();
                } else {
                    Log.e(TAG, "会议室关闭失败");
                }
            }
        });
    }

    @Override
    public synchronized void onMemberJoined(EMConferenceMember emConferenceMember) {
        // 有成员加入
        if (create)
            if (!roleTalkerMap.containsKey(emConferenceMember.memberName))
                roleTalkerMap.put(emConferenceMember.memberName, new RoleTalker(emConferenceMember.memberName, roomId, null));
    }

    @Override
    public synchronized void onMemberExited(EMConferenceMember emConferenceMember) {
        // 有成员离开
        if (create)
            roleTalkerMap.remove(emConferenceMember.memberName);
    }

    @Override
    public synchronized void onStreamAdded(final EMConferenceStream stream) {
        // 有流加入
        Log.e(TAG, "有流加入:" + stream.getUsername());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!subscribeStreamMap.containsKey(stream.getMemberName())) {
                    if (ids == null || ids.size() <= 0) {
                        subscribeStreamMap.put(stream.getMemberName(), new SubscribeStream(input, stream, MeetRoom.this));
                    } else {
                        for (String id : ids) {
                            Log.e(TAG, "id:" + id);
                            if (stream.getUsername().equals(id)) {
                                subscribeStreamMap.put(stream.getMemberName(), new SubscribeStream(input, stream, MeetRoom.this));
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public synchronized void onStreamRemoved(final EMConferenceStream stream) {
        // 有流移除
        Log.e(TAG, "有流移除:" + stream.getUsername());
        handler.post(new Runnable() {
            @Override
            public void run() {
                SubscribeStream subscribeStream = subscribeStreamMap.get(stream.getMemberName());
                if (subscribeStream != null)
                    subscribeStream.unSubscribe();
                subscribeStreamMap.remove(stream.getMemberName());
            }
        });
    }

    @Override
    public synchronized void onStreamUpdate(EMConferenceStream stream) {
        // 有流更新
        onStreamRemoved(stream);
        onStreamAdded(stream);
    }

    @Override
    public void onPassiveLeave(int error, String message) {
        // 被动离开
        Log.e(TAG, "被动离开:error" + error + ",msg:" + message);
//        destroy();
    }

    @Override
    public void onConferenceState(ConferenceState state) {
        // 聊天室状态回调
    }

    @Override
    public void onStreamSetup(String streamId) {
        // 流操作成功回调
    }

    @Override
    public void onSpeakers(final List<String> speakers) {
        // 当前说话者回调
    }

    @Override
    public void onReceiveInvite(String confId, String password, String extension) {
        // 收到会议邀请

    }


    @Override
    public void onStreamStatistics(EMStreamStatistics emStreamStatistics) {

    }

    @Override
    public void onRoleChanged(final EMConferenceManager.EMConferenceRole emConferenceRole) {
        Log.e(TAG, "角色改变");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!create) {
                    if (emConferenceRole == EMConferenceManager.EMConferenceRole.Talker || emConferenceRole == EMConferenceManager.EMConferenceRole.Admin) {
                        // 管理员把当前用户角色更改为主播,可以进行publish本地流等操作
                        if (publishStream == null)
                            publishStream = new PublishStream(output, output.getContext(), MeetRoom.this);
                    } else if (emConferenceRole == EMConferenceManager.EMConferenceRole.Audience) {
                        // 管理员把当前用户角色改变为观众
                        if (publishStream != null)
                            publishStream.unPublishStream();
                    }
                }
            }
        });
    }

    public interface MeetRoomListener {
        void meetRoomJoinSuccess(String roomId, String pass);

        void meetRoomJoinFailuer();
    }

    public interface MeetRoomSubscribeListener {
        void subscribeStreamSuccess();

        void subcribeStreamFailure();

        void publishStreamSuccess();

        void publishStreamFailure();
    }

    @Override
    public void subscribeStreamSuccess() {
        if (roomCreateListener != null)
            meetRoomSubscribeListener.subscribeStreamSuccess();
    }

    @Override
    public void subcribeStreamFailure() {
        if (meetRoomSubscribeListener != null)
            meetRoomSubscribeListener.subcribeStreamFailure();
    }

    @Override
    public void publishStreamSuccess() {
        if (meetRoomSubscribeListener != null)
            meetRoomSubscribeListener.publishStreamSuccess();
    }

    @Override
    public void publishStreamFailure() {
        if (meetRoomSubscribeListener != null)
            meetRoomSubscribeListener.publishStreamFailure();
    }
}
