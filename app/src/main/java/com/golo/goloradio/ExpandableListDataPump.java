package com.golo.goloradio;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ExpandableListDataPump {
    private  HashMap<String, List<RadioItem>> allStationMap = new HashMap<String, List<RadioItem>>();
    public HashMap<String, List<RadioItem>> getAllStationMap() {
        return this.allStationMap;
    }
    public void addStationItem(Context c,String name,String url,String groupname,int id){
        RadioItem newStation = new RadioItem(c);
        newStation.id = id;
        newStation.name = name;
        newStation.url = url;
        if (allStationMap.containsKey(groupname)){
            allStationMap.get(groupname).add(newStation);
        }else {
            List<RadioItem> newList = new  ArrayList<RadioItem>();
            newList.add(newStation);
            allStationMap.put(groupname, newList);
        }
    }
}
