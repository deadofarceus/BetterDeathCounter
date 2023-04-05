package betterdeathcounter.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Game;

public class APIService {

    private CalculateService calculateService = new CalculateService();
    
    private static final String APPLICATION_NAME = "BetterDeathcounter";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES =
        Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "google/credentials.json";

    public void sendData(Game game, Boss boss) throws GeneralSecurityException, IOException {
        final String spreadsheetId = game.getSpreadsheetId();
        final String range = "D2:D4";

        Sheets sheetsService = getSheetService();

        if (sheetsService == null) {
            TimeService.print("No Data sent!");
            System.out.println();
            return;
        }

        ValueRange requestBody = new ValueRange().setValues(
                    Arrays.asList(
                        Arrays.asList(calculateService.getNumOfDeaths(game)),
                        Arrays.asList(
                            "Abboniert mit Prime" + "\n" + 
                            "ihr Halunken"),
                        Arrays.asList(Integer.toString(boss.getDeaths().size()))
                    )
                );

        UpdateValuesResponse response = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, requestBody)
                    .setValueInputOption("RAW")
                    .execute();

        TimeService.print(response.getUpdatedCells() + " cells succesfull updated");
        System.out.println();
    }
    
    private Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);;
        if (in.available() == 0) {
            TimeService.print("Resource not found: " + CREDENTIALS_FILE_PATH);
            TimeService.print("Please add your google Cresentials here: " +
                "resources/credentials.json");
            TimeService.print("If you dont now how look here: ");
            TimeService.print("https://developers.google.com/sheets/api/quickstart/java");
            in.close();
            return null;
        }

        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver())
            .authorize("deadofarceus");

        return credential;
    }

    private Sheets getSheetService() throws GeneralSecurityException, IOException {
        Credential credential = authorize();
        if (credential == null) {
            return null;
        }
        return new Sheets.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), 
            GsonFactory.getDefaultInstance(), 
            credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

}
