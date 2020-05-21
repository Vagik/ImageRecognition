package com.client.imagerecognition;


import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class ServerDetectionResult {
    public int class_id;
    public float score;
    public int top_left_x;
    public int top_left_y;
    public int bottom_right_x;
    public int bottom_right_y;
}

interface ISendImageCallback {
    void OnServerDetectionCompleted(String detectionResult);
}

public class SendImageTask extends AsyncTask<String, Void, String> {

    private String imageFilePath;
    private Socket socket;
    private ISendImageCallback sendImageCallback;

    SendImageTask(String imageFilePath, ISendImageCallback sendImageCallback) {
        this.imageFilePath = imageFilePath;
        this.sendImageCallback = sendImageCallback;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            socket = new Socket("192.168.0.103", 8000);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            FileInputStream inputStream = new FileInputStream(imageFilePath);
            int data;
            while ((data = inputStream.read()) > -1)
                outputStream.write(data);
            outputStream.write("finished".getBytes());
            inputStream.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String recognitionResult = br.readLine();
            socket.close();
            return recognitionResult;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String detected) {
        super.onPostExecute(detected);
        sendImageCallback.OnServerDetectionCompleted(detected);
    }
}
