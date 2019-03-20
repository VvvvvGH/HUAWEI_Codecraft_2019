package com.judgment;

import com.huawei.Car;

import java.util.Comparator;

public class JCar extends Car {

    private CarState state;
    private int currentSpeed;
    private int position = -1;
    private int laneId =-1;

    public JCar(String line){
        super(line);
    }


    public static Comparator<JCar> idComparator = Comparator.comparing(JCar::getId);

    public static Comparator<JCar> crossDisComparator = (o1,o2) -> o2.getPosition() - o1.getPosition();

    public static Comparator<JCar> startTimeComparator = (o1,o2) -> o2.getStartTime() - o1.getStartTime();

    public void setState(CarState state) {
        this.state = state;
    }

    @Override
    public void setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public CarState getState() {
        return state;
    }

    @Override
    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}
