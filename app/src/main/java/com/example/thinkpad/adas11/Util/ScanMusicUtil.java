package com.example.thinkpad.adas11.Util;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.thinkpad.adas11.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thinkpad on 2018/9/8.
 */

public class ScanMusicUtil {
    public static ArrayList<Music> scanMusic(Context context){
       ArrayList<Music> musicList= new ArrayList<Music>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        if(cursor!=null){
            while(cursor.moveToNext()){
                Music music=new Music();
                music.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                music.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                music.setCoverId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
                musicList.add(music);
            }
        }
        cursor.close();
        return musicList;
    }
}
