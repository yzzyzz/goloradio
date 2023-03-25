package com.golo.goloradio;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
//import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static ExoPlayer mediaPlayer;
    public static int intPlayingId = -1;
    public static String playingStationName;
    public static MarqueeText playingBar;
    public static TextView playStateBar;
    public static List playList;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 申请权限
        verifyStoragePermissions(this);
        // 1、获取资源列表
        playList = getUrlListFromRes();
        playingBar =  findViewById(R.id.playing_info);
        playingBar.setText("无");
        playStateBar = findViewById(R.id.playing_state);
        if(mediaPlayer == null){
            mediaPlayer = new ExoPlayer.Builder(this.getApplication()).build();
        }
        mediaPlayer.addListener(new Player.Listener() {
            public void onMediaMetadataChanged(MediaMetadata mediaMetadata){
                if (mediaMetadata.title != null && mediaMetadata.title.length()>2) {
                    if(playingStationName.length()>1){
                        playingBar.setText(playingStationName +" _ "+mediaMetadata.title);
                    }else {
                        playingBar.setText(mediaMetadata.title);
                    }
                }
            }

            public void onPlaybackStateChanged( int playbackState) {
                switch(playbackState){
                    case Player.STATE_BUFFERING:
                        playStateBar.setText("正在缓存 - ");
                        break;
                    case Player.STATE_IDLE:
                        playStateBar.setText("停止播放 - ");
                        break;
                    default:
                        playStateBar.setText("正在播放 - ");
                        break;
                }
            }
        });

            // 2、生成buttom 列表
        LinearLayout layout = findViewById(R.id.radiolist);
        for(int i=0;i<playList.size();i++)
        {
            Button radioItem = new Button(this);
            String[] datainfo = (String[]) playList.get(i);
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
    private List getUrlListFromRes(){

        String rootParh =  Environment.getExternalStorageDirectory().getAbsolutePath();
        String radioFilePath=rootParh + "/data/radiolist.txt";
        Log.e("main",radioFilePath);

        List ret = new ArrayList();
        File file = new File(radioFilePath);

        if(file.exists()){
            Log.i("file info", "find  local file:"+radioFilePath);
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String line = br.readLine();
                Log.i("first line", "get 1st line: "+line);
                // 先读一行
                if (line.length()>4) {
                    // if
                    Log.i("read file", "read fir line in if: ");
                    if (line.startsWith("http")) {
                        // http开头代表网络文件处理
                        try {
                            // Create a URL for the desired page
                            URL url = new URL(line.trim());
                            Thread thread1 = new Thread(new Runnable(){
                                public void run(){
                                    try {
                                        // Create a URL for the desired page
                                        //First open the connection
                                        HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                                        conn.setConnectTimeout(10000); // timing out in a minute
                                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                        String str = "";
                                        while (( str = in.readLine()) != null) {
                                            String[] split = str.split(",");
                                            if (split.length == 2) {
                                                ret.add(split);
                                            }
                                        }
                                        in.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            thread1.start();
                            thread1.join();
                            // Read all the text returned by the server
                            return ret;
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            //
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.i("read file", "file not http info ");
                        String[] split = line.split(",");
                        if (split.length == 2) {
                            ret.add(split);
                        }
                        while ((line = br.readLine()) != null) {
                            split = line.split(",");
                            if (split.length == 2) {
                                ret.add(split);
                            }
                        }
                        return ret;
                    }
                }
                fis.close();
            } catch (IOException e) {
                // 读取文件异常
                e.printStackTrace();
            }
        } else {
            Log.i("file info", "not  local file");
            try {
                InputStream inputStream = getResources().openRawResource(R.raw.radio);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                BufferedReader reader = new BufferedReader(inputStreamReader);
                StringBuffer sb = new StringBuffer("");
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        //Log.i("line info", "getUrlListFromRes: "+line);
                        String[] split = line.split(",");
                        if (split.length == 2) {
                            ret.add(split);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return ret;
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
                // 暂停播放
                mediaPlayer.stop();
                intPlayingId = -1;
                playingStationName = "";
                //playingBar.setText("无");
                return;
            }
            try {
                mediaPlayer.stop();
                if(!mediaPlayer.isPlaying()){

/*

                    RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
// This is the MediaSource representing the media to be played.
                    MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                            .createMediaSource(Uri.parse("rtmp://stream1.livestreamingservices.com:1935/tvmlive/tvmlive"));

*/
                    mediaPlayer.setMediaItem(MediaItem.fromUri(this.playUrl));
                    mediaPlayer.prepare();
                    mediaPlayer.setPlayWhenReady(true);
                    //mediaPlayer.play();
                    intPlayingId = this.buttonId;
                    playingStationName = this.stationName;
                    playingBar.setText(this.stationName);
                }
            } catch (Exception e) {
                playingBar.setText("加载失败,请重试或更换！");
                e.printStackTrace();
            }
        }
    }
}

