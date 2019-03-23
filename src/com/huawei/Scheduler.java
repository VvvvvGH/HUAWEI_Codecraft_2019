package com.huawei;

import java.util.*;

public class Scheduler {

    private TreeMap<Integer, CrossRoads> crossMap = new TreeMap<>();
    private TreeMap<Integer, Road> roadMap = new TreeMap<>();
    private TreeMap<Integer, Car> carMap = new TreeMap<>();
    private ArrayList<Car> garage = new ArrayList<>();

    // 基于统计的死锁检测，　若系统一段时间内状态没有发生变化，则认为是死锁
    public static boolean carStateChanged = false;
    public  final int DEADLOCK_DETECT_THRESHOLD = 100;
    public  int deadLockCounter = 0;

    public static Long totalScheduleTime;
    public static Long systemScheduleTime = 0L;
    public static final int UNIT_TIME = 1;

    //全局车辆状态统计
    public static HashMap<CarState, Integer> carStateCounter = new HashMap<CarState, Integer>() {{
        put(CarState.OFF_ROAD, 0);
        put(CarState.WAIT, 0);
        put(CarState.END, 0);
        put(CarState.IN_GARAGE, 0);
    }};

    public void runAndPrintResult() {
        // Add car to garage
        garage.addAll(carMap.values());
        // 对车库内的车按ID进行排序
        Collections.sort(garage, Car.idComparator);


        while (carStateCounter.get(CarState.OFF_ROAD) != carMap.size()) {

            System.out.printf("Car State at time %d : OFF_ROAD: %d IN_GARAGE: %d WAIT: %d END: %d  \n", systemScheduleTime, carStateCounter.get(CarState.OFF_ROAD), carStateCounter.get(CarState.IN_GARAGE), carStateCounter.get(CarState.WAIT), carStateCounter.get(CarState.END));

            step();
        }
        System.out.println("SystemScheduleTime: " + getSystemScheduleTime());
    }

    public void printCarStates() {

        System.out.printf("Car State at time %d : OFF_ROAD: %d IN_GARAGE: %d WAIT: %d END: %d  \n", systemScheduleTime, carStateCounter.get(CarState.OFF_ROAD), carStateCounter.get(CarState.IN_GARAGE), carStateCounter.get(CarState.WAIT), carStateCounter.get(CarState.END));
        if (carStateCounter.get(CarState.OFF_ROAD) == carMap.size())
            System.out.println("系统调度时间: " + getSystemScheduleTime());

    }

    public boolean stepUntilFinish(int numberOfCars) {
        while (carStateCounter.get(CarState.OFF_ROAD) != numberOfCars) {
            if (!step())
                return false;
        }
        return true;
    }

    public void stepUntilFinishDebug() {
        while (carStateCounter.get(CarState.OFF_ROAD) != carMap.size()) {
            if (!step())
                return;
            printCarStates();
        }
    }


    public boolean step() {
        //全局车辆状态标识
        carStateChanged = false;
        //系统调度时间
        systemScheduleTime += UNIT_TIME;

        //       １升序循环整个地图中所有的道路
        //       ２让所有在道路上的车开始行驶到等待或终止状态
        driveAllCarOnRoad();

        do {
            // 应该用do while
            for (CrossRoads cross : crossMap.values()) {
                cross.schedule();
            }
        } while (!allCarInEndState());

        driveCarInGarage();

        if (detectDeadLock())
            return false;

        return true;
    }


    public boolean detectDeadLock() {
        if (!carStateChanged)
            deadLockCounter++;
        if (deadLockCounter == DEADLOCK_DETECT_THRESHOLD) {
            System.err.println("Dead lock detected!");
            return true;
        }
        if (systemScheduleTime % DEADLOCK_DETECT_THRESHOLD == 0)
            deadLockCounter = 0;

        return false;
    }

    public void driveAllCarOnRoad() {
        for (Road road : roadMap.values()) {

            road.moveCarsOnRoad();
            // FIXME: Waiting queue
            if (road.isBidirectional()) {
                road.offerWaitingQueue(road.getStart());
                road.offerWaitingQueue(road.getEnd());
            } else
                road.offerWaitingQueue(road.getEnd());
        }
    }

