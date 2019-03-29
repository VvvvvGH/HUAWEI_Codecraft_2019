package com.huawei;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class Scheduler {

    private TreeMap<Integer, CrossRoads> crossMap = new TreeMap<>();
    private TreeMap<Integer, Road> roadMap = new TreeMap<>();
    private TreeMap<Integer, Car> carMap = new TreeMap<>();
    private ArrayList<Car> garage = new ArrayList<>();

    private HashMap<Long, HashMap<String, Object>> timeStateMap = new HashMap<>();

    // 基于统计的死锁检测，　若系统一段时间内状态没有发生变化，则认为是死锁
    public static boolean carStateChanged = false;
    public final int DEADLOCK_DETECT_THRESHOLD = 1000;
    public int deadLockCounter = 0;

    public static Long totalScheduleTime = 0L;
    public static Long totalActualScheduleTime = 0L;
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
        if (carStateCounter.get(CarState.WAIT)==0&&carStateCounter.get(CarState.END)==0&&carStateCounter.get(CarState.IN_GARAGE)==0) {
            System.out.println("系统调度时间: " + systemScheduleTime);
            System.out.println("所有车辆实际总调度时间: " + totalScheduleTime);
            System.out.println("所有车辆总调度时间: " + totalActualScheduleTime);
        }

    }

    public boolean stepUntilFinish() {
        while (carStateCounter.get(CarState.WAIT)!=0||carStateCounter.get(CarState.END)!=0||carStateCounter.get(CarState.IN_GARAGE)!=0) {
            if (!step())
                return false;
        }
        return true;
    }

    public boolean stepUntilFinishDebug(int numberOfCars) {
        while (carStateCounter.get(CarState.OFF_ROAD) != numberOfCars) {
            if (!stepWithPlot())
                return false;
        }
        return true;
    }

    public boolean stepWithPlot() {
        plotScheduleState();
        return step();
    }

    public boolean step() {

        //系统调度时间
        systemScheduleTime += UNIT_TIME;

        //       １升序循环整个地图中所有的道路
        //       ２让所有在道路上的车开始行驶到等待或终止状态
        driveAllCarOnRoad();

        do {
            //全局车辆状态标识
            carStateChanged = false;

            // 应该用do while
            for (CrossRoads cross : crossMap.values()) {
                cross.schedule();
            }

            if (detectDeadLock())
                return false;
        } while (!allCarInEndState());

        driveCarInGarage();

        return true;
    }


    public boolean detectDeadLock() {
        if (!carStateChanged)
            deadLockCounter++;
        else
            deadLockCounter = 0;
        if (deadLockCounter == DEADLOCK_DETECT_THRESHOLD) {
            System.err.println("Dead lock detected!");
            return true;
        }

        return false;
    }

    public void driveAllCarOnRoad() {
        for (Road road : roadMap.values()) {

            road.moveCarsOnRoad();
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
                    car.setActualStartTime(systemScheduleTime);
                    iterator.remove();
                }
            } else if (car.getStartTime() < car.getPlanTime())
                System.err.println("车不能早于计划时间出发!");
        }
    }

    private boolean allCarInEndState() {
        if (Scheduler.carStateCounter.get(CarState.WAIT) != 0)
            return false;
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
        car.setState(CarState.IN_GARAGE);
        garage.add(car);
        // 对车库内的车按ID进行排序
        Collections.sort(garage, Car.idComparator);
    }

    public void addAllToGarage(ArrayList<Car> cars) {
        cars.forEach(car -> {
            car.setState(CarState.IN_GARAGE);
            garage.add(car);
        });
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
        totalScheduleTime = 0L;
        totalActualScheduleTime = 0L;

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

    public void plotScheduleState() {
        String dataFilePath = "SDK_java/bin/config/data.txt";
        exportScheduleState(dataFilePath);
        String cmd = "python3 plotMap/visualization.py\n";
        try {
            Process exeEcho = Runtime.getRuntime().exec("bash");
            exeEcho.getOutputStream().write(cmd.getBytes());
            exeEcho.getOutputStream().flush();

            //等待200毫秒,让Python画图
            Thread.currentThread().sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportScheduleState(String dataFilePath) {
        try (BufferedWriter br = new BufferedWriter(new FileWriter(dataFilePath))) {
            br.write("time:" + systemScheduleTime + "\n");

            for (Road road : roadMap.values()) {
                br.write(exportRoadLaneList(road, "forward") + "\n");
                if (road.isBidirectional()) {
                    br.write(exportRoadLaneList(road, "backward") + "\n");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String exportRoadLaneList(Road road, String direction) {
        StringBuilder builder = new StringBuilder();
        ArrayList<Lane> laneList;
        if (direction.equals("forward"))
            laneList = road.getLaneListBy(road.getEnd());
        else
            laneList = road.getLaneListBy(road.getStart());

        builder.append(String.format("(%s,%s,[", road.getId(), direction));

        for (int i = 0; i < road.getNumOfLanes(); i++) {
            builder.append("[");
            Lane lane = laneList.get(i);

            for (int j = road.getLen(); j >= 1; j--) {
                Car car = lane.getCarMap().get(j);
                if (car == null)
                    builder.append("-1");
                else
                    builder.append(car.getId());

                builder.append(",");
            }
            builder.append("],");
        }
        builder.append("])");

        return builder.toString();
    }

    public void saveSchedulerState(long time) {

        HashMap<String, Object> stateMap = new HashMap<>();

        ArrayList<Car.CarStates> carStates = new ArrayList<>();

        carMap.values().forEach(car ->
                carStates.add(car.dumpStates())
        );

        stateMap.put("carState", carStates);

        ArrayList<Road.RoadStates> roadStates = new ArrayList<>();

        roadMap.values().forEach(road ->
                roadStates.add(road.dumpStates())
        );

        stateMap.put("roadState", roadStates);


        ArrayList<Car> garageToSave = new ArrayList<>();
        garageToSave.addAll(garage);

        stateMap.put("garage", garageToSave);

        stateMap.put("totalScheduleTime", totalScheduleTime);
        stateMap.put("totalActualScheduleTime", totalActualScheduleTime);
        stateMap.put("systemScheduleTime", systemScheduleTime);
        stateMap.put("carStateChanged", carStateChanged);
        stateMap.put("deadLockCounter", deadLockCounter);


        stateMap.put("CarState.WAIT", carStateCounter.get(CarState.WAIT));
        stateMap.put("CarState.IN_GARAGE", carStateCounter.get(CarState.IN_GARAGE));
        stateMap.put("CarState.OFF_ROAD", carStateCounter.get(CarState.OFF_ROAD));
        stateMap.put("CarState.END", carStateCounter.get(CarState.END));

        timeStateMap.put(time, stateMap);
    }

    public void restoreSchedulerState(long time) {

        HashMap<String, Object> stateMap = timeStateMap.get(time);

        ArrayList<Car.CarStates> carStates = (ArrayList<Car.CarStates>) stateMap.get("carState");

        carStates.forEach(carState -> {
            carMap.get(carState.getId()).restoreStates(carState);
        });

        ArrayList<Road.RoadStates> roadStates = (ArrayList<Road.RoadStates>) stateMap.get("roadState");

        roadStates.forEach(roadState -> {
            roadMap.get(roadState.getRoadId()).restoreStates(roadState);
        });

        garage = (ArrayList<Car>) stateMap.get("garage");
        // 对车库内的车按ID进行排序
        Collections.sort(garage, Car.idComparator);


        totalScheduleTime = (Long) stateMap.get("totalScheduleTime");
        totalActualScheduleTime = (Long) stateMap.get("totalActualScheduleTime");
        systemScheduleTime = (Long) stateMap.get("systemScheduleTime");
        carStateChanged = (boolean) stateMap.get("carStateChanged");
        deadLockCounter = (int) stateMap.get("deadLockCounter");


        carStateCounter.put(CarState.WAIT, (Integer) stateMap.get("CarState.WAIT"));
        carStateCounter.put(CarState.IN_GARAGE, (Integer) stateMap.get("CarState.IN_GARAGE"));
        carStateCounter.put(CarState.OFF_ROAD, (Integer) stateMap.get("CarState.OFF_ROAD"));
        carStateCounter.put(CarState.END, (Integer) stateMap.get("CarState.WAIT"));

    }


}
