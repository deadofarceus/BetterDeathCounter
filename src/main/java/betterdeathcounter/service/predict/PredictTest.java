package betterdeathcounter.service.predict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import betterdeathcounter.service.TimeService;

public class PredictTest {
    public static void main(String[] args) throws Exception {
        double[] data = {100, 100, 99, 100, 100, 100, 100, 99, 99, 100, 
            98, 100, 97, 100, 100, 99, 100, 99, 99, 99, 97, 96, 97, 100, 
            100, 90, 97, 91, 88, 97, 99, 95, 96, 87, 100, 98, 99, 97, 97, 
            88, 98, 94, 99, 97, 99, 99, 99, 100, 100, 92, 98, 81, 92, 99, 
            96, 92, 93, 98, 96, 98, 100, 97, 100, 94, 97, 95, 98, 100, 95, 
            98, 95, 99, 95, 98, 100, 95, 99, 91, 100, 94, 98, 85, 94, 99, 91, 
            82, 99, 90, 95, 94, 98, 90, 95, 83, 92, 94, 90, 87, 95, 92, 95, 
            82, 96, 99, 87, 87, 93, 91, 95, 99, 98, 100, 98, 93, 94, 89, 
            100, 96, 78, 91, 93, 90, 86, 82, 100, 100, 99, 95, 93, 97, 95, 
            98, 92, 97, 95, 92, 97, 91, 82, 99, 96, 80, 93, 90, 88, 92, 96, 
            92, 100, 98, 92, 98, 83, 90, 87, 98, 94, 91, 74, 78, 87, 91, 98, 
            90, 90, 98, 97, 96, 95, 87, 74, 99, 51, 63, 70, 98, 83, 36, 85, 87, 
            91, 95, 100, 97, 92, 97, 98, 100, 97, 91, 88, 98, 91, 94, 93, 95, 92, 
            78, 80, 54, 87, 93, 97, 97, 87, 96, 96, 94, 93, 86, 97, 82, 95, 98, 82,
            84, 97, 78, 95, 85, 75, 89, 92, 95, 73, 99, 64, 98, 32, 65, 97, 94, 
            66, 68, 90, 89, 83, 98, 93, 74, 86, 99, 95, 99, 96, 97, 95, 75, 86, 
            93, 68, 36, 54, 72, 98, 87, 93, 92, 92, 83, 91, 57, 90, 30};
        
        // double[] data2 = {100, 100, 100, 99, 100, 99, 100, 99, 100, 99, 100, 100, 98, 99, 100, 97, 100, 99, 100, 97, 98, 98, 100, 100, 99, 99, 99, 97, 98, 99, 100, 99, 98, 98, 100, 96, 100, 99, 100, 96, 93, 100, 100, 100, 99, 99, 92, 92, 89, 98, 78, 100, 98, 94, 92, 98, 86, 72, 89, 98, 94, 98, 98, 92, 78, 85, 86, 100, 98, 96, 98, 98, 98, 80, 94, 92, 88, 90, 93, 93, 90, 98, 98, 96, 96, 90, 73, 100, 95, 80, 78, 93, 98, 85, 78, 100, 94, 73, 100, 92, 85, 95, 80, 99, 78, 80, 74, 93, 72, 71, 100, 90, 77, 87, 86, 73, 100, 100, 78, 76, 77, 88, 99, 98, 99, 89, 58, 100, 76, 86, 100, 100, 70, 75, 97, 80, 80, 78, 76, 96, 86, 94, 80, 95, 97, 89, 97, 97, 93, 100, 78, 76, 98, 57, 94, 84, 95, 85, 92, 98, 71, 69, 98, 98, 63, 92, 93, 98, 97, 78, 63, 72, 72, 94, 74, 73, 71, 93, 76, 78, 67, 98, 98, 78, 84, 60, 98, 87, 80, 56, 95, 98, 97, 98, 97, 100, 100, 98, 100, 99, 99, 99, 98, 97, 97, 100, 99, 98, 100, 99, 100, 100, 98, 98, 99, 98, 99, 99, 96, 98, 100, 99, 99, 99, 98, 100, 99, 100, 98, 99, 97, 99, 99, 98, 96, 99, 99, 98, 97, 96, 96, 97, 98, 99, 100, 98, 98, 98, 96, 99, 100, 99, 99, 99, 95, 97, 98, 100, 97, 97, 100, 99, 88, 62, 73, 100, 94, 100, 96, 96, 97, 77, 94, 90, 69, 97, 94, 69, 79, 64, 89, 77, 99, 99, 93, 94, 78, 59, 78, 95, 99, 53, 98, 100, 95, 94, 94, 98, 96, 63, 80, 78, 100, 97, 97, 99, 66, 92, 95, 98, 99, 90, 100, 98, 61, 94, 96, 72, 83, 99, 85, 94, 98, 57, 98, 89, 93, 68, 99, 95, 92, 98, 95, 96, 80, 59, 95, 98, 69, 99, 98, 98, 80, 94, 76, 65, 94, 93, 87, 72, 99, 91, 98, 88, 88, 99, 82, 81, 91, 56, 90, 91, 74, 57, 98, 77, 87, 87, 91, 96, 77, 100, 93, 100, 73, 98, 80, 97, 93, 93, 98, 80, 88, 99, 98, 93, 93, 87, 82, 70, 74, 60, 83, 88, 93, 98, 100, 97, 82, 98, 89, 77, 83, 98, 99, 99, 95, 100, 98, 97, 95, 87, 84, 98, 96, 60, 87, 74, 83, 96, 98, 90, 98, 96, 94, 99, 96, 97, 97, 75, 83, 85, 60, 77, 96, 96, 70, 87, 88, 92, 88, 86, 99, 99, 89, 92, 80, 62, 100, 60, 100, 96, 82, 73, 77, 79, 83, 91, 95, 92, 99, 54, 96, 57, 73, 64, 76, 91, 98, 57, 94, 99, 85, 66, 97, 99, 94, 96, 96, 57, 99, 97, 85, 74, 98, 98, 56, 99, 93, 95, 80, 53, 98, 98, 98, 79, 100, 72, 59, 58, 88, 81, 96, 51, 98, 87, 98, 95, 71, 83, 99, 55, 80, 63, 79, 77, 94, 72, 55, 55, 100, 99, 43, 56, 94, 99, 68, 33, 94, 58, 72, 97, 79, 91, 94, 58, 0};
        
        List<Integer> datalist = List.of(100, 100, 99, 100, 100, 100, 100, 99, 99, 100, 
        98, 100, 97, 100, 100, 99, 100, 99, 99, 99, 97, 96, 97, 100, 
        100, 90, 97, 91, 88, 97, 99, 95, 96, 87, 100, 98, 99, 97, 97, 
        88, 98, 94, 99, 97, 99, 99, 99, 100, 100, 92, 98, 81, 92, 99, 
        96, 92, 93, 98, 96, 98, 100, 97, 100, 94, 97, 95, 98, 100, 95, 
        98, 95, 99, 95, 98, 100, 95, 99, 91, 100, 94, 98, 85, 94, 99, 91, 
        82, 99, 90, 95, 94, 98, 90, 95, 83, 92, 94, 90, 87, 95, 92, 95, 
        82, 96, 99, 87, 87, 93, 91, 95, 99, 98, 100, 98, 93, 94, 89, 
        100, 96, 78, 91, 93, 90, 86, 82, 100, 100, 99, 95, 93, 97, 95, 
        98, 92, 97, 95, 92, 97, 91, 82, 99, 96, 80, 93, 90, 88, 92, 96, 
        92, 100, 98, 92, 98, 83, 90, 87, 98, 94, 91, 74, 78, 87, 91, 98, 
        90, 90, 98, 97, 96, 95, 87, 74, 99, 51, 63, 70, 98, 83, 36, 85, 87, 
        91, 95, 100, 97, 92, 97, 98, 100, 97, 91, 88, 98, 91, 94, 93, 95, 92, 
        78, 80, 54, 87, 93, 97, 97, 87, 96, 96, 94, 93, 86, 97, 82, 95, 98, 82,
        84, 97, 78, 95, 85, 75, 89, 92, 95, 73, 99, 64, 98, 32, 65, 97, 94, 
        66, 68, 90, 89, 83, 98, 93, 74, 86, 99, 95, 99, 96, 97, 95, 75, 86, 
        93, 68, 36, 54, 72, 98, 87, 93, 92, 92, 83, 91, 57, 90);     
        //
        
        double[] weights = getWeights(data, 999);
        double mean = new Mean().evaluate(data);
        double standardDeviation = new StandardDeviation().evaluate(data);
        TimeService.print("Mean: " + mean);
        TimeService.print("Standard deviation: " + standardDeviation);

        double weightedAvg = new Mean().evaluate(data, weights);

        List<Integer> pbList = new ArrayList<>();
        List<Integer> pbDiff = new ArrayList<>();
        
        int pb = Integer.MAX_VALUE;
        int prevPB = 0;
        for (int i = 0; i < datalist.size(); i++) {
            if (isInPBRange(datalist.get(i), pb)) {
                int ppdifff = prevPB;
                pbDiff.add(ppdifff);
                prevPB = 0;
            } else {
                prevPB++;
            }
            if(datalist.get(i) < pb) {
                pb = datalist.get(i);
                pbList.add(datalist.get(i));
            }
        }

        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println(pbList.toString());
        System.out.println(pbDiff.toString());
        System.out.println("------------------------------------------");
        System.out.println();
        System.out.println("WeightedAvg " + weightedAvg);
        
        double possiblePB = weightedAvg - (mean - weightedAvg);
        System.out.println("possiblePB " + possiblePB);

        System.out.println(data.length);

        while (possiblePB > 1) {
            double nextValue = getNextValue(data, pb);
            if (nextValue < pb) {
                pb = (int)nextValue;
            }
            // System.out.println("NEXT VALUE: " + nextValue);

            // add bad trys
            double[] badTrys = getBadTrys(data);
            // System.out.println(Arrays.toString(badTrys));

            data = appendArray(data, badTrys);

            // add nextValue
            data = Arrays.copyOf(data, data.length + 1);
            data[data.length - 1] = nextValue;

            weights = getWeights(data, 999);
            mean = new Mean().evaluate(data);
            standardDeviation = new StandardDeviation().evaluate(data);

            weightedAvg = new Mean().evaluate(data, weights);
            possiblePB = weightedAvg - (mean - weightedAvg);
            // System.out.println("New possible PB: " + possiblePB);
        }

        System.out.println(data.length);
    }

