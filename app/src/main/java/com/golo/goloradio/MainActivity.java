package com.golo.goloradio;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 1、获取资源列表
        List playList = getUrlListFromRes();
        // 2、生成buttom 列表
        // 3、设置点击播放
        LinearLayout layout = findViewById(R.id.radiolist);

        //MediaPlayer mediaPlayer = new MediaPlayer();



        ExoPlayer mediaPlayer = new ExoPlayer.Builder(this.getApplication()).build();

        for(int i=0;i<playList.size();i++)
        {
            Button radioItem = new Button(this);
            String[] datainfo = (String[]) playList.get(i);
            //Log.i("读取的信息", "onCreate: "+ datainfo[0]);
            radioItem.setText(datainfo[0]);
            radioItem.setTextSize(22);
            radioItem.setHeight(25);
            radioItem.setFocusable(true);
            //radioItem.setFontFeatureSettings();
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
        private ExoPlayer mediaPlayer;
        public PlayM3uRadio(String url, ExoPlayer mediaPlayer) {
            this.playUrl = url;
            this.mediaPlayer=mediaPlayer;
            Log.i("url", "PlayM3uRadio: " + this.playUrl);
        }

        @Override
        public void onClick(View view) {
            try {
                this.mediaPlayer.stop();
                if(!this.mediaPlayer.isPlaying()){

                    //String type= NetworkHelper.detectContentType(station.getStreamUri()).type;

                    // Create a data source factory.
                    DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
// Create a HLS media source pointing to a playlist uri.
                    HlsMediaSource hlsMediaSource =
                            new HlsMediaSource.Factory(dataSourceFactory)
                                    .createMediaSource(MediaItem.fromUri(this.playUrl));
// Create a player instance.


                    this.mediaPlayer.setMediaSource(hlsMediaSource);
                    this.mediaPlayer.prepare();
                }
                this.mediaPlayer.play();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

