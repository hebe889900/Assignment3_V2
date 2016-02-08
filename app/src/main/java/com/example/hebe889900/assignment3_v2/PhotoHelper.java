package com.example.hebe889900.assignment3_v2;

import android.content.Context;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by hebe889900 on 2015-02-15.
 */
public class PhotoHelper {

    static final String LOG_TAG = "Camera Direct Access";
    GPSTracker gps;
    public static String myAddress = "";


    public static int getDisplayOrientationForCamera(Context context, int cameraId){

        final int DEGREES_IN_CIRCLE = 360;
        int temp = 0;
        int previewOrientation = 0;

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId,cameraInfo);

        int deviceOrientation = getDeviceOrientationDegree(context);
        switch(cameraInfo.facing){
            case Camera.CameraInfo.CAMERA_FACING_BACK:
                temp = cameraInfo.orientation - deviceOrientation + DEGREES_IN_CIRCLE;
                previewOrientation = temp % DEGREES_IN_CIRCLE;
                break;
            case Camera.CameraInfo.CAMERA_FACING_FRONT:
                temp = (cameraInfo.orientation + deviceOrientation) % DEGREES_IN_CIRCLE;
                previewOrientation = (DEGREES_IN_CIRCLE - temp) % DEGREES_IN_CIRCLE;
                break;

        }
        return previewOrientation;

    }

    private static int getDeviceOrientationDegree(Context context) {

        int degrees = 0;
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch(rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        return degrees;

    }

    public static File generateTimeStampPhotoFile(){
        File photoFile = null;
        File outputDir = getPhotoDirectory();

        if(outputDir != null){
            String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss").format(new Date());
            String photoFileName = "IMG" + timeStamp  + ".jpg";
            photoFile = new File(outputDir, photoFileName);
        }
        return photoFile;

    }

    public static Uri generateTimeStampPhotoFileUri(){

        Uri photoFileUri = null;

        File photoFile = generateTimeStampPhotoFile();
        if(photoFile!= null){
            photoFileUri = Uri.fromFile(photoFile);
        }

        return photoFileUri;

    }

    public static File getPhotoDirectory() {
        File outputDir = null;
        File pictureDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        outputDir = new File(pictureDir,"MyThirdAssignment");


        if(!outputDir.exists()){
            if(!outputDir.mkdirs()){
                outputDir = null;
            }

        }

        return outputDir;
    }


}
