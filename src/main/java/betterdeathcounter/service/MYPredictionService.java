package betterdeathcounter.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.google.common.util.concurrent.AtomicDouble;

import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Death;
import betterdeathcounter.model.Settings;

public class MYPredictionService {
    
    private final Mean calculateMean = new Mean();
    private final StandardDeviation calculateStandardDeviation = new StandardDeviation();
    private final CalculateService calculateService = new CalculateService();

    private static final int NORMAL_DISTRIBUTION_SCALING = 999;
    private static final int MAX_VALUE = Integer.MAX_VALUE;
    private static final int PB_RANGE_THRESHOLD = 5;
    private static final int NUM_THREADS = 5;

    private int nextPB = 0;

    public double[] getMYPredictions(Boss boss, Settings settings) {
        List<Death> deaths = boss.getDeaths();
        if (deaths.size() < 9 || settings.getNumBadTrys() < 1 
            || boss.getName().equals("Other Monsters or Heights") 
            || boss.getName().equals("Please create a new game")
            || calculateService.bossDead(boss)) {
            return new double[]{};
        }

        double[] data = getData(deaths);
        double[] weights = getWeights(data, NORMAL_DISTRIBUTION_SCALING); //TODO wirklich egal?
        double mean = calculateMean.evaluate(data);
        double weightedAvg = calculateMean.evaluate(data, weights);
        double possiblePB = weightedAvg - (mean - weightedAvg);
        
        int pb = MAX_VALUE;
        for (int i = 0; i < data.length; i++) {
            if(data[i] < pb) {
                pb = (int) data[i];
            }
        }

        // System.out.println();
        // System.out.println();
        // System.out.println();
        // System.out.println();

        int realPb = pb;

        int startIndex = data.length-1;
        double nextValue = 11;
        int iterations = 0;// Limit iterations
        boolean foundPB = true;

        while (possiblePB > 1 && nextValue-5 > 1 && iterations < 10) {
            nextValue = getNextValue(data, pb);
            if (nextValue < pb) {
                pb = (int)nextValue;
            }

            
            // add bad trys
            double[] badTrys = getBadTrys(data, settings.getNumBadTrys(), settings.getCumulativeProbabilityScaling());
            data = appendArray(data, badTrys);
            
            // add nextValue
            data = Arrays.copyOf(data, data.length + 1);
            data[data.length - 1] = nextValue;
            

            if (foundNextPb(data, realPb, foundPB)) {
                foundPB = false;
            }
            if (foundPB && nextValue < realPb) {
                nextPB = (int)nextValue;
                foundPB = false;
            }

            weights = getWeights(data, NORMAL_DISTRIBUTION_SCALING);
            mean = calculateMean.evaluate(data);
            weightedAvg = calculateMean.evaluate(data, weights);
            possiblePB = weightedAvg - (mean - weightedAvg);
            // System.out.println("Mean: " + mean);
            // System.out.println("Weighted Average: " + weightedAvg);
            // System.out.println("Possible PB: " + possiblePB);

            iterations++;
        }

        // Add last Try
        data = Arrays.copyOf(data, data.length + 1);
        data[data.length - 1] = (double)((int)possiblePB*100)/100;

        // Add next PB
        data = Arrays.copyOf(data, data.length + 1);
        data[data.length - 1] = nextPB;

        return Arrays.copyOfRange(data, startIndex, data.length);
    }

    private boolean foundNextPb(double[] data, int realPb, boolean foundPB) {
        if (!foundPB) {
            return false;
        }

        for (int i = 0; i < data.length; i++) {
            if(data[i] < realPb) {
                nextPB = (int) data[i];
                return true;
            }
        }
        return false;
    }

    private double[] getData(List<Death> deaths) {
        List<Double> percentages = new ArrayList<>();
        for (Death death : deaths) {
            percentages.add((double) death.getPercentage());
        }
        return percentages.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private static double[] appendArray(double[] arr1, double[] arr2) {
        double[] result = new double[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    private double[] getBadTrys(double[] data, int numBadTrys, double cumulativeProbabilityScaling) {
        double[] badTrys = new double[numBadTrys];

        double mean = calculateMean.evaluate(data);
        double standardDeviation = calculateStandardDeviation.evaluate(data);
        
        NormalDistribution normal = new NormalDistribution(mean, standardDeviation + NORMAL_DISTRIBUTION_SCALING);
        
        for (int i = 0; i < badTrys.length; i++) {
            double sample = Integer.MAX_VALUE;
            while (sample >= 101 || sample < 0 || !(normal.cumulativeProbability(sample) > cumulativeProbabilityScaling)) {
                sample = normal.sample();
            }
            badTrys[i] = sample;
        }
        return badTrys;
    }

    private boolean isInPBRange(double percentage, int pb) {
        if(percentage < pb || percentage - pb < PB_RANGE_THRESHOLD) {
            return true;
        } else {
            return false;
        }
    }

    private double getNextValue(double[] data, int pb) {
        final double mean = calculateMean.evaluate(data);
        final double standardDeviation = calculateStandardDeviation.evaluate(data);
        final NormalDistribution normal = new NormalDistribution(mean, standardDeviation);

        AtomicDouble nextValue = new AtomicDouble(-1.0);
        AtomicBoolean running = new AtomicBoolean(true);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        Runnable sampleSearch = new Runnable() {
            @Override
            public void run() {
                double sample;
                do {
                    sample = normal.sample();
                } while ((sample >= 101 || sample < 0 || !isInPBRange(sample, pb)) && running.get());
                if (nextValue.get() == -1.0) {
                    nextValue.set(sample);
                    running.set(false);
                }
            }
        };

        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(sampleSearch);
        }

        while (nextValue.get() == -1.0) {}
        
        // Shut down the executor
        executor.shutdown();

        return nextValue.get();
    }

    private double[] getWeights(double[] data, double weightScaling) {
        double mean = calculateMean.evaluate(data);
        double standardDeviation = calculateStandardDeviation.evaluate(data);
        
        NormalDistribution normal = new NormalDistribution(mean, standardDeviation+weightScaling);

        double[] weights = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            double density = data[i];
            
            //Mirror normal Distribution 
            density = normal.density(density);
            density *= -1;
            density += normal.density(mean);
            density *= 10000;

            weights[i] = density;
        }

        return weights;
    }

    public double[] getPredInfos(List<Death> deaths, Settings settings, double lastPred) {
        if (deaths.size() < 9 || settings.getNumBadTrys() < 1) {
            return new double[]{};
        }

        double[] data = getData(deaths);
        double[] weights = getWeights(data, NORMAL_DISTRIBUTION_SCALING);
        double weightedAvg = calculateMean.evaluate(data, weights);

        return new double[]{lastPred, weightedAvg};
    }
}
