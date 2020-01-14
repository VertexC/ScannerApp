package com.example.a3dscannerapp;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a3dscannerapp.MainActivity;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class GalleryActivity extends AppCompatActivity {

    private String mAppFilesFolderName = MainActivity.appFilesFolderName;

    TableLayout mGalleryTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mGalleryTable = (TableLayout) findViewById(R.id.galleryTable);
        createGalleryRowThread.start();
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static Bitmap retrieveVideoFrameFromVideo(String videoPath)
            throws Throwable {
        Bitmap bitmap = null;
        FileInputStream inputStream = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        FFmpegMediaMetadataRetriever retriever = new  FFmpegMediaMetadataRetriever();
        try {
            inputStream = new FileInputStream(videoPath);
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(inputStream.getFD());

            bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String videoPath)"
                            + e.getMessage());

        } finally {
            if (retriever != null) {
                retriever.release();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return bitmap;
    }

    Thread createGalleryRowThread = new Thread() {
        @Override
        public void run() {
            // TODO: load file information from public file storage

            // TODO: add button option for uploading

            File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File mAppFilesFolder = new File(movieFile, mAppFilesFolderName);
            if (mAppFilesFolder.exists()) {
                for (File inFile1 : mAppFilesFolder.listFiles()) {
                    if (inFile1.isDirectory()) {
                        File videoFile = null;
                        for (File inFile2 : inFile1.listFiles()) {
                            if (isVideoFile(inFile2.getAbsolutePath())) {
                                videoFile = inFile2;
                                break;
                            }
                        }
                        if (videoFile != null) {
                            createGalleryRow(videoFile);
                        }
                    }
                }
            }
        }
    };


    public void createGalleryRow(final File videoFile) {
        TableLayout tl = (TableLayout) findViewById(R.id.galleryTable);
        Bitmap bitmap = null;

        try {
            bitmap = retrieveVideoFrameFromVideo(videoFile.getAbsolutePath());
            //                            bitmap = ThumbnailUtils.createVideoThumbnail(videoFile.getAbsolutePath(),
            //                                    MediaStore.Images.Thumbnails.MINI_KIND);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (bitmap != null) {
            bitmap = Bitmap.createScaledBitmap(bitmap, 240, 240, false);

            final Bitmap scaled_bitmap = bitmap;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TableRow row = new TableRow(GalleryActivity.this);
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(5, 10, 5, 10);
                    row.setLayoutParams(lp);
                    ImageView thumbnailView = new ImageView(GalleryActivity.this);
                    thumbnailView.setImageBitmap(scaled_bitmap);
                    Button uploadButton = new Button(GalleryActivity.this);
                    uploadButton.setText("upload");
                    TextView descriptionText = new TextView(GalleryActivity.this);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    try {
                        Date d = sdf.parse(videoFile.getName().substring(6, 21));
                        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
                        descriptionText.setText("Time: " + sdf.format(d));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    row.addView(thumbnailView);
                    row.addView(descriptionText);
                    row.addView(uploadButton);
                    mGalleryTable.addView(row, lp);
                }
            });

        }
    }
}
