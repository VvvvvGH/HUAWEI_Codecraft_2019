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
        logger.info("Start...");


        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String presetAnswerPath = args[3];
        String answerPath = args[4];

        logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " and answerPath = " + answerPath);

        // Read input files
        logger.info("start read input files");
        ArrayList<String> cars = readFile(carPath);
        ArrayList<String> roads = readFile(roadPath);
        ArrayList<String> crossRoads = readFile(crossPath);
        ArrayList<String> presetAnswers = readFile(presetAnswerPath);

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

        presetAnswers.forEach(
                answerLine -> {
                    String[] vars = answerLine.split(",");
                    int carId = Integer.parseInt(vars[0]);
                    Car car = scheduler.getCar(carId);
                    car.clearPath();
                    car.setStartTime(Integer.parseInt(vars[1]));
                    if (car.getPath().size() == 0) {
                        for (int i = 2; i < vars.length; i++) {
                            if (Integer.parseInt(vars[i]) > 0) {
                                car.addPath((Integer.parseInt(vars[i])));
                            }
                        }
                    }
                }
        );
    }

    public static void main(String[] args) {
        initiate(args);

        long startTime = System.currentTimeMillis();


        trafficMap.initGraphEdge();



        trafficMap.preSchedule3(10);

        trafficMap.preSchedule3(10);

        trafficMap.preSchedule3(10);

        trafficMap.preSchedule3(10);
        trafficMap.preSchedule3(20);

        trafficMap.scheduleTest2(55);


        //打印结果，无需打印预置车辆
        ArrayList<String> answer = new ArrayList<>();
        String answerPath = args[4];
        trafficMap.getCars().forEach(
                (carId, car) -> {
                    if (!car.isPreset()) {
                        answer.add(car.outputResult());
                    }
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
