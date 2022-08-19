package com.example.remote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.ConsoleHandler;

public class Bluetooth extends AppCompatActivity {

    ListView listView;
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    List savedList;
    Set<BluetoothDevice> pairedDevices;
    Integer selectedDeviceIndex;
    private BluetoothSocket _socket;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //init and assing variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //set dashboard selected
        bottomNavigationView.setSelectedItemId(R.id.bluetooth);

        //perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.bluetooth:
                        return true;
                    case R.id.control:
                        startActivity(new Intent(getApplicationContext(), Control.class));
                        overridePendingTransition(0, 0);
                        return true;

                }
                return false;
            }
        });


        //getting devices into listview
        listView = findViewById(R.id.devicesListView);
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Missing BLUETOOTH_CONNECT permission");
            return;
        }

        pairedDevices = bluetoothAdapter.getBondedDevices();
        savedList = new ArrayList();

        for (BluetoothDevice bt : pairedDevices) {
            savedList.add(bt.getName());
        }

        Adapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedList);
        listView.setAdapter((ListAdapter) adapter);


        //setting on click listener to get chosen device
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedDeviceIndex = i;
            }
        });
    }

    public void bluetoothConnect(View view) {

        //create temp list to get device by index
        List<BluetoothDevice> tmpList = new ArrayList<>(pairedDevices);
        BluetoothDevice device = tmpList.get(selectedDeviceIndex);
        String deviceName = tmpList.get(selectedDeviceIndex).toString();

        try {
            //checking permissions
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Tag", "permissions not granted");
                return;
            }

            //if socket is already connected, disconnect, do it in 2 steps because otherwise it throws exception
            if(_socket != null){
                if (_socket.isConnected()) {
                    Log.d("Tag", "Closing already connected socket...");
                    _socket.close();
                }
            }

            //initialize socket
            _socket = device.createRfcommSocketToServiceRecord(MY_UUID);

            //connect to socket
            _socket.connect();

            if (_socket != null && _socket.isConnected()) {
                //setting global connected socket
                ((MyApplication) this.getApplication()).setConnectedSocket(_socket);
                Log.d("Tag", "Successfully connected to device \"" + deviceName + "\"");
            }

        } catch (Exception e) {
            Log.d("Tag", "Error connecting to device \"" + deviceName + "\"");
        }

    }

    public void bluetoothDisconnect(View view) {

        String deviceName = savedList.get(selectedDeviceIndex).toString();

        try {
            //check if connected
            if (_socket != null && _socket.isConnected()) {

                //closing and setting global to null
                _socket.close();
                ((MyApplication) this.getApplication()).setConnectedSocket(null);

                Log.d("Tag", "Successfully disconnected from device \"" + deviceName + "\"");

            }

        } catch (Exception e) {
            Log.d("Tag", "Error disconnecting from device \"" + deviceName + "\"");
        }
    }
}