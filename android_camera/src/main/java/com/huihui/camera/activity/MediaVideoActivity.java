package com.huihui.camera.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import com.huihui.camera.R;

public class MediaVideoActivity extends AppCompatActivity {

    private VideoView mVideoView;

    private MediaController mMediaController;
    private int mPlayingPos=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_video);
        mVideoView = ((VideoView) findViewById(R.id.videoView));

        mMediaController = new MediaController(this);

        mMediaController.setMediaPlayer(mVideoView);

        String url = getIntent().getStringExtra("data");

        if (url != null && !"".equals(url)) {

            mVideoView.setVideoPath(url);

            mVideoView.start();
        }


    }

    @Override
    protected void onResume() {


        if (mPlayingPos > 0) {
            //此处为更好的用户体验,可添加一个progressBar,有些客户端会在这个过程中隐藏底下控制栏,这方法也不错
            mVideoView.start();
            mVideoView.seekTo(mPlayingPos);
            mPlayingPos = 0;
        }

        super.onResume();


    }

    @Override
    protected void onStop() {
        super.onStop();

        mVideoView.stopPlayback();
    }



    @Override
    protected void onPause() {
        super.onPause();

        mPlayingPos = mVideoView.getCurrentPosition(); //先获取再stopPlay(),原因自己看源码
        mVideoView.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
