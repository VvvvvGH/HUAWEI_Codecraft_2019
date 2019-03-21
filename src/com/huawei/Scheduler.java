package com.huawei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

public class Scheduler {

    private TreeMap<Integer, CrossRoads> crossMap = new TreeMap<>();
    private TreeMap<Integer, Road> roadMap = new TreeMap<>();
    private TreeMap<Integer, Car> carMap = new TreeMap<>();
    private ArrayList<Car> garage = new ArrayList<>();


    private Long scheduleTime;
    private Long systemScheduleTime = 0L;
    private final int UNIT_TIME = 1;

    public static void main(String[] args) {
        if (args.length != 4) {
            return;
        }


        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];

        ArrayList<String> cars = Main.readFile(carPath);
        ArrayList<String> roads = Main.readFile(roadPath);
        ArrayList<String> crossRoads = Main.readFile(crossPath);
        ArrayList<String> answer = Main.readFile(answerPath);

        Scheduler scheduler = new Scheduler();

        roads.forEach(
                road -> scheduler.addRoad(new Road(road))
        );

        crossRoads.forEach(
                cross -> scheduler.addCross(new CrossRoads(cross))
        );

        cars.forEach(
                car -> scheduler.addCar(new Car(car))
        );
        answer.forEach(
                ans -> scheduler.updateCarFromAnswer(ans)
        );

        boolean allOut = false;
        int carInGarage = 0;
        int carWait = 0;
        int carEnd = 0;
        int carOffRoad = 0;
        while (!allOut) {
            carInGarage = 0;
            carWait = 0;
            carEnd = 0;
            carOffRoad = 0;
            allOut = true;
            for (int carId : scheduler.getCarMap().keySet()) {
                CarState carState = scheduler.getCar(carId).getState();
                if (carState != CarState.OFF_ROAD)
                    allOut = false;
                if (carState == CarState.IN_GARAGE)
                    carInGarage++;
                if (carState == CarState.OFF_ROAD)
                    carOffRoad++;
                if (carState == CarState.WAIT)
                    carWait++;
                if (carState == CarState.END)
                    carEnd++;
            }
            if (allOut == true)
                break;
            System.out.printf("Car State at time %d : OFF_ROAD: %d IN_GARAGE: %d WAIT: %d END: %d\n", scheduler.systemScheduleTime, carOffRoad, carInGarage, carWait, carEnd);

            scheduler.step();
        }
        System.out.println("SystemScheduleTime: " + scheduler.getSystemScheduleTime());

    }

    public void step() {

        printCarsOnRoad();

        //系统调度时间加1
        systemScheduleTime += UNIT_TIME;
//        while (!allCarInEndState()) {

        // TODO: １升序循环整个地图中所有的道路
        //       ２让所有在道路上的车开始行驶到等待或终止状态
        driveAllCarOnRoad();
        System.out.println("DEBUG: Step 1 DONE");

//        }
        do {
            // 应该用do while
            for (CrossRoads cross : crossMap.values()) {
                cross.schedule();
            }
//            driveAllCarOnRoad();
        } while (!allCarInEndState());
        System.out.println("DEBUG: Step 2 DONE");
        driveCarInGarage();
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
                System.out.println("next " + nextCrossRoadId);


                if (road.putCarOnRoad(car, nextCrossRoadId)) {
                    // 上路成功,从车库中删除车辆。否则车等待下一时刻才开。
                    car.setStartTime(systemScheduleTime);
                    // TODO: 上路的车处于等待状态还是终止状态呢？
//                    car.setState(CarState.WAIT);
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
                System.err.println("allCarInEndState false");
                return false;
            }
        }

        System.err.println("allCarInEndState ture");
        return true;
    }

    public void updateCarFromAnswer(String answer) {
        String[] vars = answer.split(",");
        int carId = Integer.parseInt(vars[0]);
        // 更新车辆行驶信息
        Car car = carMap.get(carId);
        car.setStartTime(Integer.parseInt(vars[1]));
        for (int i = 2; i < vars.length; i++) {
            if (Integer.parseInt(vars[i]) > 0) {
                car.addPath((Integer.parseInt(vars[i])));
            }
        }
        // 把车加入车库
        garage.add(car);
        // 对车库内的车按ID进行排序
        Collections.sort(garage, Car.idComparator);
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

    public Long getScheduleTime() {
        return scheduleTime;
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
            if(car.getState()!=CarState.IN_GARAGE)
                System.out.printf("Car %d state %-15s position %d lane \n", carId,car.getState(),car.getPosition(),car.getLaneId());
        });
        System.out.println();
    }
}
