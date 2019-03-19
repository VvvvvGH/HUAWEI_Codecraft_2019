package com.judgment;

import com.huawei.Road;

import java.util.ArrayList;
import java.util.HashMap;

public class JRoad extends Road implements Comparable<JRoad> {

    private HashMap<Integer, ArrayList<JCar>> paneMap = new HashMap<>();

    public JRoad(String line) {
        super(line);
    }

    public void setCar(JCar car, int pane, JCar frontCar) {
        int speed = car.getCurrentSpeed() - frontCar.getCurrentSpeed() >0 ? frontCar.getCurrentSpeed() : car.getCurrentSpeed();
        car.setCurrentSpeed(speed);
        paneMap.get(pane).add(car);
    }

    public void moveCar(JCar car, JCar frontCar) {
        int len = car.getCurrentSpeed();
        if(frontCar!=null && len > frontCar.getPassedLength() - car.getPassedLength()){
            car.setPassedLength(frontCar.getPassedLength() - 1);
            car.setState(0);
        }else if (len > this.getLen() - car.getPassedLength()) {
            car.setPassedLength(this.getLen());
            car.setState(0);
        } else{
            car.setPassedLength(car.getPassedLength() + len);
            car.setState(-1);
        }
    }

    public ArrayList<JCar> getCarOfOnePane(int i) {
        return paneMap.get(i);
    }

    @Override
    public int compareTo(JRoad r) {
        return this.getId() - r.getId();
    }
}
