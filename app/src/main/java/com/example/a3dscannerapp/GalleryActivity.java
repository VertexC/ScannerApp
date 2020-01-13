package com.example.a3dscannerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.os.Bundle;
import android.widget.TableLayout;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
    }


    public void createGalleryRow() {
        TableLayout tl = (TableLayout) findViewById(R.id.galleryTable);
        // TODO: load file information from public file storage

        // TODO: add button option for uploading
    }
}
