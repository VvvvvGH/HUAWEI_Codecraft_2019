package com.huawei;


import java.util.ArrayList;
import java.util.HashMap;
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


    public void schedule(Scheduler scheduler) {

        // 状态变化 flag
        stateChanged = false;

        // 每个路口，升序排列。
        for (Road road : roadTreeMap.values()) {

            Car car;
            while ((car = fetchCarFromList(road)) != null) {

                // 优先队列里每一辆车
                Lane laneContainCar = road.getLaneListBy(getId()).get(car.getLaneId()-1);


                // 车的行进方向是否有优先级
                if (carHasPriorityToMove(car, road)) {
                    //车到达目的地
                    if (carReachedDestination(car, road)) {
                        road.updateLane(laneContainCar);
                        // 上路优先车辆
                        scheduler.driveCarInGarage(true);
                        continue;
                    }

                    //移车
                    if (moveCarToNextRoad(car)) {
                        road.updateLane(laneContainCar);
                        // 上路优先车辆
                        scheduler.driveCarInGarage(true);
                    } else
                        break;


                } else
                    break;

            }
        }


    }

    private Car fetchCarFromList(Road road) {
        ArrayList<Car> carArrayList = road.getCarSequenceList(getId());
        // 当路为单向时，查找 start端的 列表为空
        if (carArrayList == null || carArrayList.size() == 0)
            return null;

        return carArrayList.get(0);
    }

    private boolean carReachedDestination(Car car, Road road) {
        // 车路径中该路的index
        int roadIdx = car.getRoadIdx();
        // 车到达目的地
        if (roadIdx == car.getPath().size() - 1) {
            Lane lane = road.laneContainsCar(car);

            // 前面有车，不能到达目的地
            if (lane.getFrontCarPosition(car.getPosition()) != -1) {
                System.err.println("前面有车，不能到达目的地: car " + car.getId() + " cross " + getId());
                return false;
            }

            car.setState(CarState.OFF_ROAD);
            car.setEndTime(Scheduler.systemScheduleTime);
            Scheduler.totalScheduleTime += car.getEndTime() - car.getPlanTime();
            road.removeCarFromRoad(car,lane);
            stateChanged = true;
            return true;
        }
        return false;
    }

    private boolean carHasPriorityToMove(Car car, Road road) {
        // 车准备到达目的地
        if (car.getRoadIdx() == car.getPath().size() - 1)
            return checkHasPriorityToReachTheEnd(car, road);
        else
            return checkHasPriorityToCross(car, road);
    }

    private boolean checkHasPriorityToCross(Car car, Road road) {
        // 车路径中该路的index
        int roadIdx = car.getRoadIdx();
        int from = car.getPath().get(roadIdx);         // 车来源路的ID
        int to = car.getPath().get(roadIdx + 1);       // 车目标路的ID

        return checkConflict(car,from,to);
    }

    private boolean checkHasPriorityToReachTheEnd(Car car, Road road) {
        if (car.isPriority())
            return true;

        int to = -1;

        for (int roadId : roadTreeMap.keySet()) {
            if (roadId != -1 && roadId != road.getId() && findDirection(road.getId(), roadId) == Turn.STRAIGHT)
                to = roadId;
        }
        // 没有直行方向
        if (to == -1)
            return true;

        return checkConflict(car,road.getId(),to);
    }


    private boolean moveCarToNextRoad(Car car) {

        // 车路径中该路的index
        int roadIdx = car.getRoadIdx();
        int from = car.getPath().get(roadIdx);                 // 车来源路的ID
        int to = car.getPath().get(roadIdx + 1);               // 车目标路的ID

        Road fromRoad = roadTreeMap.get(from);
        Road toRoad = roadTreeMap.get(to);
        Lane laneContainCarOnFrom = fromRoad.laneContainsCar(car);

        int v1 = car.getCurrentSpeed();
        int s1 = fromRoad.getLen() - car.getPosition();


        if (v1 <= s1) {
            System.err.println("moveCarToNextRoad#error#v1<=s1");
        }

        if (laneContainCarOnFrom.getFrontCarPosition(car.getPosition()) != -1) {
            System.err.println("moveCarToNextRoad#error#laneContainCarOnFrom");
        }


        //下一条道路可行驶的最大速度
        int v2 = Math.min(toRoad.getTopSpeed(), car.getTopSpeed());
        // 当前道路可行驶的距离

        // 下一条道路可行驶的距离
        int s2 = v2 - s1;

        // 记录下一路口车的状态。即使不过路口也会记录，保证 wait -> wait 和　wait -> end 状态。
        CarState frontCarState = CarState.END;
        boolean hasPosition = false;
        boolean carWillCrossTheRoad = true;


        // 车一定不会过马路
        if (s2 <= 0) {
            int positionOnNextRoad = laneContainCarOnFrom.getLength();
            if (car.getPosition() != positionOnNextRoad) {
                laneContainCarOnFrom.updateCar(car, car.getPosition(), positionOnNextRoad);
            }
            car.setState(CarState.END);
            return true;
        }

        // 看 to road 是否全面堵塞在第一个位置
        ArrayList<Lane> laneListOnToRoad = getLaneListFromTargetRoad(toRoad);
        for (Lane lane : laneListOnToRoad) {
            if (lane.hasPosition()) {
                hasPosition = true;
                break;
            }
        }

        //没有位置， 要考虑车的状态
        if (!hasPosition) {
            CarState firstCarState = CarState.END;
            for (Lane lane : laneListOnToRoad) {
                Car firstCar = lane.getCar(1);
                if (firstCar.getState() == CarState.WAIT) {
                    firstCarState = CarState.WAIT;
                    break;
                }
            }

            int positionOnNextRoad = laneContainCarOnFrom.getLength();
            if (firstCarState == CarState.END) {
                if (car.getPosition() != positionOnNextRoad) {
                    laneContainCarOnFrom.updateCar(car, car.getPosition(), positionOnNextRoad);
                }
                car.setState(CarState.END);
                return true;
            } else if (firstCarState == CarState.WAIT) {
                return false;
            } else {
                System.err.println("moveCarToNextRoad#error#unexception state");
            }
        }


        if (hasPosition) {
            Lane laneToPut = null;
            for (Lane lane : laneListOnToRoad) {
                if (lane.hasPosition()) {
                    laneToPut = lane;
                    break;
                }
            }

            //真正的把车移到下一条路。
            if (laneToPut.isEmpty()) {
                int positionOnNextRoad = s2;
                if (laneToPut.putCar(car, positionOnNextRoad)) {
                    fromRoad.removeCarFromRoad(car);
                    fromRoad.getCarSequenceList(getId()).remove(car);
                    car.setCurrentSpeed(v2).setState(CarState.END).setRoadIdx(car.getRoadIdx() + 1);
                    return true;
                } else {
                    System.err.println("putCar failed");
                }
            } else {
                Car frontCar = laneToPut.getCar(laneToPut.getFrontCarPosition(0));
                int frontCarPosition = frontCar.getPosition();
                int positionOnNextRoad = -1;
                frontCarState = frontCar.getState();

                if (frontCarPosition > s2) {
                    positionOnNextRoad = s2;
                    if (laneToPut.putCar(car, positionOnNextRoad)) {
                        fromRoad.removeCarFromRoad(car);
                        fromRoad.getCarSequenceList(getId()).remove(car);
                        car.setCurrentSpeed(v2).setState(CarState.END).setRoadIdx(car.getRoadIdx() + 1);
                        return true;
                    } else {
                        System.err.println("putCar failed");
                    }

                } else {
                    positionOnNextRoad = frontCarPosition - 1;
                    if (frontCar.getState() == CarState.END) {
                        if (laneToPut.putCar(car, positionOnNextRoad)) {
                            fromRoad.removeCarFromRoad(car);
                            fromRoad.getCarSequenceList(getId()).remove(car);
                            car.setCurrentSpeed(v2).setState(CarState.END).setRoadIdx(car.getRoadIdx() + 1);
                            return true;
                        } else {
                            System.err.println("putCar failed");
                        }
                    } else if (frontCar.getState() == CarState.WAIT) {
                        return false;
                    } else {
                        System.err.println("unExpected state");
                    }
                }

            }
        }

        System.err.println("moveCarToNextRoad#unExpected state");
        return false;
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

    private boolean checkConflict(Car carToMove, int roadId, int to, Turn conflictRoadDirection, Turn conflictCarDirection) {
        for (int otherRoadId : roadTreeMap.keySet()) {
            if (roadId != otherRoadId && findDirection(roadId, otherRoadId) == conflictRoadDirection) {
                Road road = roadTreeMap.get(otherRoadId);
                Car car;

                //那里没有车，可以走
                if ((car = fetchCarFromList(road)) == null)
                    return false;

                // 车不走那条路，没有冲突
                int roadIdx = car.getRoadIdx();
                if ((car.getPath().size() - 1) != roadIdx && car.getPath().get(roadIdx + 1) != to) {
                    return false;
                }

                // 优先级更高，不用管别的方向
                if (!car.isPriority() && carToMove.isPriority())
                    return false;

                // 优先级低于那辆车，不能走
                if (car.isPriority() && !carToMove.isPriority())
                    return true;

                // 优先级相同，需要比较

                //同优先级直行车辆
                if (conflictCarDirection == conflictRoadDirection)
                    return false;

                // 车走那条路
                //如果别的车更高优先级，就不可以走
                int from = car.getPath().get(roadIdx);
                if (findDirection(from, to) == conflictCarDirection)
                    return true;

                System.err.println("Error#");

            }
        }
        return false;
    }

    private boolean checkConflict(Car carToMove, int from, int to) {

        int maxVal = -1;
        for (Road road : roadTreeMap.values()) {
            Car car = fetchCarFromList(road);
            if (car != null && car != carToMove) {
                int val = calculatePriority(car, road.getId(), to);
                if (val > maxVal)
                    maxVal = val;
            }
        }

        // Debug
        if(calculatePriority(carToMove, from, to) == maxVal)
            System.err.println("checkConflict#error");


        return calculatePriority(carToMove, from, to) > maxVal;
    }

    private int calculatePriority(Car car, int from, int roadToEnter) {
        int val = -1;
        Turn direction = null;
        int to = -1;
        if (car.getRoadIdx() == car.getPath().size() - 1) {
            if (findDirection(from, roadToEnter) != Turn.STRAIGHT)
                return -1;
            else
                to = roadToEnter;
        } else {
            to = car.getPath().get(car.getRoadIdx() + 1);
        }

        direction = findDirection(from, to);


        if (to != roadToEnter) {
            return -1;
        }
        if (car.isPriority()) {
            return 100 - direction.ordinal();
        } else
            return 10 - direction.ordinal();
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
