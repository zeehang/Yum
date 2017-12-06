package com.example.michaelzhang.yum;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

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

    public static byte[] serializeArrayList(ArrayList<String> list) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        for (String element : list) {
            out.writeUTF(element);
        }
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    public static ArrayList<String> deserializeArrayList(byte[] data) throws IOException {
        ArrayList<String> generatedList = new ArrayList<String>();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        while (in.available() > 0) {
            String element = in.readUTF();
            generatedList.add(element);
        }
        return generatedList;
    }

}
