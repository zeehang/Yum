package com.example.michaelzhang.yum;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class ClientConnectionActivity extends AppCompatActivity {

    //tag for log
    private static final String TAG = "ClientConnectActivity";

    //this is for the intent!
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBtAdapter;

    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private ProgressBar pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_connection);
        setTitle("Choose a host");

        //set result CANCELED if user backs out
        setResult(Activity.RESULT_CANCELED);

        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(View.GONE);

        Log.d(TAG, "we've created the viewws!");

        Button refreshButton = (Button) findViewById(R.id.button_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                doDiscovery();
            }
        });

        //array adapters for previously paired and newly discovered devices
        ArrayAdapter<String> pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter  = new ArrayAdapter<String>(this, R.layout.device_name);

        //find and set up ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        //find and set up the listview for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        //register for braodcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        //register for broadcasts when discover has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        //get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        //get a list of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        //if there are paired - add to the array adapter
        if (pairedDevices.size() > 0) {
            //TODO:some visibility stuff here with titles
            for(BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }

        doDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery() {
        Log.d(TAG, "doDiscovery() has been called");

        // some UI stuff
        pb.setVisibility(View.VISIBLE);
        if(mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        //clear the oldlist
        mNewDevicesArrayAdapter.clear();

        mBtAdapter.startDiscovery();
    }

    /**
     * Onclick listener for the devices in the listview TODO:we need to add a UI thing designating previous devices
     */
    private AdapterView.OnItemClickListener mDeviceClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mBtAdapter.cancelDiscovery();

                    String info = ((TextView) view).getText().toString();
                    String address = info.substring(info.length() - 17);

                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                    //set result and finish this activity TODO: this should launch a diff act
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            };
    /**
     * The broadcast reciever for discovered devices TODO: clear this when we hit the refresh button
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "entered the broadcast reciever");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.e(TAG, "found a device!");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //if its already paired, skip it b/c its been listed
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                //when disc is finished
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                pb.setVisibility(View.GONE);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                    CharSequence text = "no devices found";
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    };
}

