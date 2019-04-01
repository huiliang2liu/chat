package com.chat.hx;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.media.EMCallSurfaceView;
import com.superrtc.sdk.VideoView;

/**
 * com.lamp.hx
 * 2019/3/29 11:26
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class SubscribeStream {
    private static final String TAG = "SubscribeStream";
    private EMConferenceStream mStream;
    private int subTime = 0;
    private int unSubTime = 0;
    private boolean unsubscribe = false;
    private EMCallSurfaceView surfaceView;
    private ViewGroup input;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SubscribeStreamListener subscribeStreamListener;
    private EMConferenceStream unSubStream;

    public SubscribeStream(ViewGroup input, EMConferenceStream stream, SubscribeStreamListener subscribeStreamListener) {
        if (stream == null)
            return;
        this.subscribeStreamListener = subscribeStreamListener;
        mStream = stream;
        surfaceView = new EMCallSurfaceView(input.getContext());
        surfaceView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        input.addView(surfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.input=input;
        Log.e(TAG, "订阅流");
        subscribe();
    }

    public synchronized void updateRemote(ViewGroup viewGroup) {
        if (viewGroup == null)
            return;
        if (surfaceView != null) {
            input.removeView(surfaceView);
            input = viewGroup;
            input.addView(surfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private synchronized void subscribe() {
        if (surfaceView == null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (subscribeStreamListener != null)
                        subscribeStreamListener.subcribeStreamFailure();
                }
            });
            return;
        }
        subTime++;
        EMClient.getInstance().conferenceManager().subscribe(mStream, surfaceView, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                Log.e(TAG, "订阅流成功:" + value);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (subscribeStreamListener != null)
                            subscribeStreamListener.subscribeStreamSuccess();
                    }
                });
            }

            @Override
            public void onError(final int error, final String errorMsg) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (subTime <= 10) {
                            subscribe();
                        } else {
                            Log.e(TAG, "订阅流失败,error:" + error + ",msg:" + errorMsg);
                            if (input != null)
                                input.removeView(surfaceView);
                            if (subscribeStreamListener != null)
                                subscribeStreamListener.subcribeStreamFailure();

                        }
                    }
                });
            }
        });
    }

    public synchronized void unSubscribe() {
        unSubTime++;
        if (input != null) {
            input.removeView(surfaceView);
            input = null;
            surfaceView = null;
        }
        Log.e(TAG, "取消订阅");
        unSubscribe1();
    }

    private synchronized void unSubscribe1() {
        if (mStream == null)
            return;
        EMClient.getInstance().conferenceManager().unsubscribe(mStream, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                mStream = null;
                Log.e(TAG, "取消订阅成功" + value);
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (unSubTime <= 10) {
                    unSubscribe();
                } else {
                    Log.e(TAG, "取消订阅失败,error:" + error + ",msg:" + errorMsg);
                }
            }
        });
    }

    public interface SubscribeStreamListener {
        void subscribeStreamSuccess();

        void subcribeStreamFailure();

    }
}

