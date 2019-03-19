package com.huawei;

import java.util.ArrayList;
import java.util.HashMap;

public class CrossRoads {
    private int id;
    //路口顺序为 顺时针方向开始。 roadIds[0] 为上方
    private int[] roadIds = new int[4];

    public CrossRoads(int id) {
        this.id = id;
    }

    public CrossRoads(String line) {
        String[] vars = line.split(",");
        this.id = Integer.parseInt(vars[0]);
        for (int i = 1; i < vars.length; i++) {
            roadIds[i - 1] = Integer.parseInt(vars[i]);
        }
    }

    public int[] getRoadIds() {
        return roadIds;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
