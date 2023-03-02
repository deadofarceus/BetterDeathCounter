package betterdeathcounter.controller;

import java.io.IOException;

import betterdeathcounter.Main;
import betterdeathcounter.model.Player;
import betterdeathcounter.service.CalculateService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

import com.sun.javafx.charts.Legend;

public class ProgressChartController implements Controller {

    private final Player player;
    private final CalculateService calculateService = new CalculateService();
    private XYChart.Series<Number, Number> series, linear, exponential;
    private double[] regressionInfos;

    public ProgressChartController(Player player) {
        this.player = player;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void init() {
        regressionInfos = calculateService.getRegressionInfos(player);
        if(regressionInfos.length == 0) return;
        double linearY = regressionInfos[1];
        double linearZero = regressionInfos[2];
        double expSlope = regressionInfos[3];
        double expY = regressionInfos[4];
        int size = player.getCurrentBoss().getDeaths().size();


        series = new XYChart.Series<>();
        series.setName("Deaths");
        for (int i = 0; i < size; i++) {
            series.getData().add(new XYChart.Data<>(i+1, 
                player.getCurrentBoss().getDeaths().get(i).getPercentage()));
        }


        linear = new XYChart.Series<>();
        linear.setName("Linear Regression");
        linear.getData().add(new XYChart.Data<>(0, linearY));
        linear.getData().add(new XYChart.Data<>(linearZero, 0));

        exponential = new XYChart.Series<>();
        exponential.setName("Exponential Regression");
        
        for (int i = 0; i < player.getCurrentBoss().getDeaths().size()+1; i++) {
            exponential.getData().add(new XYChart.Data<>(i, expY - Math.exp(expSlope*i)));
        }
    }

    @Override
    public Parent render() throws IOException {
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/ProgressChart.fxml"));
        if(regressionInfos.length == 0) return parent;
        parent.getStylesheets().add(Main.class.getResource("style/LinechartStyle.css").toString());

        final VBox graphBox = (VBox) parent.lookup("#graphBox");
        
        final NumberAxis xaxis = new NumberAxis(0, player.getCurrentBoss().getDeaths().size(), 25);  
        final NumberAxis yaxis = new NumberAxis(0,105,10);  
        xaxis.setLabel("Trys");
        yaxis.setLabel("Boss HP left in %");

        LineChart<Number, Number> lineChart = new LineChart<>(xaxis, yaxis);
        lineChart.setAnimated(false);

        if (player.getCurrentBoss().getSecondPhase()) {
            yaxis.setUpperBound(205);
            lineChart.setMinHeight(672);
        } else {
            lineChart.setMaxHeight(348);
        }
        lineChart.setMinWidth(1265);
        lineChart.getData().add(series);


        /*
         * show regression
         */
        if (player.getShowExp()) {
            lineChart.getData().add(exponential);
        } else {
            lineChart.getData().add(new XYChart.Series<Number, Number>());
        }
        if (player.getShowLinear()) {
            lineChart.getData().add(linear);
        } else {
            lineChart.getData().add(new XYChart.Series<Number, Number>());
        }
        if (player.getShowExp()) {
            for (Node n : lineChart.getChildrenUnmodifiable()) {
                if (n instanceof Legend) {
                    final Legend legend = (Legend) n;
                    legend.getItems().get(1).getSymbol()
                        .setStyle("-fx-background-color: rgba(255, 0, 0), rgba(130, 0, 0);");
                }
            }
        }

        lineChart.applyCss();
        lineChart.layout(); 

        graphBox.getChildren().add(lineChart);

        return parent;
    }

    @Override
    public void destroy() {

    }
    
}
