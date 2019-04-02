package com.huawei;

import java.util.*;
import java.util.stream.Collectors;

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
    private HashMap<Integer, ArrayList<Car>> carSequenceListMap = new HashMap<>();


    public static Comparator<Road> roadCompareByLength = (Road r1, Road r2) -> Integer.compare(r2.getLen(), r1.getLen());

    public static Comparator<Road> roadCompareBySpeed = (Road r1, Road r2) -> Integer.compare(r2.getTopSpeed(), r1.getTopSpeed());

    public static Comparator<Road> roadCompareByWidth = (Road r1, Road r2) -> Integer.compare(r2.getNumOfLanes(), r1.getNumOfLanes());

    public Road(int id, int len, int topSpeed, int numOfLanes, int start, int end, boolean bidirectional) {
        this.id = id;
        this.len = len;
        this.topSpeed = topSpeed;
        this.numOfLanes = numOfLanes;
        this.start = start;
        this.end = end;
        this.bidirectional = bidirectional;

        initLaneList();
        initCarSequence();
    }

    public Road(String line) {
        String[] vars = line.split(",");
        this.id = Integer.parseInt(vars[0]);
        this.len = Integer.parseInt(vars[1]);
        this.topSpeed = Integer.parseInt(vars[2]);
        this.numOfLanes = Integer.parseInt(vars[3]);
        this.start = Integer.parseInt(vars[4]);
        this.end = Integer.parseInt(vars[5]);
        this.bidirectional = vars[6].equals("1");

        initLaneList();
        initCarSequence();
    }

    private void initLaneList() {
        if (!this.isBidirectional())
            laneList = new ArrayList<>(this.getNumOfLanes());
        else
            laneList = new ArrayList<>(this.getNumOfLanes() * 2);
        // 从１开始
        for (int i = 1; i <= this.getNumOfLanes(); i++) {
            laneList.add(i - 1, new Lane(i, getLen()));
        }
        if (this.isBidirectional()) {
            for (int i = 1 + getNumOfLanes(); i <= this.getNumOfLanes() * 2; i++) {
                laneList.add(i - 1, new Lane(i - getNumOfLanes(), getLen()));
            }
        }
    }

    private void initCarSequence() {
        carSequenceListMap.clear();
        // initCarSequence
        carSequenceListMap.put(getEnd(), new ArrayList<>());
        if (isBidirectional()) {
            carSequenceListMap.put(getStart(), new ArrayList<>());
        }
    }

    // 出发的车
    public boolean putCarOnRoad(Car car, int nextCrossRoadId) {
        int sv1 = Math.min(car.getTopSpeed(), this.getTopSpeed());
        for (int i = 1; i <= getNumOfLanes(); i++) {
            Lane lane = null;
            // Get lane
            if (isBidirectional()) {
                if (car.getFrom() == this.getStart() && this.getEnd() == nextCrossRoadId)
                    lane = getLaneListBy(this.getEnd()).get(i - 1);
                else
                    lane = getLaneListBy(this.getStart()).get(i - 1);
            } else
                lane = laneList.get(i - 1);
            Integer front = lane.getFrontCarPosition(0);

            if (front != -1) {
                if (front > 1) {
                    // 前方有车 而且车道有位置
                    Car frontCar = lane.getCar(front);
                    CarState state = frontCar.getState();

                    int dist = frontCar.getPosition() - 1;
                    if (sv1 <= dist) {
                        car.setPosition(sv1);
                    } else {
                        // 在进入车辆的最大可行距离内有等待车辆阻挡，不出车。
                        if (state == CarState.WAIT)
                            return false;

                        // 前面车是处于END State
                        car.setPosition(frontCar.getPosition() - 1);
                    }
                    lane.putCar(car, car.getPosition());
                    car.setLaneId(lane.getId()).setCurrentSpeed(sv1).setState(CarState.END).setRoadIdx(0);
                    return true;
                } else
                    // 该车道已满
                    continue;
            } else {
                // 车可以上路，设置状态
                if (sv1 < this.getLen()) {
                    car.setPosition(sv1).setState(CarState.END).setLaneId(lane.getId()).setCurrentSpeed(sv1).setRoadIdx(0);
                    lane.putCar(car, car.getPosition());
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
        if (lane.isEmpty())
            //车道为空 没必要继续
            return;

        List<Integer> positionList = lane.getDescendingPositionList();
        for (Integer position : positionList) {
            Car car = lane.getCar(position);
            int sv1 = car.getCurrentSpeed(); // 当前车速在当前道路的最大行驶距离
            int front = lane.getFrontCarPosition(position);
            if (front != -1) { // 前方有车
                Car frontCar = lane.getCar(front);
                CarState state = frontCar.getState();
                int dist = frontCar.getPosition() - car.getPosition() - 1;
                if (sv1 <= dist) {
                    car.setPosition(sv1 + car.getPosition()).setState(CarState.END);
                } else {
                    // 会碰上车。
                    if (state == CarState.END) {
                        car.setPosition(frontCar.getPosition() - 1).setState(CarState.END);
                    } else if (state == CarState.WAIT) {
                        car.setState(CarState.WAIT);
                    } else {
                        System.err.println("Road#moveCarsOnRoad#error");
                    }
                }
            } else {
                // 前方没有车
                // 需要等于
                if (sv1 <= this.getLen() - position) {
                    car.setPosition(sv1 + car.getPosition()).setState(CarState.END);
                } else { // 可以出路口
                    car.setState(CarState.WAIT);
                }
            }
            if (car.getPosition() != position) {
                lane.updateCar(car, position, car.getPosition());
            }
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

    public void createSequenceList(int crossRoadId) {
        // 把车放入等待列表， 需要根据车道进行排序

        ArrayList<Car> carSequenceList = carSequenceListMap.get(crossRoadId);
        carSequenceList.clear();
        ArrayList<Lane> lanes = getLaneListBy(crossRoadId);
        lanes.forEach(
                lane -> {
                    Map<Integer, Car> carMap = lane.getCarMap();
                    carMap.forEach((carId, car) -> {
                        // 保证没有重复
                        if (car.getState() == CarState.WAIT && !carSequenceList.contains(car))
                            carSequenceList.add(car);
                    });
                }
        );
        carSequenceList.sort(Car.priorityLaneIdComparator);
    }

    public void updateCarSequenceList(int crossRoadId) {
        //更新等待列表
        ArrayList<Car> carArrayList = getCarSequenceList(crossRoadId);
        Iterator iterator = carArrayList.iterator();

        while (iterator.hasNext()) {
            Car car = (Car) iterator.next();
            if (car.getState() != CarState.WAIT)
                iterator.remove();
        }
        carArrayList.sort(Car.priorityLaneIdComparator);

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

    public ArrayList<Car> getCarSequenceList(int crossId) {
        return carSequenceListMap.get(crossId);
    }

    // 把车辆从路上移除
    public void removeCarFromRoad(Car car) {
        for (Lane lane : laneList) {
            Iterator<Car> it = lane.getCarMap().values().iterator();
            while (it.hasNext()) {
                Car carOnLane = it.next();
                if (carOnLane.getId() == car.getId()) {
                    it.remove();
                    return;
                }
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

    public void resetRoadState() {
        initLaneList();
        initCarSequence();
    }

    public double calculateLoad() {
        int totalCapacity = getNumOfLanes() * getLen() * (isBidirectional() ? 2 : 1);
        int numberOfCar = 0;
        for (Lane lane : laneList) {
            numberOfCar += lane.getCarMap().size();
        }
        return numberOfCar / (totalCapacity * 1.0);
    }

    public void updateLane(Lane lane) {
        if (lane == null) {
            System.err.println("Road#updateLane#error");
            System.exit(0);
        }

        if (lane.isEmpty()) {
            //车道为空 没必要继续
            return;
        }

        List<Integer> positionList = lane.getDescendingPositionList();
        for (Integer position : positionList) {
            Car car = lane.getCar(position);
            if (car.getState() != CarState.WAIT) {
                continue;
            }
            int sv1 = car.getCurrentSpeed(); // 当前车速在当前道路的最大行驶距离
            int front = lane.getFrontCarPosition(position);
            if (front != -1) { // 前方有车
                Car frontCar = lane.getCar(front);
                CarState state = frontCar.getState();
                int dist = frontCar.getPosition() - car.getPosition() - 1;
                if (sv1 <= dist) {
                    car.setPosition(sv1 + car.getPosition()).setState(CarState.END);
                } else {
                    // 会碰上车。
                    if (state == CarState.END) {
                        car.setPosition(frontCar.getPosition() - 1).setState(CarState.END);
                    } else if (state == CarState.WAIT) {
                        car.setState(CarState.WAIT);
                    } else {
                        System.err.println("Road#moveCarsOnRoad#error");
                    }
                }
            } else {
                // 前方没有车
                // 需要等于
                if (sv1 <= this.getLen() - position) {
                    car.setPosition(sv1 + car.getPosition()).setState(CarState.END);
                } else { // 可以出路口
                    car.setState(CarState.WAIT);
                }
            }
            if (car.getPosition() != position) {
                lane.updateCar(car, position, car.getPosition());
            }
        }
    }

    public Lane laneContainsCar(Car car) {
        Lane laneContainCar = null;
        for (Lane l : getLaneList()) {
            if (l.getCarMap().containsValue(car)) {
                laneContainCar = l;
                break;
            }
        }
        if (laneContainCar == null) {
            System.err.println("Road#laneContainsCar#null");
        }
        return laneContainCar;
    }

    public RoadStates dumpStates() {
        ArrayList<Lane> lanes = getLaneList();
        HashMap<Integer, ArrayList<Integer>> carsMap = new HashMap<>();
        for (int i = 0; i < lanes.size(); i++) {
            carsMap.put(i, new ArrayList<Integer>(lanes.get(i).getCarMap().values().stream().map(Car::getId).collect(Collectors.toList())));
        }

        return new RoadStates(getId(), carsMap);
    }

    public void restoreStates(RoadStates roadStates) {
        if (roadStates.getRoadId() != getId())
            System.err.println("Id not match !!! #restoreStates");
        initLaneList();
        initCarSequence();

        ArrayList<Lane> lanes = getLaneList();

        for (int i = 0; i < lanes.size(); i++) {

            for (int carId : roadStates.getCarsMap().get(i)) {
                Car car = Main.scheduler.getCar(carId);
                lanes.get(i).putCar(car, car.getPosition());
            }

        }
        // Restore Waiting queue
        if (
                isBidirectional()) {
            createSequenceList(getStart());
            createSequenceList(getEnd());
        } else

            createSequenceList(getEnd());

    }

    public class RoadStates {
        int roadId;
        HashMap<Integer, ArrayList<Integer>> carsMap;

        public RoadStates(int roadId, HashMap<Integer, ArrayList<Integer>> carsMap) {
            this.roadId = roadId;
            this.carsMap = carsMap;
        }

        public int getRoadId() {
            return roadId;
        }

        public RoadStates setRoadId(int roadId) {
            this.roadId = roadId;
            return this;
        }

        public HashMap<Integer, ArrayList<Integer>> getCarsMap() {
            return carsMap;
        }

        public RoadStates setCarsMap(HashMap<Integer, ArrayList<Integer>> carsMap) {
            this.carsMap = carsMap;
            return this;
        }
    }

}
