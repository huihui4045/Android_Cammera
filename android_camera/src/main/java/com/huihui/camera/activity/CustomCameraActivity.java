package com.huihui.camera.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.huihui.camera.R;
import com.huihui.camera.listener.OnRecordFinishListener;
import com.huihui.camera.listener.OnRecordProgressListener;
import com.huihui.camera.listener.OnTakePictureCallBack;
import com.huihui.camera.view.CameraView;

public class CustomCameraActivity extends AppCompatActivity implements OnTakePictureCallBack, View.OnLongClickListener, View.OnClickListener, View.OnTouchListener, OnRecordProgressListener, OnRecordFinishListener {

    private FrameLayout mSurfaceLayout;
    private CameraView mCameraPreviewView;
    private Button mStartRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏


        setContentView(R.layout.activity_custom_camera);

        mSurfaceLayout = ((FrameLayout) findViewById(R.id.surfaceViewLayout));

        mCameraPreviewView = new CameraView(this);

        mSurfaceLayout.addView(mCameraPreviewView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mCameraPreviewView.setOnTakePictureCallBack(this);

        mStartRecord = ((Button) findViewById(R.id.startRecord));

        mStartRecord.setOnTouchListener(this);

        mStartRecord.setOnLongClickListener(this);
        mStartRecord.setOnClickListener(this);

        mCameraPreviewView.setOnRecordProgressListener(this);

        mCameraPreviewView.setOnRecordFinishListener(this);

    }


    /***
     * 拍照
     * @param view
     */
    public void openCamera(View view){
        mCameraPreviewView.takePicture();
    }

    /***
     * 切换摄像头
     * @param view
     */
    public void switchCamera(View view){

        mCameraPreviewView.switchCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCameraPreviewView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mCameraPreviewView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        mSurfaceLayout.removeAllViews();

        mCameraPreviewView=null;
    }


    /***
     * 拍照结果
     * @param path
     */
    @Override
    public void callback(String path, int cameraId) {


        Intent intent = new Intent(this, PhotoActivity.class);

        intent.putExtra("data", path);
        intent.putExtra("cameraId", cameraId);

        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        isLongClick=true;
        /***
         * 开始录制
         */
        mCameraPreviewView.startRecord();
        return true;
    }

    private boolean isLongClick=false;

    @Override
    public void onClick(View v) {
        isLongClick=false;

        mCameraPreviewView.takePicture();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction()==MotionEvent.ACTION_UP&& isLongClick){

                mCameraPreviewView.stopRecord();
        }

        return false;
    }

    private String TAG=this.getClass().getSimpleName();
    @Override
    public void onProgressChanged(int maxTime, int currentTime) {

        Log.e(TAG,"onProgressChanged:"+currentTime*100/maxTime+"线程："+Thread.currentThread().getName());
    }

    @Override
    public void onRecordFinish() {

        Log.e(TAG,"onRecordFinish:"+Thread.currentThread().getName());
    }
}
