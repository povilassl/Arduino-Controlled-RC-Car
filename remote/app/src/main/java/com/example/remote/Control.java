package com.example.remote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Control extends AppCompatActivity implements View.OnClickListener {

    private DataOutputStream _outStream;
    private BluetoothSocket _socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        //init and assing variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //set dashboard selected
        bottomNavigationView.setSelectedItemId(R.id.control);

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
                        startActivity(new Intent(getApplicationContext(), Bluetooth.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.control:

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
            Log.d("Tag", "Couldnt get socket or output stream");
//            return;
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
        }

    }

}