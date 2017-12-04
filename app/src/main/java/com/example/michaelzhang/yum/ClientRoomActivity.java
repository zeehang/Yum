package com.example.michaelzhang.yum;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.nio.charset.StandardCharsets;

public class ClientRoomActivity extends AppCompatActivity {

    private static String mConnectToDeviceAddress;

    private BluetoothService mBtService;

    private BluetoothDevice mBtHost;

    private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                //TODO:state change here?
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    //construct a string
                    String writeMessage = new String(writeBuf);
                    Log.d("ClientRoomActivity", "wrote message" + writeMessage);
                    //TODO: do we need this?
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    //construct a string from valid bytes
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_room);
        Intent passedIntent = getIntent();
        Bundle extras = passedIntent.getExtras();
        mConnectToDeviceAddress = extras.getString("device_address");
        mBtService = new BluetoothService(this, mHandler);
        initializeConnection();
        String writeMessage = "Hello!";
        byte[] writeOut =  writeMessage.getBytes(StandardCharsets.UTF_8);
        mBtService.write(writeOut);
    }

    private void initializeConnection() {
        //we create a bluetooth device based on the address that we were given from the prev activity
        mBtHost = mBtAdapter.getRemoteDevice(mConnectToDeviceAddress);
        //then we connect!
        mBtService.connect(mBtHost, true);
    }

    public void sendTest(View view) {
        String writeMessage = "Hello!";
        byte[] writeOut =  writeMessage.getBytes(StandardCharsets.UTF_8);
        mBtService.write(writeOut);
    }
}
