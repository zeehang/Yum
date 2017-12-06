package com.example.michaelzhang.yum;

/**

 * Created by annikatsai on 12/4/17.
 */

public class Constants {
    public static final String YELP_TOKEN = BuildConfig.YELP_TOKEN;
    public static final String YELP_BASE_URL = "https://api.yelp.com/v3/businesses/search?term=restaurants";
    public static final String YELP_LOCATION_QUERY_PARAMETER = "location";

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

    // these define actions when the host and the client are communicating with each other
    public static final int MESSAGE_LIST_ROOM_DEVICES = 7;
    public static final int MESSAGE_CONTINUE_TO_YELP = 8; // sent by the host to the clients after the room activity
    public static final int MESSAGE_INITIAL_CLIENT_CONNECT = 9; // initial client connection - CONTAINS clients friendly name in String
    public static final int MESSAGE_ACK = 10; // acknowledgment that we've received things
    public static final int MESSAGE_YELP__START_JSON_ARRAY = 11; // the yelp json sent from the host to clients at the beginning
    public static final int MESSAGE_APPROVED_CHOICES_JSON_ARRAY = 12; // the yelp json that the client said yes to! sent back to the server
    public static final int MESSAGE_FINAL_RESULT_JSON = 13; // the final yelp JSON that the server sends back to the client
}
