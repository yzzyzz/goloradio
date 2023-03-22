package com.golo.goloradio;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String downdir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
        //String StringFileContent=new FileInputStream(downdir+"/aa.m3u").toString();
        Button currentSelectItem = findViewById(R.id.button_zgzs);
        String currentUrl = "http://ngcdn016.cnr.cn/live/gsgljtgb/index.m3u8?";
        currentSelectItem.setOnClickListener(new PlayM3uRadio(currentUrl));

    }

    static class PlayM3uRadio implements View.OnClickListener{
        private String playUrl ;
        MediaPlayer mediaPlayer = new MediaPlayer();
        public PlayM3uRadio(String url){
            this.playUrl = url;
            Log.i("url", "PlayM3uRadio: "+this.playUrl);
        }
        @Override
        public void onClick(View view){

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                mediaPlayer.reset();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(this.playUrl);
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(50,50);


                //mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public boolean isPaused() {
            return !mediaPlayer.isPlaying();
        }
    }

}

