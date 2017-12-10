package com.huihui.camera.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.huihui.camera.listener.OnTakePictureCallBack;
import com.huihui.camera.utils.CameraUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;

/**
 * Created by gavin
 * Time 2017/12/8  14:11
 * Email:molu_clown@163.com
 */

public class CameraPreviewView extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback, Camera.ShutterCallback,GestureDetector.OnGestureListener {


    private Camera mCamera;

    private SurfaceHolder mHolder;

    private OnTakePictureCallBack callBack;

    private Context context;

    private int mWidth;//视频录制分辨率宽度
    private int mHeight;//视频录制分辨率高度
    private boolean isOpenCamera;//是否一开始就打开摄像头
    private int recordMaxTime;//最长拍摄时间
    private int timeCount;//时间计数
    private File recordFile = null;//视频文件

    private GestureDetector mGestureDetector;

    /***
     * 默认是前置摄像头
     */
    private static int CAMERA_FACING_TYPE = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public CameraPreviewView(Context context) {
        super(context);
        this.mCamera = CameraUtils.getCamera(CAMERA_FACING_TYPE);

        this.context=context;

        mHolder = getHolder();
        mHolder.addCallback(this);

        mGestureDetector=new GestureDetector(context,this);

        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    public void setOnTakePictureCallBack(OnTakePictureCallBack callBack) {
        this.callBack = callBack;
    }

    private void showView() {

        if (mCamera == null) {

            mCamera = CameraUtils.getCamera(CAMERA_FACING_TYPE);
        }

        try {
            mCamera.setPreviewDisplay(mHolder);

            //mCamera.setDisplayOrientation(90);

            CameraUtils.setCameraDisplayOrientation((Activity) context,CAMERA_FACING_TYPE,mCamera);

            /***
             * 开始预览
             */
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void onResume() {

        if (mCamera == null) {
            mCamera = CameraUtils.getCamera(CAMERA_FACING_TYPE);

            if (mHolder != null) {

                showView();
            }
        }
    }

    /**
     * 释放相机的内存
     */
    public void clearCamera() {

        // 释放hold资源
        if (mCamera != null) {
            // 停止预览
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            // 释放相机资源
            mCamera.release();
            mCamera = null;
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

                        mCamera.takePicture(CameraPreviewView.this, null, CameraPreviewView.this);
                    }
                }
            });

        }
    }

    /****
     * 拍照成功的返回
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {


        File file = new File(String.format("%s%s%s", Environment.getExternalStorageDirectory().getPath(), File.separator, "photo.jpg"));


        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            outputStream.write(data);

            outputStream.close();

            if (callBack != null) {

                callBack.callback(file.getPath(),CAMERA_FACING_TYPE);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


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


    @Override
    public void onShutter() {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }


    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    /***
     * 长按事件
     * @param e
     */
    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
