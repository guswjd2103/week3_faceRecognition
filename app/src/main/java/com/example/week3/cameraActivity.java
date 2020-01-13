package com.example.week3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;

import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;


public class cameraActivity extends AppCompatActivity {

    private TextureView mTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mTextureView = (TextureView) findViewById(R.id.camera_texture_view);
        startCamera();
    }



    //camera settinig
    private void startCamera() {
        CameraX.unbindAll();
        Size screen = new Size(mTextureView.getWidth(), mTextureView.getHeight()); //size of the screen

        // Rational aspectRatio = new Rational (mTextureView.getWidth(), mTextureView.getHeight());
        // AspectRatio aspectRatio = new AspectRatio();
        // setTargetAspectRatio(aspectRatio)

        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    //to update the surface texture we  have to destroy it first then re-add it
                    @Override
                    public void onUpdated(Preview.PreviewOutput output){
                        ViewGroup parent = (ViewGroup) mTextureView.getParent();
                        parent.removeView(mTextureView);
                        parent.addView(mTextureView, 0);

                        mTextureView.setSurfaceTexture(output.getSurfaceTexture());
                        // Log.d("output~! : ",output);

                        updateTransform();
                    }
                });


        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);

        //bind to lifecycle:
        CameraX.bindToLifecycle((LifecycleOwner)this, preview, imgCap);

    }


    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = mTextureView.getMeasuredWidth();
        float h = mTextureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)mTextureView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        mTextureView.setTransform(mx);
    }





}



