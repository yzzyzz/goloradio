package com.golo.goloradio;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static String[] reqCate = {"我的最爱","音乐电台","综合资讯","文化曲艺"};
    public static String playingStationName;
    public static MarqueeText playingBar;
    public static TextView playStateBar;
    private View root;

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
        ExoPlayer mediaPlayer = MainActivity.mediaPlayer;

        if(root == null){
            isFirstLoad = true;
            root = inflater.inflate(R.layout.fragment_radio_list, container, false);
            playingBar =  root.findViewById(R.id.playing_info);
            playingBar.setText("无");
            playStateBar = root.findViewById(R.id.playing_state);
            if(MainActivity.playingInfo.playingStationName!=null && playingStationName.length()>2){
                playingBar.setText(playingStationName);
            }else {
                playingBar.setText("无");
                playingStationName = "";
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
            List<String> expandableListTitle = new LinkedList<String>();
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

                    if(intToPlayId == MainActivity.playingInfo.playingId){
                        // 暂停播放
                        MainActivity.playingInfo.playingId = -1;
                        mediaPlayer.stop();
                        MainActivity.playingInfo.playingStationName = "";
                        MainActivity.playingInfo.isShowingPic = false;
                        return true;
                    }

                    try {
                        MainActivity.playingInfo.playingId =  intToPlayId;
                        MainActivity.playingInfo.playingStationName = playingStationName;
                        MainActivity.playingInfo.playingMusictile = "曲目";
                        mediaPlayer.stop();
                        if(!mediaPlayer.isPlaying()){
                            mediaPlayer.setMediaItem(MediaItem.fromUri(expandableListDetail.get(
                                    expandableListTitle.get(groupPosition)).get(
                                    childPosition).url));
                            mediaPlayer.prepare();
                            mediaPlayer.setPlayWhenReady(true);
                            playingBar.setText(playingStationName);
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
        return root;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MetaMessage event) {

        switch (event.type){
            case PLAYING_STATE_CHANGE:
                switch(event.play_state){
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
            case META_CHANGE:
                playingBar.setText(event.message);
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

}