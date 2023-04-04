package betterdeathcounter.controller;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import com.jfoenix.controls.JFXSlider;

import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Player;
import betterdeathcounter.model.Settings;
import betterdeathcounter.service.CalculateService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class BossController implements Controller {

    private final Player player;
    private final Settings settings;
    private final Boss boss;
    private final CalculateService calculateService = new CalculateService();
    private PropertyChangeListener deathListener, garbageListener;
    private double[] regressionInfos;

    public BossController(Player player, Boss boss) {
        this.player = player;
        this.boss = boss;
        this.settings = player.getSettings();
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
        int numDeaths = boss.getDeaths().size();
        allBossDeaths.setText("Deaths: " + numDeaths);

        if (regressionInfos.length != 0) {
            if(calculateService.bossDead(boss)) {
                calculatedTrys.setText("You already killed that Boss!");
            } else {
                calculatedTrys.setText(String.format("Consistent zero: %d\nExponential Last Try: %d", 
                                                   (int)regressionInfos[2], (int)regressionInfos[5]));  
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
            settings.setGarbageFactor(newValue.doubleValue());
        });

        adjustRegression.textProperty().addListener((observable, oldValue, newValue) -> {
            double garbageFactor = settings.getGarbageFactor();
            try {
                garbageFactor = Double.parseDouble(newValue);
                adjustRegression.setStyle("-fx-text-fill: green;");
                settings.setGarbageFactor(garbageFactor);
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
                calculatedTrys.setText(String.format("Consistent zero: %d\nExponential Last Try: %d", 
                                                    (int)regressionInfos[2], (int)regressionInfos[5]));
            } else {
                calculatedTrys.setText("You cant kill that Boss!");
            }
        };
        settings.listeners().addPropertyChangeListener(Settings.PROPERTY_GARBAGE_FACTOR, garbageListener);

        /*
         * Boss Listener
         */
        deathListener = e -> {
            regressionInfos = calculateService.getRegressionInfos(player);
            int numOfDeaths = boss.getDeaths().size();
            allBossDeaths.setText("Deaths: " + numOfDeaths);

            if (regressionInfos.length != 0) {
                if (calculateService.bossDead(boss)) {
                    calculatedTrys.setText("You already killed that Boss!");
                } else {
                    calculatedTrys.setText(String.format("Consistent zero: %d\nExponential Last Try: %d", 
                                                        (int)regressionInfos[2], (int)regressionInfos[5]));
                }
            } else {
                calculatedTrys.setText("You cant kill that Boss!");
            }
        };

        boss.listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);

        return parent;
    }

    @Override
    public void destroy() {
        settings.listeners().removePropertyChangeListener(garbageListener);
        boss.listeners().removePropertyChangeListener(deathListener);
    }
    
}
