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
import android.widget.Toast;
import com.example.michaelzhang.yum.Serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.example.michaelzhang.yum.Serializer.serialize;

public class ClientRoomActivity extends AppCompatActivity {

    private static String mConnectToDeviceAddress;

    private String mFriendlyName;

    private BluetoothService mBtService;

    private BluetoothDevice mBtHost;

    private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch(msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            // when connected, send our name to the host
                            sendName();
                            break;
                    }
                    break;
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
                    CharSequence text = readMessage.toString();
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
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

        //we want to get the name from the intent as well
        mFriendlyName = extras.getString("friendly_name");

        initializeConnection();
    }

    private void initializeConnection() {
        //we create a bluetooth device based on the address that we were given from the prev activity
        mBtHost = mBtAdapter.getRemoteDevice(mConnectToDeviceAddress);
        //then we connect!
        mBtService.connect(mBtHost, true);
    }

    public void sendName() {
        byte[] writeOut =  mFriendlyName.getBytes(StandardCharsets.UTF_8);
        DataSendObject mObject = new DataSendObject(Constants.MESSAGE_INITIAL_CLIENT_CONNECT, writeOut);
        byte toSend[] = null;
        try {
            toSend = serialize(mObject);
        } catch (IOException e) {
            e.printStackTrace(); // TODO: error recovery here
        }
        mBtService.write(toSend);
    }
}
