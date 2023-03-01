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
        for (Boss b : game.getBosses()) {
            numofDeaths += b.getDeaths().size();
        }
        return numofDeaths;
    }

    public double[] getRegressionInfos(Player player) {
        Boss boss = player.getCurrentBoss();
        if(boss.getName().equals("Other Monsters or Heights")
            || boss.getName().equals("Please create a new game")) {
            return new double[] {};
        }
        if (boss.getDeaths().size() < 10) {
            return new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        }
        int linearTry = 0;
        // int secLinearTry = 0;

        double[] linear = getLinearRegression(boss.getDeaths(), player.getGarbageFactor());
        // double[] secLinear = getSeclinearRegression(allDeaths, player.getGarbageFactor(), linear);
        double[] exp = getExpRegression(boss.getDeaths(), player.getGarbageFactor(), linear);
        int expLastTry = predictLastTryExp(exp);

        linearTry = predictLastTry(linear);
        // secLinearTry = predictLastTry(secLinear);
        // exp = predictLastTryExp(exp);
        
        return new double[] {linear[0], linear[1], linearTry, exp[0], exp[1], expLastTry};

        // return new double[] {linear[0], linear[1], linearTry, secLinear[0], secLinear[1], secLinearTry};
    }

    public boolean bossDead(Boss boss) {
        List<Death> fp = boss.getDeaths();
        if(!fp.isEmpty() && fp.get(fp.size()-1).getPercentage() == 0) return true;
        return false;
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

        // System.out.println();
        // System.out.println("------------------------");
        // System.out.println("f(x) = a - e^b*x ");
        // System.out.println("EXP Variable a = " + intercept + " b: " + slope + " Zero: " + zero);
        // System.out.println("------------------------");
        // System.out.println();

        return zero;
    }

    private int predictLastTry(double[] linear) {
        return (int)((-linear[1])/linear[0]);
    }

    private double[] calculateRegression(int j, List<Integer> x, List<Double> y, int percantage) {
        Integer numberOfDataValues = j;

        List<Double> xSquared = x
            .stream()
            .map(position -> Math.pow(position, 2))
            .collect(Collectors.toList());

        ArrayList<Double> xMultipliedByY = new ArrayList<>();
        for(int i = 0; i < numberOfDataValues; i++) {
            xMultipliedByY.add(x.get(i)*y.get(i));
        }

        Integer xSummed = x
            .stream()
            .reduce((prev, next) -> prev + next)
            .get();

        Double ySummed = 0.0;
        
        for(int i = 0; i < numberOfDataValues; i++) {
            ySummed += y.get(i);
        }

        Double sumOfXSquared = xSquared
            .stream()
            .reduce((prev, next) -> prev + next)
            .get();

        Double sumOfXMultipliedByY = 0.0;
        for(int i = 0; i < numberOfDataValues; i++) {
            sumOfXMultipliedByY += xMultipliedByY.get(i);
        }

        double slopeNominator = numberOfDataValues * sumOfXMultipliedByY - ySummed * xSummed;
        double slopeDenominator = numberOfDataValues * sumOfXSquared - Math.pow(xSummed, 2);
        double steigung = slopeNominator / slopeDenominator;

        double intercept = percantage +  (-1)*steigung;

        return new double[]{steigung, intercept};
    }

    private double[] getExpRegression(List<Death> deaths, double garbageFactor, double[] linear) {

        double linearSlope = linear[0];
        double linearY = linear[1];

        ArrayList<Integer> x1 = new ArrayList<>();
        ArrayList<Double> y1 = new ArrayList<>();

        int j1 = 0;
        for(int i = 0; i < deaths.size(); i++) {
            int index = i;
            if(deaths.get(i).getPercentage() > (index + 0.0) * linearSlope * garbageFactor + linearY) {
                continue;
            }
            x1.add(index+1);
            double d = deaths.get(index).getPercentage();
            y1.add(d);
            j1++;
        }

        double[] replace = calculateRegression(j1, x1, y1, deaths.get(0).getPercentage());
        double replaceSlope = replace[0];
        double replaceIntercept = replace[1];

        ArrayList<Integer> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        int j = 0;
        for(int i = 0; i < deaths.size(); i++) {
            int index = i;
            
            x.add(index+1);
            if(deaths.get(i).getPercentage() > index * linearSlope * garbageFactor + linearY) {
                y.add(Math.log(replaceSlope*index + replaceIntercept));
            } else {
                if (deaths.get(index).getPercentage() == 0) {
                    y.add(Math.log(0.00000000000000001));
                } else {
                    y.add(Math.log(deaths.get(index).getPercentage()));
                }
                j++;
            }
        }

        return calculateRegression(j, x, y, deaths.get(0).getPercentage());
    }

    private double[] getLinearRegression(List<Death> deaths, double garbageFactor) {

        ArrayList<Integer> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();

        int j = 0;
        for(int i = 0; i < deaths.size(); i++) {
            x.add(j+1);
            double d = deaths.get(i).getPercentage();
            y.add(d);
            j++;
        }

        return calculateRegression(j, x, y, deaths.get(0).getPercentage());
    }
    
}
