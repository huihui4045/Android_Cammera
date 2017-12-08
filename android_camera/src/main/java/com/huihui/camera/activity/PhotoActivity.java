package com.huihui.camera.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.huihui.camera.R;
import com.huihui.camera.utils.PhotoBitmapUtils;

public class PhotoActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView mTextView;

    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        imageView = ((ImageView) findViewById(R.id.image));

        mTextView = ((TextView) findViewById(R.id.image_path));


        String path = getIntent().getStringExtra("data");

        int cameraId=getIntent().getIntExtra("cameraId", Camera.CameraInfo.CAMERA_FACING_FRONT);

        if (path != null && !"".equals(path)) {

            mTextView.setText(String.format("图片路径：%s", path));

            try {

                Bitmap bitmap = BitmapFactory.decodeFile(path);


                int angle=0;

                if (cameraId==Camera.CameraInfo.CAMERA_FACING_FRONT){

                    angle=-90;
                }else {

                    angle=90;
                }



                Bitmap result = PhotoBitmapUtils.rotaingImageView(angle, bitmap);


                if (result != null) {

                    imageView.setImageBitmap(result);
                }


            } catch (Exception e) {
                e.printStackTrace();

                Log.e(TAG, "异常：" + e.getMessage());
            }
        }


    }
}
