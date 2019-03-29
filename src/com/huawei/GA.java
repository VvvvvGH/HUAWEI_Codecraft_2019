package com.huawei;

import io.jenetics.*;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.DoubleRange;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class GA {

    private static TrafficMap trafficMap;

    public static void main(String[] args) {
        Main.initiate(args);
        trafficMap = Main.trafficMap;

        final Engine<DoubleGene, Double> engine = Engine
                .builder(GA::scheduleTime, Codecs.ofVector(DoubleRange.of(1, 100), trafficMap.getRoads().size()))
                .executor(Executors.newSingleThreadExecutor())
                .optimize(Optimize.MINIMUM)
                .maximalPhenotypeAge(11)
                .populationSize(5)
                .selector(new TournamentSelector<>())
                .alterers(new SwapMutator<>(0.40), new UniformCrossover<>(0.15))
                .build();

        final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

        final Phenotype<DoubleGene, Double> best = engine.stream()
                .limit(bySteadyFitness(20))
                .limit(10)
                .peek(statistics)
                .collect(toBestPhenotype());

        System.out.println(statistics);
        System.out.println(best.getGeneration());
        System.out.println(best.getFitness());
        System.out.println(best.toString().split("[\\[\\]]"));

        trafficMap.initGraphEdge(getResultWeight(best.toString()));
        trafficMap.scheduleTest(34);

        File file = new File("trainedWeight.txt");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(best.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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
        trafficMap.initGraphEdge(weights);
        return trafficMap.scheduleTest(34);
    }

    private static double[] getResultWeight(String str) {
        str = "[[[1.0699999579859487],[46.522618841380776],[58.66766535711375],[54.805418488247895],[36.84365270389485],[92.19809575478088],[34.25464447613558],[82.86493900447581],[59.24396089439871],[69.44438493657213],[43.23200676463196],[79.49399963597872],[9.367095596363116],[94.32690160137345],[9.367095596363116],[56.04672517982384],[87.48113344010633],[8.067617024171096],[66.26543499662704],[54.0513391458363],[16.833300185474002],[71.98552471976738],[57.65745451582441],[9.462994900929443],[44.86167041396951],[69.44438493657213],[91.53398225924752],[59.24396089439871],[83.52012769502224],[43.23200676463196],[24.265393128159804],[13.861519906718218],[47.912435491371255],[27.123823281742098],[34.07470189637507],[78.39170628661195],[53.278593308413235],[59.24396089439871],[72.02685946309222],[40.97232761553397],[10.48339550326495],[22.607843568917758],[42.39399741847569],[8.067617024171096],[62.85591178736871],[64.30332721641622],[24.84680145848691],[98.16281248002714],[28.270614576123233],[49.32724437845454],[79.49399963597872],[57.65745451582441],[79.49399963597872],[5.810445770755251],[57.65745451582441],[1.0699999579859487],[27.759039765716143],[82.7702017726158],[98.16281248002714],[56.04672517982384],[95.90298217094413],[78.1474243436827],[80.65237766066285],[8.63311466358303],[74.51947412676068],[80.48683741593193],[62.85591178736871],[34.25464447613558],[81.44003976017035],[2.2200621775961045],[95.69340382049207],[76.39003391007508],[99.19116665322616],[28.647001827197066],[66.85119345209299],[12.865992400437177],[31.07815570841946],[97.07943042427061],[3.5381861239161285],[58.66766535711375],[84.22108837260308],[2.2200621775961045],[42.39399741847569],[75.0280890822823],[79.49399963597872],[76.39003391007508],[5.810445770755251],[36.84365270389485],[94.32690160137345],[13.521314846362811],[51.736466614756814],[11.588003685783512],[44.86167041396951],[66.26543499662704],[27.759039765716143],[18.856570831092082],[71.98552471976738],[34.519162704018584],[81.44003976017035],[67.72353392638303],[75.0280890822823],[9.462994900929443],[12.865992400437177],[30.281852238673103],[9.367095596363116]]] --> 457.0".split("-->")[0];
        str = str.replaceAll("[\\]\\[]", "");
        String[] results = str.split(",");

        double[] doubles = new double[results.length];
        for (int i = doubles.length -1; i >= 0 ; i--) {
            doubles[i] = Double.parseDouble(results[i]);
        }

        return doubles;
    }
}

