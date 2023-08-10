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
import android.util.DisplayMetrics;
import android.util.Log;


import com.golo.goloradio.model.MessageType;
import com.golo.goloradio.model.MetaMessage;
import com.golo.goloradio.model.PlayingInfo;
import com.golo.goloradio.utils.Func;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static ExoPlayer mediaPlayer;

    public static PlayingInfo playingInfo;
    private static PlayerViewFragment playerViewFragment;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String TAG = "主界面 MainActivity";



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

        playingInfo = (PlayingInfo) getApplication();

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

            mediaPlayer.setTrackSelectionParameters(
                    mediaPlayer.getTrackSelectionParameters()
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, true)
                            .build());

            mediaPlayer.addListener(new Player.Listener() {
                public void onMediaMetadataChanged(MediaMetadata mediaMetadata){
                    if (mediaMetadata.title != null && mediaMetadata.title.length()>2) {
                        String newtitle = mediaMetadata.title.toString().replaceAll("\\p{C}", "");;
                        playingInfo.playingMusictile = newtitle;
                        playingInfo.hasMeta = true;
                        Log.e("发送消息", "onMediaMetadataChanged: 准备发送 newtitle:"+newtitle );
                        EventBus.getDefault().post(new MetaMessage(MessageType.META_CHANGE,newtitle));
                    }
                }
                public void onPlaybackStateChanged( int playbackState) {
                    //int tmpState = 1;
                    Log.e("发送消息", "onPlaybackStateChanged: 准备发送 playbackState:" + playbackState);
                    //状态变化
                    playingInfo.playingStatus = playbackState;
                    EventBus.getDefault().post(new MetaMessage(MessageType.PLAYING_STATE_CHANGE, playbackState));
                    // 如果播放url是音乐，则继续播放
                    if (playingInfo.playUrl.contains("mymusic.php")) {
                        if (playbackState == Player.STATE_ENDED) {
                            Log.e(TAG, "onMessageEvent: " + " 监测到结束播放 ");
                            playMusicList();
                        }
                    }
                }

                public void onPlayerError(PlaybackException error) {
                    //Log.e(TAG, "onPlayerError: "+"player erro kankan ++++++++++++++++="+error.errorCode );
                    if(error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ){
                        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
                        HlsMediaSource hlsMediaSource =
                                new HlsMediaSource.Factory(dataSourceFactory)
                                        .createMediaSource(MediaItem.fromUri(playingInfo.playUrl));
                        mediaPlayer.clearMediaItems();
                        mediaPlayer.setMediaSource(hlsMediaSource);
                        mediaPlayer.prepare();
                        mediaPlayer.setPlayWhenReady(true);
                    }
                }
            });
        }

        if (savedInstanceState == null) {

            // 设置是否大屏
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
//屏幕实际宽度（像素个数）
            int width = metrics.widthPixels;
            if(width>=720){
                Func.isBigScreen = true;
            }

            RadioListFragment rootListFG = new RadioListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, rootListFG)
                    .commit();
        }
    }

    //正确的做法,切换fragment
    private void switchFragment(Fragment targetFragment) {
        //已经显示就不切换
       // return;
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container_view,targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MetaMessage event) {
        // Do something
        if(event.type == MessageType.META_CHANGE){
            if(playerViewFragment == null){
                playerViewFragment = PlayerViewFragment.getInstance();
            }
            if(!playerViewFragment.isVisible()){
                Log.e("PlayerViewFragment is not isVisible", "be fore : switchFragment" );
                switchFragment(playerViewFragment);
            }
        }
    }


    @Override
    protected void onResume() {
        Log.e("Mainactivity", "onResume: " );
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onStop() {
        Log.e("Mainactivity", "onStop: " );
        //this.getClass().getPackageName();
        super.onStop();
        EventBus.getDefault().unregister(this);

    }

    //播放音乐列表
    public static void playMusicList(){
        List musicUrlList = Func.getMusicListFromUrl(playingInfo.playUrl);
        mediaPlayer.clearMediaItems();
        for (int i =0 ;i<musicUrlList.size();i++) {
            String[] musicItem = (String[])musicUrlList.get(i);
            Log.e(TAG, "playMusicList: add url:"+musicItem[1] );
            mediaPlayer.addMediaItem(MediaItem.fromUri(musicItem[1]));
        }
        mediaPlayer.prepare();
        mediaPlayer.setPlayWhenReady(true);
    }
}

