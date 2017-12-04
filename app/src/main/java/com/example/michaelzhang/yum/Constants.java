package com.example.michaelzhang.yum;

/**
 * Created by michaelzhang on 12/3/17.
 */

public interface Constants {
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // startactivity for result
    public static final int CLIENT_SEARCH = 6;
}
