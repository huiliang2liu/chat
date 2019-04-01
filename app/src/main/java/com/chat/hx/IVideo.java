package com.chat.hx;

/**
 * com.video.iface
 * 2019/1/11 17:32
 * instructions：视频通话接口
 * author:liuhuiliang  email:825378291@qq.com
 **/
public interface IVideo {
    /**
     * 2019/1/11 17:33
     * annotation：结束通话
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    void close();

    /**
     * 2019/1/11 17:39
     * annotation：关闭视频
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    void closeOrOpenVideo();

    /**
     * 2019/1/11 17:40
     * annotation：关闭声频
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    void closeOrOpenAudio();

    /**
     * 2019/1/11 18:32
     * annotation：切换摄像头
     * author：liuhuiliang
     * email ：825378291@qq.com
     */
    void switchCamera();


}
