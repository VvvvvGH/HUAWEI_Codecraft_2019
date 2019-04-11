package com.huawei;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;


public class TrafficMap {
    private Graph<CrossRoads, RoadEdge> graph =
            new SimpleDirectedWeightedGraph<>(RoadEdge.class);

    public static final int DIRECTION = 3;
    // Direction = 1 north south
    // Direction = 2 east west
    // Direction = 3 both

    public TrafficMap(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    private Scheduler scheduler;

    DijkstraShortestPath dijkstraShortestPath = new DijkstraShortestPath(graph);

    private PriorityQueue<Car> priorityQueue = new PriorityQueue<>();
    private ArrayList<Car> carOrderByStartList = new ArrayList<>();

    private HashMap<Integer, CrossRoads> crossMap = new HashMap<>();
    private HashMap<Integer, Road> roads = new HashMap<>();
    private HashMap<Integer, Car> cars = new HashMap<>();

    public void initGraphEdge() {

        crossMap.forEach((cross, crossObj) -> graph.addVertex(crossObj));
        roads.forEach((roadId, road) -> {
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            graph.removeAllEdges(from, to);

            RoadEdge roadEdge = new RoadEdge(road, from, to);
            graph.setEdgeWeight(roadEdge, roadEdge.road.getLen());
            graph.addEdge(from, to, roadEdge);

            if (road.isBidirectional()) {
                graph.removeAllEdges(to, from);
                RoadEdge roadEdge1 = new RoadEdge(road, to, from);
                graph.setEdgeWeight(roadEdge1, roadEdge1.road.getLen());
                graph.addEdge(to, from, roadEdge1);
            }
        });
    }

    public void initGraphEdge(double[] weights) {
        crossMap.forEach((cross, crossObj) -> graph.addVertex(crossObj));
        Iterator<Integer> it = roads.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Integer roadId = it.next();
            Road road = roads.get(roadId);
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            graph.removeAllEdges(from, to);

            RoadEdge roadEdge = new RoadEdge(road, from, to);
            graph.setEdgeWeight(roadEdge, weights[i]);
            graph.addEdge(from, to, roadEdge);

            if (road.isBidirectional()) {
                graph.removeAllEdges(to, from);
                RoadEdge roadEdge1 = new RoadEdge(road, to, from);
                graph.setEdgeWeight(roadEdge1, weights[i]);
                graph.addEdge(to, from, roadEdge1);
            }
            i++;
        }
    }

