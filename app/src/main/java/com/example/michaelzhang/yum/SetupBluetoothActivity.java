package com.example.michaelzhang.yum;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class SetupBluetoothActivity extends AppCompatActivity {

    final static int REQUEST_ENABLE_BT = 1;
    private Handler btHandler;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothService mBTService = null;

    public void checkBluetoothState() {
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
            //TODO: check if this succeeds or not and deal with it accordingly, might need to override with our own startActivityforResult https://developer.android.com/training/basics/intents/result.html#ReceiveResult
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_bluetooth);
        checkBluetoothState();

    }
}
