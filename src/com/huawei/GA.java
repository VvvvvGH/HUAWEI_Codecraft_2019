package com.huawei;

import io.jenetics.*;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.DoubleRange;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class GA {

    private static TrafficMap trafficMap;
    private static int MAX_CAR_FLOW = 35;

    public static void main(String[] args) {
        Main.initiate(args);
        trafficMap = Main.trafficMap;
        trafficMap.initGraphEdge();
        trafficMap.preScheduleDirection(10);
        trafficMap.preScheduleDirection(10);
        trafficMap.preScheduleDirection(10);
        trafficMap.preScheduleDirection(10);
        double[] weights = trafficMap.readGraphEdgeWeight();
        DoubleRange[] ranges = new DoubleRange[weights.length];
        for (int i = 0; i < weights.length; i++) {
            ranges[i] = DoubleRange.of(weights[i] / 2.0, weights[i] * 2.0);
        }
        Codec<double[], DoubleGene> codec = Codecs.ofVector(ranges);
        final Engine<DoubleGene, Double> engine = Engine
                //.builder(Main::scheduleTime, Codecs.ofVector(DoubleRange.of(1, 100), roads.size()))
                .builder(GA::scheduleTime, codec)
                .executor((Executor) Runnable::run)
                //.survivorsSize()
                .selector(new TournamentSelector<>())
                .optimize(Optimize.MINIMUM)
                .maximalPhenotypeAge(10)
                .populationSize(10)
                .alterers(new Mutator<>(0.2), new UniformCrossover<>(0.2))
                .build();

        final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

        final Phenotype<DoubleGene, Double> best = engine.stream()
                .limit(bySteadyFitness(5))
                .limit(300)
                .peek(statistics)
                .collect(toBestPhenotype());

        System.out.println(statistics);
        System.out.println(best);

        double[][] results = best.getGenotype()
                .stream()
                .map(dc -> dc.as(DoubleChromosome.class).toArray())
                .toArray(double[][]::new);

        double[] bestWeight = new double[results.length];
        for (int i = 0; i < bestWeight.length; i++) {
            bestWeight[i] = results[i][0];
            System.out.print(bestWeight[i]);
        }
        System.out.println();

        writeTrainedWeight(bestWeight);

//        double[] bestWeight = readTrainedWeight("weight_1195615010.txt");
        trafficMap.initGraphEdge(bestWeight);
        trafficMap.scheduleTest(MAX_CAR_FLOW);

        //打印结果
        ArrayList<String> answer = new ArrayList<>();
        String answerPath = args[3];
        trafficMap.getCars().forEach(
                (carId, car) -> {
                    answer.add(car.outputResult());
                }
        );

        Main.writeFile(answer, answerPath);
        System.exit(0);
    }

    private static double scheduleTime(final double[] weights) {
        System.out.print("[");
        for (int i = 0; i < weights.length; i++) {
            System.out.print(String.format("%.1f", weights[i]) + ", ");
        }
        System.out.println();
        trafficMap.initGraphEdge(weights);
        Long time = trafficMap.scheduleTest(MAX_CAR_FLOW);
        if (time == -1)
            return 10000;
        return time;
    }

    public static void writeTrainedWeight(double[] weights) {
        File file = new File("weight_" + weights.hashCode() + ".txt");
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (double weight : weights) {
                fileWriter.write(weight + ",");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static double[] readTrainedWeight(String path) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            String string = bufferedReader.readLine();
            String[] strings = string.split(",");
            double[] weights = new double[strings.length];
            for (int i = 0; i < weights.length; i++) {
                weights[i] = Double.parseDouble(strings[i]);
            }
            return weights;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.err.println("Read file failed ");
        return null;
    }
}

