package com.huawei;


import java.nio.charset.CoderMalfunctionError;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

public class Car implements Comparable<Car> {
    private int id;
    private int from;
    private int to;
    private int topSpeed;
    private int planTime;
    private boolean priority;
    private boolean preset;
    private ArrayList<Integer> path = new ArrayList<>();

    private int currentSpeed = 0;
    private long startTime = -1;
    private long actualStartTime = -1;
    private long endTime = -1;
    private int laneId = -1;
    private int roadIdx = -1;

    private CarState state;
    private int position = -1;


    public static Comparator<Car> idComparator = Comparator.comparing(Car::getId);

    public static Comparator<Car> priorityTimeIdComparator = new Comparator<Car>() {
        @Override
        public int compare(Car car1, Car car2) {
            long i1;
            long i2;
            if (car1.isPriority())
                i1 = (long) Math.pow(10, 8) * car1.getStartTime() + car1.getId();
            else
                i1 = (long) Math.pow(10, 10) * car1.getStartTime() + car1.getId();

            if (car2.isPriority())
                i2 = (long) Math.pow(10, 8) * car2.getStartTime() + car2.getId();
            else
                i2 = (long) Math.pow(10, 10) * car2.getStartTime() + car2.getId();

            return Long.compare(i1, i2);
        }
    };

    public static Comparator<Car> priorityLaneIdComparator = new Comparator<Car>() {
        @Override
        public int compare(Car car1, Car car2) {
            int i1;
            int i2;
            if (car1.isPriority())
                i1 = car1.getPosition() * 100000 - car1.getLaneId();
            else
                i1 = car1.getPosition() * 1000 - car1.getLaneId();

            if (car2.isPriority())
                i2 = car2.getPosition() * 100000 - car2.getLaneId();
            else
                i2 = car2.getPosition() * 1000 - car2.getLaneId();

            return Integer.compare(i2, i1);
        }
    };

    public static Comparator<Car> speedComparator = new Comparator<Car>() {
        @Override
        public int compare(Car car1, Car car2) {
            return Integer.compare(car2.getTopSpeed(),car1.getTopSpeed());
        }
    };

    public Car(int id, int start, int to, int topSpeed, int planTime, boolean priority, boolean preset) {
        this.id = id;
        this.from = start;
        this.to = to;
        this.topSpeed = topSpeed;
        this.planTime = planTime;
        this.priority = priority;
        this.preset = preset;
//        setState(CarState.IN_GARAGE);
    }

    public Car(String line) {
        String[] vars = line.split(",");
        this.id = Integer.parseInt(vars[0]);
        this.from = Integer.parseInt(vars[1]);
        this.to = Integer.parseInt(vars[2]);
        this.topSpeed = Integer.parseInt(vars[3]);
        this.planTime = Integer.parseInt(vars[4]);
        this.priority = Integer.parseInt(vars[5]) == 1;
        this.preset = Integer.parseInt(vars[6]) == 1;
//        setState(CarState.IN_GARAGE);
    }

    public Car addPath(int roadId) {
        path.add(roadId);
        return this;
    }

    public void clearPath(){
        path.clear();
    }

    public ArrayList<Integer> getPath() {
        return path;
    }

    public String outputResult() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        stringBuilder.append(this.id);
        stringBuilder.append(",");
        stringBuilder.append(this.startTime);
        stringBuilder.append(",");

        path.forEach(roadId -> {
            stringBuilder.append(roadId);
            stringBuilder.append(",");
        });

        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public int compareTo(Car car) {
        // 根据速度排序
        if (getTopSpeed() > car.getTopSpeed())
            return -1;
        else if (getTopSpeed() == car.getTopSpeed())
            return 0;
        else
            return 1;
    }

    public int getId() {
        return id;
    }


    public int getFrom() {
        return from;
    }


    public int getTo() {
        return to;
    }


    public int getTopSpeed() {
        return topSpeed;
    }


    public int getPlanTime() {
        return planTime;
    }


    public int getCurrentSpeed() {
        return currentSpeed;
    }


    public long getStartTime() {
        return startTime;
    }


    public long getEndTime() {
        return endTime;
    }


    public int getLaneId() {
        return laneId;
    }


    public static Comparator<Car> getIdComparator() {
        return idComparator;
    }

    public static void setIdComparator(Comparator<Car> idComparator) {
        Car.idComparator = idComparator;
    }

    public CarState getState() {
        return state;
    }


    public int getPosition() {
        return position;
    }

    public Car setPosition(int position) {
        if (position <= 0) {
            System.out.println("Current position: " + getPosition());
            System.out.println("Illegal position: " + position);
            System.err.println("Position must greater than 0");
        }
        this.position = position;
        return this;
    }

