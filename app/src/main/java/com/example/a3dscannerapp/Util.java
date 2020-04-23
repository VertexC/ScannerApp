package com.example.a3dscannerapp;

import android.app.Activity;
import android.app.UiAutomation;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.appcompat.view.menu.ActionMenuItem;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Comparator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Url;

public class Util {


    public static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;

    public static File createAppFilesFolder(String appFilesFolderName) {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File appFilesFolder = new File(movieFile, appFilesFolderName);
        if(!appFilesFolder.exists()) {
            appFilesFolder.mkdirs();
        };
        return appFilesFolder;
    }

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * rhs.getHeight() /
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        // FIXME: what does this means?
        Size bigEnough = null;
        int minAreaDiff = Integer.MAX_VALUE;
        for (Size option : choices) {
            int diff = (width*height)-(option.getWidth()*option.getHeight()) ;
            if (diff >=0 && diff < minAreaDiff &&
                    option.getWidth() <= width &&
                    option.getHeight() <= height) {
                minAreaDiff = diff;
                bigEnough = option;
            }
        }
        if (bigEnough != null) {
            return bigEnough;
        } else {
            Arrays.sort(choices,new CompareSizeByArea());
            return choices[0];
        }
    }

    public static String makePlyHeader(String type, String name, int numOfElement, boolean asciiFlag) {
        String header = "ply\n";
        if (type == "imu") {
            // header for imu sensor data
            if(asciiFlag) {
                header += "format ascii 1.0\n";
            }
            else {
                header += "format binary_big_endian 1.0\n";
            }
            header += "element " + name + " " + numOfElement + "\n";
            header += "comment imu sensor data with timestamp\n";
            if (name == "orientation") {
                header += "property uint64 timestamp\nproperty double roll\nproperty double pitch\nproperty double yall\n";
            }
            else {
                header += "property uint64 timestamp\nproperty double x\nproperty double y\nproperty double z\n";
            }
        }
        header += "end_header\n";

        return header;
    }
}