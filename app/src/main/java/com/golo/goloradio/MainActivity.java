package com.golo.goloradio;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public MediaPlayer mediaPlayer = new MediaPlayer();
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 1、获取资源列表
        List playList = getUrlListFromRes();
        // 2、生成buttom 列表
        // 3、设置点击播放
        LinearLayout layout = findViewById(R.id.radiolist);

        MediaPlayer mediaPlayer = new MediaPlayer();
        for(int i=0;i<playList.size();i++)
        {
            Button radioItem = new Button(this);
            String[] datainfo = (String[]) playList.get(i);
            //Log.i("读取的信息", "onCreate: "+ datainfo[0]);


            radioItem.setText(datainfo[0]);
            //radioItem.setTextColor(R.drawable.itemcolor);
            radioItem.setOnClickListener(new PlayM3uRadio(datainfo[1] ,mediaPlayer) );
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
        private MediaPlayer mediaPlayer;
        public PlayM3uRadio(String url, MediaPlayer mediaPlayer) {
            this.playUrl = url;
            this.mediaPlayer=mediaPlayer;
            Log.i("url", "PlayM3uRadio: " + this.playUrl);
        }

        @Override
        public void onClick(View view) {
            try {
                mediaPlayer.reset();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(this.playUrl);
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(50, 50);
                //mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

