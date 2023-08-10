package com.golo.goloradio.model;


import android.app.Application;

public class PlayingInfo extends Application {
    public int playingId;
    public String playingStationName;
    public String playingMusictile;
    public String ShowMQName;
    public int playingStatus;
    public boolean hasMeta;
    public String playUrl;

    public boolean hiresPic;

    public boolean listMode;

    public void onCreate() {

        super.onCreate();
        this.playingId = -1;
        this.playingStationName = "无";
        this.playingMusictile = "";
        this.ShowMQName = "";
        this.hasMeta = false;
        this.playingStatus = 0;
        this.playUrl = "";
        this.hiresPic = false;
    }

    public void InitPlayingInfo(){
        this.playingId = -1;
        this.playingStationName = "无";
        this.playingMusictile = "";
        this.ShowMQName = "";
        this.hasMeta = false;
        this.playingStatus = 0;
        this.playUrl = "";
    }
}