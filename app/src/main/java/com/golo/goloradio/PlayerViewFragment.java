package com.golo.goloradio;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.Player;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerViewFragment extends Fragment {

    private static final String TAG = "播放图片界面";
    private View playerPicView;

    public TextView stationTextView;
    public static MarqueeText musicTitleTextView;
    public static String LoadingPicName = ""; //需要展示的图片
    public static String LoadedUrl = "";
    public static ImageView musicArtView;

    public PlayingInfo playingInfo;
    private boolean downloadLock = false;
    private static PlayerViewFragment instance = null;

    public PlayerViewFragment() {
        // Required empty public constructor
    }

    public static PlayerViewFragment getInstance(){
        if (instance == null) {
            instance = new PlayerViewFragment();
        }
        return instance;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayerViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayerViewFragment newInstance(String param1, String param2) {
        PlayerViewFragment fragment = new PlayerViewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(playingInfo == null){
            playingInfo = (PlayingInfo) getActivity().getApplication();
        }
        if(playerPicView == null){
            playerPicView = inflater.inflate(R.layout.fragment_player_view, container, false);
            stationTextView = playerPicView.findViewById(R.id.playerview_station_name);
            stationTextView.setText(playingInfo.playingStationName);
            musicTitleTextView = playerPicView.findViewById(R.id.playerview_titlename);
            musicTitleTextView.setText(playingInfo.playingMusictile);
            musicArtView = playerPicView.findViewById(R.id.artist_pic);
            musicArtView.setImageResource(R.drawable.coverart);
        }
        return playerPicView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        playerPicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do your operation here
                // this will be called whenever user click anywhere in Fragment
                if(MainActivity.mediaPlayer.isPlaying()){
                    MainActivity.mediaPlayer.pause();
                    stationTextView.setText(playingInfo.playingStationName+" ▶");
                    //titleNameView.setText(MainActivity.currentMusicName+" ▶ ⏸");
                }else {
                    stationTextView.setText(playingInfo.playingStationName);
                    MainActivity.mediaPlayer.play();
                }
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MetaMessage event) {
        // Do something
        if(event.type == MessageType.META_CHANGE){
            Log.e("播放界面", "onMessageEvent: 取得meta消息 "+ event.message );
            setMusicTitle();
            setPicimage(event.message);
        }else if(event.type == MessageType.PLAYING_STATE_CHANGE){
            Log.e("播放界面", "onMessageEvent: 取得 status 消息 "+ event.play_state );
            setStationInfo();
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

    private class PicLoadTask extends AsyncTask<String, Integer, String>
    {
        //onPreExecute方法在execute()后执行
        @Override
        protected void onPreExecute()
        {
            Log.i("PicUrlTask", "onPreExecute() enter");
            musicArtView.setImageResource(R.drawable.coverart);
        }

        @Override
        protected String doInBackground(String... params)
        {
            LoadingPicName = params[0].replaceAll("\\p{C}", "");;
            if(downloadLock){
                return "";
            }
            downloadLock = true;
            return Func.getPicUrlByTitle(params[0]);
        }
        @Override
        protected void onPostExecute(String result)
        {
            LoadedUrl = result;
            //Log.e(TAG, "onPostExecute: LoadingPicName"+LoadingPicName );
            if(PlayerViewFragment.this.isVisible() ){
                if(result.length()>5){
                    Glide.with(PlayerViewFragment.this.getContext()).load(result).into(musicArtView);
                }
            }
            downloadLock = false;
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        downloadLock = false;
        playingInfo.isShowingPic = true;
        //Log.e("resume ", "onResume: old name  " + LoadingPicName +" current "+playingInfo.playingMusictile);
        if(!MainActivity.mediaPlayer.isPlaying()){
            stationTextView.setText(playingInfo.playingStationName+" ▶");
        }else {
            setStationInfo();
        }
        setMusicTitle();
        if(!LoadingPicName.equals(playingInfo.playingMusictile)) {
            setPicimage(playingInfo.playingMusictile);
        }
    }

    public void setStationInfo(){
        switch(playingInfo.playingStatus){
            case Player.STATE_BUFFERING:
                stationTextView.setText(playingInfo.playingStationName+" ...");
                break;
            case Player.STATE_IDLE:
                // 尝试重新播放
                stationTextView.setText(playingInfo.playingStationName+" ▶");
                break;
            default:
                stationTextView.setText(playingInfo.playingStationName);
                break;
        }
    }
    public void setMusicTitle(){
        musicTitleTextView.setText(playingInfo.playingMusictile);
    }

    public void setPicimage(String  newtitle){
        //Log.e(TAG, "setPicimage:  旧名称 链接"+LoadingPicName + "   " +LoadedUrl + "新名称:" +  newtitle );
        // 加载优先级判定
        if(newtitle.contains("音乐")|| newtitle.contains("台标") || newtitle.contains("Asia")){
            musicArtView.setImageResource(R.drawable.coverart);
            return;
        }
        if(!newtitle.equals(LoadingPicName) ) {
            PicLoadTask newt = new PicLoadTask();
            newt.execute(newtitle);
            return;
        }
        if(newtitle.equals(LoadingPicName) && LoadedUrl.length()>5) { //已经加载过
            //Log.e(TAG, "图片已经加载 名称 " +newtitle );
            Glide.with(PlayerViewFragment.this.getContext()).load(LoadedUrl).into(musicArtView);
            return;
        }
    }
}