package com.client.imagerecognition;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SendImageTask extends AsyncTask<String, Void, String> {

    private String sendImagePath;
    private Socket socket;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder alertDialog;

    public SendImageTask(String sendImagePath, ProgressDialog progressDialog, AlertDialog.Builder alertDialog) {
        this.sendImagePath = sendImagePath;
        this.progressDialog = progressDialog;
        this.alertDialog = alertDialog;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            socket = new Socket("192.168.0.102", 8000);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            FileInputStream inputStream = new FileInputStream(sendImagePath);
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
        progressDialog.dismiss();
        alertDialog
                .setTitle("Recognition complete")
                .setMessage("A " + detected + " was founded on the image")
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
