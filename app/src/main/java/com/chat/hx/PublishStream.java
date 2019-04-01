package com.chat.hx;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMStreamParam;
import com.hyphenate.media.EMCallSurfaceView;

/**
 * com.lamp.hx
 * 2019/3/29 12:33
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class PublishStream {
    private static final String TAG = "PublishStream";
    private ViewGroup output;
    private String id;
    private PublishStreamListener publishStreamListener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int publishTime = 0;
    private int unPublishTime = 0;
    private EMCallSurfaceView localView;

    public PublishStream(ViewGroup output, Context context, PublishStreamListener publishStreamListener) {
        if (context == null)
            throw new RuntimeException("you context null");
        if (output == null)
            output = new FrameLayout(context);
        this.output = output;
        this.publishStreamListener = publishStreamListener;
        Log.e(TAG, "发布流");
        publishStream();
    }

    private synchronized void publishStream() {
        publishTime++;
        EMStreamParam param = new EMStreamParam();
        param.setStreamType(EMConferenceStream.StreamType.NORMAL);
        param.setVideoOff(false);
        param.setAudioOff(false);
        EMClient.getInstance().conferenceManager().publish(param, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String streamId) {
                Log.e(TAG, "发布流成功:" + streamId);
                id = streamId;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (PublishStream.this) {
                            if (output == null) {
                                unPublishStream();
                                return;
                            }
                            if (publishStreamListener != null)
                                publishStreamListener.publishStreamSuccess();
                            localView = new EMCallSurfaceView(output.getContext());
                            localView.setZOrderMediaOverlay(true);
                            localView.setZOrderOnTop(true);
                            output.addView(localView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            EMClient.getInstance().conferenceManager().setLocalSurfaceView(localView);
                        }
                    }
                });
            }

            @Override
            public void onError(final int error, final String errorMsg) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (publishTime <= 10 && output != null) {
                            publishStream();
                        } else {
                            Log.e(TAG, "发布流失败: error=" + error + ", msg=" + errorMsg);
                            if (publishStreamListener != null)
                                publishStreamListener.publishStreamFailure();
                        }
                    }
                });
            }
        });
    }

    public synchronized void unPublishStream() {
        unPublishTime++;
        if (output != null && localView != null) {
            output.removeView(localView);
            output = null;
            localView = null;
        }
        if (id == null)
            return;
        EMClient.getInstance().conferenceManager().unpublish(id, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "取消发布成功,id:" + s);
            }

            @Override
            public void onError(int i, String s) {
                if (unPublishTime <= 10) {
                    unPublishStream();
                } else {
                    Log.e(TAG, "取消发布失败,error:" + i + ",msg:" + s);
                }
            }
        });
    }

    public interface PublishStreamListener {
        void publishStreamSuccess();

        void publishStreamFailure();
    }

}
