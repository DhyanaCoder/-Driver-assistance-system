package com.example.thinkpad.adas11.http;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by thinkpad on 2018/8/22.
 */

public class pictureHttpUtil {
    private final static  String apikey="LzQUsyWYuvT5kNqAAuUuY1pBmhhS37V7";
    private final static  String address="http://guolin.tech/api/bing_pic";
    public static void sendOkhttpRequest(okhttp3.Callback callback){
        //建立RequestBody对象存放待提交的参数，参数有 apikey,text,userid.
        /*RequestBody requestBody=new FormBody.Builder()
                .add("key",apikey)
                .add("info",text)
                .add("userid","128")
                .build();*/
        OkHttpClient client =new OkHttpClient();
        Request request=new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);//enqueue方法在内部开好了子线程并最终将结果回调到okhttp3.Callback当中。
    }
}
