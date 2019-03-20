package com.huawei;

import java.util.TreeMap;

public class Lane {

    private TreeMap<Integer, Car> carMap = new TreeMap<>();
    private int s1 = -1;
    private int id;

    public TreeMap<Integer,Car> getCarMap(){
        return carMap;
    }

    public int getS1(){
        return s1;
    }
    public int getId(){
        return id;
    }
    public void setS1(int s1){
        this.s1 = s1;
    }
    public void setId(int id){
        this.id = id;
    }

}
