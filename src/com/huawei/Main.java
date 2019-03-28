package com.huawei;

import io.jenetics.*;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.DoubleRange;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static TrafficMap trafficMap = new TrafficMap();
    public static Scheduler scheduler = new Scheduler();

    public static int bestVal = 0;

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
        long startTime = System.currentTimeMillis();

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
        ArrayList<String> answer = new ArrayList<>();

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


        final Engine<DoubleGene, Double> engine = Engine
                .builder(Main::scheduleTime, Codecs.ofVector(DoubleRange.of(1, 100), roads.size()))
                .executor(Executors.newSingleThreadExecutor())
                .optimize(Optimize.MINIMUM)
                .maximalPhenotypeAge(11)
                .populationSize(5)
                .alterers(new SwapMutator<>(0.1), new UniformCrossover<>(0.4))
                .build();

        final EvolutionStatistics<Double,?> statistics = EvolutionStatistics.ofNumber();

        final Phenotype<DoubleGene,Double> best = engine.stream()
                .limit(bySteadyFitness(5))
                .limit(100)
                .peek(statistics)
                .collect(toBestPhenotype());


        System.out.println(statistics);
        System.out.println(best);
        //打印结果
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
    private static double scheduleTime(final double[] weights) {
        trafficMap.initGraphEdgeByWeight(weights);
        trafficMap.scheduleTest1(25);
        return scheduler.getScheduleTime();
    }
}
