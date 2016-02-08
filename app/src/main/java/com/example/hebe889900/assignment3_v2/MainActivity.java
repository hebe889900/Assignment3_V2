package com.example.hebe889900.assignment3_v2;



        import java.io.BufferedOutputStream;
        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.OutputStream;
        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Locale;
        import java.util.Map;
        import android.annotation.SuppressLint;
        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.content.pm.PackageManager;
        import android.hardware.Camera;
        import android.hardware.Camera.CameraInfo;
        import android.hardware.Camera.PictureCallback;
        import android.hardware.SensorManager;
        import android.location.Address;
        import android.location.Geocoder;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.media.MediaScannerConnection;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Environment;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.view.WindowManager;
        import android.widget.ImageButton;
        import android.widget.LinearLayout;
        import android.widget.TextView;
        import android.widget.Toast;

public class MainActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private ImageButton capture, switchCamera,galleryMode;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    String _lastMediaFilePath = null;
    Uri _lastMediaFileUri = null;
    static GPSTracker gps;
    public static String myAddress = "";
    public static Map<String, String> AddressList;
    final String LOG_TAG ="Camera Direct Access";
    public static String filename = PhotoHelper.getPhotoDirectory().getAbsolutePath();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        initialize();
    }



    public void initialize() {
        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);
        AddressList = new HashMap<String,String>();

        capture = (ImageButton) findViewById(R.id.button_capture);
        capture.setOnClickListener(captureListener);

        switchCamera = (ImageButton) findViewById(R.id.button_ChangeCamera);
        switchCamera.setOnClickListener(switchCameraListener);

        galleryMode = (ImageButton) findViewById(R.id.Button01);
        galleryMode.setOnClickListener(galleryModeListener);
    }


    OnClickListener captureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    onPictureJpeg(bytes, camera, MainActivity.this);
                }

            });
        }
    };

    OnClickListener switchCameraListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //get the number of cameras
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                //release the old camera instance
                //switch camera, from the front and the back and vice versa

                releaseCamera();
                chooseCamera();
            } else {
                Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    OnClickListener galleryModeListener = new OnClickListener(){

        @Override
        public void onClick(View v) {

            releaseCamera();
            Intent intent = new Intent(MainActivity.this,GridViewActivity.class);
            startActivity(intent);
        }
    };


    void onPictureJpeg(byte[] bytes, Camera camera,Context context){
        String usermessage = null;
        String addressMessage = null;

        int i = bytes.length;
        Log.d(LOG_TAG, String.format("bytes= %d", i));
        File f = PhotoHelper.generateTimeStampPhotoFile();
        myAddress = Location();
        String path = f.getPath();
        setDefaults(path,myAddress,MainActivity.this);//Store the File path and Address information as Key Value Pair on the SharedPreference Memory
        try{
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(f));
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            usermessage = "Picture saved as " + f.getName()+"\n"+ getDefaults(path,this);
            Log.e(LOG_TAG,"image saved in : " + f.getAbsolutePath());

        }catch(Exception e){
            Log.e(LOG_TAG,"Error accessing photo output file: " + e.getMessage());
            usermessage = "Error saving photo";

        }

        if(usermessage != null){

            Toast.makeText(this,usermessage,Toast.LENGTH_LONG).show();
            Toast.makeText(this,"Please wait for recording GPS information",Toast.LENGTH_LONG).show();


            doScanFile(f.toString());
        }


        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
        //Uri.parse("file://" + Environment.getExternalStorageDirectory())));


        mCamera.startPreview();

    }


    void doScanFile(String fileName){
        String[] filesToScan = {fileName};


        MediaScannerConnection.scanFile(this, filesToScan, null
                , new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String filePath, Uri uri) {
                mediaFileScanComplete(filePath, uri);
            }
        });
    }

    void mediaFileScanComplete(String mediaFilePath, Uri mediaFileUri){

        Log.d(LOG_TAG, String.format("File = %s | Uri = %s", mediaFilePath, mediaFileUri));

        _lastMediaFilePath = mediaFilePath;
        _lastMediaFileUri = mediaFileUri;

    }
    public void chooseCamera() {
        //if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            //if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                switchCamera.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findBackFacingCamera());
            mPreview.refreshCamera(mCamera);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }



    private String Location() {
        //get the location data of the current place.

        gps = new GPSTracker(MainActivity.this);
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if(addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("Address:\n");
                for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                myAddress = strReturnedAddress.toString();
            }
            else{
                myAddress = "No Address returned!" ;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            myAddress = "Cant get address!";
        }
        return myAddress;
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }



    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }



}