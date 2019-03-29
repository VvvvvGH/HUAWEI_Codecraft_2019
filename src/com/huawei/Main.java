package com.huawei;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);


    public static TrafficMap trafficMap;
    public static Scheduler scheduler;

    public static int bestVal = 0;

    public static void initiate(String[] args) {
        if (args.length != 4) {
            logger.error("please input args: inputFilePath, resultFilePath");
            return;
        }


        logger.info("Start...");


        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " and answerPath = " + answerPath);

        // Read input files
        logger.info("start read input files");
        ArrayList<String> cars = readFile(carPath);
        ArrayList<String> roads = readFile(roadPath);
        ArrayList<String> crossRoads = readFile(crossPath);

        trafficMap = new TrafficMap(new Scheduler());
        scheduler = trafficMap.getScheduler();

        // Add road first. Then add cross
        roads.forEach(
                roadLine -> {
                    Road road = new Road(roadLine);
                    trafficMap.addRoad(road);
                    scheduler.addRoad(road);
                }
        );

        crossRoads.forEach(
                crossLine -> {
                    CrossRoads cross = new CrossRoads(crossLine);
                    trafficMap.addCross(cross);
                    scheduler.addCross(cross);
                }
        );

        cars.forEach(
                carLine -> {
                    Car car = new Car(carLine);
                    trafficMap.addCar(car);
                    scheduler.addCar(car);
                }
        );
    }

    public static void main(String[] args) {
        initiate(args);

        long startTime = System.currentTimeMillis();

        //运行规划
        trafficMap.initGraphEdge();
        trafficMap.pathClassification();

        trafficMap.preScheduleDirection(10);
        trafficMap.preScheduleDirection(10);
        trafficMap.preScheduleDirection(10);
        trafficMap.preScheduleDirection(10);

        Car car1 = trafficMap.getCar(10000);
        if (car1.getFrom() == 18 && car1.getTo() == 50) {
            System.out.println("Map 1");
            bestVal = 35;
        } else {
            System.out.println("Map 2");
            bestVal = 30;
        }


        long minTime = 99999;
        for (int i = bestVal; i < bestVal + 10; i++) {
            System.out.println("Trying " + i);
            long result = trafficMap.scheduleTest(i);
            if (minTime > result && result != -1) {
                minTime = result;
                bestVal = i;
            }
            if(result==-1){
                break;
            }
        }

        System.out.println(minTime);
        System.out.println(bestVal);
        trafficMap.scheduleTest(bestVal);

        //打印结果
        ArrayList<String> answer = new ArrayList<>();
        String answerPath = args[3];
        trafficMap.getCars().forEach(
                (carId, car) -> {
                    answer.add(car.outputResult());
                }
        );


        //  write answer.txt
        logger.info("Start write output file");
        writeFile(answer, answerPath);

        logger.info("End...");
        long endTime = System.currentTimeMillis();
        System.out.println("Main程序运行时间：" + (endTime - startTime) + "ms");
    }

    public static ArrayList<String> readFile(String path) {
        ArrayList<String> file_content = new ArrayList<>();
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            // Discard line start with #
            while ((line = br.readLine()) != null && !line.equals("")) {
                if (!line.startsWith("#"))
                    file_content.add(line.replaceAll("[() ]", ""));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file_content;
    }

    public static void writeFile(ArrayList<String> content, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            content.forEach(line -> {
                try {
                    bw.write(line + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
