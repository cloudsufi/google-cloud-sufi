package com.google.adapter.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ReadSerializedFile {
    public ObjectInputStream readFile(String fileName){
        ObjectInputStream objectInputStream = null;
        try {
            FileInputStream fin = new FileInputStream(fileName);
            objectInputStream = new ObjectInputStream(fin);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return objectInputStream;
    }
}
