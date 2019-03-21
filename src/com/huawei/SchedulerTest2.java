package com.huawei;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SchedulerTest2 {
    private Scheduler scheduler = new Scheduler();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {

        //Road(int id, int len, int topSpeed, int numOfLanes, int start, int end, boolean bidirectional)
        //CrossRoads(int id,int road1,int road2,int road3,int road4) {
        //Car (int id, int start, int to, int topSpeed, int planTime)
        scheduler.addCross(new CrossRoads(11, -1, 1, -1, -1));
        scheduler.addCross(new CrossRoads(12, -1, 2, -1, 1));
        scheduler.addCross(new CrossRoads(13, -1, -1, -1, 2));

        scheduler.addRoad(new Road(1, 10, 6, 3, 11, 12, false));
        scheduler.addRoad(new Road(2, 10, 6, 3, 12, 13, false));

        scheduler.getCrossMap().get(11).addRoads(scheduler.getRoadMap());
        scheduler.getCrossMap().get(12).addRoads(scheduler.getRoadMap());
        scheduler.getCrossMap().get(13).addRoads(scheduler.getRoadMap());


        scheduler.addCar(new Car(100, 11, 13, 5, 1));
        scheduler.addCar(new Car(200, 11, 13, 3, 1));
        scheduler.addCar(new Car(300, 11, 13, 5, 1));
        scheduler.addCar(new Car(101, 11, 13, 5, 1));
        scheduler.addCar(new Car(201, 11, 13, 5, 1));
        scheduler.addCar(new Car(301, 11, 13, 5, 1));

        scheduler.getCarMap().get(100).setState(CarState.WAIT).setCurrentSpeed(5).setLaneId(1).setStartTime(1).setPosition(10).addPath(1).addPath(2);
        scheduler.getCarMap().get(200).setState(CarState.WAIT).setCurrentSpeed(3).setLaneId(2).setStartTime(1).setPosition(10).addPath(1).addPath(2);
        scheduler.getCarMap().get(300).setState(CarState.WAIT).setCurrentSpeed(5).setLaneId(3).setStartTime(1).setPosition(10).addPath(1).addPath(2);
        scheduler.getCarMap().get(101).setState(CarState.WAIT).setCurrentSpeed(5).setLaneId(1).setStartTime(1).setPosition(9).addPath(1).addPath(2);
        scheduler.getCarMap().get(201).setState(CarState.WAIT).setCurrentSpeed(5).setLaneId(2).setStartTime(1).setPosition(9).addPath(1).addPath(2);
        scheduler.getCarMap().get(301).setState(CarState.WAIT).setCurrentSpeed(5).setLaneId(3).setStartTime(1).setPosition(9).addPath(1).addPath(2);

        for (Lane lane : scheduler.getRoad(1).getLaneList()) {
            scheduler.getCarMap().forEach((carId, car) -> {
                if (car.getLaneId() == lane.getId()) {
                    lane.getCarMap().put(car.getPosition(), car);
                }
            });
        }

        scheduler.getRoad(1).offerWaitingQueue(12);


    }

    @org.junit.jupiter.api.Test
    void priorityQueue() {
        Car car1 = scheduler.getRoad(1).getWaitingQueue(12).remove();
        Car car2 = scheduler.getRoad(1).getWaitingQueue(12).remove();
        assert (car1.getId() == 100);
        assert (car2.getId() == 200);
    }

    @org.junit.jupiter.api.Test
    void driveAllCarOnRoad() {
        scheduler.driveAllCarOnRoad();
    }

    @org.junit.jupiter.api.Test
    void step() {
        scheduler.step();

        assertAll("Car position",
                () -> assertEquals(1, scheduler.getCar(100).getLaneId()),
                () -> assertEquals(5, scheduler.getCar(100).getPosition()),
                () -> assertEquals( 1,scheduler.getCar(200).getLaneId()),
                () -> assertEquals( 3,scheduler.getCar(200).getPosition()),
                () -> assertEquals(1,scheduler.getCar(300).getLaneId()),
                () -> assertEquals(2,scheduler.getCar(300).getPosition()),
                () -> assertEquals(1,scheduler.getCar(101).getLaneId()),
                () -> assertEquals(1,scheduler.getCar(101).getPosition()),
                () -> assertEquals(2,scheduler.getCar(201).getLaneId()),
                () -> assertEquals(4,scheduler.getCar(201).getPosition()),
                () -> assertEquals(2,scheduler.getCar(301).getLaneId()),
                () -> assertEquals(3,scheduler.getCar(301).getPosition())
                );

        scheduler.step();
        assertAll("Car position",
                () -> assertEquals(1, scheduler.getCar(100).getLaneId()),
                () -> assertEquals(10, scheduler.getCar(100).getPosition()),
                () -> assertEquals( 1,scheduler.getCar(200).getLaneId()),
                () -> assertEquals( 6,scheduler.getCar(200).getPosition()),
                () -> assertEquals(1,scheduler.getCar(300).getLaneId()),
                () -> assertEquals(5,scheduler.getCar(300).getPosition()),
                () -> assertEquals(1,scheduler.getCar(101).getLaneId()),
                () -> assertEquals(4,scheduler.getCar(101).getPosition()),
                () -> assertEquals(2,scheduler.getCar(201).getLaneId()),
                () -> assertEquals(9,scheduler.getCar(201).getPosition()),
                () -> assertEquals(2,scheduler.getCar(301).getLaneId()),
                () -> assertEquals(8,scheduler.getCar(301).getPosition())
        );
    }

}