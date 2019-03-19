package com.judgment;

import com.huawei.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class Judger {

    private ArrayList<JCrossRoads> crossList = new ArrayList<>();
    private ArrayList<JRoad> roadList = new ArrayList<>();
    private ArrayList<JCar> carList = new ArrayList<>();

    private Long scheduleTime;
    private Long systemScheduleTime;
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

        judger.judge();

    }

    public void judge(){
        Collections.sort(roadList);
        Collections.sort(carList,JCar.IdComparator);
        while(allCarInEndState()){
            for (JRoad road : roadList) {
                for(int i=0;i<road.getNumOfLanes();i++){
                    ArrayList<JCar> carList = road.getCarOfOnePane(i);
                    Collections.sort(carList,JCar.CrossDisComparator);
                    for(JCar car : carList){

                    }
                }
            }
        }
    }

    private boolean allCarInEndState(){
        for(JCar car : carList){
            if(car.getState() >= 0)
                return false;
        }
        return true;
    }

    public void addCross(JCrossRoads cross) {
        crossList.add(cross);
    }

    public JCrossRoads getCross(int crossId) {
        return crossList.get(crossId);
    }

    public void addRoad(JRoad road) {
        roadList.add(road);
    }

    public JRoad getRoad(int roadId) {
        return roadList.get(roadId);
    }

    public void addCar(JCar car) {
        carList.add(car);
    }

    public JCar getCar(int carId) {
        return carList.get(carId);
    }

    public ArrayList<JCar> getCarList() {
        return carList;
    }

}
