package com.example.michaelzhang.yum;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;

import static android.support.v4.app.ActivityCompat.finishAfterTransition;
import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class BluetoothService extends Service {

    private final static int REQUEST_ENABLE_BT = 1;
    private Handler btHandler;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//    if (mBluetoothAdapter) {
//      //this doesn't work for some reason check on this?
//    }

    public BluetoothService() {

    }

    public void checkBluetoothState() {
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
            finishAfterTransition();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
