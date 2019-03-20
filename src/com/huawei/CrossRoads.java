package com.huawei;

import java.util.ArrayList;
import java.util.HashMap;

public class CrossRoads {
    private int id;

    private HashMap<Integer,RoadPosition> roadDirection = new HashMap<>();

    public enum Turn{
        LEFT,RIGHT,STRAIGHT
    }

    public enum RoadPosition{
        NORTH,WEST,SOUTH,EAST,
    }

    public CrossRoads(int id) {
        this.id = id;
    }

    public CrossRoads(String line) {
        String[] vars = line.split(",");
        this.id = Integer.parseInt(vars[0]);
        //路口顺序为 顺时针方向，第一个是北方。
        roadDirection.put(Integer.parseInt(vars[1]),RoadPosition.NORTH);
        roadDirection.put(Integer.parseInt(vars[2]),RoadPosition.WEST);
        roadDirection.put(Integer.parseInt(vars[3]),RoadPosition.SOUTH);
        roadDirection.put(Integer.parseInt(vars[4]),RoadPosition.EAST);
    }

    public Integer[] getRoadIds() {
        return (Integer[])roadDirection.keySet().toArray();
    }

    public HashMap<Integer, RoadPosition> getRoadDirection() {
        return roadDirection;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
