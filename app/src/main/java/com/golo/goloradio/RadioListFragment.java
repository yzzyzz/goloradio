package com.golo.goloradio;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RadioListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RadioListFragment extends Fragment {

    public static String[] reqCate = {"我的最爱","音乐电台","综合资讯","文化曲艺"};
    public static String playingStationName;
    public static MarqueeText playingBar;
    public static TextView playStateBar;
    private View root;

    public PlayingInfo playingInfo;
    private static boolean isFirstLoad = true;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    public static HashMap<String, List<RadioItem>> expandableListDetail;


    public RadioListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RadioListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RadioListFragment newInstance(String param1, String param2) {
        RadioListFragment fragment = new RadioListFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ExoPlayer mediaPlayer = MainActivity.mediaPlayer;
        playingInfo = (PlayingInfo) getActivity().getApplication();


        if(root == null){
            isFirstLoad = true;
            root = inflater.inflate(R.layout.fragment_radio_list, container, false);
            playingBar =  root.findViewById(R.id.playing_info);
            playStateBar = root.findViewById(R.id.playing_state);

            if(playingInfo.playingStationName!=null && playingInfo.playingStationName.length()>4){
                playingBar.setText(playingInfo.playingStationName);
            }else {
                playingBar.setText("无");
            }
        }

        if(isFirstLoad){
            // 1、获取资源列表
            List playList = Func.getUrlListFromRes(getContext());
            ExpandableListDataPump expStationList = new ExpandableListDataPump();
            for(int i=0;i<playList.size();i++) {
                String[] datainfo = (String[]) playList.get(i);
                String groupName = "未分类";
                if(datainfo.length>=3){
                    groupName = datainfo[2];
                }
                expStationList.addStationItem(getContext(),datainfo[0],datainfo[1],groupName,i);
            }

            expandableListView = (ExpandableListView) root.findViewById(R.id.expandableListViewFragment);
            expandableListDetail = expStationList.getAllStationMap();
            expandableListView.setItemsCanFocus(true);


            List<String> allListTitle = new ArrayList<String>(expandableListDetail.keySet());

            // 排个序列
            expandableListTitle = new LinkedList<String>();
            for(int i =0;i<reqCate.length;i++){
                expandableListTitle.add(reqCate[i]);
                if(!allListTitle.contains(reqCate[i])){
                    Toast.makeText(getContext(),("!数据文件缺乏必须的分类:"+reqCate[i]), Toast.LENGTH_LONG).show();
                    return root;
                }
            }

            for(int i = 0;i< allListTitle.size();i++){
                if(!expandableListTitle.contains(allListTitle.get(i)) ){
                    expandableListTitle.add(allListTitle.get(i));
                }
            }
            expandableListAdapter = new CustomExpandableListAdapter(getContext(), expandableListTitle, expandableListDetail);
            expandableListView.setAdapter(expandableListAdapter);

            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {
                    int intToPlayId = expandableListDetail.get(
                            expandableListTitle.get(groupPosition)).get(
                            childPosition).id;
                    playingStationName = expandableListDetail.get(
                            expandableListTitle.get(groupPosition)).get(
                            childPosition).name;

                    if(intToPlayId == playingInfo.playingId){
                        // 暂停播放
                        playingInfo.playingId = -1;
                        mediaPlayer.stop();
                        playStateBar.setText("停止播放 - ");
                        playingInfo.isShowingPic = false;
                        return true;
                    }
                    try {
                        playingInfo.playingId =  intToPlayId;
                        playingInfo.playingStationName = playingStationName;
                        playingInfo.playingMusictile = "曲目";
                        mediaPlayer.stop();
                        if(!mediaPlayer.isPlaying()){
                            playStateBar.setText("正在加载 - ");
                            playingBar.setText(playingStationName);
                            mediaPlayer.setMediaItem(MediaItem.fromUri(expandableListDetail.get(
                                    expandableListTitle.get(groupPosition)).get(
                                    childPosition).url));
                            mediaPlayer.prepare();
                            mediaPlayer.setPlayWhenReady(true);
                        }
                    } catch (Exception e) {
                        playingBar.setText("加载失败,请重试或更换！");
                        e.printStackTrace();
                    }
                    return false;
                }
            });
            isFirstLoad = false;
        }
        if(mediaPlayer.isPlaying()){
            playStateBar.setText("正在播放 - ");
        }
        return root;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MetaMessage event) {
        switch (event.type){
            case PLAYING_STATE_CHANGE:
                setStateBar(event.play_state);
            case META_CHANGE:
                if(event.message.length()>2){
                    playingBar.setText(playingInfo.playingStationName+" _ "+event.message);
                }
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

    private void setStateBar(int state){
        switch(state){
            case Player.STATE_BUFFERING:
                playStateBar.setText("正在加载 - ");
                break;
            case Player.STATE_IDLE:
                // 尝试重新播放
                playStateBar.setText("停止播放 - ");
                break;
            default:
                playStateBar.setText("正在播放 - ");
                break;
        }
    }
    @Override
    public void onResume() {
        if(MainActivity.mediaPlayer.isPlaying()){
            setStateBar(playingInfo.playingStatus);
            if(playingInfo.playingMusictile.length()>2){
                playingBar.setText(playingInfo.playingStationName+"_"+playingInfo.playingMusictile);
            }else {
                playingBar.setText(playingInfo.playingStationName);
            }
        }
        super.onResume();
    }
}