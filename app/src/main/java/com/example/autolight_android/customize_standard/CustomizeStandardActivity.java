package com.example.autolight_android.customize_standard;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static com.example.autolight_android.MainActivity.btThread;
import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

import com.example.autolight_android.R;
import com.example.autolight_android.database.DBHelper;
import com.example.autolight_android.database.StandardItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class CustomizeStandardActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private DBHelper mDBHelper;
    private StandardItem mStandardItem;

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

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_customize_standard);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view1);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(CAMERA_ID_FRONT);

        mDBHelper = new DBHelper(this);
        mStandardItem = mDBHelper.getStandard();

        SeekBar seekBar = findViewById(R.id.seekBar);
        TextView seekText = findViewById(R.id.seekText);

        btThread.write("65c");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                seekText.setText(String.valueOf(progress));
                btThread.write(String.valueOf(seekBar.getProgress())+"c");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekText.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekText.setText(String.valueOf(seekBar.getProgress()));
            }
        });

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
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
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

    public native long loadCascade2(String cascadeFileName);
    public native int getFacelight2(long cascadeClassfier_face, long matAddrInput, long matAddrResult);
    public long cascadeClassifier_face =0;

    private void read_cascade_file(){
        copyFile("haarcascade_frontalface_alt.xml");
        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_face = loadCascade2( getFilesDir().getAbsolutePath() + "/haarcascade_frontalface_alt.xml");
        Log.d(TAG, "read_cascade_file:");

    }


    // 카메라에서 받는 프레임 가지고 작업하는 함수
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat inputMat = inputFrame.rgba();

        Mat matResult = null;
        if ( matResult == null )
            matResult = new Mat(inputMat.rows(), inputMat.cols(), inputMat.type());

        int stLight = getFacelight2(cascadeClassifier_face,inputMat.getNativeObjAddr(), matResult.getNativeObjAddr());

        ImageButton okButton = findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stLight > -1) {
                    mDBHelper.updateStLight(mStandardItem.getId(), stLight);
                    Toast.makeText(getApplicationContext(), "기준 밝기값(" + stLight + ")을 저장했습니다.", Toast.LENGTH_SHORT).show();

                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "얼굴을 추출하지 못했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return matResult;
    }

}