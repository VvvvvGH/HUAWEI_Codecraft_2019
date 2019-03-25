package com.huawei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class CrossRoads implements Comparable<CrossRoads> {
    private int id;
    // 这个flag 表明该次schedule状态是否发生变化，默认是true。 每次调用schedule()就会先设false，如果有变化就设置true
    private boolean stateChanged = true;
    private boolean internalStateChanged = true;

    private HashMap<Integer, RoadPosition> roadDirection = new HashMap<>();
    private TreeMap<Integer, Road> roadTreeMap = new TreeMap<>();

    public enum Turn {
        STRAIGHT, LEFT, RIGHT
    }

    public enum RoadPosition {
        NORTH, WEST, SOUTH, EAST,
    }

    public CrossRoads(int id, int road1, int road2, int road3, int road4) {
        this.id = id;
        //路口顺序为 顺时针方向，第一个是北方。
        roadDirection.put(road1, RoadPosition.NORTH);
        roadDirection.put(road2, RoadPosition.WEST);
        roadDirection.put(road3, RoadPosition.SOUTH);
        roadDirection.put(road4, RoadPosition.EAST);
    }

    public CrossRoads(String line) {
        String[] vars = line.split(",");
        this.id = Integer.parseInt(vars[0]);
        //路口顺序为 顺时针方向，第一个是北方。
        roadDirection.put(Integer.parseInt(vars[1]), RoadPosition.NORTH);
        roadDirection.put(Integer.parseInt(vars[2]), RoadPosition.WEST);
        roadDirection.put(Integer.parseInt(vars[3]), RoadPosition.SOUTH);
        roadDirection.put(Integer.parseInt(vars[4]), RoadPosition.EAST);
    }


    @Override
    public int compareTo(CrossRoads c) {
        return this.getId() - c.getId();
    }


    public void schedule() {

        // 状态变化 flag
        stateChanged = false;
        internalStateChanged = true;

        while (internalStateChanged) {
            internalStateChanged = false;
            // 每个路口，升序排列。
            for (int roadId : roadTreeMap.keySet()) {
                Road road = roadTreeMap.get(roadId);

                Car car;

                while (true) {

                    // 优先队列里每一辆车
                    if ((car = fetchCarFromQueue(road)) != null) {

                        // 车是否到达目的地。如果到达，就将其从路上移除
                        if (!carReachedDestination(car, road)) {

                            // 车的行进方向是否有优先级
                            if (carHasPriorityToMove(car, road)) {

                                // 车路径中该路的index
                                int roadIdx = car.getPath().lastIndexOf(road.getId());
                                int from = car.getPath().get(roadIdx);                 // 车来源路的ID
                                int to = car.getPath().get(roadIdx + 1);               // 车目标路的ID

                                //移车
                                moveCarToNextRoad(from, to, car);

                                //更新等待队列
                                road.updateWaitingQueue(getId());

                                //该车仍是wait状态
                                if (car.getState() == CarState.WAIT)
                                    break;
                            } else
                                //车没有方向的优先级
                                break;

                        }

                    } else
                        // 该路口队列为空
                        break;
                }
            }
        }

    }

    private Car fetchCarFromQueue(Road road) {
        PriorityQueue<Car> queue = road.getWaitingQueue(getId());
        // 当路为单向时，查找 start端的 queue为空
        if (queue == null)
            return null;

        return queue.peek();
    }

    private boolean carReachedDestination(Car car, Road road) {
        // 车路径中该路的index
        int roadIdx = car.getPath().lastIndexOf(road.getId());

        if (roadIdx == car.getPath().size() - 1) {
            // 车到达目的地
            // 移除waiting queue 里面的车
            car.setState(CarState.OFF_ROAD);
            car.setEndTime(Scheduler.systemScheduleTime);
            Scheduler.totalScheduleTime += car.getEndTime() - car.getActualStartTime();
            Scheduler.totalActualScheduleTime += car.getEndTime() - car.getStartTime();
            road.removeCarFromRoad(car);
            road.getWaitingQueue(getId()).remove(car);
            stateChanged = true;
            internalStateChanged = true;
            return true;
        }
        return false;
    }

    private boolean carHasPriorityToMove(Car car, Road road) {
        // 车路径中该路的index
        int roadIdx = car.getPath().lastIndexOf(road.getId());
        int from = car.getPath().get(roadIdx);         // 车来源路的ID
        int to = car.getPath().get(roadIdx + 1);       // 车目标路的ID

        Turn turn = findDirection(from, to);
        if (turn == Turn.LEFT) {
            // 检查直行优先
            // 检查要左转的车 右边路口有没有直行的车
            if (checkConflict(road.getId(), to, Turn.RIGHT, Turn.STRAIGHT)) {
                return false;
            }
        }
        if (turn == Turn.RIGHT) {
            // 检查左转优先
            // 检查要右转的车 对面路口有没有左转的车
            if (checkConflict(road.getId(), to, Turn.STRAIGHT, Turn.LEFT)) {
                return false;
            }
            // 检查直行优先
            // 检查要右转的车 左边路口有没有直行的车
            if (checkConflict(road.getId(), to, Turn.LEFT, Turn.STRAIGHT)) {
                return false;
            }
        }
        return true;
    }

    private void moveCarToNextRoad(int from, int to, Car car) {

        Road fromRoad = roadTreeMap.get(from);
        Road toRoad = roadTreeMap.get(to);

        //下一条道路可行驶的最大速度
        int v2 = Math.min(toRoad.getTopSpeed(), car.getTopSpeed());
        // 当前道路可行驶的距离
        int s1 = fromRoad.getLen() - car.getPosition();
        // 下一条道路可行驶的距离
        int s2 = v2 - s1;

        // 记录下一路口车的状态。即使不过路口也会记录，保证 wait -> wait 和　wait -> end 状态。
        CarState frontCarState = CarState.END;
        boolean hasPosition = false;
        boolean carWillCrossTheRoad = true;
        int positionOnNextRoad = -1;
        int frontCarPosition = -1;

        // 先判断车距离上会不会过路口
        if (s2 <= 0) {
            carWillCrossTheRoad = false;
        }

        // 每一条车道处理
        ArrayList<Lane> laneList = getLaneListFromTargetRoad(toRoad);
        for (Lane lane : laneList) {

            if (!lane.hasPosition()) {
                Car frontCar = lane.getCar(lane.getFrontCarPosition(0));
                if (frontCar.getState() == CarState.WAIT)
                    frontCarState = CarState.WAIT;
                continue;
            }

            hasPosition = true;

            if (!lane.isEmpty()) {
                Car frontCar = lane.getCar(lane.getFrontCarPosition(0));
                frontCarPosition = lane.getFrontCarPosition(0);
                frontCarState = frontCar.getState();

                //该车道被堵住了　或者前车状态为wait
                if (frontCarPosition == 1)
                    continue;

                if (frontCarPosition > s2)
                    positionOnNextRoad = s2;
                else {
                    positionOnNextRoad = frontCarPosition - 1;
                    if (frontCar.getState() == CarState.WAIT)
                        return;
                }

            } else {
                frontCarState = CarState.END;
                positionOnNextRoad = s2;
            }

            //车会过马路
            if (carWillCrossTheRoad) {
                // 开始将车移到下一条路
                if (lane.putCar(car, positionOnNextRoad)) {
                    fromRoad.removeCarFromRoad(car);
                    fromRoad.getWaitingQueue(getId()).remove(car);
                    car.setCurrentSpeed(v2).setState(CarState.END);
                    internalStateChanged = true;
                    return;
                }
            }
        }
        if (!hasPosition) {
            carWillCrossTheRoad = false;
        }

        //车不过马路
        if (!carWillCrossTheRoad && fromRoad.getLen() != car.getPosition()) {
            Lane laneContainsThisCar = null;
            for (Lane lane : fromRoad.getLaneList()) {
                if (lane.getCar(car.getPosition()) != null && lane.getCar(car.getPosition()).equals(car))
                    laneContainsThisCar = lane;
            }

            if (laneContainsThisCar == null)
                System.err.println("CrossRoad Error: lane is null");


            frontCarPosition = laneContainsThisCar.getFrontCarPosition(car.getPosition());
            if (frontCarPosition != -1) {
                if ((frontCarPosition - car.getPosition()) <= car.getCurrentSpeed())
                    positionOnNextRoad = frontCarPosition - 1;
                else
                    positionOnNextRoad = car.getPosition() + car.getCurrentSpeed();
            } else {
                positionOnNextRoad = fromRoad.getLen();
            }
            laneContainsThisCar.updateCar(car, car.getPosition(), positionOnNextRoad);
            car.setState(CarState.END);
            internalStateChanged = true;
        } else
            car.setState(frontCarState);


    }

    private ArrayList<Lane> getLaneListFromTargetRoad(Road road) {
        ArrayList<Lane> laneList;
        if (road.isBidirectional()) {
            if (getId() == road.getStart())
                laneList = road.getLaneListBy(road.getEnd());
            else
                laneList = road.getLaneListBy(road.getStart());
        } else
            laneList = road.getLaneList();
        return laneList;
    }


    private Turn findDirection(int from, int to) {
        int val = getRoadDirection().get(from).ordinal() - getRoadDirection().get(to).ordinal();
        if (Math.abs(val) == 2)
            return Turn.STRAIGHT;
        else if (val == -1 || val == 3)
            return Turn.LEFT;
        return Turn.RIGHT;
    }

    private boolean checkConflict(int roadId, int to, Turn conflictRoadDirection, Turn conflictCarDirection) {
        for (int otherRoadId : roadTreeMap.keySet()) {
            if (roadId != otherRoadId && findDirection(roadId, otherRoadId) == conflictRoadDirection) {
                Road road = roadTreeMap.get(otherRoadId);
                Car car;

                //那里没有车，可以走
                if ((car = fetchCarFromQueue(road)) == null)
                    return false;

                // 车不走那条路，没有冲突 or 车到达目的地
                int roadIdx = car.getPath().lastIndexOf(to);
                if (roadIdx == -1) {
                    return false;
                }

                //如果别的车更高优先级，就不可以走
                int from = car.getPath().get(roadIdx - 1);
//                int to1 = car.getPath().get(roadIdx + 1);
                if (findDirection(from, to) == conflictCarDirection)
                    return true;

            }
        }
        return false;
    }

    public void addRoads(TreeMap<Integer, Road> roadMap) {
        //添加道路到路口
        for (int roadId : getRoadIds()) {
            if (roadId != -1) {
                roadTreeMap.put(roadId, roadMap.get(roadId));
            }
        }
    }

    public Integer[] getRoadIds() {
        return roadDirection.keySet().toArray(new Integer[roadDirection.size()]);
    }

    public HashMap<Integer, RoadPosition> getRoadDirection() {
        return roadDirection;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isStateChanged() {
        return stateChanged;
    }

    public void setStateChanged(boolean stateChanged) {
        this.stateChanged = stateChanged;
    }


}
