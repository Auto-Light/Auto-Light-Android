package com.example.autolight_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

import static android.Manifest.permission.CAMERA;

import java.util.Collections;
import java.util.List;

public class RecordVideo extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private int m_Camidx = 0; // front: 1, back: 0
    private CameraBridgeViewBase m_CameraView;

    private Mat matInput;

    private static final int CAMERA_PERMISSION_CODE = 200;
    private static final String TAG = "opencv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);

        m_CameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view);
        m_CameraView.setVisibility(SurfaceView.VISIBLE);
        m_CameraView.setCvCameraViewListener(this);
        m_CameraView.setCameraIndex(m_Camidx);
    }

    // 카메라 권한 받아오기
    @Override
    protected void onStart() {
        super.onStart();
        boolean _Permission = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION_CODE);
                _Permission = false;
            }
        }

        if (_Permission) {
            onCameraPermissionGranted();
        }
    }

    private void onCameraPermissionGranted() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();

        if (cameraViews == null) {
            return;
        }

        for (CameraBridgeViewBase cameraBridgeViewBase: cameraViews) {
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.setCameraPermissionGranted();
            }
        }
    }

    private List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(m_CameraView);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return null;
    }
}