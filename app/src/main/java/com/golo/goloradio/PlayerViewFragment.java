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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerViewFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View playerPicView;

    public TextView stationTextView;
    public static MarqueeText musicTitleTextView;
    public static String LoadingPicName = ""; //需要展示的图片
    public static ImageView musicArtView;

    private boolean downloadLock = false;

    public PlayerViewFragment() {
        // Required empty public constructor
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
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(playerPicView == null){
            playerPicView = inflater.inflate(R.layout.fragment_player_view, container, false);
        }
        stationTextView = playerPicView.findViewById(R.id.playerview_station_name);
        stationTextView.setText(MainActivity.playingInfo.playingStationName);
        musicTitleTextView = playerPicView.findViewById(R.id.playerview_titlename);
        musicTitleTextView.setText(MainActivity.playingInfo.playingMusictile);
        musicArtView = playerPicView.findViewById(R.id.artist_pic);
        musicArtView.setImageResource(R.drawable.coverart);


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
                    //titleNameView.setText(MainActivity.currentMusicName+" ▶ ⏸");
                    musicTitleTextView.setText(MainActivity.playingInfo.playingMusictile+" ▶");
                }else {
                    MainActivity.mediaPlayer.play();
                    musicTitleTextView.setText(MainActivity.playingInfo.playingMusictile);
                }
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MetaMessage event) {
        // Do something
        if(event.type == MessageType.META_CHANGE){
            musicTitleTextView.setText(event.message);
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
            if(LoadingPicName.contains("音乐") || LoadingPicName.contains("台标")){return "";}
            if(downloadLock){
                return "";
            }
            downloadLock = true;
            return Func.getPicUrlByTitle(params[0]);
        }
        @Override
        protected void onPostExecute(String result)
        {
            Log.i("onPostExecute", "onPostExecute(Result result) called");
            if(PlayerViewFragment.this.isVisible() ){
                if(result.length()>5){
                    Glide.with(PlayerViewFragment.this.getContext()).load(result).into(musicArtView);
                }else {
                    musicArtView.setImageResource(R.drawable.coverart);
                }
            }

            downloadLock = false;
        }
    }

    @Override
    public void onResume(){
        downloadLock = false;
        MainActivity.playingInfo.isShowingPic = true;
        Log.e("resume ", "onResume: 2 name  " + LoadingPicName +" current "+MainActivity.playingInfo.playingMusictile);
        if(LoadingPicName != MainActivity.playingInfo.playingMusictile) {
            musicTitleTextView.setText(MainActivity.playingInfo.playingMusictile);
            PicLoadTask newt = new PicLoadTask();
            newt.execute(MainActivity.playingInfo.playingMusictile);
            //LoadingPicName = MainActivity.playingInfo.playingMusictile;
        }
        super.onResume();
    }
}