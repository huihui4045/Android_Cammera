package com.huihui.camera.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.huihui.camera.listener.OnRecordFinishListener;
import com.huihui.camera.listener.OnRecordProgressListener;
import com.huihui.camera.listener.OnTakePictureCallBack;
import com.huihui.camera.utils.CameraUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;

/**
 * Created by molu_ on 2017/12/10.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback,
        Camera.ShutterCallback, MediaRecorder.OnErrorListener, Camera.PreviewCallback {




    /***
     * 默认是前置摄像头
     */
    private static int CAMERA_FACING_TYPE = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private Camera mCamera;

    private SurfaceHolder mHolder;

    private MediaRecorder mRecorder;

    private Context context;

    private OnTakePictureCallBack callBack;
    private OnRecordFinishListener mOnRecordFinishListener;

    private OnRecordProgressListener mOnRecordProgressListener;

    private int mRecordMaxTime;//最长拍摄时间
    private int mTimeCount;//时间计数

    private int mVideoWidth;

    private int mVideoHeight;



    private String TAG=this.getClass().getSimpleName();
    private File recordFile;
    private Timer mTimer;



    public CameraView(@NonNull Context context) {
        super(context);

        this.context = context;



        this.mCamera = CameraUtils.getCamera(CAMERA_FACING_TYPE);

        mHolder = getHolder();
        mHolder.addCallback(this);

        /***
         * 默认60秒
         */
        mRecordMaxTime=30;




    }

    public void setOnRecordFinishListener(OnRecordFinishListener mOnRecordFinishListener) {
        this.mOnRecordFinishListener = mOnRecordFinishListener;
    }

    public void setOnRecordProgressListener(OnRecordProgressListener mOnRecordProgressListener) {
        this.mOnRecordProgressListener = mOnRecordProgressListener;
    }

    public void setOnTakePictureCallBack(OnTakePictureCallBack callBack) {
        this.callBack = callBack;
    }


    private void showView() {

        if (mCamera == null) {

            mCamera = CameraUtils.getCamera(CAMERA_FACING_TYPE);
        }

        mCamera.setPreviewCallback(this);

        if (mRecorder==null){

            mRecorder=new MediaRecorder();
        }

        try {
            mCamera.setPreviewDisplay(mHolder);

            List<Camera.Size> supportedVideoSizes = mCamera.getParameters().getSupportedVideoSizes();

            Camera.Size   mPreviewSize=null;

            if (supportedVideoSizes != null) {
            mPreviewSize = getOptimalPreviewSize(supportedVideoSizes,
                        Math.max(mVideoWidth, mVideoHeight), Math.min(mVideoWidth, mVideoHeight));
            }

            Camera.Parameters mParams = mCamera.getParameters();
            mParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(mParams);

            //mCamera.setDisplayOrientation(90);

            CameraUtils.setCameraDisplayOrientation((Activity) context, CAMERA_FACING_TYPE, mCamera);

            /***
             * 开始预览
             */
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /****
     * 视频录制的初始化
     */
    private void initRecord() throws IOException {

        if (mRecorder==null){

            mRecorder=new MediaRecorder();
        }

        mRecorder.reset();

        if (mCamera==null){

            showView();
        }


        mRecorder.setCamera(mCamera);
        /****
         * 屏幕录制之前 必须调用unlock()
         */
        mCamera.unlock();


        mRecorder.setOnErrorListener(this);

        CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

       // mRecorder.setProfile(mProfile);

        /***
         * 设置视频源
         */
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        /****
         * 设置音频源
         */
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        /****
         *  视频输出格式 也可设为3gp等其他格式
         */
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);




        /***
         * 设置音频采集编码模式
         */
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        /***
         * 设置视频采集编码模式
         */
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        //mRecorder.setVideoSize(mVideoWidth, mVideoHeight);

        Log.e(TAG,"mVideoWidth:"+mVideoWidth);

        Log.e(TAG,"mVideoHeight:"+mVideoHeight);
        /****
         * 设置分辨率
         */
        mRecorder.setVideoSize(mVideoHeight, mVideoWidth);

        /****
         *  这是设置视频录制的帧率，即1秒钟16帧
         */
        mRecorder.setVideoFrameRate(100);


        mRecorder.setAudioSamplingRate(30);

        /***
         * 设置所录制视频的编码位率。  这个属性很重要，这个也直接影响到视频录制的大小，这个设置的越大，视频越清晰，我做了简单的比较
         */
       mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);



        //mRecorder.setOnInfoListener();


        if (CAMERA_FACING_TYPE ==Camera.CameraInfo.CAMERA_FACING_FRONT){

            mRecorder.setOrientationHint(270);
        }else {

            mRecorder.setOrientationHint(90);
        }





        mRecorder.setPreviewDisplay(mHolder.getSurface());

        /****
         * 视频保存的路径
         */
        mRecorder.setOutputFile(recordFile.getAbsolutePath());



        mRecorder.prepare();

        /***
         * 开始录制
         */
        mRecorder.start();

    }

    /**
     * 释放视频资源
     */
    public void releaseRecord() {

        if (mRecorder != null) {
            mRecorder.setOnErrorListener(null);
            try {
                mRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mRecorder = null;

    }
    /**
     * 停止录制
     */
    public void stopRecord() {

        Log.e(TAG,"停止录制");
        //progressBar.setProgress(0);

        if (mTimer != null)
            mTimer.cancel();
        if (mRecorder != null) {
            mRecorder.setOnErrorListener(null);//设置后防止崩溃
            mRecorder.setPreviewDisplay(null);
            try {
                mRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mOnRecordFinishListener!=null&& isStartRecord){

            mOnRecordFinishListener.onRecordFinish(recordFile.getAbsolutePath());
        }

        isStartRecord=false;
    }



    private boolean isStartRecord=false;

    /***
     * 开始录制视频
     */
    public void startRecord(){

        createRecordDir();

        try {
            initRecord();
            mTimeCount=0;
            isStartRecord=true;

            mTimer=new Timer();

            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                   mTimeCount++;

                   Log.e(TAG,"定时器："+mTimeCount);

                   if (mOnRecordProgressListener!=null){

                       mOnRecordProgressListener.onProgressChanged(mRecordMaxTime,mTimeCount);
                   }

                   if (mTimeCount==mRecordMaxTime){


                       stopRecord();

                   }


                }
            }, 1000,1000);



        } catch (IOException e) {
            e.printStackTrace();

            releaseRecord();

            Log.e(TAG,"error:"+e.getMessage());
        }


    }

    /****
     * 获取视频文件
     * @return
     */
   public String getRecordFilePath(){

        if (recordFile.exists()){

            return  recordFile.getAbsolutePath();
        }

        return null;
   }

    /**
     * 创建视频文件
     */
    private void createRecordDir() {
        File sampleDir = new File(Environment.getExternalStorageDirectory() + File.separator + "SampleVideo/video/");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        try {
            //TODO 文件名用的时间戳，可根据需要自己设置，格式也可以选择3gp，在初始化设置里也需要修改
            recordFile = new File(sampleDir, System.currentTimeMillis() + ".mp4");
//            recordFile = new File(sampleDir, System.currentTimeMillis() + ".mp4");
//            File.createTempFile(AccountInfo.userId, ".mp4", sampleDir);
//            LogUtil.e(LOG_TAG, recordFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放相机的内存
     */
    private void clearCamera() {

        // 释放hold资源
        if (mCamera != null) {
            // 停止预览
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            // 释放相机资源
            mCamera.release();
            mCamera = null;
        }

        if (mRecorder!=null){
            mRecorder.release();
            mRecorder=null;
        }
    }



    public void onResume() {

        if (mCamera == null) {
            mCamera = CameraUtils.getCamera(CAMERA_FACING_TYPE);

            if (mHolder != null) {

                showView();
            }
        }
    }

    /****
     * 停止拍摄
     */
    public void onStop() {

        stopRecord();

        releaseRecord();

        clearCamera();
    }

    /****
     * 切换相机摄像头
     */
    public void switchCamera() {

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        /****
         * 获取摄像头的个数
         */
        int numberOfCameras = Camera.getNumberOfCameras();

        if (numberOfCameras > 1) {

            Camera.getCameraInfo(CAMERA_FACING_TYPE, cameraInfo);

            /***
             * 当前为后置 修改为前置摄像头
             */
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {

                /***
                 * 清空当前摄像头
                 */
                clearCamera();

                CAMERA_FACING_TYPE = Camera.CameraInfo.CAMERA_FACING_FRONT;

                showView();


                /****
                 * 当前为前置摄像头  切换为后置
                 */
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

                /***
                 * 清空当前摄像头
                 */
                clearCamera();

                CAMERA_FACING_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

                showView();
            }
        }

    }


    /***
     * 开始拍照
     */
    public void takePicture() {


        if (mCamera != null) {

            /****
             * 获取当前相机的参数
             */
            Camera.Parameters parameters = mCamera.getParameters();


            parameters.setPictureFormat(ImageFormat.JPEG);
            /***
             * 设置预览大小
             */
            parameters.setPreviewSize(getWidth(), getHeight());



            /***
             * 设置对焦方式，这里设置自动对焦
             * auto  //自动
             infinity //无穷远
             macro //微距
             continuous-picture //持续对焦
             fixed //固定焦距
             */
            parameters.setFocusMode(FOCUS_MODE_AUTO);

            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    /****
                     * 对焦成功
                     */
                    if (success) {

                        mCamera.takePicture(CameraView.this, null, CameraView.this);
                    }
                }
            });



        }
    }

    /***
     * 当含有surfaceView的试图层级结构被放到屏幕上时surfaceCreated被调用，
     * 此时surfaceholder对surface及其客户端进行关联
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        showView();


    }

    /***
     * surface首次出现在屏幕上调用这个方法，通过传入的参数，可以告诉客户端surface的像素格式以及surface的宽度和高度，
     33             //告知客户端有多大的绘制区域可以使用。
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        mVideoWidth=width;
        mVideoHeight=height;

        mCamera.stopPreview();

        showView();

    }

    /***
     * surfaceView从屏幕上移除时调用此方法，也就意味着surface被销毁，surfaceholder断开surface及其客户端的联系。
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {


        clearCamera();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {


        File file = new File(String.format("%s%s%s", Environment.getExternalStorageDirectory().getPath(), File.separator, "photo.jpg"));


        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            outputStream.write(data);

            outputStream.close();

            if (callBack != null) {

                callBack.callback(file.getPath(), CAMERA_FACING_TYPE);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShutter() {

    }



    /****
     * 视频录制出错
     * @param mr
     * @param what
     * @param extra
     */
    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }


    public Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public byte[] yuv420sp = null;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {


        final int len = data.length;
        for(int i=0; i<len; ++i){
            data[i] *= 5;
        }
    }


}
