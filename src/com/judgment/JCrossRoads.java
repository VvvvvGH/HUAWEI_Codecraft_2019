package com.judgment;

import com.huawei.CrossRoads;

import java.util.ArrayList;

public class JCrossRoads extends CrossRoads implements Comparable<JCrossRoads>{

    private ArrayList<JRoad> roadList = new ArrayList<>();

    public JCrossRoads(String line){
        super(line);
    }

    @Override
    public int compareTo(JCrossRoads c) {
        return this.getId() - c.getId();
    }

    // 调度四条道路
    public void schedule(){

    }
}
