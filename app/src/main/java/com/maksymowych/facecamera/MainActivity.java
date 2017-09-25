package com.maksymowych.facecamera;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private static final String LOG_TAG = "FaceCamera";

    private static final int CAMERA_PERMISSION_REQUEST = 1000;

    private boolean hasCameraPermission;
    private Camera frontCamera;

    private CameraPreview cameraPreview;

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasCameraPermission && checkCameraHardware()) {
            frontCamera = getCameraInstance();

            if (frontCamera == null) {
                return;
            }

            cameraPreview = new CameraPreview(this, frontCamera);
            frontCamera.setDisplayOrientation(90);

            final FrameLayout cameraPreviewLayout = (FrameLayout) findViewById(R.id.preview);
            cameraPreviewLayout.addView(cameraPreview);
        } else {
            frontCamera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (frontCamera != null) {
            frontCamera.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    hasCameraPermission = true;
                } else {
                    // permission denied, boo!
                    hasCameraPermission = false;
                }
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

        if (frontCamera != null) {
            frontCamera.takePicture(null, null, pictureCallback);
        }
    }

    private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(LOG_TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FaceCamera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
