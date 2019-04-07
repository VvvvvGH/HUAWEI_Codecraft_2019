package com.huawei;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Scheduler {

    private TreeMap<Integer, CrossRoads> crossMap = new TreeMap<>();
    private TreeMap<Integer, Road> roadMap = new TreeMap<>();
    private TreeMap<Integer, Car> carMap = new TreeMap<>();

    private HashMap<Long, HashMap<String, Object>> timeStateMap = new HashMap<>();

    // 基于统计的死锁检测，　若系统一段时间内状态没有发生变化，则认为是死锁
    public static boolean carStateChanged = false;
    public final int DEADLOCK_DETECT_THRESHOLD = 1000;
    public int deadLockCounter = 0;

    public static Long totalScheduleTime = 0L;
    public static Long systemScheduleTime = 0L;
    public static final int UNIT_TIME = 1;

    //全局车辆状态统计
    public static HashMap<CarState, Integer> carStateCounter = new HashMap<CarState, Integer>() {{
        put(CarState.OFF_ROAD, 0);
        put(CarState.WAIT, 0);
        put(CarState.END, 0);
        put(CarState.IN_GARAGE, 0);
    }};

    public void printCarStates() {

        System.out.printf("Car State at time %d : OFF_ROAD: %d IN_GARAGE: %d WAIT: %d END: %d  \n", systemScheduleTime, carStateCounter.get(CarState.OFF_ROAD), carStateCounter.get(CarState.IN_GARAGE), carStateCounter.get(CarState.WAIT), carStateCounter.get(CarState.END));
        if (carStateCounter.get(CarState.WAIT) == 0 && carStateCounter.get(CarState.END) == 0 && carStateCounter.get(CarState.IN_GARAGE) == 0) {
            System.out.println("系统调度时间: " + systemScheduleTime);
            System.out.println("所有车辆实际总调度时间: " + totalScheduleTime);
        }

    }

    public boolean stepUntilFinish() {
        while (carStateCounter.get(CarState.WAIT) != 0 || carStateCounter.get(CarState.END) != 0 || carStateCounter.get(CarState.IN_GARAGE) != 0) {
            if (!step())
                return false;
        }
        return true;
    }

    public boolean stepUntilFinishDebug() {
        while (carStateCounter.get(CarState.WAIT) != 0 || carStateCounter.get(CarState.END) != 0 || carStateCounter.get(CarState.IN_GARAGE) != 0) {
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
        // 优先上路车辆
        driveCarInGarage(true);
        while (!allCarInEndState()) {
            //全局车辆状态标识
            carStateChanged = false;

            // 应该用do while
            for (CrossRoads cross : crossMap.values()) {
                cross.schedule(this);
            }

            if (detectDeadLock())
                return false;

        }
        // 所有车辆上路
        driveCarInGarage(false);

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
                road.createSequenceList(road.getStart());
                road.createSequenceList(road.getEnd());
            } else
                road.createSequenceList(road.getEnd());
        }
    }

    public void driveCarInGarage(boolean highPriority) {
        for (Road road : roadMap.values()) {
            road.runCarsInGarage(highPriority);
        }
    }

    private boolean allCarInEndState() {
        if (Scheduler.carStateCounter.get(CarState.WAIT) != 0)
            return false;
        // 无需遍历所有路口，上面已经达到判断的目的
//        for (CrossRoads cross : crossMap.values()) {
//            if (cross.isStateChanged()) {
//                return false;
//            }
//        }
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
        roadMap.get(car.getPath().get(0)).addToGarage(car);
    }


    public void resetCarStatusCounter() {
        carStateCounter.clear();
        carStateCounter.put(CarState.WAIT, 0);
        carStateCounter.put(CarState.IN_GARAGE, 0);
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


        systemScheduleTime = 0L;
        totalScheduleTime = 0L;

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


        stateMap.put("garage", garageToSave);

        stateMap.put("totalScheduleTime", totalScheduleTime);
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


        totalScheduleTime = (Long) stateMap.get("totalScheduleTime");
        systemScheduleTime = (Long) stateMap.get("systemScheduleTime");
        carStateChanged = (boolean) stateMap.get("carStateChanged");
        deadLockCounter = (int) stateMap.get("deadLockCounter");


        carStateCounter.put(CarState.WAIT, (Integer) stateMap.get("CarState.WAIT"));
        carStateCounter.put(CarState.IN_GARAGE, (Integer) stateMap.get("CarState.IN_GARAGE"));
        carStateCounter.put(CarState.OFF_ROAD, (Integer) stateMap.get("CarState.OFF_ROAD"));
        carStateCounter.put(CarState.END, (Integer) stateMap.get("CarState.WAIT"));

    }

    public static void main(String[] args) {

        ArrayList<String> cars = Main.readFile("/home/cheng/IdeaProjects/HUAWEI_Codecraft_2019/SDK_java/bin/config/car.txt");
        ArrayList<String> roads = Main.readFile("/home/cheng/IdeaProjects/HUAWEI_Codecraft_2019/SDK_java/bin/config/road.txt");
        ArrayList<String> crossRoads = Main.readFile("/home/cheng/IdeaProjects/HUAWEI_Codecraft_2019/SDK_java/bin/config/cross.txt");
        ArrayList<String> presetAnswers = Main.readFile("/home/cheng/IdeaProjects/HUAWEI_Codecraft_2019/SDK_java/bin/config/presetAnswer.txt");
        ArrayList<String> answers = Main.readFile("/home/cheng/IdeaProjects/HUAWEI_Codecraft_2019/SDK_java/bin/config/answer.txt");

        answers.addAll(presetAnswers);

        Scheduler scheduler = new Scheduler();


        // Add road first. Then add cross
        roads.forEach(
                roadLine -> {
                    Road road = new Road(roadLine);
                    scheduler.addRoad(road);
                }
        );

        crossRoads.forEach(
                crossLine -> {
                    CrossRoads cross = new CrossRoads(crossLine);
                    scheduler.addCross(cross);
                }
        );

        cars.forEach(
                carLine -> {
                    Car car = new Car(carLine);
                    scheduler.addCar(car);
                }
        );

//        presetAnswers.forEach(
//                answerLine -> {
//                    String[] vars = answerLine.split(",");
//                    int carId = Integer.parseInt(vars[0]);
//                    Car car = scheduler.getCar(carId);
//                    car.setStartTime(Integer.parseInt(vars[1]));
//                    car.clearPath();
//
//                    if (car.getPath().size() == 0) {
//                        for (int i = 2; i < vars.length; i++) {
//                            car.addPath((Integer.parseInt(vars[i])));
//                        }
//                    }
//                    scheduler.addToGarage(car);
//                }
//        );
        answers.forEach(
                answer -> {
                    scheduler.updateCarFromAnswer(answer);
                }
        );
        long startTime = System.currentTimeMillis();

        scheduler.stepUntilFinish();
        scheduler.printCarStates();

        long endTime = System.currentTimeMillis();
        System.out.println("Main程序运行时间：" + (endTime - startTime) + "ms");
    }

    public void updateCarFromAnswer(String answer) {
        String[] vars = answer.split(",");
        int carId = Integer.parseInt(vars[0]);
        // 更新车辆行驶信息
        Car car = carMap.get(carId);
        car.setStartTime(Integer.parseInt(vars[1]));
        if (car.getPath().size() == 0) {
            for (int i = 2; i < vars.length; i++) {
                if (Integer.parseInt(vars[i]) > 0) {
                    car.addPath((Integer.parseInt(vars[i])));
                }
            }
        }
        // 把车加入车库
        addToGarage(car);
    }


}
