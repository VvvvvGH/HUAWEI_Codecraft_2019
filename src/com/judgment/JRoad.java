package com.judgment;

import com.huawei.Road;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class JRoad extends Road implements Comparable<JRoad> {

    private HashMap<Lane, ArrayList<JCar>> laneMap = new HashMap<>();

    private PriorityQueue<JCar> waitQueue = new PriorityQueue<>();

    public JRoad(String line) {
        super(line);
    }

    public boolean setCar(JCar car) {
        for(int i = 0; i < laneMap.size() ; i++){
            ArrayList<JCar> paneCars = laneMap.get(i);
            JCar frontCar = paneCars.get(paneCars.size() - 1);
            if(paneCars.size()==0 || frontCar.getPassedLength() > 0) {
                int speed = car.getCurrentSpeed() - frontCar.getCurrentSpeed() >0 ? frontCar.getCurrentSpeed() : car.getCurrentSpeed();
                car.setCurrentSpeed(speed);
                paneCars.add(car);
                return true;
            }
        }
        return false;
    }

    public void moveCars() {
        for(Lane lane : laneMap.keySet()){
            // TODO
        }
    }


    public ArrayList<JCar> getCarOfPane(int i) {
        return laneMap.get(i);
    }

    @Override
    public int compareTo(JRoad r) {
        return this.getId() - r.getId();
    }
}
