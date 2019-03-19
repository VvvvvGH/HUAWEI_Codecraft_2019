package com.judgment;

import com.huawei.Car;

import java.util.ArrayList;
import java.util.Comparator;

public class JCar extends Car {

    private int state = -2; // 0:wait 1:move -1:end -2:lock　－３:final
    private int currentSpeed;
    private ArrayList<String> path = new ArrayList<>();
    private int passedLength = 0;

    public JCar(String line){
        super(line);
    }


    public static Comparator<JCar> IdComparator = new Comparator<JCar>() {
        @Override
        public int compare(JCar o1, JCar o2) {
            return o1.getId() - o2.getId();
        }
    };

    public static Comparator<JCar> CrossDisComparator = new Comparator<JCar>() {
        @Override
        public int compare(JCar o1, JCar o2) {
            return o2.getPassedLength() - o1.getPassedLength();
        }
    };

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public void setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public int getState() {
        return state;
    }

    @Override
    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public void setPassedLength(int passedLength) {
        this.passedLength = passedLength;
    }

    public int getPassedLength() {
        return passedLength;
    }
}
