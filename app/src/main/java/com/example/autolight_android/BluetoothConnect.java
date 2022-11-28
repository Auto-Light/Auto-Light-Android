package com.example.autolight_android;

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
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnect {
    private final String TAG = MainActivity.class.getSimpleName();

    // 블루투스 변수
    private final static int REQUEST_ENABLE_BT = 1;
    private Context context;
    private Activity activity;
    BluetoothAdapter btAdapter;

    Set<BluetoothDevice> pairedDevices;

    String deviceName = "HC-06";
    String deviceAddress;
    private int searchCount=0;
    private int searchPair=0;

    BluetoothSocket btSocket = null;
    boolean flag;

    public BluetoothConnect(Activity _activity, Context _context){
        this.activity = _activity;
        this.context = _context;
    }

    public void start() {
        bluetooth();
    }

    private void bluetooth() {
        // checkSelfPermission을 해주어야 밑에 enableBtIntent 사용 가능
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
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
        searchCount = 1;
        btAdapter.startDiscovery();
        Toast.makeText(activity.getApplicationContext(), "기기 검색을 시작합니다.", Toast.LENGTH_LONG).show();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  //기기 검색 종료
        context.registerReceiver(receiver,filter); // context, activity
    }

    // 블루투스 기기 검색에서 필요한 부분, BroadCastReceiver 생성
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    if(device.getName().equals(deviceName)){
                        deviceAddress = device.getAddress();
                        bluetoothConnect();
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:    //블루투스 기기 검색 종료
                    Toast.makeText(activity.getApplicationContext(), "기기를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
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
        if(searchCount==1){
            context.unregisterReceiver(receiver);
            searchCount = 0;
        }
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
        // create & connect socket
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB".toString());
            btSocket = device.createRfcommSocketToServiceRecord(deviceUUID);
            btSocket.connect();
            ConnectedThread btThread = new ConnectedThread(btSocket);
            btThread.start();
            Toast.makeText(activity.getApplicationContext(), deviceName + " 연결에 성공하였습니다.", Toast.LENGTH_LONG).show();
            for(int i =0;i<20;i++) {
                btThread.write("1");
            }
            for(int i =0;i<20;i++) {
                btThread.write("0");
            }
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
    /*public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        buffer = new byte[1024];
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } */
}
