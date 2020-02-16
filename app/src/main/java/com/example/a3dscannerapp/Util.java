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

    public static void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }

    public static void uploadFile_new(String filePath, String uploadUrl, String description) throws IOException {
        // create upload service client
        trustAllCertificates();
        ServiceGenerator generator = new ServiceGenerator();

        try
        {
            URL url = new URL(uploadUrl);
            String baseUrl = url.getProtocol() + "://" + url.getHost();
            generator.setup(baseUrl);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            return;
        }


        ApiConfig service = generator.createService(ApiConfig.class);

        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = new File(filePath);

        InputStream in = new FileInputStream(file);
        byte[] buf;
        buf = new byte[in.available()];
        while (in.read(buf) != -1);

        // create RequestBody instance from file
        RequestBody requestBody =
                RequestBody.create(
                        MediaType.parse("application/octet-stream"),
                        buf
                );

        // finally, execute the request
        trustAllCertificates();
//        Call<ResponseBody> call = service.upload(file.getName(), requestBody);
        Call<ResponseBody> call = service.uploadFullUrl(uploadUrl, file.getName(), requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Upload:", response.message());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

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
}