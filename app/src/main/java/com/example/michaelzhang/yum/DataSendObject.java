package com.example.michaelzhang.yum;

import java.io.Serializable;

/**
 * Created by michaelzhang on 12/4/17.
 */

public class DataSendObject implements Serializable{

    private static final long serialVersionUID = 123L;

    // This is the type of message that's being sent
    private int mType;
    private byte[] mData;

    public DataSendObject (int type, byte[] dataBuffer) {
        mType = type;
        mData = dataBuffer;
    }

    public int getType() {
        return mType;
    }

    public byte[] getData() {
        return mData;
    }

    // we need to have a serialize and deserialize so we can send over Bluetooth - use the Serializer class
}
