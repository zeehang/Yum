package com.example.michaelzhang.yum;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.example.michaelzhang.yum.Serializer.deserialize;
import static com.example.michaelzhang.yum.Serializer.serialize;
import static com.example.michaelzhang.yum.Serializer.deserializeArrayList;
import static com.example.michaelzhang.yum.Serializer.serializeArrayList;

public class HostConnectionActivity extends AppCompatActivity {

    private BluetoothService mBtService;

    private ListView mRoomUsersView;

    private ArrayAdapter<String> mRoomUsersArrayAdapter;

    private ArrayList<String> mCurrentConnectedUsers;

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
                    DataSendObject readMessage = null;
                    try {
                        readMessage = deserialize(readBuf);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(readMessage.getType() == Constants.MESSAGE_INITIAL_CLIENT_CONNECT ) {
                        //if it's a client connecting add it to the list
                        String addName = new String(readMessage.getData(), StandardCharsets.UTF_8);
                        mCurrentConnectedUsers.add(addName);
                        mRoomUsersArrayAdapter.clear();
                        mRoomUsersArrayAdapter.addAll(mCurrentConnectedUsers);

                        // and then send back the new listing to all the clients
                        sendUpdatedRoomList();
                    }
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

    /**
     * Sends the start message to the clients
     * @param savedInstanceState
     */

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

        // initializes button for the host to send command to go to the next activity
        Button continueButton = (Button) findViewById(R.id.button_continue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                goToYelpActivity();
            }
        });

        // add the host name to the user list and initialize
        Intent passedIntent = getIntent();
        Bundle extras = passedIntent.getExtras();
        mCurrentConnectedUsers = new ArrayList<String>();
        mCurrentConnectedUsers.add(extras.getString("friendly_name"));
        mRoomUsersArrayAdapter.addAll(mCurrentConnectedUsers);
        initializeConnection();
    }

    /**
     * Proceeds to the next activity by sending a start message to the clients
     */
    private void goToYelpActivity() {
        String writeMessage = "this is from the host";
        byte[] writeOut =  writeMessage.getBytes(StandardCharsets.UTF_8);
        mBtService.write(writeOut);
    }

    /**
     * Sends updated room list to the client
     */
    private void sendUpdatedRoomList() {
        // we need to serialize the arrayList first
        byte[] writeOut = null;
        try {
            writeOut = serializeArrayList(mCurrentConnectedUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataSendObject mObject = new DataSendObject(Constants.MESSAGE_LIST_ROOM_DEVICES, writeOut);
        byte toSend[] = null;
        try {
            toSend = serialize(mObject);
        } catch (IOException e) {
            e.printStackTrace(); // TODO: error recovery here
        }
        mBtService.write(toSend);
    }

    private void initializeConnection() {
        ensureDiscoverable();
        mBtService.start();
    }

    // we need to make the host discoverable
    // then open up a socket with the BTService
    // but to do that we need to create a handler (one for client and one for server?

}
