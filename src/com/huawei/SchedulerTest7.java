package com.huawei;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SchedulerTest7 {
    private Scheduler scheduler = new Scheduler();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {

        //Road(int id, int len, int topSpeed, int numOfLanes, int start, int end, boolean bidirectional)
        //CrossRoads(int id,int road1,int road2,int road3,int road4) {
        //Car (int id, int start, int to, int topSpeed, int planTime)
        scheduler.addCross(new CrossRoads(11, -1, 1, 4, -1));
        scheduler.addCross(new CrossRoads(12, -1, -1, 2, 1));
        scheduler.addCross(new CrossRoads(13, 2, -1, -1, 3));
        scheduler.addCross(new CrossRoads(14, 4, 3, -1, -1));

        scheduler.addRoad(new Road(1, 10, 8, 1, 11, 12, false));
        scheduler.addRoad(new Road(2, 10, 8, 1, 12, 13, false));
        scheduler.addRoad(new Road(3, 10, 8, 1, 13, 14, false));
        scheduler.addRoad(new Road(4, 10, 8, 1, 14, 11, false));

        scheduler.getCrossMap().get(11).addRoads(scheduler.getRoadMap());
        scheduler.getCrossMap().get(12).addRoads(scheduler.getRoadMap());
        scheduler.getCrossMap().get(13).addRoads(scheduler.getRoadMap());
        scheduler.getCrossMap().get(14).addRoads(scheduler.getRoadMap());


        scheduler.addCar(new Car(100, 14, 12, 6, 1));
        scheduler.addCar(new Car(200, 14, 12, 6, 1));
        scheduler.addCar(new Car(300, 13, 11, 6, 1));
        scheduler.addCar(new Car(400, 13, 11, 6, 1));
        scheduler.addCar(new Car(500, 12, 14, 6, 1));
        scheduler.addCar(new Car(600, 12, 14, 6, 1));
        scheduler.addCar(new Car(700, 11, 13, 6, 1));
        scheduler.addCar(new Car(800, 11, 13, 6, 1));


        scheduler.getCarMap().get(100).setState(CarState.WAIT).setCurrentSpeed(6).setLaneId(1).setStartTime(1).setPosition(8).addPath(3).addPath(4).addPath(1);
        scheduler.getCarMap().get(200).setState(CarState.WAIT).setCurrentSpeed(6).setLaneId(1).setStartTime(1).setPosition(2).addPath(3).addPath(4).addPath(1);
        scheduler.getCarMap().get(300).setState(CarState.WAIT).setCurrentSpeed(6).setLaneId(1).setStartTime(1).setPosition(8).addPath(2).addPath(3).addPath(4);
        scheduler.getCarMap().get(400).setState(CarState.WAIT).setCurrentSpeed(6).setLaneId(1).setStartTime(1).setPosition(2).addPath(2).addPath(3).addPath(4);
        scheduler.getCarMap().get(500).setState(CarState.WAIT).setCurrentSpeed(6).setLaneId(1).setStartTime(1).setPosition(9).addPath(1).addPath(2).addPath(3);
        scheduler.getCarMap().get(600).setState(CarState.WAIT).setCurrentSpeed(6).setLaneId(1).setStartTime(1).setPosition(3).addPath(1).addPath(2).addPath(3);
        scheduler.getCarMap().get(700).setState(CarState.WAIT).setCurrentSpeed(6).setLaneId(1).setStartTime(1).setPosition(9).addPath(4).addPath(1).addPath(2);
        scheduler.getCarMap().get(800).setState(CarState.WAIT).setCurrentSpeed(6).setLaneId(1).setStartTime(1).setPosition(3).addPath(4).addPath(1).addPath(2);

        scheduler.getRoad(4).getLaneList().get(0).putCar(scheduler.getCar(100),scheduler.getCar(100).getPosition());
        scheduler.getRoad(4).getLaneList().get(0).putCar(scheduler.getCar(200),scheduler.getCar(200).getPosition());

        scheduler.getRoad(1).getLaneList().get(0).putCar(scheduler.getCar(700),scheduler.getCar(700).getPosition());
        scheduler.getRoad(1).getLaneList().get(0).putCar(scheduler.getCar(800),scheduler.getCar(800).getPosition());

        scheduler.getRoad(2).getLaneList().get(0).putCar(scheduler.getCar(500),scheduler.getCar(500).getPosition());
        scheduler.getRoad(2).getLaneList().get(0).putCar(scheduler.getCar(600),scheduler.getCar(600).getPosition());

        scheduler.getRoad(3).getLaneList().get(0).putCar(scheduler.getCar(300),scheduler.getCar(300).getPosition());
        scheduler.getRoad(3).getLaneList().get(0).putCar(scheduler.getCar(400),scheduler.getCar(400).getPosition());

        scheduler.getRoad(1).offerWaitingQueue(12);
        scheduler.getRoad(2).offerWaitingQueue(13);
        scheduler.getRoad(3).offerWaitingQueue(14);
        scheduler.getRoad(4).offerWaitingQueue(11);

    }

    @org.junit.jupiter.api.Test
    void priorityQueue() {
    }

    @org.junit.jupiter.api.Test
    void driveAllCarOnRoad() {
        scheduler.driveAllCarOnRoad();
    }

    @org.junit.jupiter.api.Test
    void step() {
//        scheduler.step();

        do {
            // 应该用do while
            for (CrossRoads cross : scheduler.getCrossMap().values()) {
                cross.schedule();
            }
        } while (!allCarInEndState());

        scheduler.printCarStates();
        System.out.println();
    }

    private boolean allCarInEndState() {

        if(Scheduler.carStateCounter.get(CarState.WAIT)!=0)
            return false;

        // 遍历所有路口
        for (CrossRoads cross : scheduler.getCrossMap().values()) {
            if (cross.isStateChanged()) {
                return false;
            }
        }
        return true;
    }

}