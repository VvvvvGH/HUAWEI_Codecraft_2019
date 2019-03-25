package com.huawei;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Car implements Comparable<Car> {
    private int id;
    private int from;
    private int to;
    private int topSpeed;
    private int planTime;

    private ArrayList<Integer> path = new ArrayList<>();

    private int currentSpeed = 0;
    private long startTime = -1;
    private long actualStartTime = -1;
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
        setState(CarState.IN_GARAGE);
    }

    public Car(String line) {
        String[] vars = line.split(",");
        this.id = Integer.parseInt(vars[0]);
        this.from = Integer.parseInt(vars[1]);
        this.to = Integer.parseInt(vars[2]);
        this.topSpeed = Integer.parseInt(vars[3]);
        this.planTime = Integer.parseInt(vars[4]);
        setState(CarState.IN_GARAGE);
    }

    public Car addPath(int roadId) {
        path.add(roadId);
        return this;
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


    public int getFrom() {
        return from;
    }


    public int getTo() {
        return to;
    }


    public int getTopSpeed() {
        return topSpeed;
    }


    public int getPlanTime() {
        return planTime;
    }


    public int getCurrentSpeed() {
        return currentSpeed;
    }


    public long getStartTime() {
        return startTime;
    }


    public long getEndTime() {
        return endTime;
    }


    public int getLaneId() {
        return laneId;
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


    public int getPosition() {
        return position;
    }

    public Car setPosition(int position) {
        if (position <= 0) {
            System.out.println("Current position: " + getPosition());
            System.out.println("Illegal position: " + position);
            System.err.println("Position must greater than 0");
        }
        this.position = position;
        return this;
    }

    public Car setId(int id) {
        this.id = id;
        return this;
    }

    public Car setFrom(int from) {
        this.from = from;
        return this;
    }

    public Car setTo(int to) {
        this.to = to;
        return this;
    }

    public Car setTopSpeed(int topSpeed) {
        this.topSpeed = topSpeed;
        return this;
    }

    public Car setPlanTime(int planTime) {
        this.planTime = planTime;
        return this;
    }

    public Car setPath(ArrayList<Integer> path) {
        this.path = path;
        return this;
    }

    public Car setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
        return this;
    }

    public Car setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Car setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    public Car setLaneId(int laneId) {
        this.laneId = laneId;
        return this;
    }

    public Car setState(CarState state) {
        if (getState() != null && !getState().equals(state))
            Scheduler.carStateChanged = true;

        updateStateCounter(getState(), state);

        this.state = state;
        return this;
    }

    public void updateStateCounter(CarState original, CarState now) {
        if (original == now)
            return;
        HashMap<CarState, Integer> carStateCounter = Scheduler.carStateCounter;

        carStateCounter.put(now, carStateCounter.get(now) == null ? 1 : (carStateCounter.get(now) + 1));
        carStateCounter.put(original, carStateCounter.get(original) == null ? 0 : (carStateCounter.get(original) - 1));

    }

    public long getActualStartTime() {
        return actualStartTime;
    }

    public Car setActualStartTime(long actualStartTime) {
        this.actualStartTime = actualStartTime;
        return this;
    }

    public void resetCarState() {
        this.currentSpeed = 0;
        this.startTime = -1;
        this.endTime = -1;
        this.laneId = -1;

        this.state = CarState.IN_GARAGE;
        this.position = -1;
    }
}
