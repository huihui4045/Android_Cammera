package com.huihui.camera.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.huihui.camera.R;
import com.huihui.camera.listener.OnTakePictureCallBack;
import com.huihui.camera.view.CameraPreviewView;

public class CustomCameraActivity extends AppCompatActivity implements OnTakePictureCallBack {

    private FrameLayout mSurfaceLayout;
    private CameraPreviewView mCameraPreviewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏


        setContentView(R.layout.activity_custom_camera);

        mSurfaceLayout = ((FrameLayout) findViewById(R.id.surfaceViewLayout));

        mCameraPreviewView = new CameraPreviewView(this);

        mSurfaceLayout.addView(mCameraPreviewView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mCameraPreviewView.setOnTakePictureCallBack(this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        mCameraPreviewView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mCameraPreviewView.clearCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSurfaceLayout.removeAllViews();
    }

    /***
     * 拍照
     * @param view
     */
    public void openCamera(View view) {

        mCameraPreviewView.takePicture();

    }

    /****
     * 切换摄像头
     * @param view
     */
    public void switchCamera(View view) {

        mCameraPreviewView.switchCamera();
    }


    /***
     * 拍照结果
     * @param path
     */
    @Override
    public void callback(String path,int cameraId) {


        Intent intent=new Intent(this,PhotoActivity.class);

        intent.putExtra("data",path);
        intent.putExtra("cameraId",cameraId);

        startActivity(intent);
    }
}
