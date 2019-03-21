package com.huawei;


import java.util.ArrayList;
import java.util.Comparator;

public class Car implements Comparable<Car> {
    private int id;
    private int from;
    private int to;
    private int topSpeed;
    private int planTime;

    private ArrayList<Integer> path = new ArrayList<>();

    private int currentSpeed = 0;
    private long startTime = -1;
    private long endTime = -1;
    private int laneId = -1;

    private CarState state;
    private int position = -1;


    public static Comparator<Car> idComparator = Comparator.comparing(Car::getId);


    public Car(int id, int start, int to, int topSpeed, int planTime) {
        this.id = id;
        this.from = start;
        this.to = to;
        this.topSpeed = topSpeed;
        this.planTime = planTime;
        this.state = CarState.IN_GARAGE;
    }

    public Car(String line) {
        String[] vars = line.split(",");
        this.id = Integer.parseInt(vars[0]);
        this.from = Integer.parseInt(vars[1]);
        this.to = Integer.parseInt(vars[2]);
        this.topSpeed = Integer.parseInt(vars[3]);
        this.planTime = Integer.parseInt(vars[4]);
        this.state = CarState.IN_GARAGE;
    }

    public void addPath(int roadId) {
        path.add(roadId);
    }

    public ArrayList<Integer> getPath() {
        return path;
    }

    public String outputResult() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        stringBuilder.append(this.id);
        stringBuilder.append(",");
        stringBuilder.append(this.startTime);
        stringBuilder.append(",");

        path.forEach(roadId -> {
            stringBuilder.append(roadId);
            stringBuilder.append(",");
        });

        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        stringBuilder.append(")");
        return stringBuilder.toString();
    }


    public int compareTo(Car car) {
        // 根据速度排序
        if (car.getPlanTime() / (car.getTopSpeed() * 1.0) > getPlanTime() / (getTopSpeed() * 1.0))
            return -1;
        else if (car.getPlanTime() / (car.getTopSpeed() * 1.0) < getPlanTime() / (getTopSpeed() * 1.0))
            return 1;
        else
            return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(int topSpeed) {
        this.topSpeed = topSpeed;
    }

    public int getPlanTime() {
        return planTime;
    }

    public void setPlanTime(int planTime) {
        this.planTime = planTime;
    }

    public void setPath(ArrayList<Integer> path) {
        this.path = path;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getLaneId() {
        return laneId;
    }

    public void setLaneId(int laneId) {
        this.laneId = laneId;
    }

    public static Comparator<Car> getIdComparator() {
        return idComparator;
    }

    public static void setIdComparator(Comparator<Car> idComparator) {
        Car.idComparator = idComparator;
    }

    public CarState getState() {
        return state;
    }

    public void setState(CarState state) {
        this.state = state;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        if(position<=0) {
            System.out.println("Current position: "+getPosition());
            System.out.println("Illegal position: "+position);
            throw new IllegalArgumentException("Position must greater than 0");
        }
        this.position = position;
    }
}
