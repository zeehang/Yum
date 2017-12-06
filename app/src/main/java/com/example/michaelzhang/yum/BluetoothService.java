package com.example.michaelzhang.yum;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Output;
import android.os.IBinder;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

//import com.example.michaelzhang.logger.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class BluetoothService implements Serializable{
    //Adapted from the BluetoothChat example code provided under the Android Developers site
    //Debugging
    private static final String TAG = "BluetoothService";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "YumSecure";
    private static final String NAME_INSECURE = "YumInsecure";

    // Generate some UUIDs!
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("31a51cba-5e51-4760-806b-b0d43e12fe58"); //TODO: SHould this be random? or sequenced for multiple threads?
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("abf93e00-8718-4497-a12e-f48e8f61d65d");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;
    private boolean isHost;

    // multiple connection fields
    private ArrayList<ConnectedThread> mConnectedThreads;
    private ArrayList<BluetoothSocket> mConnectedSockets;
    private ArrayList<String> mDeviceAddresses;
    private ArrayList<UUID> mUuids;

    // current connection state
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    // Constructor - prepares a new Bluetooth session
//
//    @param context The UI Activity Context
//    @param handler A Handler to send messages back to the UI Activity

    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;

        mConnectedThreads = new ArrayList<ConnectedThread>();
        mConnectedSockets = new ArrayList<BluetoothSocket>();
        mDeviceAddresses = new ArrayList<String>();
        mUuids = new ArrayList<UUID>();
        for (int i = 0; i < 7; i++) {
            mDeviceAddresses.add(null);
            mConnectedThreads.add(null);
            mConnectedSockets.add(null);
        }

        mUuids.add(UUID.fromString("a77dfc8c-d3b8-4c3b-86f8-490d37ab10ef"));
        mUuids.add(UUID.fromString("08e85975-d007-40e8-ad5a-29a6757cf8c7"));
        mUuids.add(UUID.fromString("1b11ae5c-30f7-4073-9d75-daa60f7e787b"));
        mUuids.add(UUID.fromString("6392f2a4-ce59-4596-af53-42b3103457e5"));
        mUuids.add(UUID.fromString("4c2a40f6-d08a-4a19-87df-c1e77e28becf"));
        mUuids.add(UUID.fromString("dfc61e65-d871-4ecd-b27f-cce9d366e525"));
        mUuids.add(UUID.fromString("763de4c4-5859-4ff9-be10-7e3b4f5d0bdb"));
    }

    //update the main activity of status!
    private synchronized void updateActivityState() {
        mState = getState();

        Log.d(TAG, "updateActivityState() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    public boolean isHost() {
        return isHost;
    }

    // return the current connection state
    public synchronized int getState() {
        return mState;
    }

    // start the service (this is the server end)
    public synchronized void start() {
        Log.d(TAG, "start");

        //cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        //cancel any currently connected thread
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }



        //start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        updateActivityState();

    }

    public synchronized void connect (BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        Log.d(TAG, "connecting to device " + device.getName());
        mConnectThread.start();
        //update activity
        updateActivityState();
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device,
                                       final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        //update activity state
        updateActivityState();
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        mState = STATE_NONE;

        updateActivityState();
    }

    // write to connected thread in an unsync manner
    public void write (byte[] out) {
        //create temporary object
        ConnectedThread r;

        for (int i = 0; i < 7; i++) {
            if(mConnectedThreads.get(i) != null) {
                Log.d("BluetoothService", "writing to uuid " + i);
                r = mConnectedThreads.get(i);
                r.write(out);
            }
        }
    }

    private void connectionFailed() {
        //send a failure message back
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST); // this is a toast
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect device, restarting service");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;

        //update activity status
        updateActivityState();

        BluetoothService.this.start();
    }

    private void connectionLost() {
        // send a filure message back
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST); // this is a toast
        Bundle bundle = new Bundle();
        bundle.putString("toast", "device connection was lost - restarting");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;

        //update UI
        updateActivityState();

        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    //this thread listens for incoming connections - behaves like a server side client

    private class AcceptThread extends Thread {
        // local server socket
        //private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread (boolean secure) {
//            BluetoothServerSocket tmp = null;
//            mSocketType = secure ? "Secure" : "Insecure";
//
//            //create a new listening server socket
//            try {
//                if (secure) {
//                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
//                } else {
//                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);
//                }
//            } catch (IOException e) {
//                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
//            }
//            mmServerSocket = tmp;
//            mState = STATE_LISTEN;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType + "BEGIN mAcceptTHread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            try {
                // we listen on all of the uuid sockets
                for (int i = 0; i < 7; i++) {
                    if(mConnectedThreads.get(i) == null) {
                        BluetoothServerSocket mmServerSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, mUuids.get(i));
                        Log.d(TAG, "opened server socketed for uuid " + mUuids.get(i));

                        BluetoothSocket mmSocket = mmServerSocket.accept();

                        try {
                            mmServerSocket.close();
                            Log.d(TAG, "Closed socket number " + i);
                        } catch (IOException e) {
                            Log.e(TAG, "Failed when trying to close serverSocket");
                        }

                        if (mmSocket != null) {
                            String address = mmSocket.getRemoteDevice().getAddress();

                            String tmpAddress = address;
                            BluetoothSocket tmpSocket = mmSocket;

                            mConnectedSockets.set(i, tmpSocket);
                            mDeviceAddresses.set(i, tmpAddress);

                            Log.d(TAG, "stored data in arrays index " + i);

                            ConnectedThread mmConnectedThread = new ConnectedThread(mmSocket, "Secure");
                            if(mmConnectedThread != null) {
                                Log.d(TAG, "not a nullConnectedTHread and should be added");
                            }
                            mConnectedThreads.set(i, mmConnectedThread);
                            mmConnectedThread.start();

                            Log.d(TAG, "started thread number " + i);
                        }
                    }
                }

            } catch (IOException e ) {
                Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);
        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
