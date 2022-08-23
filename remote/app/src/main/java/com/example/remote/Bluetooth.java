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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {

    TextView textView;
    ListView listView;
    List<String> savedList;
    Set<BluetoothDevice> pairedDevices;
    Integer selectedDeviceIndex;
    private BluetoothSocket _socket;
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
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.bluetooth) {
                return false;
            } else if (id == R.id.controller) {
                startActivity(new Intent(getApplicationContext(), Controller.class));
            } else if (id == R.id.microphone) {
                startActivity(new Intent(getApplicationContext(), Microphone.class));
            } else if (id == R.id.tilt) {
                startActivity(new Intent(getApplicationContext(), Tilt.class));
            }

            overridePendingTransition(0, 0);
            return false;
        });

        //set text view
        textView = (TextView) findViewById(R.id.connected_device_text);

        //getting devices into listview
        listView = findViewById(R.id.devicesListView);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();


        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT
                }, 1);
            }
            return;
        }

        pairedDevices = bluetoothAdapter.getBondedDevices();
        savedList = new ArrayList<>();

        for (BluetoothDevice bt : pairedDevices) {
            savedList.add(bt.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedList);
        listView.setAdapter(adapter);


        //setting on click listener to get chosen device
        listView.setOnItemClickListener((adapterView, view, i, l) -> selectedDeviceIndex = i);

        updateText(false);

        Button btConnect = (Button) findViewById(R.id.bluetooth_connect);
        Button btDisconnect = (Button) findViewById(R.id.bluetooth_disconnect);

        btConnect.setOnClickListener(this::bluetoothConnect);
        btDisconnect.setOnClickListener(this::bluetoothDisconnect);

    }


    public void bluetoothConnect(View view) {

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

            //starting new thread to not stop main
            BluetoothConnectThread thread = new BluetoothConnectThread(_socket, device, deviceName);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void bluetoothDisconnect(View view) {


        //if no device connected
        if (_socket == null || !_socket.isConnected()) {
            showDialogBox(4);
            return;
        }

        //checking permissions
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT
                }, 1);
            }
            return;
        }

        String deviceName = savedList.get(selectedDeviceIndex);

        try {

            //closing and setting global to null
            _socket.close();
            ((MyApplication) this.getApplication()).setConnectedSocket(null);

            Log.d("Tag", "Successfully disconnected from device \"" + deviceName + "\"");
            updateText(false);


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
            String deviceName = savedList.get(selectedDeviceIndex);
            text = text.concat(deviceName);
        } else {
            text = text.concat("none");
        }

        textView.setText(text);

    }

    //check if the permissions where granted, act accordingly
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //restart activity to get list
                finish();
                startActivity(new Intent(getApplicationContext(), Bluetooth.class));
            } else {
                showDialogBox(3);
            }
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
            case 4:
                title = "Connection";
                message = "No device currently connected";
                break;
            default:
                title = "Error";
                message = "Error";
                break;
        }

        //show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Bluetooth.this);
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    class BluetoothConnectThread extends Thread {
        BluetoothSocket socket;
        BluetoothDevice device;
        String name;

        BluetoothConnectThread(BluetoothSocket socket, BluetoothDevice device, String name) {
            this.socket = socket;
            this.device = device;
            this.name = name;
        }

        @Override
        public void run() {
            try {

                //checking permissions
                if (!(ContextCompat.checkSelfPermission(Bluetooth.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(Bluetooth.this, new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT
                        }, 1);
                    }
                    return;
                }

                //initialize socket
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);

                //stop user interactions while connecting -  on UI thread
                runOnUiThread(() -> {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    //gif starts spinning
                    findViewById(R.id.loading_gif).setVisibility(View.VISIBLE);
                });

                //connect to socket
                socket.connect();

                //set global socket
                ((MyApplication) Bluetooth.this.getApplication()).setConnectedSocket(socket);
                _socket = socket;

            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                //update text depending of the outcome
                if (socket != null && socket.isConnected()) {
                    updateText(true);
                } else {
                    updateText(false);

                    //show dialog with info
                    showDialogBox(1);
                }

                //set window back to interactive
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                //gif stops spinning
                findViewById(R.id.loading_gif).setVisibility(View.GONE);
            });
        }
    }
}