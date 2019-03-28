package com.huawei;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.prog.ProgramGene;
import io.jenetics.util.DoubleRange;
import io.jenetics.util.ISeq;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;


public class GA {
    public static TrafficMap trafficMap = new TrafficMap();
    public static Scheduler scheduler = new Scheduler();


    private static double scheduleTime(final double[] weights) {
        //trafficMap.initGraphByDistance(weights);
        trafficMap.scheduleTest(25);
        return scheduler.getScheduleTime();
    }


    public static void main(String[] args) {
        initialContext(args);

        final Engine<DoubleGene, Double> engine = Engine
                .builder(GA::scheduleTime, Codecs.ofVector(DoubleRange.of(0, 1), 59))
                .optimize(Optimize.MINIMUM)
                .maximalPhenotypeAge(11)
                .populationSize(50)
                .alterers(new SwapMutator<>(0.2), new UniformCrossover<>(0.35))
                .build();

        final EvolutionStatistics<Double,?> statistics = EvolutionStatistics.ofNumber();

        final Phenotype<DoubleGene,Double> best = engine.stream()
                .limit(bySteadyFitness(15))
                .limit(100)
                .peek(statistics)
                .collect(toBestPhenotype());

        System.out.println(statistics);
        System.out.println(best);
    }


    public static void initialContext(String[] args) {
        if (args.length != 4) {
            return;
        }
        long startTime = System.currentTimeMillis();

        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];

        // Read input files
        ArrayList<String> cars = Main.readFile(carPath);
        ArrayList<String> roads = Main.readFile(roadPath);
        ArrayList<String> crossRoads = Main.readFile(crossPath);
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

    }

}