//            try {
//                mmServerSocket.close();
//            } catch (IOException e) {
//                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed ", e);
//            }
        }
    }

    // this thread runs while attempting to make an outgoing connection with a device
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket  = null;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            mState = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "Begin mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            //always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            int indexnumber = -1;

            mmSocket = null;

            //make a conncection to the BluetoothSocket
            for(int i = 0; i < 7 && mmSocket == null; i++) {
                indexnumber = i;
                //we try two times for each UUID
                for(int j = 0; j < 2 && mmSocket == null; j++) {
                    try {
                        mmSocket = mmDevice.createRfcommSocketToServiceRecord(mUuids.get(i));
                        mmSocket.connect();
                    } catch (IOException e) {
                        Log.e(TAG, "IOException in trying to connect to a socket with uuid ");
                        try {
                            mmSocket.close();
                            mmSocket = null;
                        } catch (IOException e2) {

                        }
                    }

                    if(mmSocket == null) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "interruptedexception in connect");
                        }
                    }
                }
            }
            if(mmSocket != null) {
                mConnectedSockets.set(indexnumber, mmSocket);
                Log.d(TAG,"client is connecting to socket of " + indexnumber);
                Log.d(TAG, "connected to device with uuid" + mUuids.get(indexnumber));
                ConnectedThread mmConnectedThread = new ConnectedThread(mmSocket, "Secure");
                mConnectedThreads.set(indexnumber, mmConnectedThread);
                String address = mmSocket.getRemoteDevice().getAddress();
                mDeviceAddresses.set(indexnumber, address);
                mmConnectedThread.start();
                mState = STATE_CONNECTED;
                updateActivityState();
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    // this thread runs during a connection with a remote ddevice - it handles all incoming and outgoing transmissions
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //Get the Bluetooth socket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedTHread");
            byte[] buffer = new byte[1024];
            int bytes;

            while (mState == STATE_CONNECTED) {
                try {
                    //read from the input stream
                    bytes = mmInStream.read(buffer);
                    Log.d("BluetoothService", "we are reading the client buffer");
                    //send obtained bytes to the UI activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget(); //Message_read = 2
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }

            }
        }

        public void write(byte [] buffer) {
            try {
                mmOutStream.write(buffer);
                Log.d(TAG, "wrote to " + mmSocket.getRemoteDevice().getName());
                //share the sent message back to the UI activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();// Message_sent =3

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
