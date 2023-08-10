package com.golo.goloradio;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.golo.goloradio.model.MetaMessage;
import com.golo.goloradio.model.PlayingInfo;
import com.golo.goloradio.model.RadioItem;
import com.golo.goloradio.utils.CustomExpandableListAdapter;
import com.golo.goloradio.utils.ExpandableListDataPump;
import com.golo.goloradio.utils.Func;
import com.golo.goloradio.utils.MarqueeText;
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

    private ExoPlayer mediaPlayer;
    public PlayingInfo playingInfo;
    private static String TAG = "列表界面";
    private static boolean isFirstLoad = true;

    LinearLayout playingbarView ;

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
        if(mediaPlayer ==null){
            mediaPlayer = MainActivity.mediaPlayer;
        }
        playingInfo = (PlayingInfo) getActivity().getApplication();
        if(root == null){
            isFirstLoad = true;
            root = inflater.inflate(R.layout.fragment_radio_list, container, false);

            root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            playingbarView = root.findViewById(R.id.playingbar);
            playingBar =  root.findViewById(R.id.playing_info);
            playStateBar = root.findViewById(R.id.playing_state);
            if(playingInfo.playingStationName.length()>1){
                playingBar.setText(playingInfo.playingStationName+" ");
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
                    if(intToPlayId == playingInfo.playingId && playingInfo.playingStatus!=Player.STATE_IDLE){
                        if(playingInfo.hasMeta){ // 有meta信息
                            switchFragment(PlayerViewFragment.getInstance());
                            return true;
                        }
                        // 暂停播放
                        stopPlayStation();
                        return true;
                    }
                    try {
                        stopPlayStation();
                        playingInfo.playingId =  intToPlayId;
                        playingInfo.playingStationName = playingStationName;
                        playingInfo.playingMusictile = "曲目";
                        playingInfo.playUrl =  expandableListDetail.get(
                                expandableListTitle.get(groupPosition)).get(
                                childPosition).url;

                        Log.e(TAG, "onChildClick: begin play url " + playingInfo.playUrl );
                        if(!mediaPlayer.isPlaying()){
                            startPlayStation();
                        }
                    } catch (Exception e) {
                        playingBar.setText("加载失败,请重试或更换！");
                        e.printStackTrace();
                    }
                    return false;
                }
            });

            playingbarView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View viewIn) {
                        try {
                            if(playingInfo.hasMeta && mediaPlayer.isPlaying()){
                                // 切换
                                switchFragment(PlayerViewFragment.getInstance());
                                return;
                            }
                            if(mediaPlayer.isPlaying()){
                                mediaPlayer.stop();
                                playStateBar.setText("暂停播放 - ");
                            }else {
                                if(playingInfo.playUrl.length()>5) {
                                   startPlayStation();
                                }
                            }
                        } catch (Exception except) {
                            Log.e(TAG,"Ooops GMAIL account selection problem "+except.getMessage());
                        }
                    }
                });

            isFirstLoad = false;
        }
        return root;
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MetaMessage event) {
        setTitle();
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

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume:  进入逻辑 status: "+playingInfo.playingStatus );
        setTitle();
    }

    private void setTitle(){
        Log.e(TAG, "setTitle: 设置title信息 status: "+playingInfo.playingStatus );
        switch(playingInfo.playingStatus){
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
        if(playingInfo.hasMeta){
            playingBar.setText(playingInfo.playingStationName+"_"+playingInfo.playingMusictile+" ");
        }else {
            playingBar.setText(playingInfo.playingStationName+" ");
        }
    }

    private void stopPlayStation(){
        mediaPlayer.stop();
        playingInfo.InitPlayingInfo();
        playStateBar.setText("停止播放 - ");
    }

    private void startPlayStation(){
        playStateBar.setText("正在加载 - ");
        playingBar.setText(playingInfo.playingStationName + " ");
        String ppurl = playingInfo.playUrl;
        if(ppurl.contains("mymusic.php")){
            playMusicList();
        }else {
            mediaPlayer.setMediaItem(MediaItem.fromUri(playingInfo.playUrl));
            mediaPlayer.prepare();
            mediaPlayer.setPlayWhenReady(true);
        }
    }

    //播放音乐列表
    private void playMusicList(){
        List musicUrlList = Func.getMusicListFromUrl(playingInfo.playUrl);
        mediaPlayer.clearMediaItems();
        for (int i =0 ;i<musicUrlList.size();i++) {
            String[] musicItem = (String[])musicUrlList.get(i);
            mediaPlayer.addMediaItem(MediaItem.fromUri(musicItem[1]));
        }
        mediaPlayer.prepare();
        mediaPlayer.setPlayWhenReady(true);
    }

    private void switchFragment(Fragment targetFragment) {
        //已经显示就不切换
        try {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container_view,targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}