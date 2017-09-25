package com.maksymowych.facecamera;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;

import java.util.Random;

public class MainActivity extends Activity {

    private static final String LOG_TAG = "FaceCamera";

    private static final int CAMERA_PERMISSION_REQUEST = 1000;

    private boolean hasCameraPermission;
    private Camera frontCamera;

    private Handler handler;

    private long lastPictureTimestamp;
    private int numPicturesTaken;

    public long randomSalt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            }
        } else {
            hasCameraPermission = true;
        }

        handler = new Handler();

        final Random random = new Random();
        randomSalt = random.nextLong();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasCameraPermission && checkCameraHardware()) {
            frontCamera = getCameraInstance();

            if (frontCamera == null) {
                return;
            }

            final CameraPreview cameraPreview = new CameraPreview(this, frontCamera);
            frontCamera.setDisplayOrientation(90);

            final ConstraintLayout cameraPreviewLayout = (ConstraintLayout) findViewById(R.id.preview);
            cameraPreviewLayout.addView(cameraPreview);
        } else {
            frontCamera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (frontCamera != null) {
            frontCamera.stopPreview();
            frontCamera.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty
                hasCameraPermission = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            }
        }
    }

    /**
     * Check if this device has a front camera
     */
    private boolean checkCameraHardware() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            // this device has a front camera
            return true;
        } else {
            // no front camera on this device
            Log.e(LOG_TAG, "Front camera feature is not available on this device");
            return false;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        final int numCameras = Camera.getNumberOfCameras();
        Log.d(LOG_TAG, String.format("Found %d cameras", numCameras));

        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d(LOG_TAG, String.format("Camera %d is facing the front", i));
                try {
                    final Camera c = Camera.open(i); // attempt to get a Camera instance

                    if (c != null) {
                        Log.d(LOG_TAG, String.format("Success opening (front) camera %d", i));
                        return c;
                    }
                } catch (Exception e) {
                    // Camera is not available (in use or does not exist)
                    Log.d(LOG_TAG, String.format("Exception opening (front) camera %d", i));
                    e.printStackTrace();
                }
            }
        }

        // return null if we can't open a front facing camera
        Log.d(LOG_TAG, "Couldn't open any front camera");
        return null;
    }

    public void recognizeButtonClicked(View view) {
        Log.d(LOG_TAG, "recognizeButtonClicked");
        numPicturesTaken = 0;

        if (frontCamera != null) {
            handler.post(takePictureRunnable);
        }
    }

    private final Runnable takePictureRunnable = new Runnable() {
        @Override
        public void run() {
            lastPictureTimestamp = System.currentTimeMillis();
            frontCamera.takePicture(null, null, pictureCallback);
            Log.d(LOG_TAG, String.format("Took picture %d", numPicturesTaken));
        }
    };

    private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            frontCamera.startPreview();
            if (++numPicturesTaken < 10) {
                final long delay = 500 - (System.currentTimeMillis() - lastPictureTimestamp);
                handler.postDelayed(takePictureRunnable, Math.max(0, Math.max(0, delay)));
            }

            final Byte[] bytes = new Byte[data.length];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = data[i];
            }
            new SavePictureTask(MainActivity.this).execute(bytes);
        }
    };

    public long getRandomSalt() {
        return randomSalt;
    }
}