    public double[] readGraphEdgeWeight() {
        crossMap.forEach((cross, crossObj) -> graph.addVertex(crossObj));
        double[] weights = new double[roads.size()];

        Iterator<Integer> it = roads.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Integer roadId = it.next();
            Road road = roads.get(roadId);
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            weights[i] = graph.getEdgeWeight(graph.getEdge(from, to));

            i++;
        }
        return weights;
    }


    public void initGraphEdgeBySpeed() {

        crossMap.forEach((cross, crossObj) -> graph.addVertex(crossObj));
        roads.forEach((roadId, road) -> {
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            graph.removeAllEdges(from, to);

            RoadEdge roadEdge = new RoadEdge(road, from, to);
            graph.setEdgeWeight(roadEdge, roadEdge.road.getLen() / roadEdge.road.getTopSpeed());
            graph.addEdge(from, to, roadEdge);

            if (road.isBidirectional()) {
                graph.removeAllEdges(to, from);
                RoadEdge roadEdge1 = new RoadEdge(road, to, from);
                graph.setEdgeWeight(roadEdge1, roadEdge1.road.getLen() / roadEdge1.road.getTopSpeed());
                graph.addEdge(to, from, roadEdge1);
            }
        });
    }

    public void updateGraphEdge() {
        crossMap.forEach((cross, crossObj) -> graph.addVertex(crossObj));
        roads.forEach((roadId, road) -> {
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            RoadEdge roadEdge = graph.getEdge(from, to);
            graph.setEdgeWeight(roadEdge, roadEdge.getWeight(graph.getEdgeWeight(roadEdge)));
            if (road.isBidirectional()) {
                RoadEdge roadEdge1 = graph.getEdge(to, from);
                graph.setEdgeWeight(roadEdge1, roadEdge1.getWeight(graph.getEdgeWeight(roadEdge1)));
            }
        });
    }


    public GraphPath shortestDistancePath(Graph graphToCompute, int from, int to) {
        return DijkstraShortestPath.findPathBetween(graphToCompute, crossMap.get(from), crossMap.get(to));
    }


    public void setCarPath(Car car, GraphPath path) {
        // Clean original path
        car.getPath().clear();
        //Find the road between two crossroads
        path.getEdgeList().forEach(edge -> {
            car.addPath(((RoadEdge) edge).road.getId());
        });
    }


    public Long scheduleTest2(int carFlowLimit) {
        scheduler.reset();
        updateGraphEdge();

        // 先把优先车辆放入车库
        cars.forEach((carId, car) -> {
            if (car.isPreset()) {
                scheduler.addToGarage(car);
            }
        });


        int time = 0;
        int count = 0;

        ArrayList<Car> carList = new ArrayList<>();
        cars.forEach((carId, car) -> {
            if (!car.isPreset()) {
                carList.add(car);
            }
        });
        carList.sort(Car.speedComparator);

        System.out.println("Start to schedule");
        while (!carList.isEmpty()) {
            time++;
            count = 0;
            Iterator iterator = carList.iterator();
            while (iterator.hasNext()) {
                Car car = (Car) iterator.next();

                if (car.getPlanTime() > time)
                    continue;

                if (count >= carFlowLimit)
                    break;

                GraphPath path = shortestDistancePath(graph, car.getFrom(), car.getTo());

                boolean hasBusyPath = false;
                for (Object edge : path.getEdgeList()) {
                    if (((RoadEdge) edge).calculateLoad() > 0.8) {
                        hasBusyPath = true;
                        break;
                    }
                }
                if (hasBusyPath) {
                    break;
                }

                setCarPath(car, path);
                car.setStartTime(time);
                scheduler.addToGarage(car);
                iterator.remove();
                count++;

            }
            if (!scheduler.step())
                return -1L;

            updateGraphEdge();
        }

        if (!scheduler.stepUntilFinish())
            return -1L;
        scheduler.printCarStates();
        return scheduler.getSystemScheduleTime();
    }

    public long preScheduleLv2(int carFlow1, double threshold1, int carFlow2, double threshold2) {
        scheduler.reset();
        updateGraphEdge();

        HashMap<Integer, Integer> carPlanTime = new HashMap<>();
        HashMap<Long, Integer> presetCarMap = new HashMap<>();

        cars.forEach((carId, car) -> {
            carPlanTime.put(carId, car.getPlanTime());
            // 先把预设车辆放入车库
            if (car.isPreset()) {
                scheduler.addToGarage(car);
                presetCarMap.put(car.getStartTime(), presetCarMap.get(car.getStartTime()) == null ? 1 : (presetCarMap.get(car.getStartTime()) + 1));
            }
        });


        long time = 0;
        int count = 0;
        boolean haveCarLeft = true;
        double busyPathThreshold;
        int carFlowLimit;

        //Busy path counter
        TreeMap<Integer, Integer> roadCounter = new TreeMap<>();

        HashMap<Integer, ArrayList<Car>> carCrossMap = distributedOrder(false);

        System.out.println("Start lv2 pre schedule");

        while (haveCarLeft) {
            time++;
            count = 0;
            if (presetCarMap.get(time) != null) {
                count = presetCarMap.get(time);
            }
            if (scheduler.havePresetCarOnRoad()) {
                carFlowLimit = carFlow1;
                busyPathThreshold = threshold1;
            } else {
                carFlowLimit = carFlow2;
                busyPathThreshold = threshold2;
            }

            boolean changed = true;
            while (changed) {
                if (count >= carFlowLimit)
                    break;
                changed = false;


                for (ArrayList<Car> carList : carCrossMap.values()) {
                    if (count >= carFlowLimit)
                        break;

                    if (carList.size() == 0)
                        continue;
                    Car car = carList.get(0);

                    if (carPlanTime.get(car.getId()) > time)
                        continue;


                    GraphPath path = shortestDistancePath(graph, car.getFrom(), car.getTo());

                    // 计算每一时间单位最忙的路
                    car.getPath().forEach(road -> {
                        if (roads.get(road).calculateLoad() > 0.80) {
                            if (roadCounter.containsKey(road))
                                roadCounter.put(road, roadCounter.get(road) + 1);
                            else
                                roadCounter.put(road, 1);
                        }
                    });

                    boolean hasBusyPath = false;
                    for (Object edge : path.getEdgeList()) {
                        if (((RoadEdge) edge).calculateLoad() > busyPathThreshold) {
                            hasBusyPath = true;
                            break;
                        }
                    }
                    if (hasBusyPath) {
                        continue;
                    }

                    setCarPath(car, path);
                    car.setStartTime(time);
                    scheduler.addToGarage(car);
                    carList.remove(car);
                    count++;
                    changed = true;
                }

            }

            if (!scheduler.step())
                return -1L;
            updateGraphEdge();
            haveCarLeft = false;
            for (ArrayList<Car> carList : carCrossMap.values()) {
                if (carList.size() != 0)
                    haveCarLeft = true;
            }
        }

        if (!scheduler.stepUntilFinish())
            return -1L;
        scheduler.printCarStates();

        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(roadCounter.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        int numOfRoadsToAdjust = roadCounter.size();
        for (int i = 0; i < numOfRoadsToAdjust; i++) {
            Road road = roads.get(list.get(i).getKey());
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            RoadEdge edge = graph.getEdge(from, to);
            graph.setEdgeWeight(edge, road.getLen() * 2);
            if (road.isBidirectional()) {
                RoadEdge opposeEdge = graph.getEdge(to, from);
                graph.setEdgeWeight(opposeEdge, road.getLen() * 2);
            }
        }


        return scheduler.getSystemScheduleTime();
    }

    public Long scheduleTest1(int carFlow1,double threshold1,int carFlow2, double threshold2,int map) {
        scheduler.reset();
        updateGraphEdge();

        HashMap<Integer, Integer> carPlanTime = new HashMap<>();
        HashMap<Long, Integer> presetCarMap = new HashMap<>();

        cars.forEach((carId, car) -> {
            carPlanTime.put(carId, car.getPlanTime());
            // 先把预设车辆放入车库
            if (car.isPreset()) {
                scheduler.addToGarage(car);
                presetCarMap.put(car.getStartTime(), presetCarMap.get(car.getStartTime()) == null ? 1 : (presetCarMap.get(car.getStartTime()) + 1));
            }
        });


        long time = 0;
        int count = 0;
        boolean haveCarLeft = true;
        boolean deadlock = false;
        double busyPathThreshold;
        int carFlowLimit;

        int rollbackThrottle = 5;
        int rollbackThrottleCount = rollbackThrottle;

        int carOnRoadLimit = -1;

        boolean once = true;

        HashMap<Integer, ArrayList<Car>> carCrossMap = distributedOrder(false);

        System.out.println("Start to schedule");

        while (haveCarLeft) {
            time++;
            count = 0;
            if (presetCarMap.get(time) != null) {
                count = presetCarMap.get(time);
            }

            if (!deadlock) {
                if (scheduler.havePriorityCarOnRoad()) {
                    carFlowLimit = carFlow1;
                    busyPathThreshold = threshold1;
                } else {
                    carFlowLimit = carFlow2;
                    busyPathThreshold = threshold2;
                    if(once){
                        carOnRoadLimit=9999; // 当没有优先车的时候重置
                        once=false;
                    }
                }

            } else {
                carFlowLimit = 1;
                busyPathThreshold = 1;
                if (rollbackThrottleCount == 0) {
                    deadlock = false;
                    rollbackThrottleCount = rollbackThrottle;
                }
                rollbackThrottleCount--;
            }
            if (carOnRoadLimit != -1 && Scheduler.carStateCounter.get(CarState.END) > carOnRoadLimit) {
                carFlowLimit = 15;
                busyPathThreshold = 0.3;
            }


            boolean changed = true;
            while (changed) {
                if (count >= carFlowLimit)
                    break;
                changed = false;

                for (ArrayList<Car> carList : carCrossMap.values()) {
                    if (count >= carFlowLimit)
                        break;

                    if (carList.size() == 0)
                        continue;
                    Car car = carList.get(0);

                    if (carPlanTime.get(car.getId()) > time)
                        continue;

                    GraphPath path = shortestDistancePath(graph, car.getFrom(), car.getTo());

                    boolean hasBusyPath = false;
                    for (Object edge : path.getEdgeList()) {
                        if (((RoadEdge) edge).calculateLoad() > busyPathThreshold) {
                            hasBusyPath = true;
                            break;
                        }
                    }
                    if (hasBusyPath && !car.isPriority()) {
                        continue;
                    }

                    setCarPath(car, path);
                    car.setStartTime(time);
                    scheduler.addToGarage(car);
                    carList.remove(car);
                    count++;
                    changed = true;
                }

            }

            if (!scheduler.step()) {
                System.err.println("Deadlock happened, start rollback");
                carOnRoadLimit = Scheduler.carStateCounter.get(CarState.END) + 100;
                time = autoRollback();
                if (time < 0L)
                    return -1L;
                carCrossMap = distributedOrder(true);
                deadlock = true;
                rollbackThrottle++;
            }
            updateGraphEdge();
//            System.out.print("count " + count + " ");
//            scheduler.printCarStates();
            haveCarLeft = false;
            for (ArrayList<Car> carList : carCrossMap.values()) {
                if (carList.size() != 0)
                    haveCarLeft = true;
            }
        }

        if (!scheduler.stepUntilFinish())
            return -1L;
        scheduler.printCarStates();
        return scheduler.getSystemScheduleTime();
    }

    public long preSchedule1(int carFlowLimit) {
        scheduler.reset();
        updateGraphEdge();

        HashMap<Integer, Integer> carPlanTime = new HashMap<>();


        cars.forEach((carId, car) -> {
            carPlanTime.put(carId, car.getPlanTime());
        });

        //Busy path counter
        TreeMap<Integer, Integer> roadCounter = new TreeMap<>();

        int time = 0;
        int count = 0;
        boolean haveCarLeft = true;


        HashMap<Integer, ArrayList<Car>> carCrossMap = distributedOrder(false);

        System.out.println("Start pre schedule");

        while (haveCarLeft) {
            time++;
            count = 0;

            for (ArrayList<Car> carList : carCrossMap.values()) {
                if (carList.size() == 0)
                    continue;
                Car car = carList.get(0);

                if (car.getPlanTime() > time)
                    continue;

                if (count >= carFlowLimit)
                    break;

                GraphPath path = shortestDistancePath(graph, car.getFrom(), car.getTo());

                // 计算每一时间单位最忙的路
                car.getPath().forEach(road -> {
                    if (roads.get(road).calculateLoad() > 0.80) {
                        if (roadCounter.containsKey(road))
                            roadCounter.put(road, roadCounter.get(road) + 1);
                        else
                            roadCounter.put(road, 1);
                    }
                });

                setCarPath(car, path);
                car.setStartTime(time);
                scheduler.addToGarage(car);
                carList.remove(car);
                count++;

            }
            if (!scheduler.step())
                return -1L;
            updateGraphEdge();
            haveCarLeft = false;
            for (ArrayList<Car> carList : carCrossMap.values()) {
                if (carList.size() != 0)
                    haveCarLeft = true;
            }
        }

        if (!scheduler.stepUntilFinish())
            return -1L;
        scheduler.printCarStates();

        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(roadCounter.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        int numOfRoadsToAdjust = roadCounter.size();
        for (int i = 0; i < numOfRoadsToAdjust; i++) {
            Road road = roads.get(list.get(i).getKey());
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            RoadEdge edge = graph.getEdge(from, to);
            graph.setEdgeWeight(edge, road.getLen() * 2);
            if (road.isBidirectional()) {
                RoadEdge opposeEdge = graph.getEdge(to, from);
                graph.setEdgeWeight(opposeEdge, road.getLen() * 2);
            }
        }

        return Scheduler.systemScheduleTime;
    }

    public long preSchedule2(int carFlowLimit) {
        scheduler.reset();
        updateGraphEdge();

        HashMap<Integer, Integer> carPlanTime = new HashMap<>();


        cars.forEach((carId, car) -> {
            carPlanTime.put(carId, car.getPlanTime());
        });

        //Busy path counter
        TreeMap<Integer, Integer> roadCounter = new TreeMap<>();

        long time = 0;
        int count = 0;
        boolean haveCarLeft = true;


        HashMap<Integer, ArrayList<Car>> carCrossMap = distributedOrder(false);

        System.out.println("Start pre schedule");

        while (haveCarLeft) {
            time++;
            count = 0;

            boolean changed = true;
            while (changed) {
                if (count >= carFlowLimit)
                    break;
                changed = false;
                for (ArrayList<Car> carList : carCrossMap.values()) {
                    if (carList.size() == 0)
                        continue;
                    Car car = carList.get(0);

                    if (car.getPlanTime() > time)
                        continue;

                    if (count >= carFlowLimit)
                        break;

                    GraphPath path = shortestDistancePath(graph, car.getFrom(), car.getTo());

                    // 计算每一时间单位最忙的路
                    car.getPath().forEach(road -> {
                        if (roads.get(road).calculateLoad() > 0.50) {
                            if (roadCounter.containsKey(road))
                                roadCounter.put(road, roadCounter.get(road) + 1);
                            else
                                roadCounter.put(road, 1);
                        }
                    });

                    setCarPath(car, path);
                    car.setStartTime(time);
                    scheduler.addToGarage(car);
                    carList.remove(car);
                    count++;
                    changed = true;

                }
                if (!scheduler.step())
                    return -1L;
                updateGraphEdge();
                haveCarLeft = false;
                for (ArrayList<Car> carList : carCrossMap.values()) {
                    if (carList.size() != 0)
                        haveCarLeft = true;
                }
            }
        }
        if (!scheduler.stepUntilFinish())
            return -1L;
        scheduler.printCarStates();

        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(roadCounter.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        int numOfRoadsToAdjust = roadCounter.size();
        for (int i = 0; i < numOfRoadsToAdjust; i++) {
            Road road = roads.get(list.get(i).getKey());
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            RoadEdge edge = graph.getEdge(from, to);
            graph.setEdgeWeight(edge, road.getLen() * 2);
            if (road.isBidirectional()) {
                RoadEdge opposeEdge = graph.getEdge(to, from);
                graph.setEdgeWeight(opposeEdge, road.getLen() * 2);
            }
        }

        return Scheduler.systemScheduleTime;
    }

    public long preSchedule3(int carFlowLimit) {
        scheduler.reset();
        updateGraphEdge();

        int time = 0;
        int count = 0;

        //Busy path counter
        TreeMap<Integer, Integer> roadCounter = new TreeMap<>();

        ArrayList<Car> carList = new ArrayList<>();
        cars.forEach((carId, car) -> {
            if (!car.isPreset()) {
                carList.add(car);
            }
        });
        carList.sort(Car.speedComparator);

        while (!carList.isEmpty()) {
            time++;
            count = 0;
            Iterator iterator = carList.iterator();
            while (iterator.hasNext()) {
                Car car = (Car) iterator.next();

                if (car.getPlanTime() > time)
                    continue;

                if (count >= carFlowLimit)
                    break;

                GraphPath path = shortestDistancePath(graph, car.getFrom(), car.getTo());

                // 计算每一时间单位最忙的路
                car.getPath().forEach(road -> {
                    if (roads.get(road).calculateLoad() > 0.50) {
                        if (roadCounter.containsKey(road))
                            roadCounter.put(road, roadCounter.get(road) + 1);
                        else
                            roadCounter.put(road, 1);
                    }
                });

                setCarPath(car, path);
                car.setStartTime(time);
                scheduler.addToGarage(car);
                iterator.remove();
                count++;

            }
            if (!scheduler.step())
                return -1L;
        }

        if (!scheduler.stepUntilFinish())
            return -1L;
        scheduler.printCarStates();

        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(roadCounter.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        int numOfRoadsToAdjust = roadCounter.size();
        for (int i = 0; i < numOfRoadsToAdjust; i++) {
            Road road = roads.get(list.get(i).getKey());
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            RoadEdge edge = graph.getEdge(from, to);
            graph.setEdgeWeight(edge, road.getLen() * 2.0);
            if (road.isBidirectional()) {
                RoadEdge opposeEdge = graph.getEdge(to, from);
                graph.setEdgeWeight(opposeEdge, road.getLen() * 2.0);
            }
        }

        return Scheduler.systemScheduleTime;
    }


    public HashMap<Integer, ArrayList<Car>> distributedOrder(boolean rollback) {
        HashMap<Integer, ArrayList<Car>> crossCarMap = new HashMap<>();
        for (CrossRoads crossRoad : crossMap.values()) {
            crossCarMap.put(crossRoad.getId(), new ArrayList<>());
        }
        for (Car car : cars.values()) {
            if (!car.isPreset()) {
                if (rollback) {
                    if (car.getStartTime() == -1L) {
                        crossCarMap.get(car.getFrom()).add(car);
                    }
                } else
                    crossCarMap.get(car.getFrom()).add(car);
            }
        }
        for (ArrayList<Car> carList : crossCarMap.values()) {
            carList.sort(Car.prioritySpeedComparator);
        }
        return crossCarMap;
    }


    public HashMap<Integer, PriorityQueue<Car>> directionClassification(int direction) {
        // Direction = 1 north south
        // Direction = 2 east west
        // Direction = 3 both

        priorityQueue.clear();

        // 0 is north, 1 is south
        HashMap<Integer, PriorityQueue<Car>> directionMap = new HashMap<>();

        directionMap.put(1, new PriorityQueue<>());
        directionMap.put(0, new PriorityQueue<>());

        this.getCars().forEach(
                (carId, car) -> {
                    if (!car.isPreset()) {
                        priorityQueue.offer(car);
                    }
                }
        );

        while (!priorityQueue.isEmpty()) {
            Car car = priorityQueue.remove();
            GraphPath<CrossRoads, RoadEdge> path = shortestDistancePath(graph, car.getFrom(), car.getTo());

            double directionSum = 0;
            for (RoadEdge roadEdge : path.getEdgeList()) {
                CrossRoads.RoadPosition roadPosition = roadEdge.getSource().getRoadDirection().get(roadEdge.road.getId());
                if (roadPosition == CrossRoads.RoadPosition.NORTH && direction == 1 || direction == 3) {
                    directionSum += roadEdge.road.getLen();
                } else if (roadPosition == CrossRoads.RoadPosition.SOUTH && direction == 1 || direction == 3)
                    directionSum -= roadEdge.road.getLen();
                else if (roadPosition == CrossRoads.RoadPosition.EAST && direction == 2 || direction == 3) {
                    directionSum += roadEdge.road.getLen();
                } else if (roadPosition == CrossRoads.RoadPosition.WEST && direction == 2 || direction == 3)
                    directionSum -= roadEdge.road.getLen();
            }
            if (directionSum <= 0)
                // D1
                directionMap.get(1).offer(car);
            else
                // D2
                directionMap.get(0).offer(car);

        }
        return directionMap;

    }


    public void addCross(CrossRoads cross) {
        crossMap.putIfAbsent(cross.getId(), cross);
    }

    public CrossRoads getCross(int crossId) {
        return crossMap.get(crossId);
    }

    public void addRoad(Road road) {
        roads.put(road.getId(), road);
    }

    public Road getRoad(int roadId) {
        return roads.get(roadId);
    }

    public void addCar(Car car) {
        cars.put(car.getId(), car);
    }

    public Car getCar(int carId) {
        return cars.get(carId);
    }

    public HashMap<Integer, Car> getCars() {
        return cars;
    }

    public Graph getGraph() {
        return graph;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public HashMap<Integer, Road> getRoads() {
        return roads;
    }
}


