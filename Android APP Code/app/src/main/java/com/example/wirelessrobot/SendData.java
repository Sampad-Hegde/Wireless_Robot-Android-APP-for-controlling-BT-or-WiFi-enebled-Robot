package com.example.wirelessrobot;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SendData
{
    @GET("/forward")
    Call<ResponseBody> goForward();

    @GET("/backward")
    Call<ResponseBody> goBackward();

    @GET("/left")
    Call<ResponseBody> goLeft();

    @GET("/right")
    Call<ResponseBody> goRight();

    @GET("/stop")
    Call<ResponseBody> goStop();

    @GET("/setspeed")
    Call<ResponseBody> setSpeed(@Query("speed") int speed);

    @GET("/setmode")
    Call<ResponseBody> setMode(@Query("mode") int mode);

}
