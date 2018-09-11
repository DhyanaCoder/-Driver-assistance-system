package com.example.thinkpad.adas11;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.InputEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.example.thinkpad.adas11.Util.MyToast;
import com.example.thinkpad.adas11.Util.ScanMusicUtil;
import com.example.thinkpad.adas11.http.pictureHttpUtil;
import com.example.thinkpad.adas11.http.weatherHttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{
private TextView loc;
    private int key=0;//播放键开关变量
    public LocationClient mLocationClient;
    private String city="广州";
    private String  temperature;
    private String weatherData;
    private TextView weather;
    private ImageView bgr;
    private ImageView imageButton;
    private int weatherImageAddress;
    private ImageButton music_playButton;
    private ImageButton music_nextButton;
    private ImageButton music_lastButton;
    private DrawerLayout mDrawerLayout;
    private ImageButton playMusic;
    private  ArrayList<Music> musicList;
    private MediaPlayer mediaPlayer=new MediaPlayer();
    private  ImageView testCover;
    private int musicNumber=0;//音乐指针，指向正在播放的音乐
    private TextView MusicName;
    private  ImageButton scan;
    private Button orderButton;
    private int order=0;//切歌顺序变量
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_main);
        mDrawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        music_lastButton=(ImageButton)findViewById(R.id.leftButton) ;
        music_nextButton=(ImageButton)findViewById(R.id.rightButton);
        music_lastButton.setOnClickListener(this);
        music_nextButton.setOnClickListener(this);
        orderButton=(Button) findViewById(R.id.order);
        orderButton.setOnClickListener(this);
        scan=(ImageButton)findViewById(R.id.scan);
        scan.setOnClickListener(this);
        playMusic=(ImageButton)findViewById(R.id.playButton) ;
        testCover=(ImageView)findViewById(R.id.musicCover);
        MusicName=(TextView)findViewById(R.id.musicName);
        playMusic.setOnClickListener(this);


        //以下代码设置播放器监听
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //切歌顺序逻辑代码
             switch (order){
                 case 0:
                     nextMusic();//顺序播放音乐
                     break;
                 case 1:
                     mediaPlayer.start();//单曲循环音乐
                     break;
                 case 2:
                     if(!(musicNumber==(musicList.size()-1))){
                         mediaPlayer.reset();
                         nextMusic();//如果当前不是最后一首音乐就直接切换下一首音乐
                     }else{
                         //如果当前是最后一首音乐就切换到列表首，播放第一首音乐
                         mediaPlayer.reset();
                         musicNumber=0;
                         InitMedia(musicList.get(0));
                         mediaPlayer.start();
                     }
                     break;
                 case 3:
                     mediaPlayer.reset();
                     int next=(int)(Math.random()*(musicList.size()-1));//随即切换音乐
                     InitMedia(musicList.get(next));
                     mediaPlayer.start();
                     break;

             }
            }
        });
        weather=(TextView) findViewById(R.id.weather);
        bgr=(ImageView) findViewById(R.id.background);
        imageButton=(ImageView) findViewById(R.id.weatherbox);
        music_playButton=(ImageButton) findViewById(R.id.musictop);
        //以下代码隐藏标题栏
        ActionBar actionBar =getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }

        music_playButton.setOnClickListener(this);
       loc=(TextView)findViewById(R.id.location);
        //以下为权限申请代码
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
        try{//等待权限完全获取
        sleep(1000);}
        catch (Exception e){
            e.printStackTrace();
        }


        GetPicture();//从必应每日一图获取背景
       musicList=  ScanMusicUtil.scanMusic(this);


        //获取上一次的播放数据
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
        musicNumber=pref.getInt("musicNumber",0);
        order=pref.getInt("order",0);
        Log.d("order",""+order);
        switch (order-1){
            case 0:
                orderButton.setText("单曲循环");

                break;
            case 1:
                orderButton.setText("循环列表");

                break;
            case 2:
                orderButton.setText("随机播放");

                break;
            case 3:
                orderButton.setText("顺序播放");

                break;}

        InitMedia(musicList.get(musicNumber));
    }
    private void InitMedia(Music music){
        try {

            mediaPlayer.setDataSource(music.getPath());
            mediaPlayer.prepare();

        }catch (Exception e){
            e.printStackTrace();
        }
        MusicName.setText(music.getName());
        //获取封面
        String id=getAlbumArt(music.getCoverId());
        if(id!=null){
            Bitmap bm = null;
            bm = BitmapFactory.decodeFile(id);
            BitmapDrawable bmpDraw = new BitmapDrawable(bm);
            testCover.setImageDrawable(bmpDraw);

        }else{
            testCover.setImageDrawable(getResources().getDrawable(R.drawable.localcover));
        }
    }
    private String getAlbumArt(int album_id)//获取封面一个关键方法
    {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[] { "album_art" };
        Cursor cur = this.getContentResolver().query(  Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),  projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0)
        {  cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        return album_art;
    }
