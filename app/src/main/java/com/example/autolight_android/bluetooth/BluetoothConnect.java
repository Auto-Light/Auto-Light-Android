package com.example.autolight_android.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnect{
    // 블루투스 변수
    private final static int REQUEST_ENABLE_BT = 1;
    private final Context context;
    private final Activity activity;
    BluetoothAdapter btAdapter;

    Set<BluetoothDevice> pairedDevices;

    public String deviceName = "HC-06";
    public String deviceAddress;
    private int searchPair=0;
    private int searchSuccess=0;
    ArrayList<String> name = new ArrayList<String>();
    private Boolean isConnect=false;


    BluetoothSocket btSocket = null;
    public ConnectedThread btThread;
    BluetoothDevice device;

    IntentFilter filter;

    public BluetoothConnect(Activity _activity, Context _context){
        this.activity = _activity;
        this.context = _context.getApplicationContext();

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  //기기 검색 종료
        context.registerReceiver(receiver,filter); // context, activity
    }

    public void start() {
        bluetooth();
    }

    private void bluetooth() {
        // checkSelfPermission을 해주어야 밑에 enableBtIntent 사용 가능
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) { // 블루투스 지원 X
            Toast.makeText(activity.getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.\n앱 이용이 불가합니다.", Toast.LENGTH_SHORT).show();
        } else { // 블루투스 지원 O
            if (!btAdapter.isEnabled()) { // 블루투스 활성화 안되어있을 때
                Toast.makeText(activity.getApplicationContext(), "앱 이용을 위해\n블루투스 활성화가 필요합니다.", Toast.LENGTH_SHORT).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            Toast.makeText(activity.getApplicationContext(), "페어링 된 기기를 검색합니다.", Toast.LENGTH_SHORT).show();
            pairedDevice();
        }
    }

    public void pairedDevice() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();}
        pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if(device.getName().equals(deviceName)){
                    searchPair=1;
                    deviceAddress = device.getAddress();
                    bluetoothConnect();
                    break;
                }
            }
        }
        if(searchPair==0) {
            Toast.makeText(activity.getApplicationContext(), "페어링된 기기에서 " + deviceName + " 을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            searchDevice();
        }
    }

    private void searchDevice() { // 블루투스 기기 검색
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();}
        // 기기 검색 시작
        btAdapter.startDiscovery();
        Toast.makeText(activity.getApplicationContext(), "기기 검색을 시작합니다.", Toast.LENGTH_LONG).show();
    }

    // 블루투스 기기 검색에서 필요한 부분, BroadCastReceiver 생성
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    if(device.getName()!= null && !name.contains(device.getName())){
                        name.add(device.getName());
                        if(device.getName().equals(deviceName)){
                            deviceAddress = device.getAddress();
                            searchSuccess = 1;
                            bluetoothConnect();
                            break;
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:    //블루투스 기기 검색 종료
                    if(searchSuccess != 1) {
                        Toast.makeText(activity.getApplicationContext(), "기기를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Toast.makeText(activity.getApplicationContext(), "기기 연결이 해제되었습니다.",Toast.LENGTH_SHORT).show();
                    isConnect = false;
                    break;
            }
        }
    };

    private void bluetoothConnect() {
        Toast.makeText(activity.getApplicationContext(), deviceName+"에 연결중 ...", Toast.LENGTH_SHORT).show();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
        // create & connect socket
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            btSocket = device.createRfcommSocketToServiceRecord(deviceUUID);
            btSocket.connect();
            btThread = new ConnectedThread(btSocket);
            btThread.start();
            isConnect = true;
            Toast.makeText(activity.getApplicationContext(), deviceName + " 연결에 성공하였습니다.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            try{
                btSocket.close();
                Toast.makeText(activity.getApplicationContext(), deviceName + " 연결에 실패하였습니다.", Toast.LENGTH_LONG).show();
            }catch(IOException e2){
                e2.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void toArduino(String str, int count){
        for(int i = 0; i < count; i++){
            btThread.write(str);
        }
    }

    public Boolean isBluetoothConnect(){
        return isConnect & btAdapter.isEnabled();
    }

}
