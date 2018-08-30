package com.example.thinkpad.adas11;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.example.thinkpad.adas11.http.pictureHttpUtil;
import com.example.thinkpad.adas11.http.weatherHttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity  {
private TextView loc;
    public LocationClient mLocationClient;
    /*private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate=true;*/
    private String city="广州";
    private String  temperature;
    private String weatherData;
    private TextView weather;
    private ImageView bgr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在setContentView之前设置为无标题
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置布局充满全屏
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
               // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //mLocationClient=new LocationClient(getApplicationContext());
       // mLocationClient.registerLocationListener(new  MyLocationListener());
//        SDKInitializer.initialize(getApplicationContext());
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_main);

        ActionBar actionBar =getSupportActionBar();
        weather=(TextView) findViewById(R.id.weather);
        bgr=(ImageView) findViewById(R.id.background);
        if(actionBar!=null){
            actionBar.hide();
        }
       loc=(TextView)findViewById(R.id.location);
        //坑长的权限申请代码
        List<String> permissionList=new ArrayList<>();//构建权限申请表，以下就是逐步申请权限
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty() ){
            String [] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            requestLocation();
        }
        try{
        sleep(2000);}
        catch (Exception e){
            e.printStackTrace();
        }
        Log.d("test",""+city);


        GetPicture();

    }

private void requestLocation(){
       initLocation();
        mLocationClient.start();
    }
    private void initLocation(){
        LocationClientOption option= new LocationClientOption();
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch(requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }

                   requestLocation();
                }else{
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
   private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
               weather.setText(city+weatherData+" "+temperature);
            }
        }
    };
public class MyLocationListener implements BDLocationListener{
    @Override
    public void onReceiveLocation(final BDLocation location){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder currentPosition=new StringBuilder();
                currentPosition.append(location.getCity());
                loc.setText(currentPosition);
              //  Toast.makeText(MainActivity.this,"sddwq"+location.getCountry()+location.getLatitude()+" "+location.getLocType()+" "+BDLocation.TypeNetWorkLocation+" "+BDLocation.TypeGpsLocation,Toast.LENGTH_SHORT).show();

                if(city!=location.getCity().toString()){//如果城市不同则更新天气
                    city=location .getCity().toString();
                    GetWeather();
                }
             //   Log.d("test1",""+city);

            }

        });
    }
}
private void GetPicture(){//获取背景

    pictureHttpUtil.sendOkhttpRequest(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("test","test！！！！！");
       /* runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bgr.setImageResource(R.drawable.bgr);
            }
        });*/
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
final String address =response.body().string();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(MainActivity.this).load(address).into(bgr);
                }
            });
        }
    });
}
private void GetWeather(){
    //Log.d("test","nice");
    weatherHttpUtil.sendOkhttpRequest(city, new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String responseData=response.body().string();
           // Log.d("test",""+responseData);
            try{
                JSONObject object=new JSONObject(responseData);
                if(object.getInt("status")==200){

                    JSONObject data=object.getJSONObject("data");
                    JSONArray jsonArrayResult=data.getJSONArray("forecast");
                    temperature=data.getString("wendu");
                    JSONObject weather=jsonArrayResult.getJSONObject(0);
                    weatherData=weather.getString("type");
                    //Toast.makeText(MainActivity.this,""+weatherData,Toast.LENGTH_SHORT).show();
                    Log.d("test",""+temperature+weatherData);
                    Message message=new Message();
                    message.what=1;
                    handler.sendMessage(message);
                }else{
                    //Toast.makeText(MainActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                e.printStackTrace();

            }
        }
    });
}
    public static boolean isNetworkAvailable(Context context) { //检查网络是否通畅

        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }

        return true;
    }


}
