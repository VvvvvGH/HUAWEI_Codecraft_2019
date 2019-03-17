package com.huawei;


import java.util.ArrayList;

public class Car implements Comparable {
    private int id;
    private int from;
    private int to;
    private int topSpeed;
    private int planTime;

    private ArrayList<Integer> path = new ArrayList<>();

    boolean started = false;
    boolean running = false;
    boolean reachedDest = false;
    int currentSpeed = 0;
    int startTime = -1;
    int endTime = -1;
    int laneId = -1;


    public Car(int id, int start, int to, int topSpeed, int planTime) {
        this.id = id;
        this.from = start;
        this.to = to;
        this.topSpeed = topSpeed;
        this.planTime = planTime;
    }

    public Car(String line) {
        String[] vars = line.split(",");
        this.id = Integer.parseInt(vars[0]);
        this.from = Integer.parseInt(vars[1]);
        this.to = Integer.parseInt(vars[2]);
        this.topSpeed = Integer.parseInt(vars[3]);
        this.planTime = Integer.parseInt(vars[4]);
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

    @Override
    public int compareTo(Object o) {
        if (o instanceof Car) {
            if (((Car) o).getPlanTime() > getPlanTime())
                return -1;
            else if (((Car) o).getPlanTime() < getPlanTime())
                return 1;
            else
                return 0;
            }
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStart() {
        return from;
    }

    public void setStart(int start) {
        this.from = start;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setPath(ArrayList<Integer> path) {
        this.path = path;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setReachedDest(boolean reachedDest) {
        this.reachedDest = reachedDest;
    }

    public void setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public void setLaneId(int laneId) {
        this.laneId = laneId;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isReachedDest() {
        return reachedDest;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public int getLaneId() {
        return laneId;
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
}
