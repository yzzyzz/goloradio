package com.golo.goloradio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;




public class musicpic extends AppCompatActivity {
    public static TextView stationNameView;
    public static MarqueeText titleNameView;
    public static String LoadingPicName = ""; //需要展示的图片
    public static ImageView  musicArtView;

    public static boolean downloadLock = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicpic);
        MainActivity.isPhowPic = true;
        Bundle data = getIntent().getBundleExtra("data");//从bundle中取出数据
        String stationName = data.getString("stationName");
        String musicTitle = data.getString("title");
        //musicTitle = "苏芮 - 亲爱的小孩";
        stationNameView = findViewById(R.id.playerview_station_name);
        stationNameView.setText(stationName);
        musicArtView = findViewById(R.id.artist_pic);
        titleNameView = findViewById(R.id.playerview_titlename);
        titleNameView.setText(musicTitle);
        musicArtView.setImageResource(R.drawable.coverart);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MetaMessage event) {
        // Do something
        titleNameView.setText(event.message);
        Log.e("get message", "onMessageEvent: "+event.message);
        if(LoadingPicName!= event.message) {
            LoadingPicName = event.message;
            PicLoadTask newt = new PicLoadTask();
            newt.execute(event.message);
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
        String newtt = java.net.URLEncoder.encode(tt);
        String queryUrl = "http://gz.999887.xyz/getmusicpic.php?title="+newtt;
        Log.e("show url", "getPicUrlByTitle: "+queryUrl );
        String res = getStringFromurl(queryUrl);
        String releaseid="";
        if(res.length()>10){
            try {
                JSONObject jsono = new JSONObject(res);
                returl = jsono.getString("picurl");
                return returl;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        downloadLock = false;
        return returl;
    }

    public static String getStringFromurl(String jsonURL){
        String ret = "";
        try {
            URL url = new URL(jsonURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(10000);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");

            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();

            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuffer.append(line).append("\n");
            }

            if (stringBuffer.length() == 0)
            {
                return "" ;
            }
            ret= stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    @Override
    protected void onPause() {
        downloadLock = false;
        super.onPause();
    }
    @Override
    protected void onResume(){
        downloadLock = false;
        MainActivity.isPhowPic = true;
        Log.e("resume ", "onResume: 2 name  " + LoadingPicName +" current "+MainActivity.currentMusicName);
        if(LoadingPicName != MainActivity.currentMusicName) {
            titleNameView.setText(MainActivity.currentMusicName);
            PicLoadTask newt = new PicLoadTask();
            newt.execute(MainActivity.currentMusicName);
        }
        super.onResume();
    }


    private class PicLoadTask extends AsyncTask<String, Integer, String>
    {
        //onPreExecute方法在execute()后执行
        @Override
        protected void onPreExecute()
        {
            Log.i("PicUrlTask", "onPreExecute() enter");
        }

        @Override
        protected String doInBackground(String... params)
        {
            LoadingPicName = params[0];
            if(LoadingPicName.contains("音乐")){return "";}
            if(downloadLock){
                return "";
            }
            downloadLock = true;
            return getPicUrlByTitle(params[0]);
        }
        @Override
        protected void onPostExecute(String result)
        {
            downloadLock = false;
            Log.i("onPostExecute", "onPostExecute(Result result) called");
            if(result.length()>5){
                Glide.with(musicpic.this).load(result).into(musicArtView);
            }else {
                musicArtView.setImageResource(R.drawable.coverart);
            }
            downloadLock = false;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 4){
            MainActivity.isPhowPic = false;
        } else if (keyCode == 66) {
            if(MainActivity.mediaPlayer.isPlaying()){
                MainActivity.mediaPlayer.pause();
                //titleNameView.setText(MainActivity.currentMusicName+" ▶ ⏸");
                titleNameView.setText(MainActivity.currentMusicName+" ▶");
            }else {
                MainActivity.mediaPlayer.play();
                titleNameView.setText(MainActivity.currentMusicName);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}