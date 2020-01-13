package com.example.week3;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
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


public class MainActivity extends AppCompatActivity implements  View.OnClickListener{

    private Context mContext;
    private FloatingActionButton fab_main, fab_sub1, fab_sub2, fab_sub3;
    private ImageView imageView;
    private VideoView videoView;

    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;
    Retrofit retrofit;
    RetrofitInterface retrofitInterface;
//    boolean running = true;
    private Thread thread;
    private RelativeLayout bigView;
    private Button shareButton;
    private Button backButton;
    private Button storeButton;
    private LinearLayout buttonLayout;
//    private Handler mHandler = new Handler(Looper.getMainLooper());

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


        buttonLayout = findViewById(R.id.ButtonLayout);
        imageView = findViewById(R.id.image);
        videoView = findViewById(R.id.video);

        fab_open = AnimationUtils.loadAnimation(mContext, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(mContext, R.anim.fab_close);

        fab_main = (FloatingActionButton) findViewById(R.id.fab_main);
        fab_sub1 = (FloatingActionButton) findViewById(R.id.fab_sub1_camera);
        fab_sub2 = (FloatingActionButton) findViewById(R.id.fab_sub2_bring);
        fab_sub3 = (FloatingActionButton) findViewById(R.id.fab_sub3_webcam);



        fab_main.setOnClickListener(this);
        fab_sub1.setOnClickListener(this);
        fab_sub2.setOnClickListener(this);
        fab_sub3.setOnClickListener(this);
        imageView.setOnClickListener(this);
        videoView.setOnClickListener(this);

//        thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(running) {
//                    try {
//                        Thread.sleep(120000);
//                    } catch (Exception e){};
//                }
//            }
//        });

//        shareButton = (Button) findViewById(R.id.Share);
//        storeButton = (Button) findViewById(R.id.store);
//        backButton = (Button) findViewById(R.id.Back);
//        bigView = findViewById(R.id.bigView);
//        imageView = (ImageView)findViewById(R.id.imgView);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_main:
                toggleFab();
                break;

            case R.id.fab_sub1_camera:
                toggleFab();
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

            case R.id.fab_sub3_webcam:
                toggleFab();
//                Intent intent3 = new Intent(MainActivity.this, cameraActivity.class);
//                startActivity(intent3);
                break;

            case R.id.image:
            case R.id.video:
                if(buttonLayout.getVisibility() == View.VISIBLE)
                    buttonLayout.setVisibility(View.GONE);
                else
                    buttonLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void toggleFab() {

        if (isFabOpen) {
//            fab_main.setImageResource(R.drawable.ic_add);
            fab_sub1.startAnimation(fab_close);
            fab_sub2.startAnimation(fab_close);
            fab_sub3.startAnimation(fab_close);
            fab_sub1.setClickable(false);
            fab_sub2.setClickable(false);
            fab_sub3.setClickable(false);
            isFabOpen = false;

        } else {
//            fab_main.setImageResource(R.drawable.ic_close);
            fab_sub1.startAnimation(fab_open);
            fab_sub2.startAnimation(fab_open);
            fab_sub3.startAnimation(fab_open);
            fab_sub1.setClickable(true);
            fab_sub2.setClickable(true);
            fab_sub3.setClickable(true);
            isFabOpen = true;
        }
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
//                thread.start();

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

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Log.d("onResponse", "failure");
                        }

//                        Thread thread2 = new Thread() {
//                            @Override
//                            public void run() {
//                                mHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    String filename = "http://192.168.0.60:80/mosaicVideo/" + response.body();
//                                    Log.d("filename",filename);
//                                    if (response.isSuccessful()) {
//                                        Log.d("성공", "성공");
//
//                                        //화면에 이미지 보여주기
//                                        try {
//
//                                            Picasso.with(getApplicationContext()).load(filename).into(imageView);
//
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                    else {
//                                        Log.d("onResponse", "failure");
//                                    }
//                                }
//                            });
//                            }
//                        };
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
