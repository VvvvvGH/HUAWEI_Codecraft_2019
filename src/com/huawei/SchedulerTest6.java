package com.huawei;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SchedulerTest6 {
    private Scheduler scheduler = new Scheduler();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {

        //Road(int id, int len, int topSpeed, int numOfLanes, int start, int end, boolean bidirectional)
        //CrossRoads(int id,int road1,int road2,int road3,int road4) {
        //Car (int id, int start, int to, int topSpeed, int planTime)
        scheduler.addCross(new CrossRoads(11, -1, -1, 1, -1));
        scheduler.addCross(new CrossRoads(12, -1, -1, -1, 2));
        scheduler.addCross(new CrossRoads(13, 3, -1, -1, -1));
        scheduler.addCross(new CrossRoads(14, -1, 4, -1, -1));
        scheduler.addCross(new CrossRoads(15, 1, 2, 3, 4));

        scheduler.addRoad(new Road(1, 10, 5, 3, 11, 15, false));
        scheduler.addRoad(new Road(2, 10, 5, 3, 15, 12, false));
        scheduler.addRoad(new Road(3, 10, 5, 3, 13, 15, false));
        scheduler.addRoad(new Road(4, 10, 5, 3, 14, 15, false));

        scheduler.getCrossMap().get(11).addRoads(scheduler.getRoadMap());
        scheduler.getCrossMap().get(12).addRoads(scheduler.getRoadMap());
        scheduler.getCrossMap().get(13).addRoads(scheduler.getRoadMap());
        scheduler.getCrossMap().get(14).addRoads(scheduler.getRoadMap());
        scheduler.getCrossMap().get(15).addRoads(scheduler.getRoadMap());



        scheduler.addCar(new Car(100, 14, 12, 5, 1));
        scheduler.addCar(new Car(200, 14, 12, 5, 1));
        scheduler.addCar(new Car(300, 14, 12, 5, 1));
        scheduler.addCar(new Car(101, 14, 12, 5, 1));
        scheduler.addCar(new Car(201, 14, 12, 5, 1));
        scheduler.addCar(new Car(301, 14, 12, 5, 1));

        scheduler.addCar(new Car(400, 13, 12, 5, 1));
        scheduler.addCar(new Car(500, 13, 12, 5, 1));
        scheduler.addCar(new Car(600, 13, 12, 5, 1));
        scheduler.addCar(new Car(401, 13, 12, 5, 1));
        scheduler.addCar(new Car(501, 13, 12, 5, 1));
        scheduler.addCar(new Car(601, 13, 12, 5, 1));

        scheduler.addCar(new Car(700, 11, 12, 5, 1));
        scheduler.addCar(new Car(800, 11, 12, 5, 1));
        scheduler.addCar(new Car(900, 11, 12, 5, 1));
        scheduler.addCar(new Car(701, 11, 12, 5, 1));
        scheduler.addCar(new Car(801, 11, 12, 5, 1));
        scheduler.addCar(new Car(901, 11, 12, 5, 1));

        scheduler.getCarMap().get(100).setState(CarState.END).setCurrentSpeed(5).setLaneId(1).setStartTime(1).setPosition(10).addPath(4).addPath(2);
        scheduler.getCarMap().get(200).setState(CarState.END).setCurrentSpeed(5).setLaneId(2).setStartTime(1).setPosition(10).addPath(4).addPath(2);
        scheduler.getCarMap().get(300).setState(CarState.END).setCurrentSpeed(5).setLaneId(3).setStartTime(1).setPosition(10).addPath(4).addPath(2);
        scheduler.getCarMap().get(101).setState(CarState.END).setCurrentSpeed(5).setLaneId(1).setStartTime(1).setPosition(9).addPath(4).addPath(2);
        scheduler.getCarMap().get(201).setState(CarState.END).setCurrentSpeed(5).setLaneId(2).setStartTime(1).setPosition(9).addPath(4).addPath(2);
        scheduler.getCarMap().get(301).setState(CarState.END).setCurrentSpeed(5).setLaneId(3).setStartTime(1).setPosition(9).addPath(4).addPath(2);

        scheduler.getCarMap().get(400).setState(CarState.END).setCurrentSpeed(5).setLaneId(1).setStartTime(1).setPosition(10).addPath(3).addPath(2);
        scheduler.getCarMap().get(500).setState(CarState.END).setCurrentSpeed(5).setLaneId(2).setStartTime(1).setPosition(10).addPath(3).addPath(2);
        scheduler.getCarMap().get(600).setState(CarState.END).setCurrentSpeed(5).setLaneId(3).setStartTime(1).setPosition(10).addPath(3).addPath(2);
        scheduler.getCarMap().get(401).setState(CarState.END).setCurrentSpeed(5).setLaneId(1).setStartTime(1).setPosition(9).addPath(3).addPath(2);
        scheduler.getCarMap().get(501).setState(CarState.END).setCurrentSpeed(5).setLaneId(2).setStartTime(1).setPosition(9).addPath(3).addPath(2);
        scheduler.getCarMap().get(601).setState(CarState.END).setCurrentSpeed(5).setLaneId(3).setStartTime(1).setPosition(9).addPath(3).addPath(2);

        scheduler.getCarMap().get(700).setState(CarState.END).setCurrentSpeed(5).setLaneId(3).setStartTime(1).setPosition(10).addPath(1).addPath(2);
        scheduler.getCarMap().get(800).setState(CarState.END).setCurrentSpeed(5).setLaneId(2).setStartTime(1).setPosition(10).addPath(1).addPath(2);
        scheduler.getCarMap().get(900).setState(CarState.END).setCurrentSpeed(5).setLaneId(1).setStartTime(1).setPosition(10).addPath(1).addPath(2);
        scheduler.getCarMap().get(701).setState(CarState.END).setCurrentSpeed(5).setLaneId(3).setStartTime(1).setPosition(9).addPath(1).addPath(2);
        scheduler.getCarMap().get(801).setState(CarState.END).setCurrentSpeed(5).setLaneId(2).setStartTime(1).setPosition(9).addPath(1).addPath(2);
        scheduler.getCarMap().get(901).setState(CarState.END).setCurrentSpeed(5).setLaneId(1).setStartTime(1).setPosition(9).addPath(1).addPath(2);

        for (int i = 1; i < 5; i++) {
            for (Lane lane : scheduler.getRoad(i).getLaneList()) {
                if (i==1){
                 scheduler.getCarMap().forEach((carId, car) -> {
                    if (car.getLaneId() == lane.getId()&&car.getId()>=700) {
                        lane.putCar(car,car.getPosition());
                    }
                });
                }
                if (i==3){
                    scheduler.getCarMap().forEach((carId, car) -> {
                        if (car.getLaneId() == lane.getId()&&(carId>=400&&carId<700)) {
                            lane.putCar(car,car.getPosition());
                        }
                    });
                }
                if (i==4){
                    scheduler.getCarMap().forEach((carId, car) -> {
                        if (car.getLaneId() == lane.getId()&&car.getId()<400) {
                            lane.putCar(car,car.getPosition());
                        }
                    });
                }

            }
        }
        scheduler.getRoad(1).offerWaitingQueue(15);
        scheduler.getRoad(2).offerWaitingQueue(12);
        scheduler.getRoad(3).offerWaitingQueue(15);
        scheduler.getRoad(4).offerWaitingQueue(15);

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
        scheduler.step();

        assertAll("Car position 1",
                () -> assertEquals(1, scheduler.getCar(100).getLaneId()),
                () -> assertEquals(5, scheduler.getCar(100).getPosition()),
                () -> assertEquals( 1,scheduler.getCar(200).getLaneId()),
                () -> assertEquals( 4,scheduler.getCar(200).getPosition()),
                () -> assertEquals(1,scheduler.getCar(300).getLaneId()),
                () -> assertEquals(3,scheduler.getCar(300).getPosition()),
                () -> assertEquals(1,scheduler.getCar(101).getLaneId()),
                () -> assertEquals(2,scheduler.getCar(101).getPosition()),
                () -> assertEquals(1,scheduler.getCar(201).getLaneId()),
                () -> assertEquals(1,scheduler.getCar(201).getPosition()),
                () -> assertEquals(2,scheduler.getCar(301).getLaneId()),
                () -> assertEquals(4,scheduler.getCar(301).getPosition())
                );

        assertAll("Car position 2",
                () -> assertEquals(3, scheduler.getCar(400).getLaneId()),
                () -> assertEquals(1, scheduler.getCar(400).getPosition()),
                () -> assertEquals( 2,scheduler.getCar(500).getLaneId()),
                () -> assertEquals( 10,scheduler.getCar(500).getPosition()),
                () -> assertEquals(3,scheduler.getCar(600).getLaneId()),
                () -> assertEquals(10,scheduler.getCar(600).getPosition()),
                () -> assertEquals(1,scheduler.getCar(401).getLaneId()),
                () -> assertEquals(10,scheduler.getCar(401).getPosition()),
                () -> assertEquals(2,scheduler.getCar(501).getLaneId()),
                () -> assertEquals(9,scheduler.getCar(501).getPosition()),
                () -> assertEquals(3,scheduler.getCar(601).getLaneId()),
                () -> assertEquals(9,scheduler.getCar(601).getPosition())
        );

        assertAll("Car position 3",
                () -> assertEquals(2, scheduler.getCar(700).getLaneId()),
                () -> assertEquals(1, scheduler.getCar(700).getPosition()),
                () -> assertEquals( 2,scheduler.getCar(800).getLaneId()),
                () -> assertEquals( 2,scheduler.getCar(800).getPosition()),
                () -> assertEquals(2,scheduler.getCar(900).getLaneId()),
                () -> assertEquals(3,scheduler.getCar(900).getPosition()),
                () -> assertEquals(3,scheduler.getCar(701).getLaneId()),
                () -> assertEquals(2,scheduler.getCar(701).getPosition()),
                () -> assertEquals(3,scheduler.getCar(801).getLaneId()),
                () -> assertEquals(3,scheduler.getCar(801).getPosition()),
                () -> assertEquals(3,scheduler.getCar(901).getLaneId()),
                () -> assertEquals(4,scheduler.getCar(901).getPosition())
        );

    }

}