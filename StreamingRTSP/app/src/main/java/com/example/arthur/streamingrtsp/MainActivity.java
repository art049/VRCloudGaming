package com.example.arthur.streamingrtsp;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;


public class MainActivity extends Activity
{

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        videoView = null;

        setContentView(R.layout.activity_main);

        videoView =(VideoView)findViewById(R.id.VideoView);
        MediaController mediaController= new MediaController(this);
        mediaController.setAnchorView(videoView);

        // Uri uri =
        Uri uri = Uri.parse("rtsp://192.168.42.220:5454/stream.sdp");

        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
    }
}