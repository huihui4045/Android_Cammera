package com.huihui.camera.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.huihui.camera.R;
import com.huihui.camera.utils.PhotoBitmapUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/****
 * http://blog.csdn.net/qq_26787115/article/details/50586554
 * 自定义相机
 * 使用Camera2自定义相机
 */
public class CameraActivity extends AppCompatActivity {

    // 返回码
    private static final int CODE = 1;

    private static final int CODE_BIG = 2;

    private static final int CODE_PERMISSION = 3;

    private ImageView imageView;



    // 记录文件保存位置
    private String mFilePath;

    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = ((ImageView) findViewById(R.id.image));

        Camera camera;



        mFilePath = String.format("%s%s%s", Environment.getExternalStorageDirectory().getPath(), File.separator, "photo.png");



        Log.e(TAG, "mFilePath:" + mFilePath);
    }

    public void openSystemCamera(View view) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent, CODE);
    }

    /***
     * 权限申请成功
     * @param grantedPermissions
     */

    @PermissionYes(CODE_PERMISSION)
    private void getPermissionYes(List<String> grantedPermissions) {
        // Successfully.

        Toast.makeText(this,"权限申请成功",Toast.LENGTH_LONG).show();

        openBigCamera();
    }

    private void openBigCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

      File  file=  new File(mFilePath);

        /***
         * 加载路径
         */
        Uri uri =null;



          try {
              file.createNewFile();


              /***
               * 适配Android 7.0
               */

              if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){

                  // uri= FileProvider.getUriForFile(this,"com.huihui.camera.provider",new File(mFilePath));

                  uri=Uri.fromFile(file);

              }else {

                  uri=Uri.fromFile(file);
              }


              /***
               * 指定存储路径，这样就可以保存原图了
               */
              intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

              intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);//保存照片的

              /***
               * 拍照返回图片
               */
              startActivityForResult(intent, CODE_BIG);



          } catch (IOException e) {
              e.printStackTrace();

              Log.e(TAG,"openBigCamera:"+e.getMessage());
          }




    }

    /****
     * 获取相机大图
     */
    public void openBigSystemCamera(View view) {


        AndPermission.with(this)
                .requestCode(CODE_PERMISSION)
                .callback(this)
                .permission(Permission.STORAGE,Permission.CAMERA)
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {

                        AndPermission.rationaleDialog(CameraActivity.this, rationale);
                    }
                })
                .start();


    }



    @PermissionNo(CODE_PERMISSION)
    private void getPermissionNo(List<String> deniedPermissions) {
        // Failure.

        Toast.makeText(this,"权限申请失败",Toast.LENGTH_LONG).show();
    }


    private FileInputStream inputStream;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == CODE) {

                Bundle extras = data.getExtras();

                Bitmap bitmap = (Bitmap) extras.get("data");

                imageView.setImageBitmap(bitmap);

            }

            if (requestCode == CODE_BIG) {

                try {
                    inputStream = new FileInputStream(mFilePath);


                    int drgee = PhotoBitmapUtils.readPictureDegree(mFilePath);

                    Log.e(TAG,"图片被旋转了："+drgee);



                   Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    Bitmap rotaingBitmap= PhotoBitmapUtils.rotaingImageView(drgee, bitmap);


                    imageView.setImageBitmap(rotaingBitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                    Log.e(TAG,"Exception:"+e.getMessage());
                }
            }


        }


    }


}
