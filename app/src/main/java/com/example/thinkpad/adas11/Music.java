package com.example.thinkpad.adas11;

/**
 * Created by thinkpad on 2018/9/8.
 */

public class Music {
    private String path;
    private String name;
    private int time;
    private  int coverId;

    public int getCoverId() {
        return coverId;
    }

    public void setCoverId(int coverId) {
        this.coverId = coverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
