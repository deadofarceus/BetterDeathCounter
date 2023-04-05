package betterdeathcounter.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeService {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("[HH:mm:ss]");

    public static void print(String message) {
        System.out.print("[" + LocalTime.now().format(TIME_FORMATTER) + "] ");
        System.out.println(message);
    }
}
