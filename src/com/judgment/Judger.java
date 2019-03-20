package com.judgment;

import com.huawei.Car;
import com.huawei.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class Judger {

    private TreeMap<Integer, JCrossRoads> crossMap = new TreeMap<>();
    private TreeMap<Integer, JRoad> roadMap = new TreeMap<>();
    private TreeMap<Integer, JCar> carMap = new TreeMap<>();

    private ArrayList<JCar> garage = new ArrayList<>();


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

        Judger judger = new Judger();

        roads.forEach(
                road -> judger.addRoad(new JRoad(road))
        );

        crossRoads.forEach(
                cross -> judger.addCross(new JCrossRoads(cross))
        );

        cars.forEach(
                car -> judger.addCar(new JCar(car))
        );
        answer.forEach(
                ans -> judger.updateCarFromAnswer(ans)
        );

        judger.judge();

    }

    public void judge() {
        //系统调度时间加1
        systemScheduleTime += UNIT_TIME;
        while (allCarInEndState()) {

            // TODO: １升序循环整个地图中所有的道路
            //       ２让所有在道路上的车开始行驶到等待或终止状态
            driveAllCarOnRoad();

        }

        while (allCarInEndState()) {
            // TODO: 1升序循环所有路口
            //       2由路口来控制　升序遍历每个路口的所有道路直到所有车为终止状态　同时把过路口的车安排到新的道路
            for (JCrossRoads cross : crossMap.values()) {
                cross.schedule();
            }
            driveAllCarOnRoad();
        }
        driveCarInGarage();

    }

    public void driveAllCarOnRoad() {
        for (JRoad road : roadMap.values()) {
            road.moveCarsOnRoad();
        }
    }

    private void driveCarInGarage() {
        //      车辆到达实际出发时间，需要上路行驶。
        //      如果存在同时多辆到达出发时间且初始道路相同，则按车辆编号由小到大的顺序上路行驶,进入道路车道编号依然由车道小的优先进入。
        //      道路上没有车位可以上位，就等下一时刻上路
        //      需要road处理车辆上路的逻辑
        garage.forEach(car -> {
            if (car.getPlanTime() <= systemScheduleTime) { // 车辆到达开始时间
                // 车的第一条路
                JRoad road = roadMap.get(car.getPath().get(0));
                if (road.moveToRoad(car)) {
                    // 上路成功,从车库中删除车辆。否则车等待下一时刻才开。
                    garage.remove(car);
                }
            }
        });


    }

    private boolean allCarInEndState() {
        return true;
    }

    public void updateCarFromAnswer(String answer) {
        String[] vars = answer.split(",");
        int carId = Integer.parseInt(vars[0]);
        // 更新车辆行驶信息
        JCar car = carMap.get(carId);
        car.setStartTime(Integer.parseInt(vars[1]));
        for (int i = 2; i < vars.length; i++) {
            if (Integer.parseInt(vars[i]) > 0) {
                car.addPath((Integer.parseInt(vars[i])));
            }
        }
        // 把车加入车库
        garage.add(car);
        // 对车库内的车按ID进行排序
        Collections.sort(garage, JCar.idComparator);
    }


    public void addRoad(JRoad road) {
        roadMap.put(road.getId(), road);
    }

    public JRoad getRoad(int roadId) {
        return roadMap.get(roadId);
    }

    public void addCar(JCar car) {
        carMap.put(car.getId(), car);
    }

    public JCar getCar(int carId) {
        return carMap.get(carId);
    }

    public void addCross(JCrossRoads cross) {
        cross.addRoads(roadMap); //添加道路到路口
        crossMap.put(cross.getId(), cross);
    }
}
