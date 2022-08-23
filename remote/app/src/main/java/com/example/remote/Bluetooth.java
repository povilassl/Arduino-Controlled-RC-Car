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
                return false; //same page
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

        //BT manager and adapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        //check permissions
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT
                }, 1);
            }
            return;
        }

        //get paired devices and put them into listview
        pairedDevices = bluetoothAdapter.getBondedDevices();
        savedList = new ArrayList<>();

        for (BluetoothDevice bt : pairedDevices) {
            savedList.add(bt.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedList);
        listView.setAdapter(adapter);

        //setting on click listener to get chosen device
        listView.setOnItemClickListener((adapterView, view, i, l) -> selectedDeviceIndex = i);

        updateText();

        //click listeners on connect and disconnect buttons
        findViewById(R.id.bluetooth_connect).setOnClickListener(this::bluetoothConnect);
        findViewById(R.id.bluetooth_disconnect).setOnClickListener(this::bluetoothDisconnect);

    }


    public void bluetoothConnect(View view) {

        //no device chosen
        if (selectedDeviceIndex == null) return;

        //create temp list to get device by index
        List<BluetoothDevice> tmpList = new ArrayList<>(pairedDevices);
        BluetoothDevice device = tmpList.get(selectedDeviceIndex);

        //get socket
        BluetoothSocket socket = ((MyApplication) this.getApplication()).getConnectedSocket();

        try {
            //if socket is already connected, disconnect
            if (socket != null) {
                if (socket.isConnected()) {
                    Log.d("Tag", "Closing already connected socket...");

                    socket.close();
                    ((MyApplication) this.getApplication()).setConnectedSocket(null);
                }
            }

            //need for device.getName();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                showDialogBox(3);
                return;
            }

            Log.d("Tag", "device name: " + device.getName());

            //starting new thread to not stop main
            BluetoothConnectThread thread = new BluetoothConnectThread(socket, device, device.getName());
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void bluetoothDisconnect(View view) {

        String name = ((MyApplication) this.getApplication()).getConnectedName();
        BluetoothSocket socket = ((MyApplication) this.getApplication()).getConnectedSocket();

        //if no device connected
        if (name == null || socket == null || !socket.isConnected()) {
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

        try {

            //closing and setting global to null
            socket.close();

            ((MyApplication) this.getApplication()).setConnectedSocket(null);
            ((MyApplication) this.getApplication()).setConnectedName(null);

            Log.d("Tag", "Successfully disconnected from device \"" + name + "\"");


        } catch (Exception e) {
            Log.d("Tag", "Error disconnecting from device \"" + name + "\"");
            Log.d("Tag", "Print error: " + e);

            //show dialog with info
            showDialogBox(2);
        }

        updateText();
    }

    private void updateText() {

        //get and update connected device name
        String name = ((MyApplication) this.getApplication()).getConnectedName();
        String text = "Connected: ";

        //name == null check must be first, otherwise (when null) throws error on name.isEmpty()
        if (name == null) {
            text = text.concat("none");
        } else {
            text = text.concat(name);
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
                message = "Device not connected";
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
                ((MyApplication) Bluetooth.this.getApplication()).setConnectedName(name);

            } catch (Exception e) {
                Log.d("Tag", "Error connecting to device");
                Log.d("Tag", "Error: " + e);

                //set global socket to null
                ((MyApplication) Bluetooth.this.getApplication()).setConnectedSocket(null);
                ((MyApplication) Bluetooth.this.getApplication()).setConnectedName(null);
            }

            runOnUiThread(() -> {
                //update text depending of the outcome
                if (socket == null || !socket.isConnected()) {
                    //show dialog with info
                    showDialogBox(1);
                }

                updateText();

                //set window back to interactive
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                //gif stops spinning
                findViewById(R.id.loading_gif).setVisibility(View.GONE);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //update text when we open this activity
//        updateText();
    }
}