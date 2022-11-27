package com.example.autolight_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    // 블루투스 변수
    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter btAdapter;

    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    ImageButton paired;
    ImageButton back;
    ImageButton search;
    ListView list;

    BluetoothSocket btSocket = null;
    boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        paired = findViewById(R.id.paired_device);
        back = findViewById(R.id.back_button);
        search = findViewById(R.id.search_device);
        list = findViewById(R.id.device_list);

        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        list.setAdapter(btArrayAdapter);

        bluetooth();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                if(btAdapter.isDiscovering()){
                    btAdapter.cancelDiscovery();}
                finish();
            }
        });

        paired.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view){
                pairedDevice();
            }
        }));

        search.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view){
                searchDevice();
            }
        }));

        list.setOnItemClickListener(new myOnItemClickListener());

    }

    public void bluetooth() {
        // checkSelfPermission을 해주어야 밑에 enableBtIntent 사용 가능
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) { // 블루투스 지원 X
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.\n앱 이용이 불가합니다.", Toast.LENGTH_LONG).show();
        } else { // 블루투스 지원 O
            if (!btAdapter.isEnabled()) { // 블루투스 활성화 안되어있을 때
                Toast.makeText(getApplicationContext(), "앱 이용을 위해\n블루투스 활성화가 필요합니다.", Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    public void pairedDevice() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();}
        btArrayAdapter.clear();
        if(deviceAddressArray!=null && !deviceAddressArray.isEmpty()){ deviceAddressArray.clear(); }
        pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "페어링된 기기가 없습니다.", Toast.LENGTH_LONG).show();
        }
    }

    public void searchDevice() { // 블루투스 기기 검색
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();}
        // 기기 검색 시작
        btAdapter.startDiscovery();
        btArrayAdapter.clear();
        Toast.makeText(getApplicationContext(), "기기 검색을 시작합니다.", Toast.LENGTH_LONG).show();
        if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
            deviceAddressArray.clear();
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    // 블루투스 기기 검색에서 필요한 부분, BroadCastReceiver 생성
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ContextCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(deviceName != null && deviceHardwareAddress !=null && !deviceAddressArray.contains(deviceHardwareAddress)) {
                    btArrayAdapter.add(deviceName);
                    deviceAddressArray.add(deviceHardwareAddress);
                    btArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onDestroy() { // 블루투스 기기 검색 에서 필요한 부분
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    public class myOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), btArrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();
            final String name = btArrayAdapter.getItem(position); // get name
            final String address = deviceAddressArray.get(position); // get address
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            // create & connect socket
            try {
                if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
                UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB".toString());
                btSocket = device.createRfcommSocketToServiceRecord(deviceUUID);
                Log.d("Where","1");
                btSocket.connect();
                Log.d("Where","2");
                ConnectedThread btThread = new ConnectedThread(btSocket);
                Log.d("Where","3");
                btThread.start();
                Log.d("Where","4");
                Toast.makeText(getApplicationContext(), name + " 연결에 성공하였습니다.", Toast.LENGTH_LONG).show();
                Log.d("Where","5");
                btThread.write("HELLO");
            } catch (IOException e) {
                try{
                    btSocket.close();
                    Toast.makeText(getApplicationContext(), name + " 연결에 실패하였습니다.", Toast.LENGTH_LONG).show();
                }catch(IOException e2){
                    e2.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }
    public class ConnectedThread extends Thread {
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

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}