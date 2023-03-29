package betterdeathcounter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Death;
import betterdeathcounter.model.Game;
import betterdeathcounter.model.Player;

public class CalculateService {

    public int getNumOfDeaths(Game game) {
        int numofDeaths = 0;
        for (Boss boss : game.getBosses()) {
            numofDeaths += boss.getDeaths().size();
        }
        return numofDeaths;
    }

    public double[] getRegressionInfos(Player player) {
        Boss boss = player.getCurrentBoss();
        if (boss.getName().equals("Other Monsters or Heights") 
            || boss.getName().equals("Please create a new game")) {

            return new double[] {};
        }
        if (boss.getDeaths().size() < 10) {
            return new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        }
    
        double[] linear = getLinearRegression(boss.getDeaths(), player.getGarbageFactor());
        double[] exp = getExpRegression(boss.getDeaths(), player.getGarbageFactor(), linear);
        int expLastTry = predictLastTryExp(exp);
        int linearLastTry = predictLastTry(linear);
    
        return new double[] {linear[0], linear[1], linearLastTry, exp[0], exp[1], expLastTry};
    }

    public boolean bossDead(Boss boss) {
        List<Death> deaths = boss.getDeaths();
        return !deaths.isEmpty() && deaths.get(deaths.size() - 1).getPercentage() == 0;
    }

    //TODO maybe there is a cool feature to predict it who knows
    // private double[] getSeclinearRegression(List<Integer> deaths, double garbageFactor, double[] linear) {

    //     double linearSlope = linear[0];
    //     double linearY = linear[1];

    //     ArrayList<Integer> x = new ArrayList<>();
    //     ArrayList<Double> y = new ArrayList<>();
    //     int j = 0;
    //     for(int i = 0; i < deaths.size(); i++) {
    //         int index = i;
            
    //         if(deaths.get(i) > index * linearSlope + linearY + garbageFactor) {
    //             continue;
    //         } else {
    //             x.add(index+1);
    //             double d = deaths.get(index);
    //             y.add(d);
    //         }
    //         j++;
    //     }

    //     return calculateRegression(j, x, y, deaths.get(0));
    // }

    private int predictLastTryExp(double[] exp) {
        exp[0] = exp[0]*-1;
        double slope = exp[0];
        double intercept = exp[1];
        int zero = (int) (Math.log(intercept)/slope);

        return zero;
    }

    private int predictLastTry(double[] linear) {
        return (int)((-linear[1])/linear[0]);
    }

    private double[] calculateRegression(int j, List<Integer> x, List<Double> y, int percentage) {
        List<Double> xSquared = x.stream().map(position -> Math.pow(position, 2)).collect(Collectors.toList());

        List<Double> xMultipliedByY = new ArrayList<>();
        for(int i = 0; i < j; i++) {
            xMultipliedByY.add(x.get(i) * y.get(i));
        }

        int xSummed = x.stream().reduce(0, (t, u) -> Integer.sum(t, u));
        double ySummed = y.stream().reduce(0.0, (t, u) -> Double.sum(t, u));

        double sumOfXSquared = xSquared.stream().reduce(0.0, (t, u) -> Double.sum(t, u));
        double sumOfXMultipliedByY = xMultipliedByY.stream().reduce(0.0, (t, u) -> Double.sum(t, u));

        double slopeNominator = j * sumOfXMultipliedByY - ySummed * xSummed;
        double slopeDenominator = j * sumOfXSquared - Math.pow(xSummed, 2);
        double slope = slopeNominator / slopeDenominator;

        double intercept = percentage + (-1) * slope;

        return new double[]{slope, intercept};
    }

    private double[] getExpRegression(List<Death> deaths, double garbageFactor, double[] linear) {
        double linearSlope = linear[0];
        double linearY = linear[1];

        List<Integer> x1 = new ArrayList<>();
        List<Double> y1 = new ArrayList<>();

        int j1 = 0;
        for(int i = 0; i < deaths.size(); i++) {
            int index = i;
            if(deaths.get(i).getPercentage() > (index + 0.0) * linearSlope * garbageFactor + linearY) {
                continue;
            }
            x1.add(index+1);
            double percentage = deaths.get(index).getPercentage();
            y1.add(percentage);
            j1++;
        }

        double[] replace = calculateRegression(j1, x1, y1, deaths.get(0).getPercentage());
        double replaceSlope = replace[0];
        double replaceIntercept = replace[1];

        List<Integer> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();
        int j = 0;

        for(int i = 0; i < deaths.size(); i++) {
            int index = i;
            x.add(index+1);
            if(deaths.get(i).getPercentage() > index * linearSlope * garbageFactor + linearY) {
                y.add(Math.log(replaceSlope*index + replaceIntercept));
            } else {
                double percentage = deaths.get(index).getPercentage();
                if (percentage == 0) {
                    y.add(Math.log(0.00000000000000001));
                } else {
                    y.add(Math.log(percentage));
                }
                j++;
            }
        }

        return calculateRegression(j, x, y, deaths.get(0).getPercentage());
    }

    private double[] getLinearRegression(List<Death> deaths, double garbageFactor) {

        List<Integer> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();

        int j = 0;
        for(int i = 0; i < deaths.size(); i++) {
            x.add(j+1);
            double percentage = deaths.get(i).getPercentage();
            y.add(percentage);
            j++;
        }

        return calculateRegression(j, x, y, deaths.get(0).getPercentage());
    }
    
}
