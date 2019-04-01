package com.chat.hx;

import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.EMServiceNotReadyException;

/**
 * com.chat.hx
 * 2019/4/1 18:14
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class Video implements EMCallStateChangeListener {
    @Override
    public void onCallStateChanged(CallState callState, CallError error) {
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

    /**
     * 拨打语音通话
     *
     * @param username
     */
    public void makeVoiceCall(String username) {
        try {//单参数
            EMClient.getInstance().callManager().makeVoiceCall(username);
        } catch (EMServiceNotReadyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 拨打视频通话
     *
     * @param username
     */
    public void makeVideoCall(String username) {
        try {//单参数
            EMClient.getInstance().callManager().makeVideoCall(username);
        } catch (EMServiceNotReadyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 接听通话
     */
    public void answerCall() {
        try {
            EMClient.getInstance().callManager().answerCall();
        } catch (EMNoActiveCallException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 拒绝接听
     */
    public void rejectCall() {
        try {
            EMClient.getInstance().callManager().rejectCall();
        } catch (EMNoActiveCallException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 挂断通话
     */
  public void endCall(){
      try {
          EMClient.getInstance().callManager().endCall();
      } catch (EMNoActiveCallException e) {
          e.printStackTrace();
      }
  }


}
