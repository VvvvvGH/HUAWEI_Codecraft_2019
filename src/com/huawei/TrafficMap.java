package com.huawei;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;

import static com.huawei.Main.scheduler;

public class TrafficMap {
    private Graph<CrossRoads, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);


    private PriorityQueue<Car> priorityQueue = new PriorityQueue<>();

    private HashMap<Integer, CrossRoads> crossMap = new HashMap<>();
    private HashMap<Integer, Road> roads = new HashMap<>();
    private HashMap<Integer, Car> cars = new HashMap<>();

    public void initGraphByDistance() {
        crossMap.forEach((cross, crossObj) -> graph.addVertex(crossObj));
        roads.forEach((roadId, road) -> {
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            DefaultWeightedEdge edge = graph.addEdge(from, to);
            graph.setEdgeWeight(edge, road.getLen());
            if (road.isBidirectional()) {
                DefaultWeightedEdge opposeEdge = graph.addEdge(to, from);
                graph.setEdgeWeight(opposeEdge, road.getLen());
            }
        });
    }

    public void initGraphByTime() {
        crossMap.forEach((cross, crossObj) -> graph.addVertex(crossObj));
        roads.forEach((roadId, road) -> {
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            DefaultWeightedEdge edge = graph.addEdge(from, to);
            graph.setEdgeWeight(edge, road.getLen() / (road.getTopSpeed() * 1.0));
            if (road.isBidirectional()) {
                DefaultWeightedEdge opposeEdge = graph.addEdge(to, from);
                graph.setEdgeWeight(opposeEdge, road.getLen() / (road.getTopSpeed() * 1.0));
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
        for (int i = 0; i < path.getLength(); i++) {
            for (int roadId1 : ((CrossRoads) path.getVertexList().get(i)).getRoadIds()) {
                for (int roadId2 : ((CrossRoads) path.getVertexList().get(i + 1)).getRoadIds()) {
                    if (roadId1 == roadId2 && roadId1 != -1) {
                        car.addPath(roadId1);
                    }
                }
            }
        }
    }

    public void schedule() {
        initGraphByDistance();

        this.getCars().forEach(
                (carId, car) -> priorityQueue.offer(car)
        );
        int time = 0;
        int count = 0;
        int carFlowLimit = 30;

        while (!priorityQueue.isEmpty()) {
            time++;
            count = 0;
            while (true) {
                Car car = priorityQueue.peek();
                if (car == null || car.getPlanTime() > time || count >= carFlowLimit)
                    break;

                car.setStartTime(time);
                GraphPath path = shortestDistancePath(graph, car.getFrom(), car.getTo());
                setCarPath(car, path);

                scheduler.addToGarage(car);
                priorityQueue.remove(car);
                count++;
            }
            scheduler.step();
        }
        scheduler.stepUntilFinish(getCars().size());
        scheduler.printCarStates();
    }

    public int scheduleTest(int carFlowLimit) {
        scheduler.reset();

        this.getCars().forEach(
                (carId, car) -> priorityQueue.offer(car)
        );
        int time = 0;
        int count = 0;

        while (!priorityQueue.isEmpty()) {
            time++;
            count = 0;
            while (true) {
                Car car = priorityQueue.peek();
                if (car == null || car.getPlanTime() > time || count >= carFlowLimit)
                    break;


                GraphPath path = shortestDistancePath(graph, car.getFrom(), car.getTo());

                car.setStartTime(time).setState(CarState.IN_GARAGE);
                setCarPath(car, path);

                scheduler.addToGarage(car);
                priorityQueue.remove(car);
                count++;
            }
            if (!scheduler.stepWithPlot())
                return -1;
            scheduler.printCarStates();
        }
        if (!scheduler.stepUntilFinish(getCars().size()))
            return -1;
        scheduler.printCarStates();
        return time;
    }

    public void scheduleOneByOne() {
        initGraphByDistance();

        int time = 1;
        int current = 5;

        while (true) {
            System.out.println(current);
            int result = scheduleTest(current);

            if (result != -1)
                current++;
            else {
                current--;
                break;
            }
        }

        scheduleTest(current - 1);

    }

    public void preSchedule(int max_car_limit) {


        this.getCars().forEach(
                (carId, car) -> priorityQueue.offer(car)
        );

        //Busy path counter
        TreeMap<Integer, Integer> roadCounter = new TreeMap<>();

        int time = 0;
        int count = 0;
        while (!priorityQueue.isEmpty()) {
            time++;
            count = 0;
            while (true) {
                Car car = priorityQueue.peek();
                if (car == null || car.getPlanTime() > time || count >= max_car_limit)
                    break;

                car.setStartTime(time);

                GraphPath path = shortestDistancePath(graph, car.getFrom(), car.getTo());
                setCarPath(car, path);

                // 计算每一时间单位最忙的路
                car.getPath().forEach(road -> {
                    if (roadCounter.containsKey(road))
                        roadCounter.put(road, roadCounter.get(road) + 1);
                    else
                        roadCounter.put(road, 1);
                });


                priorityQueue.remove(car);
                count++;
            }
        }

        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(roadCounter.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            //升序排序
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        //调整1/2的路的长度为原来1/3
        int numOfRoadsToAdjust = roads.size() / 2;
        for (int i = 0; i < numOfRoadsToAdjust; i++) {
            Road road = roads.get(list.get(i).getKey());
            CrossRoads from = crossMap.get(road.getStart());
            CrossRoads to = crossMap.get(road.getEnd());
            DefaultWeightedEdge edge = graph.getEdge(from, to);
            graph.setEdgeWeight(edge, road.getLen() / 1.5);
            if (road.isBidirectional()) {
                DefaultWeightedEdge opposeEdge = graph.getEdge(to, from);
                graph.setEdgeWeight(opposeEdge, road.getLen() / 1.5);
            }
        }
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
}


