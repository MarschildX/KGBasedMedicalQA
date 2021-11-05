package com.example.healworld.utils;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

public class HttpConnection {
    public String qa_communicate(String mac, String question) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("mac", mac);
            obj.put("question", question);

            // build url resource
            URL url = new URL("http://106.15.90.138:5320/question");
            // build http connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // set allow output or not
            conn.setDoOutput(true);
            // set allow input or not
            conn.setDoInput(true);
            // set cache
            conn.setUseCaches(false);
            // set method
            conn.setRequestMethod("POST");
            // keep alive
            conn.setRequestProperty("Connection", "Keep-Alive");
            // set char set:
            conn.setRequestProperty("Charset", "UTF-8");
            // convert to byte array
            byte[] data = (obj.toString()).getBytes();
            // set the length of data
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            // set data type:
            conn.setRequestProperty("contentType", "application/json");

            // connect
            conn.connect();
            OutputStream out = conn.getOutputStream();
            // write the data to server
            out.write((obj.toString()).getBytes());
            out.flush();
            out.close();

            // response status
            if (conn.getResponseCode() == 200) {
                try {
                    // get the return data
                    InputStream in = conn.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while((len = in.read(buffer)) != -1){
                        baos.write(buffer, 0, len);
                    }
                    // convert to string
                    String jsonRecvData = baos.toString();
                    baos.close();
                    in.close();
                    return jsonRecvData;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                return "响应码错误，不是200";
            }
        } catch (Exception e) {
            // do nothing
        }
        return "在拿到响应之前出错";
    }

    public boolean feedback_communicate(String mac, String question, String feedback, String context){
        try {
            JSONObject obj = new JSONObject();
            obj.put("mac", mac);
            obj.put("question", question);
            obj.put("feedback", feedback);
            obj.put("context", context);

            // build url resource
            URL url = new URL("http://106.15.90.138:5320/feedback");
            // build http connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // set allow output or not
            conn.setDoOutput(true);
            // set allow input or not
            conn.setDoInput(true);
            // set cache
            conn.setUseCaches(false);
            // set method
            conn.setRequestMethod("POST");
            // keep alive
            conn.setRequestProperty("Connection", "Keep-Alive");
            // set char set:
            conn.setRequestProperty("Charset", "UTF-8");
            // convert to byte array
            byte[] data = (obj.toString()).getBytes();
            // set the length of data
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            // set data type:
            conn.setRequestProperty("contentType", "application/json");

            // connect
            conn.connect();
            OutputStream out = conn.getOutputStream();
            // write the data to server
            out.write((obj.toString()).getBytes());
            out.flush();
            out.close();

            // response status
            if (conn.getResponseCode() == 200) {
                try {
                    // get the return data
                    InputStream in = conn.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while((len = in.read(buffer)) != -1){
                        baos.write(buffer, 0, len);
                    }
                    // convert to string
                    String jsonRecvData = baos.toString();
                    baos.close();
                    in.close();
                    JSONObject jsonObject = new JSONObject(jsonRecvData);
                    if(jsonObject.getString("feedback").equals("got it")){
                        return true;
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
