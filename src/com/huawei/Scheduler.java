package com.huawei;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.*;

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
    public static Long specialScheduleTime = 0L;
    public static Long totalSpecialScheduleTime = 0L;
    public static final int UNIT_TIME = 1;

    // factor information
    public static Long numOfPriorityCars = 0L;

    public static int maxSpeedOfAllCars = 0;
    public static int minSpeedOfAllCars = 999;
    public static int maxSpeedOfPriorityCars = 0;
    public static int minSpeedOfPriorityCars = 999;


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
            long minPlanTimeOfAllCars = 999;
            long maxPlanTimeOfAllCars = 0;
            long minPlanTimeOfPriorityCars = 999;
            long maxPlanTimeOfPriorityCars = 0;


            Set allCarStartDistribute = new HashSet();
            Set allCarEndDistribute = new HashSet();

            Set priorityCarStartDistribute = new HashSet();
            Set priorityCarEndDistribute = new HashSet();


            for (Car car : carMap.values()) {
                if (car.isPriority() && car.getPlanTime() < minPlanTimeOfPriorityCars)
                    minPlanTimeOfPriorityCars = car.getPlanTime();
                if (car.getPlanTime() < minPlanTimeOfAllCars)
                    minPlanTimeOfAllCars = car.getPlanTime();

                if (car.isPriority() && car.getPlanTime() > maxPlanTimeOfPriorityCars)
                    maxPlanTimeOfPriorityCars = car.getPlanTime();
                if (car.getPlanTime() > maxPlanTimeOfAllCars)
                    maxPlanTimeOfAllCars = car.getPlanTime();

                allCarStartDistribute.add(car.getFrom());
                allCarEndDistribute.add(car.getTo());
                if (car.isPriority()) {
                    priorityCarStartDistribute.add(car.getFrom());
                    priorityCarEndDistribute.add(car.getTo());
                }
            }

            double factorA = (formatFive(carMap.size() / (numOfPriorityCars * 1.0))) * 0.05 +
                    formatFive(formatFive(maxSpeedOfAllCars * 1.0 / minSpeedOfAllCars) / formatFive(maxSpeedOfPriorityCars * 1.0 / minSpeedOfPriorityCars)) * 0.2375 +
                    formatFive(formatFive(maxPlanTimeOfAllCars * 1.0 / minPlanTimeOfAllCars) / formatFive(maxPlanTimeOfPriorityCars * 1.0 / minPlanTimeOfPriorityCars)) * 0.2375 +
                    formatFive(allCarStartDistribute.size() * 1.0 / priorityCarStartDistribute.size()) * 0.2375 +
                    formatFive(allCarEndDistribute.size() * 1.0 / priorityCarEndDistribute.size()) * 0.2375;

            double factorB = formatFive(carMap.size() / (numOfPriorityCars * 1.0)) * 0.8 +
                    formatFive(formatFive(maxSpeedOfAllCars * 1.0 / minSpeedOfAllCars) / formatFive(maxSpeedOfPriorityCars * 1.0 / minSpeedOfPriorityCars)) * 0.05 +
                    formatFive(formatFive(maxPlanTimeOfAllCars * 1.0 / minPlanTimeOfAllCars) / formatFive(maxPlanTimeOfPriorityCars * 1.0 / minPlanTimeOfPriorityCars)) * 0.05 +
                    formatFive(allCarStartDistribute.size() * 1.0 / priorityCarStartDistribute.size()) * 0.05 +
                    formatFive(allCarEndDistribute.size() * 1.0 / priorityCarEndDistribute.size()) * 0.05;


            System.out.println("优先车辆调度时间: " + (specialScheduleTime - minPlanTimeOfPriorityCars));
            System.out.println("优先车辆总调度时间: " + totalSpecialScheduleTime);
            System.out.println("原系统调度时间: " + systemScheduleTime);
            System.out.println("原所有车辆实际总调度时间: " + totalScheduleTime);
            System.out.println("系统调度时间: " + Math.round(systemScheduleTime + factorA * (specialScheduleTime - minPlanTimeOfPriorityCars)));
            System.out.println("所有车辆实际总调度时间: " +(totalScheduleTime + formatFive(factorB) * totalSpecialScheduleTime));
        }
    }

    public double formatFive(double value) {
        DecimalFormat df = new DecimalFormat("0.00000");
        return Double.parseDouble(df.format(value));
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
            if (!step())
                return false;
            printCarStates();
        }
        return true;
    }

    public boolean stepWithExport() {
        DecimalFormat df = new DecimalFormat("0000");
        exportScheduleState("SDK_java/bin/config/" + df.format(systemScheduleTime) + ".log");
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

//    public boolean step(TrafficMap trafficMap) {
//        //系统调度时间
//        systemScheduleTime += UNIT_TIME;
//
//        //       １升序循环整个地图中所有的道路
//        //       ２让所有在道路上的车开始行驶到等待或终止状态
//        driveAllCarOnRoad();
//        trafficMap.putPriorityCar(this);
//        // 优先上路车辆
//        driveCarInGarage(true);
//        while (!allCarInEndState()) {
//            //全局车辆状态标识
//            carStateChanged = false;
//
//            // 应该用do while
//            for (CrossRoads cross : crossMap.values()) {
//                cross.schedule(this);
//            }
//
//            if (detectDeadLock())
//                return false;
//
//        }
//        trafficMap.putNormalCar(this);
//        // 所有车辆上路
//        driveCarInGarage(false);
//
//        return true;
//    }


    public boolean detectDeadLock() {
        if (!carStateChanged)
            deadLockCounter++;
        else
            deadLockCounter = 0;
        if (deadLockCounter == DEADLOCK_DETECT_THRESHOLD) {
            System.err.println("Dead lock detected!");
            System.out.println();
            printDeadLockRoads();
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

    public void rollback(long time) {
        // 重置调度器所有参数的状态
        resetCarStatusCounter();
        resetDeadlockCounter();

        systemScheduleTime = 0L;
        totalScheduleTime = 0L;

        carMap.forEach((carId, car) -> car.rollbackCarState(time));
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
            for (Road road : roadMap.values()) {
                br.write("# Road ID = " + road.getId() + "\n");
                br.write(exportRoadLaneList(road, "forward"));
                if (road.isBidirectional()) {
                    br.write(exportRoadLaneList(road, "backward"));
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

        for (int i = 0; i < road.getNumOfLanes(); i++) {
            builder.append("(");
            Lane lane = laneList.get(i);

            for (int j = road.getLen(); j >= 1; j--) {
                Car car = lane.getCarMap().get(j);
                if (car != null)
                    builder.append(String.format("(%s, %s), ", car.getId(), lane.getLength() - car.getPosition()));
            }
            builder.append(")\n");
        }

        return builder.toString();
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

        scheduler.stepUntilFinishDebug();

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

    public void printDeadLockRoads() {
        System.out.print("DeadLock position = [");
        for (Road road : roadMap.values()) {
            if (road.getCarSequenceList(road.getEnd()).size() != 0) {
                System.out.print(road.getId() + ", ");
            }
            if (road.isBidirectional()) {
                if (road.getCarSequenceList(road.getStart()).size() != 0) {
                    System.out.print(road.getId() + ", ");
                }
            }
        }
        System.out.println("]");
    }

    public boolean havePresetCarOnRoad() {
        for (Car car : carMap.values()) {
            if (car.isPreset() && car.getState() != CarState.OFF_ROAD) {
                return true;
            }
        }
        return false;
    }

    public boolean havePriorityCarOnRoad() {
        for (Car car : carMap.values()) {
            if (car.isPriority() && car.getState() != CarState.OFF_ROAD) {
                return true;
            }
        }
        return false;
    }

    public long resetDeadlockedCars() {
        ArrayList<Car> deadlockCarList = new ArrayList<>();
        long minTime = 9999L;
        for (Road road : roadMap.values()) {
            ArrayList<Car> list = road.getCarSequenceList(road.getEnd());
            if (list.size() != 0) {
                for (Car car : list) {
                    if (!car.isPriority() && !car.isPreset()) {
                        if (car.getStartTime() < minTime)
                            minTime = car.getStartTime();
                        car.resetCarState();
                        deadlockCarList.add(car);
                    }
                }
            }
            if (road.isBidirectional()) {
                list = road.getCarSequenceList(road.getStart());
                if (list.size() != 0) {
                    for (Car car : list) {
                        if (!car.isPriority() && !car.isPreset()) {
                            if (car.getStartTime() < minTime)
                                minTime = car.getStartTime();
                            car.resetCarState();
                            deadlockCarList.add(car);
                        }
                    }
                }
            }
        }
        return minTime;
    }

}
