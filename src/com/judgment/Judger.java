package com.judgment;

import com.huawei.Main;

import java.util.ArrayList;
import java.util.TreeMap;

public class Judger {

    private TreeMap<Integer, JCrossRoads> crossMap = new TreeMap<>();
    private TreeMap<Integer, JRoad> roadMap = new TreeMap<>();
    private TreeMap<Integer, JCar> carMap = new TreeMap<>();


    private Long scheduleTime;
    private Long systemScheduleTime = 0L;
    private int unitScheduleTime = 1;

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

        // TODO: １升序循环整个地图中所有的道路
        //        ２让所有在道路上的车开始行驶到等待或终止状态
        driveAllCarOnRoad();


        // TODO: 1升序循环所有路口
        //          2由路口来控制　升序遍历每个路口的所有道路直到所有车为终止状态　同时把过路口的车安排到新的道路
        for(JCrossRoads cross : crossMap.values()){
            cross.schedule();
        }

        driveCarInGarage();

    }
    public void driveAllCarOnRoad(){
        for(JRoad road : roadMap.values()){
            road.moveCars();
        }
    }

    private void driveCarInGarage() {

    }

    private boolean allCarInEndState() {
        return true;
    }

    public void updateCarFromAnswer(String answer) {
        String[] vars = answer.split(",");
        int carId = Integer.parseInt(vars[0]);
        JCar car = carMap.get(carId);
        car.setStartTime(Integer.parseInt(vars[1]));

        for (int i = 2; i < vars.length; i++) {
            if (Integer.parseInt(vars[i]) > 0) {
                car.addPath((Integer.parseInt(vars[i])));
            }
        }

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

    public void addCross(JCrossRoads cross){
        crossMap.put(cross.getId(),cross);
    }
}