    private static double[] appendArray(double[] arr1, double[] arr2) {
        double[] result = new double[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    private static double[] getBadTrys(double[] data) {
        List<Integer> pbList = new ArrayList<>();
        List<Integer> pbDiff = new ArrayList<>();
        int pb = Integer.MAX_VALUE;
        int prevPB = 1;
        for (int i = 0; i < data.length; i++) {
            if (isInPBRange(data[i], pb)) {
                int ppdifff = prevPB;
                pbDiff.add(ppdifff);
                prevPB = 1;
            } else {
                prevPB++;
            }
            if(data[i] < pb) {
                pb = (int) data[i];
                pbList.add((int)data[i]);
            }
        }

        int numBadTrys = pbDiff.get(pbDiff.size()-1);
        int j = pbDiff.size()-1;
        while (numBadTrys < 1) {
            if (j < 0) {
                break;
            }
            numBadTrys = pbDiff.get(j);
            j--;
        }

        double[] badTrys = new double[numBadTrys];

        double sample = Integer.MAX_VALUE;
        double mean = new Mean().evaluate(data);
        double standardDeviation = new StandardDeviation().evaluate(data);
        
        NormalDistribution normal = new NormalDistribution(mean, standardDeviation+999);

        // System.out.println(normal.inverseCumulativeProbability(0.495));//TODO maybe hier regler

        for (int i = 0; i < badTrys.length; i++) {
            sample = Integer.MAX_VALUE;
            while (sample >= 101 || sample < 0 || !(normal.cumulativeProbability(sample) > 0.495)) {
                sample = normal.sample();
            }
            badTrys[i] = sample;
        }
        return badTrys;
    }

    private static boolean isInPBRange(double percentage, int pb) {
        if(percentage < pb || percentage - pb < 4) {// TODO Slider
            return true;
        } else {
            return false;
        }
    }

    private static double getNextValue(double[] data, int pb) {
        double sample = Integer.MAX_VALUE;
        double mean = new Mean().evaluate(data);
        double standardDeviation = new StandardDeviation().evaluate(data);
        NormalDistribution normal = new NormalDistribution(mean, standardDeviation+999);

        while (sample >= 101 || sample < 0 || !isInPBRange(sample, pb)) {
            sample = normal.sample();
        }

        return sample;
    }

    private static double[] getWeights(double[] data, double weightScaling) {
        double mean = new Mean().evaluate(data);
        double standardDeviation = new StandardDeviation().evaluate(data);
        
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
}
