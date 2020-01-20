package com.example.a3dscannerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.icu.util.Output;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MultiCameraVideoCaptureActivity extends AppCompatActivity {

    private File mAppFilesFolder;
    private String mAppFilesFolderName = MainActivity.appFilesFolderName;
    private File mVideoFolder;
    private String mVideoFileName;

    private TableLayout mMultiCamCaptureTable;
    private ImageButton mRecordImageButton;

    private static final int MAX_PREVIEW_WIDTH = 320;
    private static final int MAX_PREVIEW_HEIGHT = 180;

    private int mTotalRotation;
    private int mSurfaceAvailableCount = 0;
    private Size mPreviewSize = new Size(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT);
    private CameraDevice mCamera;
    List<String> mPhysicalCameraIds = new ArrayList<String>();
    String mLogicalCameraId;
    HashMap<String, Size> mPreviewSizes = new HashMap<String, Size>();
    HashMap<String, Size> mVideoSizes = new HashMap<String, Size>();
    HashMap<String, TextureView> mTextureViews = new HashMap<String, TextureView>();
    HashMap<String, TextureView.SurfaceTextureListener> mSurfaceTextureListeners = new HashMap<String, TextureView.SurfaceTextureListener>();

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mCaptureRequestBuilder;

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            showToast("Camera connection Made!");
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            showToast("Camera close!");
            closeCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            showToast("Camera close with Error!");
            closeCamera();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_camera_video_capture);

        // TODO: check if the api>=28 (for multiple camera)

        mMultiCamCaptureTable = (TableLayout) findViewById(R.id.multiCamCaptureTable);

        mAppFilesFolder = Util.createAppFilesFolder(mAppFilesFolderName);

        getCameraIds();

        mRecordImageButton = (ImageButton) findViewById(R.id.multiCamVideoOnlineImageButton);
        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void getCameraIds() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int[] capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) ==
                        characteristics.LENS_FACING_FRONT) {
                    continue;
                }
                if (Arrays.asList(capabilities).contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA)){
                    mLogicalCameraId = cameraId;
                    continue;
                }
                mPhysicalCameraIds.add(cameraId);
                mSurfaceTextureListeners.put(cameraId, createSurfaceTextureListener());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TableRow row = new TableRow(MultiCameraVideoCaptureActivity.this);
                        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(5, 10, 5, 10);
                        row.setLayoutParams(lp);


                        TextureView textureView = new TextureView(MultiCameraVideoCaptureActivity.this);
                        mTextureViews.put(cameraId, textureView);
                        row.addView(textureView);

                        mMultiCamCaptureTable.addView(row, lp);
                    }
                });
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MultiCameraVideoCaptureActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("3DScan");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();

        boolean surfaceAllAvailable = true;
        for(String cameraId:mPhysicalCameraIds) {
            if (!mTextureViews.get(cameraId).isAvailable()) {
                surfaceAllAvailable = false;
                mTextureViews.get(cameraId).setSurfaceTextureListener(mSurfaceTextureListeners.get(cameraId));
            } else {
                mSurfaceAvailableCount += 1;
            }
        }

        if(surfaceAllAvailable){
            connectCamera();
        }
    }

    @Override
    protected void onPause() {
        closeCamera();

        stopBackgroundThread();

        mSurfaceAvailableCount = 0;
        super.onPause();
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private TextureView.SurfaceTextureListener createSurfaceTextureListener() {
        return new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurfaceAvailableCount += 1;
                if (mSurfaceAvailableCount == mPhysicalCameraIds.size()){
                    connectCamera();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Util.REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showToast("Application will not run without camera services!");
            }
        }
    }

    private void connectCamera() {
        // FIXME: how to do multiple
        // FIXME: what does context means in android
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            // FIXME: what is this?
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mLogicalCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        showToast("Video app required access to camera");
                    }
                    // FIXME: why REQUEST_CAMERA_PERMISSION_RESULT is final?
                    requestPermissions(new String[] {Manifest.permission.CAMERA}, 0);
                }
            } else {
                cameraManager.openCamera(mLogicalCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            List<String> mPhysicalCameraIds = new ArrayList<String>();
            List<OutputConfiguration> outputConfigs = new ArrayList<OutputConfiguration>();
            for(String cameraId:mPhysicalCameraIds) {
                SurfaceTexture surfaceTexture = mTextureViews.get(cameraId).getSurfaceTexture();
                surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

                Surface surface = new Surface(surfaceTexture);
                OutputConfiguration outputConfig = new OutputConfiguration(surface);
                outputConfig.setPhysicalCameraId(cameraId);
                outputConfigs.add(outputConfig);
                mCaptureRequestBuilder.addTarget(surface);
            }

            mCameraDevice.createCaptureSessionByOutputConfigurations(outputConfigs, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    showToast("Unable to setup camera preview");
                }
            }, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
