package com.golo.goloradio;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static ExoPlayer mediaPlayer;
    public static int intPlayingId = -1;
    public static MarqueeText playingBar;
    public static List playList;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 1、获取资源列表
        playList = getUrlListFromRes();
        // 2、生成buttom 列表
        LinearLayout layout = findViewById(R.id.radiolist);
        //MediaPlayer mediaPlayer = new MediaPlayer();
        if(mediaPlayer == null){
            mediaPlayer = new ExoPlayer.Builder(this.getApplication()).build();
        }

        for(int i=0;i<playList.size();i++)
        {
            Button radioItem = new Button(this);
            String[] datainfo = (String[]) playList.get(i);
            playingBar =  findViewById(R.id.playing_info);
            playingBar.setText("空");
            String newName = String.format("%02d", i+1) +"."+ datainfo[0];
            radioItem.setText(newName);
            radioItem.setTextSize(23);
            radioItem.setHeight(25);
            radioItem.setFocusable(true);
            radioItem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        radioItem.setTextColor(Color.RED);
                    }else {
                        radioItem.setTextColor(Color.BLACK);
                    }
                }
            });
            // 3、设置点击播放
            radioItem.setOnClickListener(new PlayM3uRadio(i) );
            layout.addView(radioItem);
        }
    }
    public List getUrlListFromRes(){
        List ret = new ArrayList();
        InputStream inputStream = getResources().openRawResource(R.raw.radio);
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                //Log.i("line info", "getUrlListFromRes: "+line);
                String[] split=line.split(",");
                if(split.length == 2){
                    ret.add(split);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  ret;
    }

    static class PlayM3uRadio implements View.OnClickListener {
        private String playUrl;
        private int buttonId;
        private String stationName;
        public PlayM3uRadio(int  bId) {
            String[] datainfo = (String[]) playList.get(bId);
            this.stationName = datainfo[0];
            this.playUrl = datainfo[1];
            this.buttonId = bId;
            //Log.i("url", "PlayM3uRadio: " + this.playUrl);
        }
        @Override
        // 3种情况
        // 1、正在播放当前，点一下要暂停
        // 2、当前没有播放，点一下播放当前
        // 3、当前播放其他，点一下切换选中
        public void onClick(View view) {
            if (this.buttonId == intPlayingId){
                mediaPlayer.stop();
                intPlayingId = -1;
                return;
            }
            try {
                mediaPlayer.stop();
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.setMediaItem(MediaItem.fromUri(this.playUrl));
                    playingBar.setText(this.stationName);
                    mediaPlayer.prepare();
                    mediaPlayer.play();
                    intPlayingId = this.buttonId;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

