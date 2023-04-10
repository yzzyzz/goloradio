package com.golo.goloradio;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    public static ExoPlayer mediaPlayer;
    public static int intPlayingId = -1;
    public static String playingStationName;
    public static MarqueeText playingBar;
    public static TextView playStateBar;
    public static List playList;

    public static String[] reqCate = {"我的最爱","音乐电台","综合资讯","文化曲艺"};


    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    public static HashMap<String, List<RadioItem>> expandableListDetail;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE);
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

        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }



        playingBar =  findViewById(R.id.playing_info);
        playStateBar = findViewById(R.id.playing_state);
        if(mediaPlayer == null){
            mediaPlayer = new ExoPlayer.Builder(this.getApplication()).build();
            playingBar.setText("无");

        }else {
            if(playingStationName.length()>2){
                playingBar.setText(playingStationName);
            }else {
                playingBar.setText("无");
            }
        }

        // 1、获取资源列表
        playList = getUrlListFromRes();

        ExpandableListDataPump expStationList = new ExpandableListDataPump();

        for(int i=0;i<playList.size();i++) {
            String[] datainfo = (String[]) playList.get(i);
            String groupName = "未分类";
            if(datainfo.length>=3){
                groupName = datainfo[2];
            }
            expStationList.addStationItem(this,datainfo[0],datainfo[1],groupName,i);
        }

        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expandableListDetail = expStationList.getAllStationMap();
        expandableListView.setItemsCanFocus(true);


        List<String> allListTitle = new ArrayList<String>(expandableListDetail.keySet());

        // 排个序列
        List<String> expandableListTitle = new LinkedList<String>();
        for(int i =0;i<reqCate.length;i++){
            expandableListTitle.add(reqCate[i]);
            if(!allListTitle.contains(reqCate[i])){
                Toast.makeText(getApplicationContext(),("!数据文件缺乏必须的分类:"+reqCate[i]), Toast.LENGTH_LONG).show();
                return ;
            }
        }

        for(int i = 0;i< allListTitle.size();i++){
            if(!expandableListTitle.contains(allListTitle.get(i)) ){
                expandableListTitle.add(allListTitle.get(i));
            }
        }

        expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                int intToPlayId = expandableListDetail.get(
                        expandableListTitle.get(groupPosition)).get(
                        childPosition).id;

                if(intToPlayId == intPlayingId){
                    // 暂停播放
                    intPlayingId = -1;
                    mediaPlayer.stop();
                    playingStationName = "";
                    return true;
                }

                try {
                    mediaPlayer.stop();
                    if(!mediaPlayer.isPlaying()){
                        mediaPlayer.setMediaItem(MediaItem.fromUri(expandableListDetail.get(
                                expandableListTitle.get(groupPosition)).get(
                                childPosition).url));
                        mediaPlayer.prepare();
                        mediaPlayer.setPlayWhenReady(true);
                        intPlayingId =  intToPlayId;
                        playingStationName = expandableListDetail.get(
                                expandableListTitle.get(groupPosition)).get(
                                childPosition).name;
                        playingBar.setText(playingStationName);
                    }
                } catch (Exception e) {
                    playingBar.setText("加载失败,请重试或更换！");
                    e.printStackTrace();
                }
                return false;
            }
        });
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
                        playStateBar.setText("正在加载 - ");
                        break;
                    case Player.STATE_IDLE:
                        // 尝试重新播放
                        playStateBar.setText("停止播放 - ");
                        break;
                    default:
                        playStateBar.setText("正在播放 - ");
                        break;
                }
            }
        });
    }

    private List getUrlListFromWeb(){
        List ret = new ArrayList();

        try {
            String line= "http://gz.999887.xyz/radio.php";
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
                            if (  split.length >= 2) {
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private List getUrlListFromRes(){

        String rootParh =  Environment.getExternalStorageDirectory().getAbsolutePath();
        String radioFilePath=rootParh + "/data/radiolist.csv";
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
                                            if (  split.length >= 2) {
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
                            return ret;
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.i("read file", "file not http info ");
                        String[] split = line.split(",");
                        if (split.length >= 2) {
                            ret.add(split);
                        }
                        while ((line = br.readLine()) != null) {
                            split = line.split(",");
                            if (split.length >= 2) {
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
                        if (split.length >= 2) {
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
}

