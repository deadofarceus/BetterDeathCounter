package betterdeathcounter.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.fulib.yaml.Yaml;
import org.fulib.yaml.YamlIdMap;

import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Death;
import betterdeathcounter.model.Game;
import betterdeathcounter.model.Player;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;

import com.sun.javafx.charts.Legend;


public class IandOService {
    
    public Player loadPlayer(String name) {

        try {
            final String yaml = Files.readString(Path.of("data/" + name + ".yaml"));
            final YamlIdMap idMap = new YamlIdMap(Player.class.getPackageName());
            return (Player) idMap.decode(yaml);
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public List<Player> loadPlayers() {
        try(final Stream<Path> stream = Files.list(Path.of("data/"))) {
            return stream
                .filter(s -> s.toString().endsWith(".yaml"))
                .sorted()
                .map(p -> loadPlayer(p.toString().substring(5, p.toString().length()-5)))
                .toList();
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public void savePlayer(Player player) {
        final String playerName = player.getName().replaceAll("[\\\\/:*?<>\"]", "_");
        
        player.setName(playerName);
        final String yaml = Yaml.encode(player);

        try {
            Files.writeString(Path.of("data/" + playerName + ".yaml") , yaml);
        } catch (IOException e) { e.printStackTrace(); }
        
    }

    public Game getGameFrom(File whatGame) {
        Game newGame = new Game();
        newGame.setName(whatGame.getName().substring(0, whatGame.getName().length()-5));
        try {
            Workbook workbook = new XSSFWorkbook(whatGame);
            Sheet sheet = workbook.getSheetAt(0);
            rows:for (Row row : sheet) {
                if(row.getCell(0) == null) {break rows;}

                String bossName = row.getCell(0).getStringCellValue();
                
                if(bossName.equals("Boss")) {
                    continue rows;
                }

                int numOfDeaths = (int)row.getCell(1).getNumericCellValue();
                
                ArrayList<Death> alleTode = new ArrayList<>();
                int i = 3;
                boolean sp = false;
                if(row.getCell(i) == null) i = 0;
                for(i = 3; row.getCell(i) != null && row.getCell(i).getNumericCellValue() > 0;i++) {
                    // System.out.println(row.getCell(i).getNumericCellValue());
                    if((int)row.getCell(i).getNumericCellValue() > 100) sp = true;
                    alleTode.add(new Death().setPercentage((int)row.getCell(i).getNumericCellValue()));
                }
                // System.out.println(bossName + " " + numOfDeaths + " Number ProzentToden " + i);
                for(int j = i; j < numOfDeaths + 3; j++) {
                    alleTode.add(new Death());
                }
                newGame.withBosses(new Boss().setName(bossName).withDeaths(alleTode).setSecondPhase(sp));
            }
            workbook.close();
            return newGame;
        } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveGame(Game game, String savePath) {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Bosse");
        sheet.setDefaultRowHeight((short)1200);
        sheet.setColumnWidth(0, 18000);
        sheet.setColumnWidth(1, 9000);
        sheet.setColumnWidth(2, 11000);

        Row header = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle bossStyle = workbook.createCellStyle();
        CellStyle primeStyle = workbook.createCellStyle();
        primeStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        primeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        primeStyle.setAlignment(HorizontalAlignment.CENTER);
        primeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        primeStyle.setWrapText(true);
        bossStyle.setWrapText(true);
        bossStyle.setAlignment(HorizontalAlignment.CENTER);
        bossStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont headerfont = ((XSSFWorkbook) workbook).createFont();
        headerfont.setFontName("Arial");
        headerfont.setFontHeightInPoints((short) 36);
        headerfont.setBold(true);
        headerStyle.setFont(headerfont);

        XSSFFont bossFont = ((XSSFWorkbook) workbook).createFont();
        bossFont.setFontName("Arial");
        bossFont.setFontHeightInPoints((short) 24);
        bossFont.setBold(false);
        bossStyle.setFont(bossFont);
        primeStyle.setFont(bossFont);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Boss");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(1);
        headerCell.setCellValue("Versuche");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(2);
        headerCell.setCellValue("Total");
        headerCell.setCellStyle(headerStyle);

        for(int i = 0; i < game.getBosses().size(); i++) {
            Row bossRow = sheet.createRow(i+1);
            Cell bossCell = bossRow.createCell(0);
            bossCell.setCellValue(game.getBosses().get(i).getName());
            bossCell.setCellStyle(bossStyle);

            bossCell = bossRow.createCell(1);
            bossCell.setCellValue(game.getBosses().get(i).getDeaths().size());
            bossCell.setCellStyle(bossStyle);

            
            int k = 3;
            for(int j = 0; j < game.getBosses().get(i).getDeaths().size(); j++) {
                if(game.getBosses().get(i).getDeaths().get(j).getPercentage() == 0) continue;
                sheet.setColumnWidth(k, 3000);
                bossCell = bossRow.createCell(k);
                bossCell.setCellValue(game.getBosses().get(i).getDeaths().get(j).getPercentage());
                bossCell.setCellStyle(bossStyle);
                k++;
            }
        }
        
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(savePath);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException f) {}
    }

    public void saveGraph(Player player) {

        XYChart.Series<Number, Number> series, linear, exponential;

        CalculateService calculateService = new CalculateService();
        double[] regressionInfos = calculateService.getRegressionInfos(player);
        if(regressionInfos.length == 0) return;

        double linearY = regressionInfos[1];
        double linearZero = regressionInfos[2];
        double expSlope = regressionInfos[3];
        double expY = regressionInfos[4];
        int size = player.getCurrentBoss().getDeaths().size();

        series = new XYChart.Series<>();
        series.setName("Deaths");
        for (int i = 0; i < size; i++) {
            series.getData().add(new XYChart.Data<>(i+1, player.getCurrentBoss().getDeaths().get(i).getPercentage()));
        }


        linear = new XYChart.Series<>();
        linear.setName("Linear Regression");
        linear.getData().add(new XYChart.Data<>(0, linearY));
        linear.getData().add(new XYChart.Data<>(linearZero, 0));

        exponential = new XYChart.Series<>();
        exponential.setName("Exponential Regression");
        // exponential.setName("Second Linear Regression");
        
        for (int i = 0; i < player.getCurrentBoss().getDeaths().size()+1; i++) {
            exponential.getData().add(new XYChart.Data<>(i, expY - Math.exp(expSlope*i)));
        }

        Parent parent = new Parent() {};
        try {
            parent = FXMLLoader.load(Main.class.getResource("view/ProgressChart.fxml"));
        } catch (IOException e) { e.printStackTrace(); }
        if(regressionInfos.length == 0) return;
        parent.getStylesheets().add(Main.class.getResource("style/LinechartOutputStyle.css").toString());

        final VBox graphBox = (VBox) parent.lookup("#graphBox");
        final NumberAxis xaxis = new NumberAxis(0, player.getCurrentBoss().getDeaths().size(), 25);  
        final NumberAxis yaxis = new NumberAxis(0,105,10);  
        xaxis.setLabel("Trys");
        yaxis.setLabel("Boss HP left in %");

        LineChart<Number, Number> lineChart = new LineChart<>(xaxis, yaxis);
        lineChart.setAnimated(false);

        if (player.getCurrentBoss().getSecondPhase()) {
            yaxis.setUpperBound(205);
            lineChart.setMinHeight(720);
        } else {
            lineChart.setMaxHeight(720);
            lineChart.setMinHeight(720);
        }
        lineChart.setMinWidth(1265);
        
        
        lineChart.getData().add(series);
        lineChart.getData().add(exponential);
        lineChart.getData().add(linear);
        
        for (Node n : lineChart.getChildrenUnmodifiable()) {
            if (n instanceof Legend) {
                final Legend legend = (Legend) n;
                // legend.setStyle("-fx-background-color: lightgray;");
                legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: rgba(255, 0, 0), rgba(130, 0, 0);");
            }
        }

        graphBox.getChildren().add(lineChart);
        graphBox.setStyle("-fx-background-color: #2c2c2c;");

        parent.applyCss();
        parent.layout(); 
        new Scene(parent, 1280, 720);
        WritableImage writableImage = new WritableImage(1280, 720);
        parent.snapshot(null, writableImage);

        savetoFile(writableImage, player.getName(), player.getCurrentBoss().getName());
    }

    private void savetoFile(WritableImage image, String player, String boss) {
        File outputFile = new File("graphs/" + player + "/" + boss + ".png");

        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
