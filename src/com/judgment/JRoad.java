package com.judgment;

import com.huawei.Road;

import java.util.*;

public class JRoad extends Road implements Comparable<JRoad> {

    private Lane[] lanes;

    private PriorityQueue<JCar> waitingQueue = new PriorityQueue<>(carComparator);

    public static Comparator<JCar> carComparator = new Comparator<JCar>() {
        @Override
        public int compare(JCar o1, JCar o2) {
            if (o1.getPosition() > o2.getPosition())
                return 1;
            else if (o1.getPosition() == o2.getPosition() && o1.getLaneId() < o2.getLaneId())
                return 1;
            else return -1;
        }
    };

    public JRoad(String line) {
        super(line);
        lanes = new Lane[this.getNumOfLanes()];
        // 从１开始
        for (int i = 1; i <= lanes.length; i++) {
            lanes[i - 1].setS1(this.getTopSpeed());
            lanes[i - 1].setId(i);
        }
    }

    public boolean setCar(JCar car) {
        /*for(int i = 0; i < laneMap.size() ; i++){
            ArrayList<JCar> paneCars = laneMap.get(i);
            JCar frontCar = paneCars.get(paneCars.size() - 1);
            if(paneCars.size()==0 || frontCar.getPosition() > 0) {
                int speed = car.getCurrentSpeed() - frontCar.getCurrentSpeed() >0 ? frontCar.getCurrentSpeed() : car.getCurrentSpeed();
                car.setCurrentSpeed(speed);
                paneCars.add(car);
                return true;
            }
        }*/
        return false;
    }

    public void moveCars() {
        for (Lane lane : lanes) {
            TreeMap<Integer, JCar> carMap = lane.getCarMap();
            int s1 = lane.getS1();   // 当前路段的最大行驶距离或者车子与前车之间的最大可行驶距离
            for (Integer position : carMap.descendingKeySet()) {
                JCar car = carMap.get(position);
                int sv1 = car.getCurrentSpeed(); // 当前车速在当前道路的最大行驶距离
                Integer higher = carMap.descendingKeySet().higher(position);
                if (higher != null) { // 前方有车
                    int dis = -car.getPosition() - carMap.get(higher).getPosition();
                    if (sv1 <= dis) {
                        car.setPosition(sv1 + car.getPosition());
                        car.setState(CarState.END);
                    } else {
                        car.setPosition(dis + car.getPosition());
                        car.setState(carMap.get(higher).getState());
                    }
                } else {
                    if (sv1 <= this.getLen() - position) {
                        car.setPosition(sv1 + car.getPosition());
                        car.setState(CarState.END);
                    } else { // 可以出路口
                        car.setPosition(this.getLen());
                        car.setState(CarState.WAIT);
                        car.setPassedLengthBeforeCross(this.getLen() - position);
                    }
                }
                carMap.put(car.getPosition(), car);
                carMap.remove(position);
            }
        }
    }

    @Override
    public int compareTo(JRoad r) {
        return this.getId() - r.getId();
    }
}
