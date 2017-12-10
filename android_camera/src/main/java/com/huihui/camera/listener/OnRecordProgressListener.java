package com.huihui.camera.listener;

/**
 * Created by molu_ on 2017/12/10.
 * 视频录制进度接口
 */
public interface OnRecordProgressListener {
    /**
     * 进度变化
     *
     * @param maxTime     最大时间，单位秒
     * @param currentTime 当前进度
     */
    void onProgressChanged(int maxTime, int currentTime);
}
