package com.huawei;

import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;

public class RoadEdge extends DefaultWeightedEdge {
    protected Road road;
    protected CrossRoads from;
    protected CrossRoads to;
    protected double preWeight=0.0;


    public RoadEdge() {
        super();
    }

    public RoadEdge(Road road, CrossRoads from, CrossRoads to) {
        super();
        this.road = road;
        this.from = from;
        this.to = to;
    }

    @Override
    protected CrossRoads getSource() {
        return from;
    }

    @Override
    protected CrossRoads getTarget() {
        return to;
    }

    @Override
    protected double getWeight() {
        preWeight = costEstimation();
        return preWeight;
    }

    private double costEstimation() {
        double distance = road.getLen();
        double width = road.getNumOfLanes();
        double speed = road.getTopSpeed();
        double load = calculateLoad();

        double cost=distance;
        if(preWeight==0.0){
            cost = distance/speed;
        }else
            cost=preWeight;

        if(load>0.5)
            cost = cost*2;



        return cost;
    }

    public double calculateLoad() {
        double capacity = road.getNumOfLanes() * road.getLen();
        int numberOfCar = 0;

        ArrayList<Lane> laneList;
        // Get lane
        if (road.isBidirectional()) {
            if (from.getId() == road.getStart())
                laneList = road.getLaneListBy(road.getEnd());
            else
                laneList = road.getLaneListBy(road.getStart());
        } else
            laneList = road.getLaneList();

        for (Lane lane : laneList) {
            numberOfCar += lane.getCarMap().size();
        }
        return numberOfCar / (capacity * 1.0);
    }
}
