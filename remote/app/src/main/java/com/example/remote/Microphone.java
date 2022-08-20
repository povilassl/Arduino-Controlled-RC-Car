package com.example.remote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.w3c.dom.Text;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Microphone extends AppCompatActivity {

    private TextView commandtext;
    private DataOutputStream _outStream;

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
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.bluetooth:
                        startActivity(new Intent(getApplicationContext(), Bluetooth.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.control:
                        startActivity(new Intent(getApplicationContext(), Control.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.microphone:
                        return true;
                }
                return false;
            }
        });

        commandtext = (TextView) findViewById(R.id.command_text);

        try {
            //get socket for sending data
            BluetoothSocket socket = ((MyApplication) this.getApplication()).getConnectedSocket();
            OutputStream tmpOut = socket.getOutputStream();
            _outStream = new DataOutputStream(tmpOut);

        } catch (IOException e) {
            Log.d("tag", e.toString());
            return;
        }


        checkPermissions();

        final TextView recordingText = findViewById(R.id.recording_text);
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
                    recordingText.setText("Input: " + matches.get(0));
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

        recordingText.addTextChangedListener(new TextWatcher() {
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

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        recordingText.setText("");
                        speechRecognizer.startListening(speechRecognizerIntent);
                        recordingText.setHint("Listening...");
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
        List<Character> commands = new ArrayList<>();

        //split input by into array of words (to iterate in order)
        List<String> inputList = new ArrayList<String>(Arrays.asList(input.split(" ")));

        for (String str : inputList) {
            if (str.contains("forward")) {
                commands.add('F');
            }

            //works with backward as well
            if (str.contains("back")) {
                commands.add('B');
            }

            if (str.contains("left")) {
                commands.add('L');
            }

            if (str.contains("straight")) {
                commands.add('S');
            }

            if (str.contains("right")) {
                commands.add('R');
            }

            if (str.contains("stop")) {
                commands.add('C');
            }
        }

        try {
            for (Character c : commands){
                _outStream.writeChar(c);
            }
        }catch (Exception e){
            Log.d("tag", e.toString());
            return;
        }

    }

}