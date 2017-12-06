package com.example.michaelzhang.yum;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

    private static BluetoothService mBtService;

    private ListView mRoomUsersView;

    private static ArrayAdapter<String> mRoomUsersArrayAdapter;

    private static ArrayList<String> mCurrentConnectedUsers;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private static int doneClients = 0;

    static ArrayList<String> preferredStrings = new ArrayList<String>();
    static int[] preferredRestaurants = new int[100];

    //buffer for outoing messages
    private StringBuffer mOutStringBuffer;

    private static final Handler mHandler = new Handler() {
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
                    if(readMessage.getType() == Constants.MESSAGE_APPROVED_CHOICES) {
                        byte[] toTransform = readMessage.getData();
                        try {
                            preferredStrings = deserializeArrayList(toTransform);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        for(int i = 0; i < preferredStrings.size(); i++) {
                            preferredRestaurants[i] += Integer.parseInt(preferredStrings.get(i));
                        }
                        doneClients++;
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
                goToMainActivity();
            }
        });

        //initialize preferredRestraunts
        for (int i = 0; i < 100; i++) {
            preferredRestaurants[i] = 0;
        }

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
    private void goToMainActivity() {
        Intent intent  = new Intent(this, MainActivity.class);
        startActivityForResult(intent, Constants.MESSAGE_CONTINUE_TO_YELP);
    }

    /**
     * Sends updated room list to the client
     */
    private static void sendUpdatedRoomList() {
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
        if(toSend == null) {
            Log.d("hostconnectionactvity()", "trying to send null stuff from host!");
        }
        mBtService.write(toSend);
    }

    private void initializeConnection() {
        ensureDiscoverable();
        mBtService.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check the request
        if(requestCode == Constants.MESSAGE_CONTINUE_TO_YELP) {
            if(resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                String zipCode = extras.getString("location");

                DataSendObject locationSend = new DataSendObject(Constants.MESSAGE_CONTINUE_TO_YELP, zipCode.getBytes());
                byte[] toWrite = null;
                try {
                    toWrite = serialize(locationSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mBtService.write(toWrite);
                Intent intent = new Intent(this, YelpActivity.class);
                intent.putExtra("location", zipCode);
                intent.putExtra("id", "host");
                startActivityForResult(intent, Constants.MESSAGE_YELP_START_SWIPE_HOST);
            }
        }

        if(requestCode == Constants.MESSAGE_YELP_START_SWIPE_HOST) {
            ArrayList<Restaurant> restaurants = (ArrayList<Restaurant>) data.getSerializableExtra("restaurants");
            for(int i = 0; i < restaurants.size(); i++) {
                preferredRestaurants[i] += restaurants.get(i).getChosen();
            }
//            while(doneClients < mCurrentConnectedUsers.size() -1) {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            //TODO: check to see if all clients are done LOL
            if(doneClients == mCurrentConnectedUsers.size() - 1) {
                int max = 0;
                int index = 0;
                for(int i = 0; i < restaurants.size(); i++ ) {
                    if(preferredRestaurants[i] > max) {
                        max = preferredRestaurants[i];
                        index = i;
                    }
                }
                Log.d("host", Integer.toString(index));
                
                DataSendObject mObject = new DataSendObject(Constants.MESSAGE_FINAL_RESULT_JSON, (Integer.toString(index)).getBytes());
                byte[] toSend = null;
                try {
                    toSend  = serialize(mObject);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mBtService.write(toSend);
                Intent intent = new Intent(this, resultsActivity.class);
                intent.putExtra("restaurant", restaurants.get(index));
                startActivity(intent);
            }
        }
    }

    // we need to make the host discoverable
    // then open up a socket with the BTService
    // but to do that we need to create a handler (one for client and one for server?

}
