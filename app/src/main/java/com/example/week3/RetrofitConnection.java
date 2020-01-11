package com.example.week3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConnection {
    Gson gson = new GsonBuilder().setLenient().create();
    Retrofit retrofit = new Retrofit.Builder()
//            .baseUrl("http://192.168.0.58:80")
            .baseUrl("http://192.168.0.60:80")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    RetrofitInterface server = retrofit.create(RetrofitInterface.class);
}
