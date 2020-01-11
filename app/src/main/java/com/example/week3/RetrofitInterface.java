package com.example.week3;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitInterface {

//    public static final String API_URL = "http://192.168.0.58:80";
    public static final String API_URL = "http://192.168.0.60:80";
    @Multipart
    @POST("uploadImage")
    Call<String> uploadImage(@Part MultipartBody.Part file);
//    @FormUrlEncoded
//    @POST("uploadImage")
//    Call<String> uploadImage(@Field("image") String image);

    @GET("hi")
    Call<String> getRoutes();
}
