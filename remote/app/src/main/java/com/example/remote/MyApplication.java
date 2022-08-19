package com.example.remote;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

public class MyApplication extends Application {

    //were using global socket for easier navigation
    private BluetoothSocket connectedSocket;

    public BluetoothSocket getConnectedSocket() {
        return connectedSocket;
    }

    public void setConnectedSocket(BluetoothSocket connectedSocket) {
        this.connectedSocket = connectedSocket;
    }
}
