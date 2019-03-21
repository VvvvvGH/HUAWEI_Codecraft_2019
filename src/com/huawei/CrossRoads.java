package com.huawei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class CrossRoads implements Comparable<CrossRoads> {
    private int id;
    // 这个flag 表明该次schedule状态是否发生变化，默认是true。 每次调用schedule()就会先设false，如果有变化就设置true
    private boolean stateChanged = true;

    private HashMap<Integer, RoadPosition> roadDirection = new HashMap<>();
    private TreeMap<Integer, Road> roadTreeMap = new TreeMap<>();

    public enum Turn {
        STRAIGHT, LEFT, RIGHT
    }

    public enum RoadPosition {
        NORTH, WEST, SOUTH, EAST,
    }

    public CrossRoads(int id,int road1,int road2,int road3,int road4) {
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

    // 调度路口
    // TODO: 检查路口是否有变化
    public void schedule() {
        stateChanged = false;
        for (int roadId : roadTreeMap.keySet()) {
            Road road = roadTreeMap.get(roadId);
            //空值异常
            PriorityQueue<Car> queue = road.getWaitingQueue(getId());
            // 当路为单向时，查找 start端的 queue为空
            if (queue == null) {
                continue;
            }

            Car car = null;
            // 直到取到等待状态的车为止
            while (!queue.isEmpty()) {
                car = queue.peek();
                if (car.getState() != CarState.WAIT) {
                    queue.remove(car);
                } else
                    break;
            }
            if (car == null)
                continue;

            // 车路径中该路的index
            int roadIdx = car.getPath().lastIndexOf(roadId);
            // 车来源路的ID
            int from = car.getPath().get(roadIdx);
            if (roadIdx == car.getPath().size() - 1) {
                // 车到达目的地
                // 移除waiting queue 里面的车
                car.setState(CarState.OFF_ROAD);
                road.removeCarFromRoad(car);
                road.getWaitingQueue(getId()).remove(car);

                stateChanged = true;
            } else {
                // 车没有到达目的地
                // 车目标路的ID
                int to = car.getPath().get(roadIdx + 1);
                Turn turn = findDirection(from, to);
                boolean canMove = true;
                if (turn == Turn.LEFT) {
                    // 检查直行优先
                    // 检查要左转的车 右边路口有没有直行的车
                    if (!isCarCanMove(roadId, Turn.RIGHT, Turn.STRAIGHT)) {
                        canMove = false;
                    }
                }
                if (turn == Turn.RIGHT) {
                    // 检查左转优先
                    // 检查要右转的车 对面路口有没有左转的车
                    if (!isCarCanMove(roadId, Turn.STRAIGHT, Turn.LEFT)) {
                        canMove = false;
                    }
                    // 检查直行优先
                    // 检查要右转的车 左边路口有没有直行的车
                    if (!isCarCanMove(roadId, Turn.LEFT, Turn.STRAIGHT)) {
                        canMove = false;
                    }
                }
                if (canMove) {
                    int previousLaneId = car.getLaneId();
                    if (moveCarBetweenRoad(roadTreeMap.get(from), roadTreeMap.get(to), car)) {
                        // 调动路口更新其车道
                        road.moveCarsOnRoad(previousLaneId, getId());
                        stateChanged = true;
                    }
                }
            }
        }
    }

    public boolean moveCarBetweenRoad(Road fromRoad, Road toRoad, Car car) {
        // TODO :把车移到下一条路
        int r1 = fromRoad.getTopSpeed();
        int r2 = toRoad.getTopSpeed();
        int v = car.getTopSpeed();
        //当前道路最大行驶速度
        int v1 = Math.min(r1, v);
        //下一条道路可行驶的最大速度
        int v2 = Math.min(r2, v);
        // 当前道路可行驶的距离
        int s1 = fromRoad.getLen() - car.getPosition();
        // 下一条道路可行驶的距离
        int s2;

        Lane lane;
        for (int i = 1; i <= toRoad.getNumOfLanes(); i++) {
            // Get lane
            if (toRoad.isBidirectional()) {
                if (getId() == toRoad.getStart())
                    lane = toRoad.getLaneListBy(toRoad.getEnd()).get(i - 1);
                else
                    lane = toRoad.getLaneListBy(toRoad.getStart()).get(i - 1);
            } else
                lane = toRoad.getLaneList().get(i - 1);
            TreeMap<Integer, Car> carMap = lane.getCarMap();
            Integer front = carMap.descendingKeySet().lower(0);
            if (front != null) {
                if (front > 1) {
                    // 前方有车 而且车道有位置
                    s2 = front - 1;
                    int diff = v2 - s1;
                    if (diff < 0) diff = 0;
                    if (s2 > diff)
                        s2 = diff;
                    if (s1 >= v2) {
                        /*
                        如果在当前道路的行驶距离S1已经大于等于下一条道路的单位时间最大行驶距离SV2，则此车辆不能通过路口，
                        只能行进至当前道路的最前方位置，等待下一时刻通过路口。
                         */
                        //移动车辆
                        car.setPosition(fromRoad.getLen());
                        lane.getCarMap().put(car.getPosition(), car);
                        car.setState(CarState.END);
                        return true;
                    }

                    // 从队列删除该车
                    fromRoad.getWaitingQueue(getId()).remove(car);
                    fromRoad.removeCarFromRoad(car);
                    // 移到下一条路
                    car.setPosition(s2);
                    lane.getCarMap().put(car.getPosition(), car);
                    //设置状态
                    car.setState(CarState.END);
                    car.setLaneId(lane.getId());
                    car.setCurrentSpeed(v2);
                    return true;

                } else
                    // 该车道已满
                    continue;
            } else {
                // 车可以上路，设置状态
                if (s1 < v2) {
                    // 从队列删除该车
                    fromRoad.getWaitingQueue(getId()).remove(car);
                    fromRoad.removeCarFromRoad(car);
                    // 移到下一条路
                    car.setPosition(v2 - s1);
                    lane.getCarMap().put(car.getPosition(), car);
                    //设置状态
                    car.setState(CarState.END);
                    car.setLaneId(lane.getId());
                    car.setCurrentSpeed(v2);
                    return true;
                }
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

    public Turn findDirection(int from, int to) {
        int val = getRoadDirection().get(from).ordinal() - getRoadDirection().get(to).ordinal();
        if (Math.abs(val) == 2)
            return Turn.STRAIGHT;
        else if (val == -1 || val == 3)
            return Turn.LEFT;
//        else if (val == 1 || val == -3)
        return Turn.RIGHT;
    }

    private boolean isCarCanMove(int roadId, Turn conflictRoadDirection, Turn conflictCarDirection) {
        boolean canMove = true;
        for (int otherRoadId : roadTreeMap.keySet()) {
            if (findDirection(roadId, otherRoadId) == conflictRoadDirection) {
                Road road = roadTreeMap.get(otherRoadId);
                Car car=null;
                if(road.isBidirectional()) {
                    if (getId() == road.getStart())
                        car = road.getWaitingQueue(road.getEnd()).peek();
                    else
                        car = road.getWaitingQueue(road.getStart()).peek();
                }else
                    car = road.getWaitingQueue(road.getEnd()).peek();
                if (car == null)
                    //那里没有车，可以走
                    return true;
                int roadIdx = car.getPath().lastIndexOf(roadId);
                if (roadIdx == -1 || roadIdx == car.getPath().size() - 1) {
                    // 车不走那条路，没有冲突
                    // 车到达目的地
                    return true;

                }
                int from = car.getPath().get(roadIdx);
                if (roadIdx != car.getPath().size() - 1) {
                    // 车还没到达终点
                    int rTo = car.getPath().get(roadIdx + 1);
                    //如果别的车更高优先级，就不可以走
                    if (findDirection(from, rTo) == conflictCarDirection)
                        canMove = false;
                }
            }
        }
        return canMove;
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