    public Car setId(int id) {
        this.id = id;
        return this;
    }

    public Car setFrom(int from) {
        this.from = from;
        return this;
    }

    public Car setTo(int to) {
        this.to = to;
        return this;
    }

    public Car setTopSpeed(int topSpeed) {
        this.topSpeed = topSpeed;
        return this;
    }

    public Car setPlanTime(int planTime) {
        this.planTime = planTime;
        return this;
    }

    public Car setPath(ArrayList<Integer> path) {
        this.path = path;
        return this;
    }

    public Car setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
        return this;
    }

    public Car setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Car setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    public Car setLaneId(int laneId) {
        this.laneId = laneId;
        return this;
    }

    public Car setState(CarState state) {
        if (getState() != null && !getState().equals(state))
            Scheduler.carStateChanged = true;

        updateStateCounter(getState(), state);

        this.state = state;
        return this;
    }

    public void updateStateCounter(CarState original, CarState now) {
        if (original == now)
            return;
        HashMap<CarState, Integer> carStateCounter = Scheduler.carStateCounter;

        carStateCounter.put(now, carStateCounter.get(now) == null ? 1 : (carStateCounter.get(now) + 1));
        carStateCounter.put(original, carStateCounter.get(original) == null ? 0 : (carStateCounter.get(original) - 1));

    }

    public long getActualStartTime() {
        return actualStartTime;
    }

    public Car setActualStartTime(long actualStartTime) {
        this.actualStartTime = actualStartTime;
        return this;
    }

    public void resetCarState() {
        this.currentSpeed = 0;
        if(!isPreset())
            this.startTime = -1;
        this.endTime = -1;
        this.laneId = -1;
        this.roadIdx = -1;

        this.state = null;
        this.position = -1;
    }

    public CarStates dumpStates() {
        ArrayList<Integer> carPath = new ArrayList<>(path);
        return new CarStates(getCurrentSpeed(), getStartTime(), getEndTime(), getLaneId(), getState(), getPosition(), getId(), getRoadIdx(), carPath);
    }

    public void restoreStates(CarStates carStates) {
        this.currentSpeed = carStates.getCurrentSpeed();
        this.startTime = carStates.getStartTime();
        this.endTime = carStates.getEndTime();
        this.laneId = carStates.getLaneId();
        this.state = carStates.getCarState();
        this.position = carStates.getPosition();
        this.path = carStates.getPath();
        this.roadIdx = carStates.getRoadIdx();
    }

    public class CarStates {
        int currentSpeed;
        long startTime;
        long endTime;
        int laneId;
        CarState carState;
        int position;
        int id;
        ArrayList<Integer> path;
        int roadIdx;

        public CarStates(int currentSpeed, long startTime, long endTime, int laneId, CarState carState, int position, int id, int roadIdx, ArrayList<Integer> path) {
            this.currentSpeed = currentSpeed;
            this.startTime = startTime;
            this.endTime = endTime;
            this.laneId = laneId;
            this.carState = carState;
            this.position = position;
            this.id = id;
            this.path = path;
            this.roadIdx = roadIdx;
        }

        public int getCurrentSpeed() {
            return currentSpeed;
        }

        public CarStates setCurrentSpeed(int currentSpeed) {
            this.currentSpeed = currentSpeed;
            return this;
        }

        public long getStartTime() {
            return startTime;
        }

        public CarStates setStartTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public long getEndTime() {
            return endTime;
        }

        public CarStates setEndTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

        public int getLaneId() {
            return laneId;
        }

        public CarStates setLaneId(int laneId) {
            this.laneId = laneId;
            return this;
        }

        public CarState getCarState() {
            return carState;
        }

        public CarStates setCarState(CarState carState) {
            this.carState = carState;
            return this;
        }

        public int getPosition() {
            return position;
        }

        public CarStates setPosition(int position) {
            this.position = position;
            return this;
        }

        public int getId() {
            return id;
        }

        public CarStates setId(int id) {
            this.id = id;
            return this;
        }

        public ArrayList<Integer> getPath() {
            return path;
        }

        public CarStates setPath(ArrayList<Integer> path) {
            this.path = path;
            return this;
        }

        public int getRoadIdx() {
            return roadIdx;
        }
    }

    public int getRoadIdx() {
        return roadIdx;
    }

    public Car setRoadIdx(int roadIdx) {
        this.roadIdx = roadIdx;
        return this;
    }

    public boolean isPriority() {
        return priority;
    }

    public Car setPriority(boolean priority) {
        this.priority = priority;
        return this;
    }

    public boolean isPreset() {
        return preset;
    }

    public Car setPreset(boolean preset) {
        this.preset = preset;
        return this;
    }

}
