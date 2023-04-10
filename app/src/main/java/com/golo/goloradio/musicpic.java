package com.golo.goloradio;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;

import com.bumptech.glide.module.AppGlideModule;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;



public class musicpic extends AppCompatActivity {

    public static TextView stationNameView;
    public static TextView titleNameView;

    public static ImageView  musicArtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicpic);

        Bundle data = getIntent().getBundleExtra("data");//从bundle中取出数据
        String stationName = data.getString("stationName");
        String musicTitle = data.getString("title");
        stationNameView = findViewById(R.id.playerview_station_name);
        stationNameView.setText(stationName);
        musicArtView = findViewById(R.id.artist_pic);
        titleNameView = findViewById(R.id.playerview_titlename);
        titleNameView.setText(musicTitle);
        musicArtView.setImageResource(R.drawable.coverart);

        //获取图片url（去掉'['和']'）
        //String url = "https://ia601506.us.archive.org/27/items/mbid-eb42fdd9-e4c3-41e3-9ca9-42ee1c04b3d5/mbid-eb42fdd9-e4c3-41e3-9ca9-42ee1c04b3d5-32875917663.jpg";
        //String url = "https://ia800704.us.archive.org/16/items/mbid-a2d12ee8-9aeb-4d91-bfab-5c21f7a577fc/mbid-a2d12ee8-9aeb-4d91-bfab-5c21f7a577fc-13359884885_thumb250.jpg";
        String newPicUrl = getPicUrlByTitle(musicTitle.trim());
        if(newPicUrl.length()>10) {
            Glide.with(this).load(newPicUrl).into(musicArtView);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MetaMessage event) {
        // Do something
        titleNameView.setText(event.message);
        String newPicUrl = getPicUrlByTitle(event.message.trim());
        if(newPicUrl.length()>10){
            Glide.with(this).load(newPicUrl).into(musicArtView);
        }else {
            musicArtView.setImageResource(R.drawable.coverart);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public String getPicUrlByTitle(String tt){
        // 解析名称和作者
        String returl = "";
        String artname = "";
        String musicName = "";
        String[] tmpData = tt.split("-");
        String queryUrl = "";
        if(tmpData.length ==2){
            artname = tmpData[0].trim();
            musicName = tmpData[1].trim();
            queryUrl = "https://musicbrainz.org/ws/2/recording/?&fmt=json&query=artist:"+artname+"+recording:"+musicName;//recording:"+musicName+"";
        }else {
            musicName = tt.trim();
            queryUrl = "https://musicbrainz.org/ws/2/recording/?&fmt=json&query=recording:"+musicName;
        }
        Log.e("show url", "getPicUrlByTitle: "+queryUrl );
        String res = getStringFromurl(queryUrl);
        String releaseid="";
        if(res.length()>10){
            try {
                JSONObject jsono = new JSONObject(res);
                JSONArray jarray = jsono.getJSONArray("recordings");
                releaseid=jarray.getJSONObject(0).getJSONArray("releases").getJSONObject(0).getString("id");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(releaseid.length()>10){
            String reUrl = "http://coverartarchive.org/release/"+releaseid+"?fmt=json";
            Log.e("show url", "getPicUrlByTitle: "+queryUrl );

            res = getStringFromurl(reUrl);
        }
        if(res.length()>10){
            try {
                JSONObject jsono = new JSONObject(res);
                JSONArray jarray = jsono.getJSONArray("images");
                returl=jarray.getJSONObject(0).getJSONObject("thumbnails").getString("small");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return returl;
    }


    public String getStringFromurl(String jsonURL){
        String[] ret = {""};
        try {
            Thread thread1 = new Thread(new Runnable(){
                public void run(){
                    try {

                        URL url = new URL(jsonURL);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.connect();
                        InputStream inputStream = urlConnection.getInputStream();

                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                        StringBuffer stringBuffer = new StringBuffer();

                        String line;

                        while ((line = bufferedReader.readLine()) != null)
                        {
                            stringBuffer.append(line).append("\n");
                        }

                        if (stringBuffer.length() == 0)
                        {
                            return ;
                        }
                        ret[0] = stringBuffer.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread1.start();
            thread1.join();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ret[0];
    }
}