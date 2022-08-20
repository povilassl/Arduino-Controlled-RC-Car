package com.example.remote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.ConsoleHandler;

public class Bluetooth extends AppCompatActivity {

    TextView textView;
    ListView listView;
    List savedList;
    Set<BluetoothDevice> pairedDevices;
    Integer selectedDeviceIndex;
    private BluetoothSocket _socket;
    private boolean permissionGranted = false;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //init and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //set bluetooth selected
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
                    case R.id.microphone:
                        startActivity(new Intent(getApplicationContext(), Microphone.class));
                        overridePendingTransition(0, 0);
                        return true;

                }
                return false;
            }
        });

        //set text view
        textView = (TextView) findViewById(R.id.connected_device_text);

        //getting devices into listview
        listView = findViewById(R.id.devicesListView);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();


        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 1);
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

        updateText(false);

    }

    public void bluetoothConnect(View view) {

        //checking permissions
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 1);
            return;
        }

        //create temp list to get device by index
        List<BluetoothDevice> tmpList = new ArrayList<>(pairedDevices);
        BluetoothDevice device = tmpList.get(selectedDeviceIndex);
        String deviceName = tmpList.get(selectedDeviceIndex).toString();


        try {

            //if socket is already connected, disconnect, do it in 2 steps because otherwise it throws exception
            if (_socket != null) {
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

            //update view - true because device connected succesfully
            updateText(true);

        } catch (Exception e) {

            //show dialog with info
            showDialogBox(1);

            //update view - false - device failed to connect
            updateText(false);
        }
    }

    public void bluetoothDisconnect(View view) {

        //checking permissions
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 1);
            return;
        }

        String deviceName = savedList.get(selectedDeviceIndex).toString();

        try {
            //if not connected, do nothing
            if (_socket != null && _socket.isConnected()) {

                //closing and setting global to null
                _socket.close();
                ((MyApplication) this.getApplication()).setConnectedSocket(null);

                Log.d("Tag", "Successfully disconnected from device \"" + deviceName + "\"");
                updateText(false);

            }

        } catch (Exception e) {
            Log.d("Tag", "Error disconnecting from device \"" + deviceName + "\"");

            //show dialog with info
            showDialogBox(2);

        }
    }

    private void updateText(boolean flag) {

        String text = "Connected: ";

        //flag means there is a connected device
        if (flag) {
            String deviceName = savedList.get(selectedDeviceIndex).toString();
            text = text.concat(deviceName);
        } else {
            text = text.concat("none");
        }

        textView.setText(text);

    }

    //check if the permissions where granted, act accordingly
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //restart activity to get list
                    finish();
                    startActivity(new Intent(getApplicationContext(), Bluetooth.class));
                } else {
                    showDialogBox(3);
                }
                return;
        }
    }


    //show custom dialog box based on choice of error provided
    private void showDialogBox(int choice) {
        String title;
        String message;

        switch (choice) {
            case 1:
                title = "Connection error";
                message = "Could not connect to this device";
                break;
            case 2:
                title = "Connection error";
                message = "Could not disconnect from this device";
                break;
            case 3:
                title = "Permissions";
                message = "This Activity can not run without previously requested permissions. " +
                        "Please allow then in your settings";
                break;
            default:
                return;
        }

        //show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Bluetooth.this);
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
}