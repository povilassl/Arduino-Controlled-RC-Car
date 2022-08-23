package com.example.remote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Microphone extends AppCompatActivity {

    private DataOutputStream _outStream;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_microphone);

        //init and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //set microphone selected
        bottomNavigationView.setSelectedItemId(R.id.microphone);

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
                        return true;
                    case R.id.controller:
                        startActivity(new Intent(getApplicationContext(), Controller.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.tilt:
                        startActivity(new Intent(getApplicationContext(), Tilt.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
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


        checkPermissions();

        textView = findViewById(R.id.text);
        final SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {
            }

            @Override
            public void onResults(Bundle bundle) {
                //getting matches
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //display first match
                if (matches != null) {
                    textView.setText("Input: " + matches.get(0));
                    sendCommand(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        findViewById(R.id.button_recording).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                checkPermissions();

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        textView.setText("");
                        speechRecognizer.startListening(speechRecognizerIntent);
                        textView.setHint("Listening...");
                        break;
                }

                return false;
            }
        });

    }

    //checks permissions and asks for access if not granted
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.RECORD_AUDIO
                }, 1);
            }
        }
    }

    private void sendCommand(String input) {

        //for storing commands to send
        List<Character> commandsSend = new ArrayList<>();

        //for textView update
        List<String> commandsText = new ArrayList<>();

        //split input by into array of words (to iterate in order)
        List<String> inputList = new ArrayList<String>(Arrays.asList(input.split(" ")));

        for (String str : inputList) {
            if (str.contains("forward") || str.equals("go")) {
                commandsSend.add('F');
                commandsText.add("Forward");
            }

            //works with backward as well
            if (str.contains("back")) {
                commandsSend.add('B');
                commandsText.add("Backward");
            }

            if (str.contains("left")) {
                commandsSend.add('L');
                commandsText.add("Left");
            }

            if (str.contains("straight")) {
                commandsSend.add('S');
                commandsText.add("Straight");

            }

            if (str.contains("right")) {
                commandsSend.add('R');
                commandsText.add("Right");
            }

            if (str.contains("stop")) {
                commandsSend.add('C');
                commandsText.add("Stop");
            }
        }

        String text = "";

        if(commandsText.isEmpty()){
            text = "none";
        }else {
            for (String str : commandsText){
                text = text.concat(str + " ");

            }
        }

        textView.setText("Commands: " + text);

        try {
            for (Character c : commandsSend){
                _outStream.writeChar(c);
            }
        }catch (Exception e){
            Log.d("tag", e.toString());
            showDialogBox(2);
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
            case 3:
                title = "Permissions";
                message = "This Activity can not run without previously requested permissions. " +
                        "Please allow then in your settings";
                break;
            default:
                title = "Error";
                message = "Error";
                break;
        }

        //show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Microphone.this);
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
                    startActivity(new Intent(getApplicationContext(), Microphone.class));
                } else {
                    showDialogBox(3);
                }
                return;
        }
    }

}