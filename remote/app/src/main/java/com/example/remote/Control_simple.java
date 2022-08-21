package com.example.remote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class Control_simple extends AppCompatActivity implements View.OnClickListener {

    private DataOutputStream _outStream;
    private BluetoothSocket _socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        //init and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //set control selected
        bottomNavigationView.setSelectedItemId(R.id.control);

        //perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.bluetooth:
                        startActivity(new Intent(getApplicationContext(), Bluetooth.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.control:
                        return true;
                    case R.id.microphone:
                        startActivity(new Intent(getApplicationContext(), Microphone.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.controller_horizontal:
                        startActivity(new Intent(getApplicationContext(), Controller_horizontal.class));
                        overridePendingTransition(0, 0);
                        return true;

                }
                return false;
            }
        });


        //get socket and set output stream
        try {
            _socket = ((MyApplication) this.getApplication()).getConnectedSocket();
            OutputStream tmpOut = _socket.getOutputStream();
            _outStream = new DataOutputStream(tmpOut);

        } catch (Exception e) {
            //err
            Log.d("Tag", "Couldn't get socket or output stream");
            showDialogBox(1);
            return;
        }

        Button buttonForward = (Button) findViewById(R.id.button_forward);
        buttonForward.setOnClickListener(this); // calling onClick() method

        Button buttonBackward = (Button) findViewById(R.id.button_backward);
        buttonBackward.setOnClickListener(this); // calling onClick() method

        Button buttonLeft = (Button) findViewById(R.id.button_left);
        buttonLeft.setOnClickListener(this); // calling onClick() method

        Button buttonStraight = (Button) findViewById(R.id.button_straight);
        buttonStraight.setOnClickListener(this); // calling onClick() method

        Button buttonRight = (Button) findViewById(R.id.button_right);
        buttonRight.setOnClickListener(this); // calling onClick() method

    }

    @Override
    public void onClick(View v) {
        Character command;
        switch (v.getId()) {
            case R.id.button_forward:
                command = 'F';
                break;
            case R.id.button_backward:
                command = 'B';
                break;
            case R.id.button_left:
                command = 'L';
                break;
            case R.id.button_right:
                command = 'R';
                break;
            case R.id.button_straight:
                command = 'S';
                break;
            default:
                command = '0';
                break;
        }

        Log.d("Command", command.toString());

        try {
            _outStream.writeChar(command);
        }catch (Exception e){
            Log.d("Tag", "Couldn't write to socket");
            showDialogBox(1);
        }

    }

    //show custom dialog box based on choice of error provided
    private void showDialogBox(int choice) {
        String title;
        String message;

        switch (choice) {
            case 1:
                title = "Connection error";
                message = "Could not get socket or output stream";
                break;
            case 2:
                title = "Connection error";
                message = "Could not write to this device";
                break;
            default:
                title = "Error";
                message = "Error";
                break;
        }

        //show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Control_simple.this);
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