    private void driveCarInGarage() {
        //      车辆到达实际出发时间，需要上路行驶。
        //      如果存在同时多辆到达出发时间且初始道路相同，则按车辆编号由小到大的顺序上路行驶,进入道路车道编号依然由车道小的优先进入。
        //      道路上没有车位可以上位，就等下一时刻上路
        //      需要road处理车辆上路的逻辑
        Iterator<Car> iterator = garage.iterator();
        while (iterator.hasNext()) {
            Car car = iterator.next();
            if (car.getState() != CarState.IN_GARAGE) {
                System.err.println("ERROR: 车库里出现错误状态的车。");
                iterator.remove();
                continue;
            }

            if (car.getStartTime() <= systemScheduleTime) { // 车辆到达开始时间

                // 车的第一条路
                Road road = roadMap.get(car.getPath().get(0));
                Road nextRoad = roadMap.get(car.getPath().get(1));

                int nextCrossRoadId = 0;
                // 计算下一路口的方向
                if (crossMap.get(road.getStart()) == crossMap.get(nextRoad.getStart()))
                    nextCrossRoadId = road.getStart();
                else if (crossMap.get(road.getEnd()) == crossMap.get(nextRoad.getStart()))
                    nextCrossRoadId = road.getEnd();
                else if (crossMap.get(road.getEnd()) == crossMap.get(nextRoad.getEnd()))
                    nextCrossRoadId = road.getEnd();
                else if (crossMap.get(road.getStart()) == crossMap.get(nextRoad.getEnd()))
                    nextCrossRoadId = road.getStart();


                if (road.putCarOnRoad(car, nextCrossRoadId)) {
                    // 上路成功,从车库中删除车辆。否则车等待下一时刻才开。
                    car.setStartTime(systemScheduleTime);
                    iterator.remove();
                }
            } else if (car.getStartTime() < car.getPlanTime())
                System.err.println("车不能早于计划时间出发!");
        }
    }

    private boolean allCarInEndState() {
        // 遍历所有路口
        for (CrossRoads cross : crossMap.values()) {
            if (cross.isStateChanged()) {
                return false;
            }
        }
        return true;
    }

    public void addRoad(Road road) {
        roadMap.put(road.getId(), road);
    }

    public Road getRoad(int roadId) {
        return roadMap.get(roadId);
    }

    public void addCar(Car car) {
        carMap.put(car.getId(), car);
    }

    public Car getCar(int carId) {
        return carMap.get(carId);
    }

    public void addCross(CrossRoads cross) {
        cross.addRoads(roadMap); //添加道路到路口
        crossMap.put(cross.getId(), cross);
    }

    public void addToGarage(Car car) {
        garage.add(car);
        // 对车库内的车按ID进行排序
        Collections.sort(garage, Car.idComparator);
    }

    public void clearGarage() {
        garage.clear();
    }

    public void resetCarStatusCounter() {
        carStateCounter.clear();
        carStateCounter.put(CarState.WAIT, 0);
        carStateCounter.put(CarState.IN_GARAGE, carMap.size());
        carStateCounter.put(CarState.OFF_ROAD, 0);
        carStateCounter.put(CarState.END, 0);
    }

    public void resetDeadlockCounter() {
        deadLockCounter = 0;
        carStateChanged = false;
    }

    public void reset() {
        // 重置调度器所有参数的状态
        resetCarStatusCounter();
        resetDeadlockCounter();
        clearGarage();

        systemScheduleTime = 0L;

        carMap.forEach((carId, car) -> car.resetCarState());
        roadMap.forEach((roadId, road) -> road.resetRoadState());
    }

    public Long getScheduleTime() {
        return totalScheduleTime;
    }

    public Long getSystemScheduleTime() {
        return systemScheduleTime;
    }

    public int getUNIT_TIME() {
        return UNIT_TIME;
    }

    public TreeMap<Integer, CrossRoads> getCrossMap() {
        return crossMap;
    }

    public TreeMap<Integer, Road> getRoadMap() {
        return roadMap;
    }

    public TreeMap<Integer, Car> getCarMap() {
        return carMap;
    }

    public ArrayList<Car> getGarage() {
        return garage;
    }

    public void printCarsOnRoad() {
        carMap.forEach((carId, car) -> {
            if (car.getState() != CarState.IN_GARAGE)
                System.out.printf("Car %d state %-15s position %-3d lane %d\n", carId, car.getState(), car.getPosition(), car.getLaneId());
        });
        System.out.println();
    }
}
