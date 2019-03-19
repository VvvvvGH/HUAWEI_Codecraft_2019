package com.huawei;

public class Road {
    private int id;
    private int len;
    private int topSpeed;
    private int numOfLanes;
    private int start;
    private int end;
    private boolean bidirectional;

    public Road(String line) {
        String[] vars = line.split(",");
        this.id = Integer.parseInt(vars[0]);
        this.len = Integer.parseInt(vars[1]);
        this.topSpeed = Integer.parseInt(vars[2]);
        this.numOfLanes = Integer.parseInt(vars[3]);
        this.start = Integer.parseInt(vars[4]);
        this.end = Integer.parseInt(vars[5]);
        this.bidirectional = vars[6].equals("1");

    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(int topSpeed) {
        this.topSpeed = topSpeed;
    }

    public int getNumOfLanes() {
        return numOfLanes;
    }

    public void setNumOfLanes(int numOfLanes) {
        this.numOfLanes = numOfLanes;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }
}
