package com.client.imagerecognition;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    private ISendImageCallback sendImageCallback;

    SendImageTask(String imageFilePath, ISendImageCallback sendImageCallback) {
        this.imageFilePath = imageFilePath;
        this.sendImageCallback = sendImageCallback;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            Socket socket = new Socket("192.168.0.103", 8000);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] byteArray = stream.toByteArray();
            scaledBitmap.recycle();
            bitmap.recycle();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
            int data;
            while ((data = inputStream.read()) > -1)
                outputStream.write(data);
            outputStream.write("finished".getBytes());
            inputStream.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String recognitionResult = br.readLine();
            socket.close();
            br.close();
            return recognitionResult;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String detected) {
        super.onPostExecute(detected);
        sendImageCallback.OnServerDetectionCompleted(detected);
    }
}
