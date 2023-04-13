package com.golo.goloradio;


import android.app.Application;

public class PlayingInfo extends Application {
    public int playingId;
    public String playingStationName;
    public String playingMusictile;
    public String ShowMQName;
    public int playingStatus;
    public boolean isShowingPic;

    public void onCreate() {

        super.onCreate();
        this.isShowingPic = false;
        this.playingId = -1;
        this.playingStationName = "无";
        this.playingMusictile = "";
        this.ShowMQName = "";
        this.playingStatus = 0;
    }
}