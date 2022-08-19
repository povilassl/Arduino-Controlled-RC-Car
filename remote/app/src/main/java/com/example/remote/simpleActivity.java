package com.example.remote;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class simpleActivity extends Control {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
    }
}