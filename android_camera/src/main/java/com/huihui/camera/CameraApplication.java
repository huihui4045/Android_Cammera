package com.huihui.camera;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

/**
 * Created by gavin
 * Time 2017/12/7  17:38
 * Email:molu_clown@163.com
 */

public class CameraApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }


    }
}
