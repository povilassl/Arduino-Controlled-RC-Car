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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class Controller extends AppCompatActivity implements View.OnTouchListener {

    private DataOutputStream _outStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller);

        //init and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //set microphone selected
        bottomNavigationView.setSelectedItemId(R.id.controller);

        //perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.bluetooth:
                        startActivity(new Intent(getApplicationContext(), Bluetooth.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.microphone:
                        startActivity(new Intent(getApplicationContext(), Microphone.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.controller:
                        return true;
                    case R.id.tilt:
                        startActivity(new Intent(getApplicationContext(), Tilt.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        Button buttonUp = findViewById(R.id.controller_button_up);
        Button buttonDown = findViewById(R.id.controller_button_down);
        Button buttonLeft = findViewById(R.id.controller_button_left);
        Button buttonRight = findViewById(R.id.controller_button_right);

        buttonUp.setOnTouchListener((View.OnTouchListener) this);
        buttonDown.setOnTouchListener((View.OnTouchListener) this);
        buttonLeft.setOnTouchListener((View.OnTouchListener) this);
        buttonRight.setOnTouchListener((View.OnTouchListener) this);


        try {
            //get socket for sending data
            BluetoothSocket socket = ((MyApplication) this.getApplication()).getConnectedSocket();
            OutputStream tmpOut = socket.getOutputStream();
            _outStream = new DataOutputStream(tmpOut);

        } catch (Exception e) {
            Log.d("tag", e.toString());
            showDialogBox(1);
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
                message = "Could not connect to device";
                break;
            case 2:
                title = "Connection error";
                message = "Could not send data to device";
                break;
            default:
                title = "Error";
                message = "Error";
                break;
        }

        //show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Controller.this);
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

    public boolean onTouch(View view, MotionEvent motionEvent) {

        //check if connected
        if(_outStream == null) return false;

        Character command = '0'; // null command

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            //when button pressed down choose specific command
            switch (view.getId()) {
                case R.id.controller_button_up:
                    command = 'F';
                    break;
                case R.id.controller_button_down:
                    command = 'B';
                    break;
                case R.id.controller_button_left:
                    command = 'L';
                    break;
                case R.id.controller_button_right:
                    command = 'R';
                    break;
                default:
                    break;
            }

        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            //when button lifted up, set horizontal or vertical movement to default
            switch (view.getId()) {
                case R.id.controller_button_up:
                case R.id.controller_button_down:
                    command = 'X';  //stop back
                    break;
                case R.id.controller_button_left:
                case R.id.controller_button_right:
                    command = 'S'; // set front straight
                    break;
                default:
                    break;
            }
        }

//        Log.d("tag", command.toString());
        try {
            _outStream.writeChar(command);
        }catch (Exception e){
            //log
            Log.d("tag", e.toString());

            //show dialog to inform we cant send data
            showDialogBox(2);

            //TODO: is this right?
            //set output to null so we cant pass the check while pressing button
            _outStream = null;
        }

        return false;
    }
}
