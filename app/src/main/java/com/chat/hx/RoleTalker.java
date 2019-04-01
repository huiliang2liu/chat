package com.chat.hx;

import android.util.Log;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;

/**
 * com.lamp.hx
 * 2019/3/29 13:37
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class RoleTalker {
    private static final String TAG = "RoleTalker";
    private String jid;
    private String roomId;
    private int roleTime = 0;
    private RoleTalkerListener roleTalkerListener;

    public RoleTalker(String jid, String roomId, RoleTalkerListener roleTalkerListener) {
        this.jid = jid;
        this.roomId = roomId;
        this.roleTalkerListener = roleTalkerListener;
        Log.e(TAG, "授权说话");
        role();
    }

    private void role() {
        roleTime++;
        EMClient.getInstance().conferenceManager().grantRole(roomId
                , new EMConferenceMember(jid, null, null)
                , EMConferenceManager.EMConferenceRole.Talker, new EMValueCallBack<String>() {
                    @Override
                    public void onSuccess(String value) {
                        Log.e(TAG, "授权成功: " + value);
                        if (roleTalkerListener != null)
                            roleTalkerListener.roleTalkerSuccess();
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        if (roleTime <= 0)
                            role();
                        else {
                            Log.e(TAG, "授权失败, error: " + error + " - " + errorMsg);
                            if (roleTalkerListener != null)
                                roleTalkerListener.roleTalkerFailuer();
                        }
                    }
                });
    }

    public interface RoleTalkerListener {
        void roleTalkerSuccess();

        void roleTalkerFailuer();
    }
}
