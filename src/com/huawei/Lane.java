package com.huawei;

import java.util.ArrayList;
import java.util.TreeMap;

public class Lane {

    private TreeMap<Integer, Car> carMap = new TreeMap<>();
    private int id;
    private int length;

    public Lane(int id, int length) {
        this.id = id;
        this.length = length;
    }

    public TreeMap<Integer, Car> getCarMap() {
        return carMap;
    }

    public Car getCar(int position) {
        return carMap.get(position);
    }

    public boolean putCar(Car car, int position) {
        if (position > length) {
            System.err.println("Lane#putCar#error: Car out of position.");
            return false;
        }
        carMap.put(position, car);

        //将车放入车道时设置lane id
        car.setLaneId(getId());
        car.setPosition(position);
        return true;
    }

    public boolean removeCar(int position) {
        if (carMap.get(position) == null)
            return false;
        carMap.remove(position);
        return true;
    }

    public boolean updateCar(Car car, int oldPosition, int newPosition) {
        if (carMap.get(newPosition) != null && oldPosition != newPosition) {
            System.err.println("Lane#updateCar#error: Override another car");
            return false;
        }
        if (removeCar(oldPosition)) {
            return putCar(car, newPosition);
        }
        return false;
    }

    public int getFrontCarPosition(int position) {
        // 若没有前车,返回-1
        Integer front = carMap.descendingKeySet().lower(position);
        if (front != null)
            return front;
        return -1;
    }

    public ArrayList<Integer> getDescendingPositionList() {
        return new ArrayList<Integer>(carMap.descendingKeySet());
    }

    public boolean hasPosition() {
        return carMap.get(1)==null;
    }

    public boolean isEmpty() {
        return carMap.size() == 0;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public Lane setLength(int length) {
        this.length = length;
        return this;
    }
}
