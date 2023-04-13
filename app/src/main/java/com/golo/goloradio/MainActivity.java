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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;
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

    public PlayingInfo playingInfo;
    private static PlayerViewFragment playerViewFragment;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private String playerFmTag = "playerfragtag";
    private static MainActivity activity;

    private static final String BUNDLE_FRAGMENTS_KEY = "android:support:fragments";


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
        activity = this;
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
        playingInfo = (PlayingInfo) getApplication();
        if (savedInstanceState == null) {
            RadioListFragment rootListFG = new RadioListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, rootListFG)
                    .commit();
        }

        mediaPlayer.addListener(new Player.Listener() {
            public void onMediaMetadataChanged(MediaMetadata mediaMetadata){
                if (mediaMetadata.title != null && mediaMetadata.title.length()>2) {
                    String newtitle = mediaMetadata.title.toString();
                    playingInfo.playingMusictile = newtitle;
                    if(playerViewFragment == null){
                        playerViewFragment = PlayerViewFragment.getInstance();
                    }
                    if(!playerViewFragment.isVisible()){
                        switchFragment(playerViewFragment,playerFmTag);
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
    private void switchFragment(Fragment targetFragment,String fmtag) {
        //已经显示就不切换

        FragmentManager fm = activity.getSupportFragmentManager();
        Log.e("get stack size", " before switchFragment: "+fm.getBackStackEntryCount());
        //fm.clearBackStack(null);
        //Log.e("get stack size", " afet clear switchFragment: "+fm.getBackStackEntryCount());
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container_view,targetFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        Log.e("get stack size", " after commit: "+fm.getBackStackEntryCount());

    }

    protected boolean clearFragmentsTag() {
        return true;
    }

    @Override
    protected void onResume() {
        Log.e("Mainactivity", "onResume: " );
        super.onResume();
    }


    @Override
    protected void onStop() {
        Log.e("Mainactivity", "onStop: " );
        //this.getClass().getPackageName();
        super.onStop();
    }
}

