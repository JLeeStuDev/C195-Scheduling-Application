package helper;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for managing appointment times.
 */
public class AppointmentTimeManager {

    /**
     * Converts a local time string to UTC time string.
     * @param localTime The local time string in the format "yyyy-MM-dd'T'HH:mm".
     * @return The UTC time string in the format "yyyy-MM-dd HH:mm:ss".
     */
    public static String convertLocalToUTC(String localTime) {

        // Parse the local time string to LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.parse(localTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        // Get the user's local time zone
        ZoneId localZone = ZoneId.systemDefault();

        // Convert local time to UTC time
        ZonedDateTime utcDateTime = localDateTime.atZone(localZone).withZoneSameInstant(ZoneOffset.UTC);

        // Format the UTC time for storage
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return utcDateTime.format(formatter);

    }

    /**
     * Converts a UTC time string to local time string.
     * @param utcTime The UTC time string in the format "yyyy-MM-dd HH:mm:ss".
     * @return The local time string in the format "yyyy-MM-dd HH:mm".
     */
    public static String convertUTCToLocal(String utcTime) {
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss");
        // Parse UTC time string to LocalDateTime
        LocalDateTime utcDateTime = LocalDateTime.parse(utcTime, dbFormatter);
        // Get local time zone
        ZoneId localZone = ZoneId.systemDefault();
        // Convert UTC to local time
        ZonedDateTime localDateTime = utcDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(localZone);
        // Format for local display
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return localDateTime.format(formatter);
    }

    /**
     * Checks if the adjusted time is within business hours.
     * Business hours are defined as 08:00 - 22:00 ET.
     * @param adjustedTime The adjusted time string in the format "yyyy-MM-dd'T'HH:mm".
     * @return True if the adjusted time is within business hours, otherwise false.
     */
    public static boolean isWithinBusinessHours(String adjustedTime) {
        // Define the start and end time for business hours in ET
        LocalTime startTimeET = LocalTime.of(8, 0);
        LocalTime endTimeET = LocalTime.of(22, 0); // 10pm ET

        // Parse the adjusted time string to LocalDateTime
        LocalDateTime adjustedLocalDateTime = LocalDateTime.parse(adjustedTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        // Convert adjusted local time to ET
        ZoneId localZone = ZoneId.systemDefault();
        ZonedDateTime adjustedET = adjustedLocalDateTime.atZone(localZone).withZoneSameInstant(ZoneId.of("America/New_York"));

        // Extract the time part (local time in ET)
        LocalTime adjustedLocalTimeET = adjustedET.toLocalTime();

        // Check if the adjusted time is within business hours (08:00 - 22:00 ET)
        return !adjustedLocalTimeET.isBefore(startTimeET) && !adjustedLocalTimeET.isAfter(endTimeET);
    }


}
