package com.example.remote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //we can set default launcher to bluetooth (in manifest), but I want to keep mainactivity JIC
        //move to bluetooth
        finish();
        startActivity(new Intent(getApplicationContext(), Bluetooth.class));

    }
}