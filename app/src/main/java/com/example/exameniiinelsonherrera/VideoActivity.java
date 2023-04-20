package com.example.exameniiinelsonherrera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {
    VideoView videoViewVideo;
    String selectedUserVideoUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videoViewVideo = findViewById(R.id.videoViewVideo);

        // Obtener los datos del usuario seleccionado enviados desde ListActivity
        Intent intent = getIntent();

        selectedUserVideoUrl = intent.getStringExtra("selected_user_video_url");

        // cargar el video desde la URL usando el método setVideoURI()
        videoViewVideo.setVideoURI(Uri.parse(selectedUserVideoUrl));
        videoViewVideo.requestFocus();
        videoViewVideo.start();
    }
}