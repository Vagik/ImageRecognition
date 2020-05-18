package com.client.imagerecognition;

import android.hardware.Camera;
import android.os.AsyncTask;

interface IStartCameraCallback
{
    void OnCameraInitialized(Camera camera);
}

public class CameraStartTask extends AsyncTask<Void, Void, Camera> {
    private  IStartCameraCallback callback;

    CameraStartTask(IStartCameraCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Camera doInBackground(Void... voids) {
        return Camera.open(0);
    }

    @Override
    protected void onPostExecute(Camera camera) {
        super.onPostExecute(camera);

        callback.OnCameraInitialized(camera);
    }
}
