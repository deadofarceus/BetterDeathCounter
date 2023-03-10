package betterdeathcounter.controller;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import com.jfoenix.controls.JFXSlider;

import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Player;
import betterdeathcounter.service.CalculateService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class BossController implements Controller {

    private final Player player;
    private final Boss boss;
    private final CalculateService calculateService = new CalculateService();
    private PropertyChangeListener deathListener, garbageListener;
    private double[] regressionInfos;

    public BossController(Player player, Boss boss) {
        this.player = player;
        this.boss = boss;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void init() {
        regressionInfos = calculateService.getRegressionInfos(player);
    }

    @Override
    public Parent render() throws IOException {
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/Boss.fxml"));
        
        /*
         * First display of Text
         */
        final Text bossName = (Text) parent.lookup("#bossName");
        final Text allBossDeaths = (Text) parent.lookup("#allBossDeaths");
        final Text calculatedTrys = (Text) parent.lookup("#calculatedTrys");
        final TextField adjustRegression = (TextField) parent.lookup("#adjustRegression");
        
        bossName.setText(boss.getName());
        allBossDeaths.setText("Deaths: " + boss.getDeaths().size());
        if (regressionInfos.length != 0) {
            if(calculateService.bossDead(boss)) {
                calculatedTrys.setText("You already killed that Boss!");
            } else {
                calculatedTrys.setText("Consistent zero: " + (int)regressionInfos[2] + "\n" + "Exponential Last Try: " + (int)regressionInfos[5]);  
            }
        } else {
            calculatedTrys.setText("You cant kill that Boss!");
        }

        /*
         * Slider
         */
        final JFXSlider progressTrysSlider = (JFXSlider) parent.lookup("#progressTrysSlider");

        progressTrysSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.doubleValue() == -0.01) {
                adjustRegression.setText("OFF");
            } else {
                adjustRegression.setText(Double.toString(newValue.doubleValue()));
            }
            player.setGarbageFactor(newValue.doubleValue());
        });

        adjustRegression.textProperty().addListener((observable, oldValue, newValue) -> {
            double garbageFactor = player.getGarbageFactor();
            try {
                garbageFactor = Double.parseDouble(newValue);
                adjustRegression.setStyle("-fx-text-fill: green;");
                player.setGarbageFactor(garbageFactor);
            } catch (NumberFormatException e) {
                adjustRegression.setStyle("-fx-text-fill: red;");
            }
        });

        /*
         * Player Listener
         */
        garbageListener = e -> {
            regressionInfos = calculateService.getRegressionInfos(player);
            if (regressionInfos.length != 0) {
                calculatedTrys.setText("Consistent zero: " + (int)regressionInfos[2] + "\n" + "Exponential Last Try: " + (int)regressionInfos[5]);  
            } else {
                calculatedTrys.setText("You cant kill that Boss!");
            }
        };

        /*
         * Boss Listener
         */
        deathListener = e -> {
            regressionInfos = calculateService.getRegressionInfos(player);
            allBossDeaths.setText("Deaths: " + boss.getDeaths().size());
            if (regressionInfos.length != 0) {
                if(calculateService.bossDead(boss)) {
                    calculatedTrys.setText("You already killed that Boss!");
                } else {
                    calculatedTrys.setText("Consistent zero: " + (int)regressionInfos[2] + "\n" + "Exponential Last Try: " + (int)regressionInfos[5]);  
                }
            } else {
                calculatedTrys.setText("You cant kill that Boss!");
            }
        };

        boss.listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
        player.listeners().addPropertyChangeListener(Player.PROPERTY_GARBAGE_FACTOR, garbageListener);

        return parent;
    }

    @Override
    public void destroy() {
        player.listeners().removePropertyChangeListener(garbageListener);
        boss.listeners().removePropertyChangeListener(deathListener);
    }
    
}
