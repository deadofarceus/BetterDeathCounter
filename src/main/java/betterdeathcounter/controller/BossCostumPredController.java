package betterdeathcounter.controller;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import com.jfoenix.controls.JFXSlider;

import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Player;
import betterdeathcounter.model.Settings;
import betterdeathcounter.service.CalculateService;
import betterdeathcounter.service.MYPredictionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class BossCostumPredController implements Controller {

    private final Settings settings;
    private final Boss boss;
    private final MYPredictionService myPredictionService = new MYPredictionService();
    private final CalculateService calculateService = new CalculateService();
    private PropertyChangeListener deathListener, predInfoListener;
    private double[] myPredInfos;

    public BossCostumPredController(Player player, Boss boss) {
        this.boss = boss;
        this.settings = player.getSettings();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void init() {
        double[] pred = myPredictionService.getMYPredictions(boss.getDeaths(), settings);
        boss.setPrediction(pred);
        if (pred.length == 0) {
            return;
        }
        myPredInfos = myPredictionService.getPredInfos(boss.getDeaths(), settings, pred[pred.length-1]);
    }

    @Override
    public Parent render() throws IOException {
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/BossCostumPred.fxml"));
        
        /*
         * First display of Text
         */
        final Text bossName = (Text) parent.lookup("#bossName");
        final Text allBossDeaths = (Text) parent.lookup("#allBossDeaths");
        final Text predInfoText = (Text) parent.lookup("#predInfoText");
        final TextField adjustCumulativeProbability = (TextField) parent.lookup("#adjustCumulativeProbability");
        final Text badTrysText = (Text) parent.lookup("#badTrysText");

        bossName.setText(boss.getName());
        int numDeaths = boss.getDeaths().size();
        allBossDeaths.setText("Deaths: " + numDeaths);

        adjustCumulativeProbability.setText(""+settings.getCumulativeProbabilityScaling());
        
        if (settings.getNumBadTrys() == 0) {
            badTrysText.setStyle("-fx-text-fill: red;");
        } else {
            badTrysText.setStyle("-fx-text-fill: black;");
        }
        badTrysText.setText("Bad Trys: " + settings.getNumBadTrys());

        if (boss.getPrediction().length != 0) {
            if(calculateService.bossDead(boss)) {
                predInfoText.setText("You already killed that Boss!");
            } else {
                predInfoText.setText(String.format("Next PB: %d\nCurrent Mean: %d", 
                                                   (int)myPredInfos[0], (int)myPredInfos[1]));  
            }
        } else {
            if (numDeaths < 11) {
                predInfoText.setText("Get Infos after 10 Trys!");
            } else {
                predInfoText.setText("You cant kill that Boss!");
            }
        }

        /*
         * Slider
         */
        final JFXSlider cumulativeProbabilitySlider = (JFXSlider) parent.lookup("#cumulativeProbabilitySlider");
        final JFXSlider numOfBadTrysSlider = (JFXSlider) parent.lookup("#numOfBadTrysSlider");

        cumulativeProbabilitySlider.setValue(settings.getCumulativeProbabilityScaling());
        numOfBadTrysSlider.setValue(settings.getNumBadTrys());

        numOfBadTrysSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = newValue.intValue();
            if (value == 0) {
                badTrysText.setStyle("-fx-text-fill: red;");
            } else {
                badTrysText.setStyle("-fx-text-fill: black;");
            }
            badTrysText.setText("Bad Trys: " + value);
            settings.setNumBadTrys(value);
        });

        cumulativeProbabilitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            adjustCumulativeProbability.setText(Double.toString(newValue.doubleValue()));
            settings.setCumulativeProbabilityScaling(newValue.doubleValue());
        });

        adjustCumulativeProbability.textProperty().addListener((observable, oldValue, newValue) -> {
            double cumulativeProbabilityScaling = settings.getCumulativeProbabilityScaling();
            try {
                cumulativeProbabilityScaling = Double.parseDouble(newValue);
                adjustCumulativeProbability.setStyle("-fx-text-fill: green;");
                settings.setCumulativeProbabilityScaling(cumulativeProbabilityScaling);
            } catch (NumberFormatException e) {
                adjustCumulativeProbability.setStyle("-fx-text-fill: red;");
            }
        });

        /*
         * Prediction Listener
         */
        predInfoListener = e -> {
            double[] pred = myPredictionService.getMYPredictions(boss.getDeaths(), settings);
            boss.setPrediction(pred);
            if (pred.length == 0) {
                return;
            }
            myPredInfos = myPredictionService.getPredInfos(boss.getDeaths(), settings, pred[pred.length-1]);
            
            if (myPredInfos.length != 0) {
                predInfoText.setText(String.format("Next PB: %d\nCurrent Mean: %d", 
                                                   (int)myPredInfos[0], (int)myPredInfos[1]));
            } else {
                if (numDeaths < 11) {
                    predInfoText.setText("Get Infos after 10 Trys!");
                } else {
                    predInfoText.setText("You cant kill that Boss!");
                }
            }
        };
        settings.listeners().addPropertyChangeListener(Settings.PROPERTY_CUMULATIVE_PROBABILITY_SCALING, predInfoListener);
        settings.listeners().addPropertyChangeListener(Settings.PROPERTY_NUM_BAD_TRYS, predInfoListener);

        /*
         * Boss Listener
         */
        deathListener = e -> {
            double[] pred = myPredictionService.getMYPredictions(boss.getDeaths(), settings);
            boss.setPrediction(pred);
            if (pred.length == 0) {
                return;
            }
            myPredInfos = myPredictionService.getPredInfos(boss.getDeaths(), settings, pred[pred.length-1]);
            
            int numOfDeaths = boss.getDeaths().size();
            allBossDeaths.setText("Deaths: " + numOfDeaths);

            if (myPredInfos.length != 0) {
                if (calculateService.bossDead(boss)) {
                    predInfoText.setText("You already killed that Boss!");
                } else {
                    predInfoText.setText(String.format("Next PB: %d\nCurrent Mean: %d", 
                                                   (int)myPredInfos[0], (int)myPredInfos[1]));
                }
            } else {
                if (numOfDeaths < 11) {
                    predInfoText.setText("Get Infos after 10 Trys!");
                } else {
                    predInfoText.setText("You cant kill that Boss!");
                }
            }
        };

        boss.listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);

        return parent;
    }

    @Override
    public void destroy() {
        boss.listeners().removePropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);

        settings.listeners().removePropertyChangeListener(Settings.PROPERTY_CUMULATIVE_PROBABILITY_SCALING, predInfoListener);
        settings.listeners().removePropertyChangeListener(Settings.PROPERTY_NUM_BAD_TRYS, predInfoListener);
    }
    
}
