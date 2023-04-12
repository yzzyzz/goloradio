package com.golo.goloradio;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;


public class MainActivity extends AppCompatActivity {
    public static ExoPlayer mediaPlayer;
    public static String currentMusicName = ""; // 用于activity 之间传递信息
    public static PlayingInfo playingInfo;
    private Fragment currentFragment;
    private PlayerViewFragment playerViewFragment;
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

        if(mediaPlayer == null){
            mediaPlayer = new ExoPlayer.Builder(this.getApplication()).build();
        }
        playingInfo = new PlayingInfo();
        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putInt("some_int", 0);
            RadioListFragment rootListFG = new RadioListFragment();
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, rootListFG)
                    .commit();
            currentFragment = rootListFG;
        }


        mediaPlayer.addListener(new Player.Listener() {
            public void onMediaMetadataChanged(MediaMetadata mediaMetadata){
                if (mediaMetadata.title != null && mediaMetadata.title.length()>2) {
                    String newtitle = mediaMetadata.title.toString();
                    playingInfo.playingMusictile = newtitle;
                    if(playerViewFragment == null){
                        Log.e("fragment is nukk need new", "onMediaMetadataChanged: " );
                        playerViewFragment = new PlayerViewFragment();
                    }
                    if(!playerViewFragment.isVisible()){
                        switchFragment(playerViewFragment);
                    }
                    EventBus.getDefault().post(new MetaMessage(MessageType.META_CHANGE,newtitle));
                }
            }
            public void onPlaybackStateChanged( int playbackState) {
                EventBus.getDefault().post(new MetaMessage(MessageType.PLAYING_STATE_CHANGE,playbackState));
            }
        });


    }

    //正确的做法,切换fragment
    private void switchFragment(Fragment targetFragment) {
        //已经显示就不切换
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.fragment_container_view,targetFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        /*
        //没有添加则先完成添加再显示
        if (!targetFragment.isAdded()) {
            transaction
                    .hide(currentFragment)
                    .add(R.id.fragment_container_view, targetFragment)
                    .commit();
            Log.i("TAG", "第一次添加 ");

        } else {//都添加了就直接隐藏当前fragment，显示目标fragment

            transaction
                    .hide(currentFragment)
                    .show(targetFragment)
                    .commit();
            Log.i("TAG","完成切换");
        }
        currentFragment = targetFragment;
         */
    }



    public class PlayingInfo{
        public int playingId;
        public String playingStationName;
        public String playingMusictile;
        public String ShowMQName;
        public int playingStatus;
        public boolean isShowingPic;

        public void PlayingInfo(){
            this.isShowingPic = false;
            this.playingId = -1;
            this.playingStationName = "";
            this.playingMusictile = "";
            this.ShowMQName = "";
            this.playingStatus = 0;
        }
    }
}

