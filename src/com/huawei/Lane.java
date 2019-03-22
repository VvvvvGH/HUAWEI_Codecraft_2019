package com.huawei;

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
        return true;
    }

    public int getFrontCarPosition(int position) {
        // 若没有前车,返回-1
        Integer front = carMap.descendingKeySet().lower(position);
        if (front != null)
            return front;
        return -1;
    }

    public boolean isFull() {
        return carMap.size() == length;
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

}
