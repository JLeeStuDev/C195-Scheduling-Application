package helper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for logging user activities.
 */
public class UserActivityLogger {

    /**
     * Logs the user activity to a file.
     * @param username The username of the user.
     * @param isSuccess True if the login attempt was successful, otherwise false.
     */
    public static void logUserActivity(String username, boolean isSuccess) {

        String fileName = "login_activity.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String formattedDateTime = now.format(formatter);

            String loginStatus = isSuccess ? "successful" : "unsuccessful";

            writer.write("User: " + username + " | Login Attempt: " + loginStatus + " | Date and Time: " + formattedDateTime);
            writer.newLine();

        }
        catch (IOException e) {
            System.err.println("Error writing to login activity file: " + e.getMessage());
        }
    }

}
