package com.example.remote;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.List;

public class Tilt extends AppCompatActivity {

    private DataOutputStream _outStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilt);

        //init and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //set bluetooth selected
        bottomNavigationView.setSelectedItemId(R.id.tilt);

        //perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bluetooth) {
                startActivity(new Intent(getApplicationContext(), Bluetooth.class));
            } else if (id == R.id.controller) {
                startActivity(new Intent(getApplicationContext(), Controller.class));
            } else if (id == R.id.microphone) {
                startActivity(new Intent(getApplicationContext(), Microphone.class));
            } else if (id == R.id.tilt) {
                return false; //same page
            }

            overridePendingTransition(0, 0);
            return false;
        });

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


        TextView tilt_text_x = findViewById(R.id.tilt_text_x);
        TextView tilt_text_y = findViewById(R.id.tilt_text_y);
        TextView tilt_text_z = findViewById(R.id.tilt_text_z);
        TextView tilt_command = findViewById(R.id.tilt_command);

        SensorManager sm = (SensorManager) getSystemService(getApplicationContext().SENSOR_SERVICE);
        List<Sensor> list = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);


        SensorEventListener se = new SensorEventListener() {

            Character commandFront = ' ';
            Character commandBack = ' ';
            Character prevCommandFront = ' ';
            Character prevCommandBack = ' ';

            String commands_text;

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                commands_text = "";

                //if activity is not visible
                if (!Tilt.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                    return;

                //check if connected
                if (_outStream == null) return;

                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                double norm_of_g = Math.sqrt(x * x + y * y + z * z);
                x = (float) (x / norm_of_g);
                y = (float) (y / norm_of_g);
                z = (float) (z / norm_of_g);

                tilt_text_x.setText("X: " + String.format("%.3f", x));
                tilt_text_y.setText("Y: " + String.format("%.3f", y));
                tilt_text_z.setText("Z: " + String.format("%.3f", z));

                //front command
                if (x < -0.2) {
                    commandFront = 'R';
                    commands_text = commands_text.concat("Right");
                } else if (x > 0.2) {
                    commandFront = 'L';
                    commands_text = commands_text.concat("Left");
                } else if (x <= 0.2 && x >= -0.2) {
                    commandFront = 'S';
                    commands_text = commands_text.concat("Straight");
                }

                //back command
                if (y < -0.2) {
                    commandBack = 'F';
                    commands_text = commands_text.concat("Forward");
                } else if (y > 0.2) {
                    commandBack = 'B';
                    commands_text = commands_text.concat("Backward");
                } else if (y <= 0.2 && y >= -0.2){
                    commandBack = 'X';
                    commands_text = commands_text.concat("Stop");
                }

                try {

                    tilt_command.setText(commands_text);

                    //minimize data to motors by checking if new commands are not equal to previously sent
                    if (commandFront != prevCommandFront)
                        _outStream.writeChar(commandFront);

                    if (commandBack != prevCommandBack)
                        _outStream.writeChar(commandBack);

                    prevCommandBack = commandBack;
                    prevCommandFront = commandFront;
                } catch (Exception e) {
                    //log
                    Log.d("tag", e.toString());

                    //show dialog to inform we cant send data
                    showDialogBox(2);

                    //TODO: is this right?
                    //set output to null so we cant pass the check while pressing button
                    _outStream = null;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };


        sm.registerListener(se, list.get(0), SensorManager.SENSOR_DELAY_NORMAL);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(Tilt.this);
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    protected void onStop() {
        super.onStop();

        //try stopping the car, does nothing if not connected
        try {
            _outStream.writeChar('S');
            _outStream.writeChar('X');
        } catch (Exception e) {
        }
    }
}

