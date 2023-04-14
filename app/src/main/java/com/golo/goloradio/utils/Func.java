package com.golo.goloradio.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.golo.goloradio.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Func {

    public static List getUrlListFromRes(Context c){

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
                InputStream inputStream = c.getResources().openRawResource(R.raw.radio);
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

    public static String getPicUrlByTitle(String tt){
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


}
