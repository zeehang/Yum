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
import java.util.ArrayList;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class BluetoothService {
    //Adapted from the BluetoothChat example code provided under the Android Developers site
    //Debugging
    private static final String TAG = "BluetoothService";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "YumSecure";
    private static final String NAME_INSECURE = "YumInsecure";

    // Generate some UUIDs!
    private static UUID MY_UUID;

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;

    /// member fields for multiple connections
    private ArrayList<String> mDeviceAddresses;
    private ArrayList<ConnectedThread> mConnThreads;
    private ArrayList<BluetoothSocket> mSockets;
    private ArrayList<UUID> mUuids;
    private ArrayList<String> mDeviceNames;

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

        mDeviceAddresses = new ArrayList<String>();
        mDeviceNames = new ArrayList<String>();
        mConnThreads = new ArrayList<ConnectedThread>();
        mSockets = new ArrayList<BluetoothSocket>();
        mUuids = new ArrayList<UUID>();

        for(int i = 0; i < 7; i++) {
            mDeviceAddresses.add(null);
            mConnThreads.add(null);
            mSockets.add(null);
            mUuids.add(null);
        }

        mUuids.add(UUID.fromString("a77dfc8c-d3b8-4c3b-86f8-490d37ab10ef"));
        mUuids.add(UUID.fromString("08e85975-d007-40e8-ad5a-29a6757cf8c7"));
        mUuids.add(UUID.fromString("1b11ae5c-30f7-4073-9d75-daa60f7e787b"));
        mUuids.add(UUID.fromString("6392f2a4-ce59-4596-af53-42b3103457e5"));
        mUuids.add(UUID.fromString("4c2a40f6-d08a-4a19-87df-c1e77e28becf"));
        mUuids.add(UUID.fromString("dfc61e65-d871-4ecd-b27f-cce9d366e525"));
        mUuids.add(UUID.fromString("763de4c4-5859-4ff9-be10-7e3b4f5d0bdb"));
    }

    public static UUID getMY_UUID() {
        return MY_UUID;
    }

    public static void setMY_UUID(UUID new_id) {
        MY_UUID = new_id;
    }


    public int getPositionIndexOfDevice(BluetoothDevice device) {
        for (int i = 0; i < mDeviceAddresses.size(); i++) {
            if (mDeviceAddresses.get(i) != null
                    && mDeviceAddresses.get(i).equalsIgnoreCase(
                    device.getAddress()))
                return i;
        }
        return -1;
    }

    public int getAvailablePositionIndexForNewConnection(BluetoothDevice device) {
        if (getPositionIndexOfDevice(device) == -1) {
            for (int i = 0; i < mDeviceAddresses.size(); i++) {
                if (mDeviceAddresses.get(i) == null) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean isDeviceConnectedAtPosition(int position) {
        if(mConnThreads.get(position) == null) {
            return false;
        }
        return true;
    }

    public ArrayList<String> getmDeviceNames() {
        return this.mDeviceNames;
    }

    public void setmDeviceNames(ArrayList<String> mDeviceNames) {
        this.mDeviceNames = mDeviceNames;
    }

    public ArrayList<String> getmDeviceAddresses() {
        return mDeviceAddresses;
    }

    public void setmDeviceAddresses(ArrayList<String> mDeviceAddresses) {
        this.mDeviceAddresses = mDeviceAddresses;
    }

    /**
     * updates the current state of the connection
     */
    private synchronized void setState (int state) {
        Log.d(TAG, "updateActivityState() " + mState + " -> " + state);
        mState = state;

        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

//    //update the main activity of status!
//    private synchronized void updateActivityState() {
//        mState = getState();
//
//        Log.d(TAG, "updateActivityState() " + mNewState + " -> " + mState);
//        mNewState = mState;
//
//        // Give the new state to the Handler so the UI Activity can update
//        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
//    }

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
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }

        setState(STATE_LISTEN);

    }

    public synchronized void connect (BluetoothDevice device, int position, boolean secure) {
        if (getPositionIndexOfDevice(device) == -1) {
            Log.d(TAG, "connect to: " + device);

            // Cancel any thread attempting to make a connection
            if (mState == STATE_CONNECTING) {
                if (mConnectThread != null) {
                    mConnectThread.cancel();
                    mConnectThread = null;
                }
            }

            // cancel any thread currently running a connection
            if(mConnThreads.get(position) != null) {
                mConnThreads.get(position).cancel();
                mConnThreads.set(position, null);
            }

            //start a new thread and try each of the UUIDS
            try {
                ConnectThread mConnectThread = new ConnectThread(device,
                        UUID.fromString("00001101-0000-1000-8000-"
                                + device.getAddress().replace(":", "")),
                        position, true);
                Log.i(TAG, "uuid-string at server side"
                        + ("00001101-0000-1000-8000-" + device.getAddress()
                        .replace(":", "")));
                mConnectThread.start();
                setState(STATE_CONNECTING);
            } catch (Exception e) {

            }
        } else {
            Log.e(TAG, "This device" + device.getName() + " already connected");
        }

    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, int position,
                                       final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

//        // Cancel the thread that completed the connection
//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }
//
//        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
//            mConnectedThread = null;
//        }
//
//        // Cancel the accept thread because we only want to connect to one device
//        if (mSecureAcceptThread != null) {
//            mSecureAcceptThread.cancel();
//            mSecureAcceptThread = null;
//        }
//        if (mInsecureAcceptThread != null) {
//            mInsecureAcceptThread.cancel();
//            mInsecureAcceptThread = null;
//        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        mConnThreads.set(position, mConnectedThread);

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        //update activity state
        setState(STATE_CONNECTED);
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

        for (int i = 0; i < 7; i++) {
            mDeviceNames.set(i, null);
            mDeviceAddresses.set(i, null);
            mSockets.set(i, null);
            if (mConnThreads.get(i) != null) {
                mConnThreads.get(i).cancel();
                mConnThreads.set(i, null);
            }
        }

        setState(STATE_NONE);
    }

    // write to connected thread in an unsync manner
    public void write (byte[] out) {
        //write to all of them!
        for(int i = 0; i < mConnThreads.size(); i++) {
            try {
                //create temporary object
                ConnectedThread r;
                //synchornize a copy of Connected Thread
                synchronized (this) {
                    if (mState != STATE_CONNECTED) return;
                    r = mConnectedThread;
                }
                //perform the write unsync
                r.write(out);
            } catch (Exception e) {

            }
        }
    }

    private void connectionFailed() {
        setState(STATE_LISTEN);
        //send a failure message back
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST); // this is a toast
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect device, restarting service");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

//        mState = STATE_NONE;
//
//        //update activity status
//        updateActivityState();
//
//        BluetoothService.this.start();
    }

    private void connectionLost(BluetoothDevice device) {
        int positionIndex = getPositionIndexOfDevice(device);
        if (positionIndex != -1) {
            mDeviceAddresses.set(positionIndex, null);
            mDeviceNames.set(positionIndex, null);
            mConnThreads.set(positionIndex, null);


            // send a filure message back
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST); // this is a toast
            Bundle bundle = new Bundle();
            bundle.putString("toast", "device connection was lost from " + device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    //this thread listens for incoming connections - behaves like a server side client

    private class AcceptThread extends Thread {
        // local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread (boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            //create a new listening server socket
            try {
                if (secure) {
                    if (mAdapter.isEnabled()) {
                        BluetoothService.setMY_UUID(UUID
                                .fromString("00001101-0000-1000-8000-"
                                        + mAdapter.getAddress().replace(":", "")));
                        Log.i(TAG, "MY_UUID.toString()=="
                                + BluetoothService.getMY_UUID().toString());
                    }

                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            BluetoothService.getMY_UUID());
                } else {
                    //tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
            //mState = STATE_LISTEN;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType + "BEGIN mAcceptTHread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // listen to the server socket if we're not connecte TODO: this needs to be updated because we want multiple conenctions
            while (mState != STATE_CONNECTED) {
                try {
                    //blocking call
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                //if a connection was accepted
                if(socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                //situation normal, start the connected thread
                                connected(socket, socket.getRemoteDevice(), getAvailablePositionIndexForNewConnection(socket.getRemoteDevice()), mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                //either not ready or already connected - terminate
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed ", e);
            }
        }
    }

    // this thread runs while attempting to make an outgoing connection with a device
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private UUID tempUuid;
        private int selectedPosition;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, UUID UuidToTry, int position, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            tempUuid = UuidToTry;
            selectedPosition = position;
            mSocketType = secure ? "Secure" : "Insecure";

            //get a bluetoothsocket for a connection with the given device
            try {
                if(secure) {
                    tmp = device.createRfcommSocketToServiceRecord(UuidToTry);
                } else {
                    //tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            //State = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "Begin mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            //always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            //make a conncection to the BluetoothSocket
            try {
                mmSocket.connect();
            } catch (IOException e) {
                // close that scoket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType + "socket during connecton failure", e2);
                }
                BluetoothService.this.start();
                return;
            }

            //reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            mDeviceAddresses.set(selectedPosition, mmDevice.getAddress());
            mDeviceNames.set(selectedPosition, mmDevice.getName());
            //start the connected thread
            connected(mmSocket, mmDevice, selectedPosition, "Secure");
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
            //mState = STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedTHread");
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    //read from the input stream
                    bytes = mmInStream.read(buffer);

                    //send obtained bytes to the UI activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget(); //Message_read = 2
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost(mmSocket.getRemoteDevice());
                    break;
                }

            }
        }

        public void write(byte [] buffer) {
            try {
                mmOutStream.write(buffer);

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
