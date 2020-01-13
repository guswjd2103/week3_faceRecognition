package com.example.week3;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.util.Base64;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

import androidx.camera.core.CameraX;
import androidx.lifecycle.LifecycleOwner;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener {

    private Context mContext;
    private FloatingActionButton fab_main, fab_sub1, fab_sub2;
    private ImageView imageView;
    private TextureView mTextureView;
    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;
    Retrofit retrofit;
    RetrofitInterface retrofitInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this ,Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this ,
                        new String[]{Manifest.permission.CAMERA,
                                     Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }

        imageView = (ImageView)findViewById(R.id.image);

        mTextureView = (TextureView) findViewById(R.id.camera_texture_view);
        startCamera();




        fab_open = AnimationUtils.loadAnimation(mContext, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(mContext, R.anim.fab_close);

        fab_main = (FloatingActionButton) findViewById(R.id.fab_main);
        fab_sub1 = (FloatingActionButton) findViewById(R.id.fab_sub1_camera);
        fab_sub2 = (FloatingActionButton) findViewById(R.id.fab_sub2_bring);



        fab_main.setOnClickListener(this);
        fab_sub1.setOnClickListener(this);
        fab_sub2.setOnClickListener(this);

    }






    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_main:
                toggleFab();
                break;

            case R.id.fab_sub1_camera:
                toggleFab();
//                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//                startActivity(intent);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "video/*");
                startActivityForResult(intent, 2);
                break;

            case R.id.fab_sub2_bring:
                toggleFab();
                Intent intent2 = new Intent(Intent.ACTION_PICK);
                intent2. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent2, 1);
                break;
        }
    }

    private void toggleFab() {

        if (isFabOpen) {
//            fab_main.setImageResource(R.drawable.ic_add);
            fab_sub1.startAnimation(fab_close);
            fab_sub2.startAnimation(fab_close);
            fab_sub1.setClickable(false);
            fab_sub2.setClickable(false);
            isFabOpen = false;

        } else {
//            fab_main.setImageResource(R.drawable.ic_close);
            fab_sub1.startAnimation(fab_open);
            fab_sub2.startAnimation(fab_open);
            fab_sub1.setClickable(true);
            fab_sub2.setClickable(true);
            isFabOpen = true;
        }
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
                        updateTransform();
                    }
                });


        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);

//        findViewById(R.id.imgCapture).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".png");
//                imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
//                    @Override
//                    public void onImageSaved(@NonNull File file) {
//                        String msg = "Pic captured at " + file.getAbsolutePath();
//                        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
//                        String msg = "Pic capture failed : " + message;
//                        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
//                        if(cause != null){
//                            cause.printStackTrace();
//                        }
//                    }
//                });
//            }
//        });

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





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        // Check which request we're responding to
        if (requestCode == 1) {
            Log.d("resultcode", Integer.toString(resultCode));
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                File file = new File(getPath(getApplicationContext(), data.getData()));

                Log.d("hihi", getPath(getApplicationContext(), data.getData()));

                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("img", file.getName(), requestFile);
                Log.d("filename", body.toString());

                retrofit = new Retrofit.Builder().baseUrl(retrofitInterface.API_URL).addConverterFactory(GsonConverterFactory.create()).build();
                retrofitInterface = retrofit.create(RetrofitInterface.class);

                Call<String> call = retrofitInterface.uploadImage(body);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) { //response.body = string
                        String filename = "http://192.168.0.60:80/mosaicImage/" + response.body();
                        Log.d("filename",filename);
                        if (response.isSuccessful()) {
                            Log.d("성공", "성공");

                            //화면에 이미지 보여주기
                            try {
                                Picasso.with(getApplicationContext()).load(filename).into(imageView);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Log.d("onResponse", "failure");
                        }


                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("onFailure", t.toString());
                    }
                });

            }
        } else if (requestCode == 2) {
            Log.d("resultcode", Integer.toString(resultCode));
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                File file = new File(getPath(getApplicationContext(), data.getData()));
                Log.d("hi", getPath(getApplicationContext(), data.getData()));

                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("img", file.getName(), requestFile);
                Log.d("filename", body.toString());

                retrofit = new Retrofit.Builder().baseUrl(retrofitInterface.API_URL).addConverterFactory(GsonConverterFactory.create()).build();
                retrofitInterface = retrofit.create(RetrofitInterface.class);

                Call<String> call = retrofitInterface.uploadVideo(body);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) { //response.body = string
                        String filename = "http://192.168.0.60:80/mosaicVideo/" + response.body();
                        Log.d("filename",filename);
                        if (response.isSuccessful()) {
                            Log.d("성공", "성공");

                            //화면에 이미지 보여주기
                            try {
                                Picasso.with(getApplicationContext()).load(filename).into(imageView);
//                                runOnUiThread(()->{
//                                    Picasso.with(getApplicationContext())
//                                            .load("http://192.168.0.60:80/mosaicImage/" + response.body())
//                                            .into(new Target() {
//                                                @Override
//                                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                                    Log.d("load", "load");
//                                                    imageView.setImageBitmap(bitmap);
//                                                }
//
//                                                @Override
//                                                public void onBitmapFailed(Drawable errorDrawable) {
//                                                    Log.d("fail", "fail");
//                                                }
//
//                                                @Override
//                                                public void onPrepareLoad(Drawable placeHolderDrawable) {
//                                                    Log.d("prepare", "prepare");
//                                                }
//                                            });
//                                });


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Log.d("onResponse", "failure");
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("onFailure", t.toString());
                    }
                });
            }
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
