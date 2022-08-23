package com.example.remote;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

public class MyApplication extends Application {

    //were using global socket and device name for easier navigation
    private BluetoothSocket connectedSocket;
    private String connectedName;

    public BluetoothSocket getConnectedSocket() {
        return connectedSocket;
    }

    public void setConnectedSocket(BluetoothSocket connectedSocket) {
        this.connectedSocket = connectedSocket;
    }

    public String getConnectedName() {
        return connectedName;
    }

    public void setConnectedName(String connectedName) {
        this.connectedName = connectedName;
    }
}
