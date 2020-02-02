package com.example.a3dscannerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.a3dscannerapp.VideoCaptureActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private Button mOpenVideoCaptureButton;
    private Button mOpenConfigurationButton;
    private Button mOpenGalleryButton;
    private Button mOpenMultiCamVideoCaptureButton;

    public static final String appFilesFolderName = "scanApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOpenVideoCaptureButton = (Button)findViewById(R.id.openVideoCaptureButton);
        mOpenConfigurationButton = (Button)findViewById(R.id.openConfiguration);
        mOpenGalleryButton = (Button)findViewById(R.id.openGalleryButton);
        mOpenMultiCamVideoCaptureButton = (Button)findViewById(R.id.openMultiCameraVideoCaptureButton);

        final Intent videoCaputureIntent = new Intent(this, VideoCaptureActivity.class);
        final Intent galleryIntent = new Intent(this, GalleryActivity.class);
        final Intent multiCamVideoCaptureIntent = new Intent(this, MultiCameraVideoCaptureActivity.class);
        final Intent preferenceIntent = new Intent(this, PreferenceActivity.class);


        mOpenVideoCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(videoCaputureIntent);
            }
        });

        mOpenGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(galleryIntent);
            }
        });

        mOpenMultiCamVideoCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(multiCamVideoCaptureIntent);
            }
        });

        mOpenConfigurationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(preferenceIntent);
            }
        });

    }
}
