package com.example.michaelzhang.yum;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class SetupBluetoothActivity extends AppCompatActivity {

    final static int REQUEST_ENABLE_BT = 1;
    private Handler btHandler;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothService mBTService = null;
    private TextView nameView;

    private void checkBluetoothState() {
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
            //TODO: check if this succeeds or not and deal with it accordingly, might need to override with our own startActivityforResult https://developer.android.com/training/basics/intents/result.html#ReceiveResult
        }
    }

    public void startHost(View view) {
        Intent intent = new Intent(this, HostConnectionActivity.class);
        intent.putExtra("friendly_name", nameView.getText().toString());
        startActivity(intent);
    }

    public void startClient(View view) {
        Intent intent = new Intent(this, ClientConnectionActivity.class);
        startActivityForResult(intent, Constants.CLIENT_SEARCH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check the request
        if(requestCode == Constants.CLIENT_SEARCH) {
            if(resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                String MACtoConnect = extras.getString("device_address");
                Intent mIntentClient = new Intent(this, ClientRoomActivity.class);
                mIntentClient.putExtra("device_address", MACtoConnect);
                mIntentClient.putExtra("friendly_name", nameView.getText().toString());
                startActivity(mIntentClient);
            }
        }
    }

    private void checkLocPermissions() {
        int COARSE_LOC = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int FINE_LOC  = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listRequestedPermissions = new ArrayList<String>();

        if (COARSE_LOC != PackageManager.PERMISSION_GRANTED) {
            listRequestedPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (FINE_LOC != PackageManager.PERMISSION_GRANTED) {
            listRequestedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(!listRequestedPermissions.isEmpty()) {
            String[] strRequestPermission = listRequestedPermissions.toArray(new String[listRequestedPermissions.size()]);
            requestPermissions(strRequestPermission, 42); //42 = REQUEST_CODE_LOC
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_bluetooth);
        nameView = findViewById(R.id.text_name);
        checkBluetoothState();
        checkLocPermissions();
    }
}
