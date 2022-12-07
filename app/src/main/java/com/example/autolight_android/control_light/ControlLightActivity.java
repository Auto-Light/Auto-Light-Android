package com.example.autolight_android.control_light;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static com.example.autolight_android.MainActivity.btThread;
import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

import com.example.autolight_android.R;
import com.example.autolight_android.database.DBHelper;
import com.example.autolight_android.database.StandardItem;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class ControlLightActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private DBHelper mDBHelper;
    private StandardItem mStandardItem;
    private int mLampDial;

    public static Boolean mIsStart;

    // 타이머
    private long nStart = 0;
    private long nEnd = 0;

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java4");
    }



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

        // start
        mIsStart = false;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_control_light);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view1);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(CAMERA_ID_FRONT);

        mDBHelper = new DBHelper(this);
        mStandardItem = mDBHelper.getStandard();

        mLampDial = mStandardItem.getLampDial();
        //btThread.write(String.valueOf(mLampDial)+"c");
        btThread.write(65 + "c");
        Toast.makeText(getApplicationContext(), mStandardItem.getLampDial() + " " + mStandardItem.getStLight(), Toast.LENGTH_SHORT).show();

        ImageButton backButton = findViewById(R.id.back_button);

        ImageButton startButton = findViewById(R.id.start_button);
        startButton.setClickable(true);

        ImageView loadingSpinner = (ImageView)findViewById(R.id.loading);
        Glide.with(this).load(R.drawable.loading).into(loadingSpinner);
        loadingSpinner.setVisibility(loadingSpinner.INVISIBLE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nStart = System.currentTimeMillis();    // 타이머 시작
                mIsStart = true;                        // 밝기 조절 시작
                startButton.setClickable(false);        // 시작 버튼 클릭 비활성화

                // 로딩 스피너 띄우기
                loadingSpinner.setVisibility(loadingSpinner.VISIBLE);

            }
        });
    }

    // 카메라 시작할 때 카메라 권한 받아오기
    @Override
    protected void onStart() {
        super.onStart();
        boolean _Permission = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA,WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
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

                read_cascade_file();
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


    // 메소드 가져오기위해 사용
    private void copyFile(String filename) {

        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();
        File outputFile = new File( getFilesDir() + "/" + filename );

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ outputFile.toString());
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

    }

    private void read_cascade_file(){
        copyFile("haarcascade_frontalface_alt.xml");
        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_face = loadCascade( getFilesDir().getAbsolutePath() + "/haarcascade_frontalface_alt.xml");
        Log.d(TAG, "read_cascade_file:");

    }

    public native long loadCascade(String cascadeFileName);
    public native int getFacelight (long cascadeClassfier_face, long matAddrInput, long matAddrResult);
    public long cascadeClassifier_face =0;


    // 카메라에서 받는 프레임 가지고 작업하는 함수
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat matInput = inputFrame.rgba();

        Mat matResult = null;
        if (matResult == null)
            matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());

        int stLight = mStandardItem.getStLight();
        int nowLight = getFacelight(cascadeClassifier_face, matInput.getNativeObjAddr(), matResult.getNativeObjAddr());

        if (mIsStart) {
            // 얼굴 추출이 된 경우
            if (nowLight > -1) {

                int diffLight = Math.abs(stLight - nowLight);

                // 적정 밝기로 조명 조절을 완료한 경우
                if (diffLight <= 5) {
                    nEnd = System.currentTimeMillis();
                    mDBHelper.updateLampDial(mStandardItem.getId(), mLampDial); // 현재 조명 다이얼 값 저장

                    // 팝업 띄우기
                    Intent intent = new Intent(this, PopUpDialogActivity.class);
                    intent.putExtra("data", "조명 조절을 완료하였습니다." + nowLight);
                    intent.putExtra("time", "실행시간 : " + (nEnd - nStart) + "ms");
                    startActivityForResult(intent, 1);
                } else if (nowLight > stLight) {
                    // 조명 밝기 낮추기
                    mLampDial--;
                    btThread.write(String.valueOf(mLampDial)+"c");
                } else if (nowLight < stLight) {
                    // 조명 밝기 높이기
                    mLampDial++;
                    btThread.write(String.valueOf(mLampDial)+"c");
                }

                // 조명 밝기를 더이상 조절할 수 없는 경우
                if (mLampDial < 25) {
                    nEnd = System.currentTimeMillis();
                    mDBHelper.updateLampDial(mStandardItem.getId(), 25); // 조명 다이얼 값 25 저장

                    // 팝업 띄우기
                    Intent intent = new Intent(this, PopUpDialogActivity.class);
                    intent.putExtra("data", "더이상 조명 밝기를 낯출 수 없습니다." + nowLight);
                    intent.putExtra("time", "실행시간 : " + (nEnd - nStart) + "ms");
                    startActivityForResult(intent, 1);
                } else if (mLampDial > 100) {
                    nEnd = System.currentTimeMillis();
                    mDBHelper.updateLampDial(mStandardItem.getId(), 100); // 조명 다이얼 값 100 저장

                    // 팝업 띄우기
                    Intent intent = new Intent(this, PopUpDialogActivity.class);
                    intent.putExtra("data", "더이상 조명 밝기를 높일 수 없습니다." + nowLight);
                    intent.putExtra("time", "실행시간 : " + (nEnd - nStart) + "ms");
                    startActivityForResult(intent, 1);
                }
            }
        }

        return matResult;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //데이터 받기
                boolean isExit = data.getBooleanExtra("EXIT", false);

                if (isExit) { finish(); }
            }
        }
    }
}