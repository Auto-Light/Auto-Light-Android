package com.example.autolight_android.control_light;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import static android.Manifest.permission.CAMERA;

import static com.example.autolight_android.MainActivity.btThread;
import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

import com.example.autolight_android.R;
import com.example.autolight_android.database.DBHelper;
import com.example.autolight_android.database.StandardItem;

import java.util.Collections;
import java.util.List;

public class ControlLightActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private DBHelper mDBHelper;
    private StandardItem mStandardItem;
    private int mLampDial;

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java4");
    }

    public native int getLight(long matAddrInput);

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_control_light);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view2);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(CAMERA_ID_FRONT);

        mDBHelper = new DBHelper(this);
        mStandardItem = new StandardItem();
        mStandardItem = mDBHelper.getStandard();

        mLampDial = mStandardItem.getLampDial();
        btThread.write(String.valueOf(mLampDial)+"c");

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // 카메라 시작할 때 카메라 권한 받아오기
    @Override
    protected void onStart() {
        super.onStart();
        boolean _Permission = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                _Permission = false;
            }
        }

        if (_Permission) {
            onCameraPermissionGranted();
        }
    }

    // 미사용 시 카메라 할당 해제
    @Override
    public void onPause() {
        super.onPause();

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume::OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "onResume::Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        }
    }

    // 미사용 시 카메라 할당 해제
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }


    // 카메라 권한 관련 메소드
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
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    // 카메라에서 받는 프레임 가지고 작업하는 함수
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat inputMat = inputFrame.rgba();
        Mat screenMat = new Mat(inputMat.rows(), inputMat.cols(), inputMat.type());

        int stLight = mStandardItem.getStLight();
        int nowLight = getLight(inputMat.getNativeObjAddr());

        // 적정 밝기로 조명 조절을 완료한 경우
        if (nowLight == stLight) {
            mDBHelper.updateLampDial(mStandardItem.getId(), mLampDial); // 현재 조명 다이얼 값 저장
            Toast.makeText(getApplicationContext(), "조명 조절을 완료하였습니다.", Toast.LENGTH_LONG).show();
            finish();   // 현재 액티비티 종료
        }

        // 조명 밝기를 더이상 조절할 수 없는 경우
        if (mLampDial <= 25 || mLampDial >= 100) {
            mDBHelper.updateLampDial(mStandardItem.getId(), mLampDial); // 현재 조명 다이얼 값 저장
            Toast.makeText(getApplicationContext(), "더이상 조명을 조절할 수 없습니다.", Toast.LENGTH_LONG).show();
            finish();   // 현재 액티비티 종료
        }
        else if (nowLight > stLight) {
            // 조명 밝기 낮추기
            mLampDial--;
            btThread.write(String.valueOf(mLampDial)+"c");
        }
        else if (nowLight < stLight) {
            // 조명 밝기 높이기
            mLampDial++;
            btThread.write(String.valueOf(mLampDial)+"c");
        }

        return screenMat;
    }
}