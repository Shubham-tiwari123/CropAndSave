package com.example.sharedocument;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.29.222:8080/";
    private static RetrofitClient client;
    private Retrofit retrofit;

    private RetrofitClient(){
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create()).build();

    }

    public static synchronized RetrofitClient getInstance(){
        if (client == null){
            client = new RetrofitClient();
        }
        return client;
    }

    public Api getApi(){
        return retrofit.create(Api.class);
    }

}
