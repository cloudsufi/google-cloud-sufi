package com.google.adapter.util;

import com.google.adapter.exceptions.SystemException;
import com.google.adapter.model.MyTestException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sap.conn.jco.JCoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class TestDataUtil {

  private ArrayList<Exception> jcoExceptionList;


  public ArrayList<Exception> getJcoExceptionList() {
    jcoExceptionList = new ArrayList<>();
    Gson gson = new Gson();
    File file = new File("C:\\Users\\cloud2\\Desktop\\TestData\\ExceptionData.json");
    JsonReader reader = null;
    try {
      reader = new JsonReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      throw SystemException.throwException(e.getMessage(), e);
    }
    MyTestException data = gson.fromJson(reader, MyTestException.class);

    if (data != null && !data.getTestJcoException().isEmpty()) {
      for (int i = 0; i < data.getTestJcoException().size(); i++) {
        jcoExceptionList.add(new Exception(data.getMessage(),
            new JCoException(
                data.getTestJcoException().get(i).getGroupId(),
                data.getTestJcoException().get(i).getKey(),
                data.getTestJcoException().get(i).getMessage()
            )));
      }
    }
        /*System.out.println("MyCustomException :" + data.getJcoException().get(0).getGroupId()
                + " Key : " + data.getJcoException().get(0).getKey()
                + " Message : " + data.getJcoException().get(0).getMessage());*/

    return jcoExceptionList;
  }
}
