package com.huawei;

import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;

public class RoadEdge extends DefaultWeightedEdge  {

    protected Road road;
    protected CrossRoads from;
    protected CrossRoads to;
    protected double weightBefore = 0.0;
    protected double weightNow = 0.0;

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

    protected double getWeight(double before) {

        if (before != 99999)
            weightBefore = before;

        if (calculateLoad() > 0.8)
            return 99999;

        return weightBefore;
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
