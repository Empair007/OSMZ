package com.example.osmz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Policy;
import java.util.Timer;

public class MainActivity extends Activity implements OnClickListener {

    private SocketServer s;
    private Integer threadsCount;
    TextView threadCounter;
    private Camera camera;
    private int MY_PERMISSION_CAMERA = 0;
    private int MY_PERMISSION_STORAGE = 1;

    private Button btn3;
    private Button btn4;
    private Handler handler;


    Camera.PictureCallback jpgCallback = new Camera.PictureCallback() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);

                if(bitmap!=null){

                    File file=new File(MainActivity.this.getBaseContext().getFilesDir().toString());
                    if(!file.isDirectory()){
                        file.mkdir();
                    }

                    file=new File(MainActivity.this.getBaseContext().getFilesDir(),"image.jpg");
                    Log.d("Something", file.getPath());
                    try
                    {
                        if (!file.exists())
                            Files.createFile(file.toPath());
                        FileOutputStream fileOutputStream=new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100, fileOutputStream);
                        fileOutputStream.write(data);
                        fileOutputStream.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                    catch(Exception exception)
                    {
                        exception.printStackTrace();
                    }

                }
            }
            camera.stopPreview();
            camera.release();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn1 = (Button) findViewById(R.id.button1);
        Button btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        btn4 = (Button) findViewById(R.id.button4);
        threadCounter = (TextView) findViewById(R.id.threadCounter);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        threadsCount = 0;
        threadCounter.setText("Working Threads " + threadsCount);

        this.requestPermission();
        if (checkCameraHardware(this)){
            btn3.setEnabled(true);
            btn4.setEnabled(true);
        } else {
            btn3.setEnabled(false);
            btn4.setEnabled(false);
        }

    }

    public void requestPermission(){
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
            btn3.setEnabled(false);
            btn4.setEnabled(false);
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.CAMERA },
                    MY_PERMISSION_CAMERA );
        }
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    MY_PERMISSION_STORAGE );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_CAMERA){
            if ( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.btn3.setEnabled(true);
                this.btn4.setEnabled(true);
            }
        }
        this.requestPermission();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {
            s = new SocketServer(messageHandler, MainActivity.this.getBaseContext().getFilesDir()+"/image.jpg");
            s.start();
        }
        if (v.getId() == R.id.button2) {
            s.close();
            try {
                s.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (v.getId() == R.id.button3) {
            handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (handler == null){
                        return;
                    }
                    camera = getCameraInstance();
                    Camera.Parameters param = camera.getParameters();
                    param.setPictureSize(1920, 1080);
                    param.setJpegQuality(100);
                    param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    camera.setParameters(param);
                    SurfaceTexture view = new SurfaceTexture(MODE_PRIVATE);
                    try {
                        camera.setPreviewTexture(view);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();
                    camera.takePicture(null, null, jpgCallback);
                    //your code
                    Log.d("uii", "uiii");
                    handler.postDelayed(this,3000);
                }
            },3000);
        }
        if (v.getId() == R.id.button4) {
            if (this.handler != null){
                this.handler = null;
            }
        }

    }




    /** Check if this device has a camera */
    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Handler messageHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            threadsCount += msg.what;
            threadCounter.setText("Working Threads " + threadsCount);
        }
    };

}

