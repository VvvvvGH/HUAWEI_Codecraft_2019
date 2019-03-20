package com.judgment;

import com.huawei.CrossRoads;
import com.sun.deploy.config.JCPConfig;

import java.util.TreeMap;

public class JCrossRoads extends CrossRoads implements Comparable<JCrossRoads> {

    private TreeMap<Integer, JRoad> roadTreeMap = new TreeMap<>();

    public JCrossRoads(String line) {
        super(line);
    }

    @Override
    public int compareTo(JCrossRoads c) {
        return this.getId() - c.getId();
    }

    // TODO: 调度路口
    public void schedule() {
//        foreach(roads){
//            Direction dir = getDirection();
//            Car car = getCarFromRoad(road, dir);
//            if (conflict){
//                break;
//            }
//
//            channle = car.getChannel();
//            car.moveToNextRoad();
//
//            /* driveAllCarJustOnRoadToEndState该处理内的算法与性能自行考虑 */
//            driveAllCarJustOnRoadToEndState(channel);
//        }
        for (int roadId : roadTreeMap.keySet()) {
            JRoad road = roadTreeMap.get(roadId);
            JCar car = road.getWaitingQueue(getId()).peek();

            int roadIdx = car.getPath().lastIndexOf(road.getId());
            int from = car.getPath().get(roadIdx);
            // TODO: 移除waiting queue 里面的车
            if (roadIdx==car.getPath().size()-1){
                // 车到达目的地
                road.removeCarFromRoad(car);
            }else {
                // 车没有到达目的地
                // TODO: 判断车是否能过路口
                //       车过路口是否成功
                //  TODO: next road.setCar()

            }
            // 移车
            road.moveCarsOnRoad(car.getLaneId());
            int to = car.getPath().get(roadIdx + 1);


        }

    }

    public void addRoads(TreeMap<Integer, JRoad> roadMap) {
        //添加道路到路口
        for (int roadId : getRoadIds()) {
            if (roadId != -1) {
                roadTreeMap.put(roadId, roadMap.get(roadId));
            }
        }
    }

    public Turn findDirection(int from, int to) {
        // TODO: direction
//        int val = getRoadDirection().get(from).ordinal() - getRoadDirection().get(to).ordinal();
//        if (Math.abs(val) == 2)
//            return Turn.STRAIGHT;
//        if (val == -1)
//            return Turn.LEFT;
//        if (val == 1)
//            return Turn.RIGHT;
//        return Turn.RIGHT;

    }
}
