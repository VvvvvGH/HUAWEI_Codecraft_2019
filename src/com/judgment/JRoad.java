package com.judgment;

import com.huawei.Road;

import java.util.*;

public class JRoad extends Road implements Comparable<JRoad> {
    // 车道实现
    // 如果为双向道路,则存储顺序为start-->end方向的道路，之后是反方向的道路
    // 如车道为6,路口id顺序为 123123
    private ArrayList<Lane> laneList;

    // Key 为路的出口路口Id
    private HashMap<Integer,PriorityQueue<JCar>> waitingQueueMap = new HashMap<>();

    public static Comparator<JCar> carComparator = new Comparator<JCar>() {
        @Override
        public int compare(JCar o1, JCar o2) {
            if (o1.getPosition() > o2.getPosition())
                return 1;
            else if (o1.getPosition() == o2.getPosition() && o1.getLaneId() < o2.getLaneId())
                return 1;
            else return -1;
        }
    };

    public JRoad(String line) {
        super(line);
        if(this.isBidirectional()) {
            laneList = new ArrayList<>(this.getNumOfLanes() * 2);
        }else
            laneList = new ArrayList<>(this.getNumOfLanes());
        // 从１开始
        for (int i = 1; i <= laneList.size(); i++) {
            laneList.set(i-1,new Lane());
            laneList.get(i-1).setS1(this.getTopSpeed());
            laneList.get(i-1).setId(i);
        }
        // Priority queue
        if(isBidirectional()){
            waitingQueueMap.put(getStart(),new PriorityQueue<JCar>(carComparator));
            waitingQueueMap.put(getEnd(),new PriorityQueue<JCar>(carComparator));
        }
        waitingQueueMap.put(getEnd(),new PriorityQueue<>());

    }

    // 		 1. 出发的车
    //       2. 入路的车
    //       3. 需要设置车辆车道Id
    //       4. 需要更新车辆数据
    public boolean moveToRoad(JCar car) {
        /*for(int i = 0; i < laneMap.size() ; i++){
            ArrayList<JCar> paneCars = laneMap.get(i);
            JCar frontCar = paneCars.get(paneCars.size() - 1);
            if(paneCars.size()==0 || frontCar.getPosition() > 0) {
                int speed = car.getCurrentSpeed() - frontCar.getCurrentSpeed() >0 ? frontCar.getCurrentSpeed() : car.getCurrentSpeed();
                car.setCurrentSpeed(speed);
                paneCars.add(car);
                return true;
            }
        }*/
        return false;
    }
    // 对单独车道处理
    public void moveCarsOnRoad(int laneId, int crossRoadId) {
    	Lane lane = getLaneListBy(crossRoadId).get(laneId-1);
    	TreeMap<Integer, JCar> carMap = lane.getCarMap();
        int s1 = lane.getS1();   // 当前路段的最大行驶距离或者车子与前车之间的最大可行驶距离
        for (Integer position : carMap.descendingKeySet()) {
            JCar car = carMap.get(position);
            int sv1 = car.getCurrentSpeed(); // 当前车速在当前道路的最大行驶距离
            Integer higher = carMap.descendingKeySet().higher(position);
            if (higher != null) { // 前方有车
            	JCar frontCar = carMap.get(higher);
            	CarState state = frontCar.getState();
                int dist = frontCar.getPosition() - car.getPosition()-1;
                if (sv1 <= dist) {
                    car.setPosition(sv1 + car.getPosition());
                    car.setState(CarState.END);
                } else {
                	// 会碰上车。
                	if( state == CarState.END) {
                		car.setPosition(frontCar.getPosition()-1);
                		car.setState( CarState.END);
                	}else if( state == CarState.WAIT){
                		car.setState( CarState.WAIT);
                	}else{
                		System.err.println("Jroad#moveCarsOnRoad#error");
                	}
                }
            } else {
                if (sv1 <= this.getLen() - position) {
                    car.setPosition(sv1 + car.getPosition());
                    car.setState(CarState.END);
                } else { // 可以出路口
                    car.setState(CarState.WAIT);
                }
            }
            carMap.remove(position);
            carMap.put(car.getPosition(), car);
        }
    	
    }

    public void moveCarsOnRoad() {
        for( int i=1 ; i<=getNumOfLanes() ; i++){
        	moveCarsOnRoad(i, getEnd());
        	if( isBidirectional()) {
        		moveCarsOnRoad(i, getStart());
        	}
        }
    }

    @Override
    public int compareTo(JRoad r) {
        return this.getId() - r.getId();
    }
    public void offerWaitingQueue(int crossRoadId) {
        // 把车放入等待队列， 需要根据车道进行排序
    	PriorityQueue<JCar> waitingQueue = waitingQueueMap.get(crossRoadId);
    	ArrayList<Lane> lanes = getLaneListBy(crossRoadId);
    	lanes.forEach( 
    		lane->{
    			 Map<Integer,JCar> carMap = lane.getCarMap();
    			 waitingQueue.addAll(carMap.values());
    		}
    	);
    }
    
    public ArrayList<Lane> getLaneListBy(int crossRoadId) {
    	// single road 
		if( crossRoadId == getEnd()) {
		     return new ArrayList<Lane>( laneList.subList(0, getNumOfLanes()));
		}else if( crossRoadId == getStart()) {
			 return new ArrayList<Lane>( laneList.subList(getNumOfLanes(), getNumOfLanes()*2));
		}else {
			System.err.println("Jroad#getLaneListBy#error");
			return null;
		}
    }
    public PriorityQueue<JCar> getWaitingQueue(int crossId) {
        return waitingQueueMap.get(crossId);
    }
    
    // 把车辆从路上移除
    //
    public void removeCarFromRoad(JCar car){
    	laneList.forEach( 
    		lane->{
    			Map map = lane.getCarMap();
    			if( map.containsValue(car)) {
    				map.remove(car);
    			}else {
    				System.err.println("Jroad#removeCarFromRoad#error");
    			}
    		}
    	);
    	
    }
}

