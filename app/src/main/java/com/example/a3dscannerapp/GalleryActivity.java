package com.example.a3dscannerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.a3dscannerapp.MainActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class GalleryActivity extends AppCompatActivity {

    private String mAppFilesFolderName = MainActivity.appFilesFolderName;

    TableLayout mGalleryTable;
    Intent mVideoPlayIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);


        mGalleryTable = (TableLayout) findViewById(R.id.galleryTable);
        mVideoPlayIntent = new Intent(this, VideoPlayActivity.class);
        mVideoPlayIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

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
            File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File appFilesFolder = new File(movieFile, mAppFilesFolderName);
            if (appFilesFolder.exists()) {
                for (File inFile1 : appFilesFolder.listFiles()) {
                    if (inFile1.isDirectory()) {
                        File videoFile = null;
                        for (File inFile2 : inFile1.listFiles()) {
                            if (isVideoFile(inFile2.getAbsolutePath())) {
                                videoFile = inFile2;
                                break;
                            }
                        }
                        if (videoFile != null) {
                            createGalleryRow(videoFile, appFilesFolder);
                        }
                    }
                }
            }
        }
    };


    public void createGalleryRow(final File videoFile, final File appFilesFolder) {
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
                    ImageButton thumbnailBtn = new ImageButton(GalleryActivity.this);
                    thumbnailBtn.setImageBitmap(scaled_bitmap);
                    thumbnailBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mVideoPlayIntent.putExtra("videoUri", Uri.fromFile(videoFile).toString());
                            startActivity(mVideoPlayIntent);
                        }
                    });
                    Button uploadButton = new Button(GalleryActivity.this);
                    uploadButton.setText("upload");
                    uploadButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            Util.upLoad2Server(videoFile.getAbsolutePath(), "10.0.2.2:5000/uploadVideo");
//                            Util.uploadFile(videoFile.getAbsolutePath(), "https://10.0.2.2:5000", GalleryActivity.this);
//                              Util.uploadFile_new(videoFile.getAbsolutePath(), "https://10.0.2.2:5000");
//                            Util.uploadFile_new(videoFile.getAbsolutePath(), "http://aspis.cmpt.sfu.ca/multiscan/");
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        File gpxfile = new File(appFilesFolder, "bowenc_test01.txt");
                                        FileWriter writer = new FileWriter(gpxfile);
                                        writer.append("test test");
                                        writer.flush();
                                        writer.close();
                                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GalleryActivity.this);
                                        String url = preferences.getString("uploadUrl", "");
//                                        Util.uploadFile_new(gpxfile.getAbsolutePath(), "http://aspis.cmpt.sfu.ca/");
//                                        Util.uploadFile_new(gpxfile.getAbsolutePath(), "http://aspis.cmpt.sfu.ca/multiscan/upload");
                                        Util.uploadFile_new(gpxfile.getAbsolutePath(), url);
//                                        Util.uploadFile_new(gpxfile.getAbsolutePath(), "http://10.0.2.2:5000");

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            thread.start();
                        }
                    });
                    // TODO: add upload functionality

                    TextView descriptionText = new TextView(GalleryActivity.this);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    try {
                        Date d = sdf.parse(videoFile.getName().substring(6, 21));
                        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
                        descriptionText.setText("Time: " + sdf.format(d));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    row.addView(thumbnailBtn);
                    row.addView(descriptionText);
                    row.addView(uploadButton);
                    mGalleryTable.addView(row, lp);
                }
            });
        }
    }



}
