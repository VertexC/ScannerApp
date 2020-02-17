package com.example.a3dscannerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;




public class ServerUploadActivity extends AppCompatActivity {

    private static final int PROGRESS = 0x1;
    private ProgressBar mProgress;
    private TextView mFileInfoView;
    private TextView mResponseView;
    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();

    enum UploadStatus {
        Init,
        Success,
        Fail
    }

    private UploadStatus mUploadStatus = UploadStatus.Init;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_upload);

        mProgress = (ProgressBar) findViewById(R.id.upload_progress_bar);
        mFileInfoView = (TextView) findViewById(R.id.upload_file_txt);
        mResponseView = (TextView) findViewById(R.id.upload_response_txt);

        // Start lengthy operation in a background thread
        String[] fileUploads = getIntent().getStringArrayExtra("fileList");
        uploadAndShowProgress(fileUploads);
    }

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

    public String uploadFile(String filePath, String uploadUrl) {
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
            return "Invalid Url!";
        }


        ApiConfig service = generator.createService(ApiConfig.class);

        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = new File(filePath);
        byte[] buf;
        try{
            InputStream in = new FileInputStream(file);
            buf = new byte[in.available()];
            while (in.read(buf) != -1);
        } catch (IOException e) {
            return "Cannot read the file!";
        }

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
                mUploadStatus = UploadStatus.Success;
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                mUploadStatus = UploadStatus.Fail;
            }
        });
        return "";
    }

    void uploadAndShowProgress(final String [] filePathList) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ServerUploadActivity.this);
        final String url = preferences.getString("uploadUrl", "");

        mProgressStatus = 0;
        final int perProgress = 100 / filePathList.length;
        new Thread(new Runnable() {
            public void run() {

                for(String filePath: filePathList) {
                    mFileInfoView.setText("Uploading:"+ new File(filePath).getName());
                    mUploadStatus = UploadStatus.Init;
                    String response;

                    response = uploadFile(filePath, url);
                    if(!response.equals("")){
                        // fail to upload
                        mResponseView.setText(response);
                        return;
                    }

                    long startTime = System.currentTimeMillis();
                    while(mUploadStatus == UploadStatus.Init) {
                        long endTime = System.currentTimeMillis();
                        long seconds = (endTime - startTime) / 1000;
                        if (seconds > 60*60){
                            // upload time out
                            mResponseView.setText("Upload Time Out!");
                        }
                    } // wait for finish

                    if(mUploadStatus == UploadStatus.Success) {
                        // start next file
                    } else {
                        mResponseView.setText("Fail to upload");
                        return;
                    }

                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgressStatus += perProgress;
                            mProgress.setProgress(mProgressStatus);
                        }
                    });
                }
                mHandler.post(new Runnable() {
                    public void run() {
                        mProgress.setProgress(100);
                    }
                });

            }
        }).start();

    }
}