public void onClick(View v){
    switch (v.getId()) {
        case R.id.musictop:
            mDrawerLayout.openDrawer(Gravity.START);
            break;
        case R.id.playButton:
            if (key == 0) {
                playMusic.setBackground(getResources().getDrawable(R.drawable.pause_cirlce));
                key = 1;
                mediaPlayer.start();
            } else {
                playMusic.setBackground(getResources().getDrawable(R.drawable.play_circle));
                mediaPlayer.pause();
                key = 0;
            }
            break;
        case R.id.rightButton:
            nextMusic();

            break;
        case R.id.leftButton:
            lastMusic();
            break;
        case R.id.scan:
            mediaPlayer.stop();
            mediaPlayer.reset();
            musicList = ScanMusicUtil.scanMusic(this);
            InitMedia(musicList.get(musicNumber));
            playMusic.setBackground(getResources().getDrawable(R.drawable.play_circle));

            key = 0;
            break;
        case R.id.order:
            //切歌秩序改变 逻辑
           orderSwitch(order);

    }
}
private void orderSwitch(int x){
    switch (x){
        case 0:
            orderButton.setText("单曲循环");
            order++;
            break;
        case 1:
            orderButton.setText("循环列表");
            order++;
            break;
        case 2:
            orderButton.setText("随机播放");
            order++;
            break;
        case 3:
            orderButton.setText("顺序播放");
            order=0;
            break;}
}
private void nextMusic(){
    if(musicList.size()-2>=musicNumber) {
        musicNumber++;
        if (musicList.get(musicNumber) != null) {
            mediaPlayer.reset();
            InitMedia(musicList.get(musicNumber));
            playMusic.setBackground(getResources().getDrawable(R.drawable.pause_cirlce));
            key = 1;
            mediaPlayer.start();
        }
    }
}
private  void lastMusic(){
    if(musicNumber>0) {
        musicNumber--;
        if (musicList.get(musicNumber) != null) {
            mediaPlayer.reset();
            InitMedia(musicList.get(musicNumber));
            playMusic.setBackground(getResources().getDrawable(R.drawable.pause_cirlce));
            key = 1;
            mediaPlayer.start();
        }
    }
}
private void requestLocation(){
       initLocation();//初始化一些地图SDK配置
        mLocationClient.start();
    }
    private void initLocation(){//初始化一些地图SDK配置
        LocationClientOption option= new LocationClientOption();
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }
    @Override
    public void onStop(){
        super.onStop();
        SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
        editor.putInt("musicNumber",musicNumber);
        editor.putInt("order",order);
        editor.apply();
    }
    @Override
    public void onDestroy(){//退出应用关闭地图定位服务

        mLocationClient.stop();
        mediaPlayer.release();

        //储存播放数据
    /*    */

        super.onDestroy();
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

                    imageButton.setBackground(getResources().getDrawable(weatherImageAddress));
               weather.setText(weatherData+" "+temperature);
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

                if(!currentPosition.toString().equals("null")){

               loc.setText(currentPosition);
                }
                if(city!=location.getCity().toString()){//如果城市不同则更新天气
                    city=location .getCity().toString();
                    GetWeather();
                }
            }

        });
    }
}
private void GetPicture(){//获取背景

    pictureHttpUtil.sendOkhttpRequest(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

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
                    switch (weatherData){
                        case "多云":
                            weatherImageAddress=R.drawable.cloud;
                            break;
                        case "晴":
                            weatherImageAddress=R.drawable.fine;
                            break;
                        case "阴":
                            weatherImageAddress=R.drawable.overcast;
                            break;
                        case "小雨":
                            weatherImageAddress=R.drawable.small_rain;
                            break;
                        case "小到中雨":
                            weatherImageAddress=R.drawable.stom_rain;
                            break;
                        case "大雨":
                        case"中到大雨":
                            weatherImageAddress=R.drawable.big_rain;
                            break;
                        case "暴雨":
                        case "大暴雨":
                        case "特大暴雨":
                        case "大到暴雨":
                        case "暴雨到大暴雨":
                        case "大暴雨到特大暴雨":
                            weatherImageAddress=R.drawable.mbig_rain;
                            break;
                        case "雨夹雪":
                            weatherImageAddress=R.drawable.rain_snow;
                            break;
                        case "阵雪":
                            weatherImageAddress=R.drawable.quick_snow;
                            break;
                        case "雾":
                            weatherImageAddress=R.drawable.fog;
                            break;
                        case "沙尘暴":
                        case "浮尘":
                        case "扬沙":
                        case "强沙尘暴":
                        case "雾霾":
                            weatherImageAddress= R.drawable.sand;
                            break;
                        case "冻雨":
                            weatherImageAddress=R.drawable.ice_rain;
                            break;
                        case  "中雨":
                            weatherImageAddress=R.drawable.mid_rain;
                            break;
                        case "雷阵雨伴有冰雹":
                            weatherImageAddress=R.drawable.quick_rain_ice2;
                            break;
                        case "阵雨":
                            weatherImageAddress= R.drawable.quick_rain;
                            break;
                        case "雷阵雨":
                            weatherImageAddress=R.drawable.lquick_rain;
                            break;
                        default:
                            weatherImageAddress=R.drawable.unknown;

                    }
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
