package betterdeathcounter.controller;

import java.io.IOException;
import java.util.List;

import com.sun.javafx.charts.Legend;

import betterdeathcounter.Main;
import betterdeathcounter.model.Death;
import betterdeathcounter.model.Player;
import betterdeathcounter.model.Settings;
import betterdeathcounter.service.CalculateService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

public class ProgressChartController implements Controller {

    private final Player player;
    private final Settings settings;
    private final CalculateService calculateService = new CalculateService();
    private XYChart.Series<Number, Number> series, linear, exponential, MYPREDICTION;
    private double[] regressionInfos, myPredArray;

    public ProgressChartController(Player player) {
        this.player = player;
        this.settings = player.getSettings();
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void init() {
        final List<Death> deaths = player.getCurrentBoss().getDeaths();
        final int size = deaths.size();
    
        series = new XYChart.Series<>();
        series.setName("Deaths");
        for (int i = 0; i < size; i++) {
            series.getData().add(new XYChart.Data<>(i+1, 
                player.getCurrentBoss().getDeaths().get(i).getPercentage()));
        }

        if (settings.getUseCostumPrediction()) {
            myPredArray = player.getCurrentBoss().getPrediction();
            if(myPredArray.length == 0) return;

            MYPREDICTION = new XYChart.Series<>();

            MYPREDICTION.setName("Dedoische prediction");

            for (int i = 0; i < myPredArray.length-2; i++) {
                MYPREDICTION.getData().add(new XYChart.Data<>(i + size, myPredArray[i]));
            }
        } else {
            regressionInfos = calculateService.getRegressionInfos(player);
            if(regressionInfos.length == 0) return;
    
            final double linearY = regressionInfos[1];
            final double linearZero = regressionInfos[2];
            linear = new XYChart.Series<>();
            linear.setName("Linear Regression");
            linear.getData().add(new XYChart.Data<>(0, linearY));
            linear.getData().add(new XYChart.Data<>(linearZero, 0));
    
            final double expSlope = regressionInfos[3];
            final double expY = regressionInfos[4];
            exponential = new XYChart.Series<>();
            exponential.setName("Exponential Regression");
            
            for (int i = 0; i < player.getCurrentBoss().getDeaths().size()+1; i++) {
                exponential.getData().add(new XYChart.Data<>(i, expY - Math.exp(expSlope*i)));
            }
        }
    }

    @Override
    public Parent render() throws IOException {
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/ProgressChart.fxml"));
        final VBox graphBox = (VBox) parent.lookup("#graphBox");

        String lineChartStyle = !settings.getShowExp() && settings.getShowLinear()
                ? "style/LinechartStyleAltered.css"
                : "style/LinechartStyle.css";
        parent.getStylesheets().add(Main.class.getResource(lineChartStyle).toString());

        int deaths = player.getCurrentBoss().getDeaths().size();

        final NumberAxis xaxis = new NumberAxis(0, (int)deaths + deaths*0.05, 25);  
        final NumberAxis yaxis = new NumberAxis(0,105,10);  
        xaxis.setLabel("Trys");
        yaxis.setLabel("Boss HP left in %");
        if (player.getCurrentBoss().getSecondPhase()) {
            yaxis.setUpperBound(205);
        }

        LineChart<Number, Number> lineChart = new LineChart<>(xaxis, yaxis);
        lineChart.setAnimated(false);
        lineChart.setMaxHeight(player.getCurrentBoss().getSecondPhase() ? 672 : 335);
        lineChart.setMinWidth(1265);
        lineChart.getData().add(series);

        if (settings.getUseCostumPrediction()) {
            if (myPredArray.length == 0) {
                return parent;
            }

            xaxis.setUpperBound((int) ((deaths + myPredArray.length)*1.05));//TODO letzter array punkt
            lineChart.getData().add(MYPREDICTION);

            for (Node n : lineChart.getChildrenUnmodifiable()) {
                if (n instanceof Legend) {
                   Legend legend = (Legend) n;
                    legend.getItems().get(1).getSymbol()
                       .setStyle("-fx-background-color: rgba(255, 0, 0), rgba(130, 0, 0);");
               }
           }
        } else {
            if (regressionInfos.length == 0) {
                return parent;
            }
    
            /*
             * show regression
             */
            if (settings.getShowExp()) {
                lineChart.getData().add(exponential);
            }
            if (settings.getShowLinear()) {
                lineChart.getData().add(linear);
            }
    
            for (Node n : lineChart.getChildrenUnmodifiable()) {
                 if (n instanceof Legend) {
                    Legend legend = (Legend) n;
                    if(settings.getShowLinear()) {
                        legend.getItems().get(1).getSymbol()
                        .setStyle("-fx-background-color: #17617d, #00b7ff;");
                    }
                    if(settings.getShowExp()) {
                        legend.getItems().get(1).getSymbol()
                        .setStyle("-fx-background-color: rgba(255, 0, 0), rgba(130, 0, 0);");
                    }
                }
            }
        }

        graphBox.getChildren().add(lineChart);

        return parent;
    }

    @Override
    public void destroy() {

    }
    
}
