package com.huawei;

import java.util.*;

public class Road {
    private int id;
    private int len;
    private int topSpeed;
    private int numOfLanes;
    private int start;
    private int end;
    private boolean bidirectional;

    // 车道实现
    // 如果为双向道路,则存储顺序为start-->end方向的道路，之后是反方向的道路
    // 如车道为6,路口id顺序为 123123
    private ArrayList<Lane> laneList;

    // Key 为路的出口路口Id
    private HashMap<Integer, PriorityQueue<Car>> waitingQueueMap = new HashMap<>();

    public static Comparator<Car> carComparator = new Comparator<Car>() {
        @Override
        public int compare(Car o1, Car o2) {
            if (o1.getPosition() > o2.getPosition())
                return 1;
            else if (o1.getPosition() == o2.getPosition() && o1.getLaneId() < o2.getLaneId())
                return 1;
            else return -1;
        }
    };

    public Road(String line) {
        String[] vars = line.split(",");
        this.id = Integer.parseInt(vars[0]);
        this.len = Integer.parseInt(vars[1]);
        this.topSpeed = Integer.parseInt(vars[2]);
        this.numOfLanes = Integer.parseInt(vars[3]);
        this.start = Integer.parseInt(vars[4]);
        this.end = Integer.parseInt(vars[5]);
        this.bidirectional = vars[6].equals("1");


        if (!this.isBidirectional())
            laneList = new ArrayList<>(this.getNumOfLanes());
        else
            laneList = new ArrayList<>(this.getNumOfLanes() * 2);
        // 从１开始
        for (int i = 1; i <= this.getNumOfLanes(); i++) {
            laneList.add(i - 1, new Lane());
            laneList.get(i - 1).setS1(this.getTopSpeed());
            laneList.get(i - 1).setId(i);
        }
        if (this.isBidirectional()) {
            for (int i = 1 + getNumOfLanes(); i <= this.getNumOfLanes() * 2; i++) {
                laneList.add(i - 1, new Lane());
                laneList.get(i - 1).setS1(this.getTopSpeed());
                laneList.get(i - 1).setId(i-getNumOfLanes());
            }
        }
        // Priority queue
        waitingQueueMap.put(getEnd(), new PriorityQueue<>(carComparator));
        if (isBidirectional()) {
            waitingQueueMap.put(getStart(), new PriorityQueue<>(carComparator));
        }

    }

    // 出发的车
    public boolean putCarOnRoad(Car car) {
        int sv1 = Math.min(car.getTopSpeed(), this.getTopSpeed());
        for (int i = 1; i <= getNumOfLanes(); i++) {
            Lane lane;
            // Get lane
            if (isBidirectional()) {
                //FIXME: Gocha!!!!
                if (car.getFrom() == getEnd())
                    lane = getLaneListBy(this.getStart()).get(i - 1);
                else
                    lane = getLaneListBy(this.getEnd()).get(i - 1);
            } else
                lane = laneList.get(i - 1);
            TreeMap<Integer, Car> carMap = lane.getCarMap();
            Integer higher = carMap.descendingKeySet().higher(0);
            if (higher != null) {
                if (higher > 1) {
                    // 前方有车 而且车道有位置
                    Car frontCar = carMap.get(higher);
                    CarState state = frontCar.getState();
                    int dist = frontCar.getPosition() - 1;
                    if (sv1 <= dist) {
                        car.setPosition(sv1);
                        car.setState(CarState.END);
                    } else {
                        // 前面车是处于END State
                        car.setPosition(frontCar.getPosition() - 1);
                        car.setState(CarState.END);
                    }
                    carMap.put(car.getPosition(), car);
                    car.setLaneId(lane.getId());
                    car.setCurrentSpeed(sv1);

                    return true;
                } else
                    // 该车道已满
                    continue;
            } else {
                // 车可以上路，设置状态
                if (sv1 <= this.getLen()) {
                    car.setPosition(sv1);
                    car.setState(CarState.END);
                    car.setLaneId(lane.getId());
                    car.setCurrentSpeed(sv1);
                    carMap.put(car.getPosition(), car);
                    return true;
                } else {
                    //官方论坛的人回答说不会出现这种情况
                    System.err.println("Road#putCarOnRoad#error");
                }
            }
        }
        return false;
    }

