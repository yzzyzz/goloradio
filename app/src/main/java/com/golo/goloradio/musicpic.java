package com.golo.goloradio;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;



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
        Log.e("onCreate", "onCreate:  before task pic" );

        /*
        if(LoadingPicName != musicTitle && !downloadLock){
            PicLoadTask taskPic = new PicLoadTask();
            taskPic.execute(musicTitle);
        }
         */
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
                Log.e("get release array", "getPicUrlByTitle: "+jarray.toString() );
                for(int i =0;i<jarray.length();i++){
                    releaseid=jarray.getJSONObject(i).getJSONArray("releases").getJSONObject(0).getString("id");
                    if(releaseid.length()>10){
                        String reUrl = "http://coverartarchive.org/release/"+releaseid+"?fmt=json";
                        Log.e("show url", "getPicUrlByTitle: "+reUrl );
                        res = getStringFromurl(reUrl);
                        Log.e("get data", "getStringFromurl done: "+res );
                        if(res.length()>10){
                            try {
                                JSONObject jsono2 = new JSONObject(res);
                                JSONArray jarray2 = jsono2.getJSONArray("images");
                                returl=jarray2.getJSONObject(0).getJSONObject("thumbnails").getString("small");
                                if(returl.length()>10){
                                    Log.e("task", "getPicUrlByTitle: final url "+reUrl );
                                    return returl;
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        downloadLock = false;
        return returl;
    }


    public String getStringFromurl(String jsonURL){
        String ret = "";
        try {

            URL url = new URL(jsonURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setDoOutput(false);
            urlConnection.connect();

            if (urlConnection.getResponseCode() != 200){
                Log.e("TASK", "getStringFromurl: rescode:"+urlConnection.getResponseCode());
            }
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
        MainActivity.isPhowPic = false;
        super.onPause();
    }
    @Override
    protected void onResume(){
        downloadLock = false;
        MainActivity.isPhowPic = true;
        if(LoadingPicName!= MainActivity.currentMusicName) {
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

        //doInBackground方法内部执行后台任务,不能在里面更新UI，否则有异常。
        @Override
        protected String doInBackground(String... params)
        {
            Log.i("PicUrlTask", "doInBackground() enter");
            if(downloadLock){
                Log.e("PicUrlTask", "doInBackground no lock!!!!");
                return "";
            }
            downloadLock = true;
            Log.i("PicUrlTask", "doInBackground(String... params) enter");
            LoadingPicName = params[0];
            return getPicUrlByTitle(params[0]);
        }
        //onPostExecute用于doInBackground执行完后，更新界面UI。
        //result是doInBackground返回的结果
        @Override
        protected void onPostExecute(String result)
        {
            downloadLock = false;
            Log.i("onPostExecute", "onPostExecute(Result result) called");
            //mShowLogTextView.setText("Down load finish result="+result);
            //mNetImageView.setImageBitmap(mDownLoadBtBitmap);
            if(result.length()>10){
                Glide.with(musicpic.this).load(result).into(musicArtView);
            }
            downloadLock = false;
        }
    }
}