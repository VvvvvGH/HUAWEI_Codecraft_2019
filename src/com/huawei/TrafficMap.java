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
            graph.setEdgeWeight(edge, road.getLen() + road.getNumOfLanes());
            if (road.isBidirectional()) {
                DefaultWeightedEdge opposeEdge = graph.addEdge(to, from);
                graph.setEdgeWeight(opposeEdge, road.getLen() + road.getNumOfLanes());
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
            if (!scheduler.step())
                return -1;
            scheduler.printCarStates();
        }
        if (!scheduler.stepUntilFinish(getCars().size()))
            return -1;
        scheduler.printCarStates();
        return time;
    }

    public int scheduleTest2(int carFlowLimit,boolean initialise) {

        // 重置时间，但不重置车的数据
        scheduler.resetTime();

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

                GraphPath path;
                // 初始化 car.path
                if(initialise) {
                    path = shortestDistancePath(graph, car.getFrom(), car.getTo());
                    setCarPath(car, path);
                }else {
                    // 更新carPath， 和oldPath比较适应性,并淘汰其中较小的
                    car.updatePath();
                    // 基因变异 TODO： 交配繁殖
                    car.setPath(mutate(car));
                }

                car.setStartTime(time).setState(CarState.IN_GARAGE);


                scheduler.addToGarage(car);
                priorityQueue.remove(car);
                count++;
            }
            // 进化一代
            if (!scheduler.step())
                return -1;
            //scheduler.printCarStates();
        }

        if (!scheduler.stepUntilFinish(getCars().size()))
            return -1;
        scheduler.printCarStates();
        return time;
    }

    // 未完成
    public void scheduleTest3(int carFlowLimit){
        scheduler.reset();
        // create population
        for(int i=0;i<4;i++){
            scheduleTest2(carFlowLimit,false);
            this.getCars().forEach(
                    (carId,car) -> car.addRoute(null,car.getPath(),true)
            );
        }
        // evolve population
        for(int i=0;i<4;i++){
            evolvePopulation();
            /*scheduleTest2(carFlowLimit);
            this.getCars().forEach(
                    (carId,car) -> car.addRoute(null,car.getPath(),false)
            );*/
        }

        scheduleTest2(carFlowLimit,false);
        this.getCars().forEach(
                (carId,car) -> {
                    car.setPath(car.getFittest());
                    if(car.getId()<10010){
                        car.getRoutes().forEach((fitness,route)->{
                            System.out.println("fitness: " + fitness +" route: " + Arrays.toString(route.toArray()));
                            System.out.println("route size: " + car.getRoutes().size());
                        });
                    }
                }
        );
    }

    private static final double mutationRate = 0.025;
    private static final int tournamentSize = 5;
    private static final boolean elitism = true;

    public void evolvePopulation(){
        // evolve population
        cars.forEach(
                (carId,car)->mutate(car)
        );

    }
    public ArrayList<Integer> mutate(Car car){
        ArrayList<Integer> path = car.getPath();
        for(int i=1;i<path.size()-1;i++){
            Integer roadId = path.get(i);
            if (Math.random() < mutationRate) {
                int newCrossId = getRandomCrossId(path);
                //ArrayList<Integer> subPath = new ArrayList<Integer>(path.subList(0,path.indexOf(roadId)));
                int crossId = getCross(getRoad(roadId).getEnd()).getId();
                GraphPath subPath1 = shortestDistancePath(graph, car.getFrom(), crossId);
                GraphPath subPath2 = shortestDistancePath(graph, crossId, newCrossId);
                GraphPath subPath3 = shortestDistancePath(graph, newCrossId, car.getTo());
                ArrayList<Integer> newPath = connectPath(subPath1, subPath2, subPath3);
                System.out.println("mutate id: " + getRoad(roadId).getId());
                System.out.println("before crossid: " + crossId);
                System.out.println("mutate crossid: " + newCrossId);
                System.out.println("car: "+car.getId() +"-oldPath--->"+ path);
                System.out.println("car: "+car.getId() +"-newPath--->"+ newPath);

                // 删除因为变异可能产生的重复路口
                boolean isRepeated = true;
                while(isRepeated){
                    isRepeated = false;
                    int index = -1;
                    ArrayList<Integer> crossIds = new ArrayList<>();
                    int fromRoadId;
                    int toRoadId;
                    int j =0 ;
                    for (; j < newPath.size(); j++) {
                        int tempCrossId = getCross(getRoad(newPath.get(j)).getEnd()).getId();
                        if (crossIds.contains(tempCrossId)) {
                            index = crossIds.indexOf(crossId);
                            fromRoadId = newPath.get(index);
                            toRoadId = newPath.get(j + 1);
                            isRepeated = true;
                            break;
                        }
                        crossIds.add(crossId);
                    }
                    if(isRepeated){
                        newPath.subList(index+1,j+1).clear();
                    }
                }
                return newPath;
            }
        }
        return path;
    }

    public ArrayList<Integer> connectPath(GraphPath subPath1, GraphPath subPath2, GraphPath subPath3){
        ArrayList<Integer> newPath = new ArrayList<>();
        System.out.print("subPath1: ");
        for (int i = 0; i < subPath1.getLength(); i++) {
            for (int roadId1 : ((CrossRoads) subPath1.getVertexList().get(i)).getRoadIds()) {
                for (int roadId2 : ((CrossRoads) subPath1.getVertexList().get(i + 1)).getRoadIds()) {
                    if (roadId1 == roadId2 && roadId1 != -1) {
                        newPath.add(roadId1);
                        System.out.print(","+roadId1);
                    }
                }
            }
        }
        System.out.print("\nsubPath2: ");
        for (int i = 0; i < subPath2.getLength(); i++) {
            for (int roadId1 : ((CrossRoads) subPath2.getVertexList().get(i)).getRoadIds()) {
                for (int roadId2 : ((CrossRoads) subPath2.getVertexList().get(i + 1)).getRoadIds()) {
                    if (roadId1 == roadId2 && roadId1 != -1) {
                        newPath.add(roadId1);
                        System.out.print(","+roadId1);
                    }
                }
            }
        }
        System.out.print("\nsubPath3: ");
        for (int i = 0; i < subPath3.getLength(); i++) {
            for (int roadId1 : ((CrossRoads) subPath3.getVertexList().get(i)).getRoadIds()) {
                for (int roadId2 : ((CrossRoads) subPath3.getVertexList().get(i + 1)).getRoadIds()) {
                    if (roadId1 == roadId2 && roadId1 != -1) {
                        newPath.add(roadId1);
                        System.out.print(","+roadId1);
                    }
                }
            }
        }
        System.out.println();
        // 删除连接道路后可能产生的重复路径
        boolean isRepeated = true;
        while(isRepeated){
            isRepeated = false;
            int index = -1;
            int i =0 ;
            for (; i < newPath.size(); i++) {
                index = newPath.lastIndexOf(newPath.get(i));
                if (index != i) {
                    isRepeated = true;
                    break;
                }
            }
            if(isRepeated){
               newPath.subList(i+1,index+1).clear();
            }
        }
        return newPath;
    }
    public int getRandomCrossId(ArrayList<Integer> path){
        Integer[] keys = crossMap.keySet().toArray(new Integer[0]);
        Random random  =new Random();
        while(true) {
            int crossId = keys[random.nextInt(keys.length)];
            Integer[] roadIds = getCross(crossId).getRoadIds();
            for(int i=0;i<roadIds.length;i++){
                if(path.contains(roadIds[i]))
                    continue;
                else if(i==roadIds.length - 1 )
                    return crossId;
            }
        }
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

                // 考虑车道数目？
                roadCounter.forEach((road,roadCount)->{
                    roadCounter.put(road, roadCount/roads.get(road).getNumOfLanes());
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