    // 对单独车道处理
    public void moveCarsOnRoad(int laneId, int crossRoadId) {
        Lane lane = getLaneListBy(crossRoadId).get(laneId - 1);
        TreeMap<Integer, Car> carMap = lane.getCarMap();
        if (carMap.size() == 0)
            //车道为空 没必要继续
            return;
        int s1 = lane.getS1();   // 当前路段的最大行驶距离或者车子与前车之间的最大可行驶距离
        // 同步问题。TODO 思考下逻辑
        List<Integer> positionList = new ArrayList(carMap.descendingKeySet());
        for (Integer position : positionList) {
            Car car = carMap.get(position);
            int sv1 = car.getCurrentSpeed(); // 当前车速在当前道路的最大行驶距离
            Integer higher = carMap.descendingKeySet().higher(position);
            if (higher != null) { // 前方有车
                Car frontCar = carMap.get(higher);
                CarState state = frontCar.getState();
                int dist = frontCar.getPosition() - car.getPosition() - 1;
                if (sv1 <= dist) {
                    car.setPosition(sv1 + car.getPosition());
                    car.setState(CarState.END);
                } else {
                    // 会碰上车。
                    if (state == CarState.END) {
                        car.setPosition(frontCar.getPosition() - 1);
                        car.setState(CarState.END);
                    } else if (state == CarState.WAIT) {
                        car.setState(CarState.WAIT);
                    } else {
                        System.err.println("Road#moveCarsOnRoad#error");
                    }
                }
            } else {
                if (sv1 <= this.getLen() - position) {
                    car.setPosition(sv1 + car.getPosition());
                    car.setState(CarState.END);
                } else { // 可以出路口
                    car.setState(CarState.WAIT);
                }
            }
            carMap.remove(position);
            carMap.put(car.getPosition(), car);
        }

    }

    public void moveCarsOnRoad() {
        for (int i = 1; i <= getNumOfLanes(); i++) {
            moveCarsOnRoad(i, getEnd());
            if (isBidirectional()) {
                moveCarsOnRoad(i, getStart());
            }
        }


    }

    public int compareTo(Road r) {
        return this.getId() - r.getId();
    }

    public void offerWaitingQueue(int crossRoadId) {
        // 把车放入等待队列， 需要根据车道进行排序
        PriorityQueue<Car> waitingQueue = waitingQueueMap.get(crossRoadId);
        ArrayList<Lane> lanes = getLaneListBy(crossRoadId);
        lanes.forEach(
                lane -> {
                    Map<Integer, Car> carMap = lane.getCarMap();
                    carMap.forEach((carId, car) -> {
                        if (car.getState() == CarState.WAIT)
                            waitingQueue.add(car);
                    });
                }
        );
    }

    public ArrayList<Lane> getLaneListBy(int crossRoadId) {
        if (crossRoadId == getEnd()) {
            return new ArrayList<Lane>(laneList.subList(0, getNumOfLanes()));
        } else if (crossRoadId == getStart()) {
            return new ArrayList<Lane>(laneList.subList(getNumOfLanes(), getNumOfLanes() * 2));
        } else {
            System.err.println("Road#getLaneListBy#error");
            return null;
        }
    }

    public ArrayList<Lane> getLaneList() {
        // Single Road
        return laneList;
    }

    public PriorityQueue<Car> getWaitingQueue(int crossId) {
        return waitingQueueMap.get(crossId);
    }

    // 把车辆从路上移除
    //
    public void removeCarFromRoad(Car car) {
        for (Lane lane : laneList) {
            if (car.getLaneId() == lane.getId()) {
                lane.getCarMap().remove(car.getPosition());
                break;
            }
        }
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
