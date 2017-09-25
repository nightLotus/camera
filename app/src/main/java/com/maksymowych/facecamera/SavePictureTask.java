package com.maksymowych.facecamera;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class SavePictureTask extends AsyncTask<Byte[], Void, Void> {

    private static final String LOG_TAG = "FaceCameraTask";

    private final MainActivity context;

    SavePictureTask(MainActivity context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Byte[][] params) {
        for (int i = 0; i < params.length; i++) {
            final byte[] bytes = new byte[params[i].length];
            for (int j = 0; j < bytes.length; j++) {
                bytes[j] = params[i][j];
            }

            final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(LOG_TAG, "Error creating media file, check storage permissions");
                return null;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bytes);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
            }
        }

        return null;
    }

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type) {
        final File mediaStorageDir = new File(context.getFilesDir(), "faceshots");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(LOG_TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        final String timeStamp = String.format(Locale.US, "%s_%d",
                new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US).format(new Date()),
                context.getRandomSalt());

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
