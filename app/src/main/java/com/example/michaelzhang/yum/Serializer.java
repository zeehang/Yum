package com.example.michaelzhang.yum;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by michaelzhang on 12/4/17.
 */

public class Serializer {

    public static byte[] serialize(DataSendObject obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public static DataSendObject deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return (DataSendObject) o.readObject();
            }
        }
    }

}
