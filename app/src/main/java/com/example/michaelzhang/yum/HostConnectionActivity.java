package com.example.michaelzhang.yum;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HostConnectionActivity extends AppCompatActivity {

    private BluetoothService mBtService;

    private ListView mRoomUsersView;

    private ArrayAdapter<String> mRoomUsersArrayAdapter;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    //buffer for outoing messages
    private StringBuffer mOutStringBuffer;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                //TODO:state change here?
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    //construct a string
                    String writeMessage = new String(writeBuf);
                    //TODO: do we need this?
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    //construct a string from valid bytes
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mRoomUsersArrayAdapter.add("added " + readMessage);
                    break;
            }
        }
    };

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_connection);

        //set up the views
        mRoomUsersView = (ListView) findViewById(R.id.user_list);

        mRoomUsersArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        //add the adapter
        mRoomUsersView.setAdapter(mRoomUsersArrayAdapter);

        //initialize the bluetooth service
        mBtService = new BluetoothService(this, mHandler);

        mOutStringBuffer = new StringBuffer("");

        initializeConnection();
    }

    private void initializeConnection() {
        ensureDiscoverable();
        mBtService.start();
    }

    // we need to make the host discoverable
    // then open up a socket with the BTService
    // but to do that we need to create a handler (one for client and one for server?

}
