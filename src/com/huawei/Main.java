package com.huawei;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static ArrayList<String> readFile(String path) {
        ArrayList<String> file_content = new ArrayList<>();
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            // Discard first line
            br.readLine();
            while ((line = br.readLine()) != null) {
                file_content.add(line.replaceAll("[() ]", ""));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file_content;
    }

    public static int writeFile(ArrayList<String> content, String filePath) {
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
        return 0;
    }

    public static void main(String[] args) {
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

        // TODO:read input files
        logger.info("start read input files");
        ArrayList<String> cars = readFile(carPath);
        ArrayList<String> roads = readFile(roadPath);
        ArrayList<String> crossRoads = readFile(crossPath);
        ArrayList<String> answer = new ArrayList<>();

        // Add road first. Then add cross
        TrafficMap trafficMap = new TrafficMap();
        roads.forEach(
                road -> trafficMap.addRoad(new Road(road))
        );

        crossRoads.forEach(
                cross -> trafficMap.addCross(new CrossRoads(cross))
        );

        cars.forEach(
                car -> trafficMap.addCar(new Car(car))
        );


        // TODO: calc
        trafficMap.initGraph();
        //运行规划并调整weight
        trafficMap.preSchedule();
        trafficMap.preSchedule();
        trafficMap.preSchedule();
//               trafficMap.preSchedule();
        trafficMap.schedule();

        trafficMap.getCars().forEach(
                (carId, car) -> answer.add(car.outputResult())
        );
        // TODO: write answer.txt
        logger.info("Start write output file");
        answer.add("#(carId,StartTime,RoadId...)");
        writeFile(answer, answerPath);

        logger.info("End...");
    }
}
