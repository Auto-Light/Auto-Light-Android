package com.example.autolight_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.autolight_android.bluetooth.BluetoothConnect;
import com.example.autolight_android.bluetooth.ConnectedThread;
import com.example.autolight_android.control_light.ControlLightActivity;
import com.example.autolight_android.customize_standard.CustomizeStandardActivity;
import com.example.autolight_android.permisson_support.PermissionSupport;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'autolight_android' library on application startup.
    //static {
        //System.loadLibrary("autolight_android");
    //}

    // 클래스 선언
    private PermissionSupport permission;
    private BluetoothConnect btConnect;
    @SuppressLint("StaticFieldLeak")
    public static ConnectedThread btThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();

        // 기준값 설정 버튼 눌렀을 때 setting activity로 이동
        ImageButton imageButton = findViewById(R.id.button1);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getApplicationContext(), CustomizeStandardActivity.class);
                //startActivity(intent);

                if(btConnect != null && btConnect.isBluetoothConnect()){
                    if(btThread == null){
                        btThread = btConnect.btThread;
                    }
                    Intent intent = new Intent(getApplicationContext(), CustomizeStandardActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(MainActivity.this, "블루투스 연결이 필요합니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // 밝기 조정 버튼 눌렀을 때 ControlLightActivity activity로 이동
        ImageButton imageButton2 = findViewById(R.id.button2);
        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getApplicationContext(), ControlLightActivity.class);
                //startActivity(intent);


                if(btConnect != null && btConnect.isBluetoothConnect()){
                    if(btThread == null){
                        btThread = btConnect.btThread;
                    }
                    Intent intent = new Intent(getApplicationContext(), ControlLightActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(MainActivity.this, "블루투스 연결이 필요합니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        ImageButton btButton = findViewById(R.id.button0);
        btButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothClick();
            }
        });
    }

    private void bluetoothClick() {
        btConnect = new BluetoothConnect(this,this);
        btConnect.start();
        btThread = btConnect.btThread;
    }

    // 권한 체크
    private void permissionCheck() {
        // PermissionSupport.java 클래스 객체 생성
        permission = new PermissionSupport(this, this);
        // 권한 체크 후 리턴이 false로 들어오면
        if (!permission.checkPermission()){
            //권한 요청
            permission.requestPermission();
        }
    }

    // Request Permission에 대한 결과 값 받아와
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //여기서도 리턴이 false로 들어온다면 (사용자가 권한 허용 거부)
        if (!permission.permissionResult(requestCode, permissions, grantResults)) {
            // 다시 permission 요청
            permission.requestPermission();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}