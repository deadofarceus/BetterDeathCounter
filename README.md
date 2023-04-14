# BetterDeathcounter

## Description
BetterDeathcounter is a death counter for Soulslike games that allows you to track your progress and predict your last try using an exponential regression. It also allows you to save and load your game from an Excel sheet and connect to a Google Spreadsheet.

## Usage
1. Create or load a player.
2. Create a game.
3. Create a boss.
4. Start dying.
5. ???
6. Success!

Move the slider under the Death button to select the boss left HP in %. If the boss has 2 phases, you can slide the slider all the way to the left and click "enable second phase" to select boss HP between 0 and 200%.

A progress graph shows your progress (simple explanation). The graph also shows functions trying to predict your last try. With a slider, you can adjust the value used for the exponential regression.

## Warning

The auto detection works only for Elden Ring in fullscreen, 1920x1080 screens and only for the first  Phase of the Boss.
## Dependencies

To run BetterDeathcounter, you will need to have the following dependencies installed:


- JavaFX library and JFeonix:
  - `implementation 'com.jfoenix:jfoenix:9.0.10'`
  - `implementation 'org.openjfx.javafxplugin:version:0.0.13'`
- Fulib Szenarios:
  - `implementation 'org.fulib.fulibGradle:version:0.5.0'`
  - `implementation 'org.fulib:fulibScenarios:version:1.7.0'`
  - `implementation 'org.slf4j:slf4j-simple:version:1.7.36`
- Apache API:
  - `implementation 'org.apache.poi:poi:5.2.0'`
  - `implementation 'org.apache.poi:poi-ooxml:5.2.0'`
- Google Sheets API:
  - `implementation 'com.google.api-client:google-api-client:2.0.0'`
  - `implementation 'com.google.oauth-client:google-oauth-client-jetty:1.34.1'`
  - `implementation 'com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0'`

## Getting Started

To get started with BetterDeathcounter, you will need to have a JDK installed on your machine. Once you have the JDK installed, you can run the provided `.bat` file to launch the program.

### Excel Sheets
To save and load your game from an Excel sheet, you need to ensure that the sheet is in the right format.

### Google Spreadsheet
Connecting to a Google Spreadsheet is more complicated. You need to have Google credentials and some other stuff. For more information, please refer to the [Google Sheets API documentation](https://developers.google.com/sheets/api/quickstart/java).

## Acknowledgements
This project uses the MVC pattern with JavaFX. It is still an early version, and there is a lot of room for improvement.
