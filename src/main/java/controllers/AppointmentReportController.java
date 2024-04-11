package controllers;

import helper.JDBC;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import models.Appointment;

import java.sql.*;
import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller class for generating appointment reports.
 */
public class AppointmentReportController {

    @FXML
    private Label appointmentReportLabel;

    @FXML
    private TextArea reportText;

    private ResourceBundle resourceBundle;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        resourceBundle = ResourceBundle.getBundle("language", Locale.getDefault());

        appointmentReportLabel.setText(resourceBundle.getString("AppointmentReport.Label"));

        reportText.clear();
        generateAppointmentReport();
    }

    /**
     * Closes the window.
     */
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) appointmentReportLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Generates the appointment report.
     */
    private void generateAppointmentReport() {

        List<Appointment> appointments = retrieveAppointmentsFromDatabase();

        Map<String, Map<String, Integer>> appointmentsByTypeAndMonth = processAppointments(appointments);

        String reportContent = formatReportContent(appointmentsByTypeAndMonth);

        reportText.setText(reportContent);
    }

    private List<Appointment> retrieveAppointmentsFromDatabase() {

        List<Appointment> appointments = new ArrayList<>();

        JDBC.openConnection();
        String query = "SELECT * FROM appointments";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int appointmentID = resultSet.getInt("Appointment_ID");
                String title = resultSet.getString("Title");
                String description = resultSet.getString("Description");
                String location = resultSet.getString("Location");
                String type = resultSet.getString("Type");

                //Convert UTC to Local Time Zones
                String startUTC = resultSet.getTimestamp("Start").toLocalDateTime().toString();
                String endUTC = resultSet.getTimestamp("End").toLocalDateTime().toString();
                //String startLocal = AppointmentTimeManager.convertUTCToLocal(startUTC);
                //String endLocal = AppointmentTimeManager.convertUTCToLocal(endUTC);

                LocalDateTime start = LocalDateTime.parse(startUTC, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                LocalDateTime end = LocalDateTime.parse(endUTC, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

                LocalDateTime createDate = resultSet.getTimestamp("Create_Date").toLocalDateTime();
                String createdBy = resultSet.getString("Created_By");
                Timestamp lastUpdate = resultSet.getTimestamp("Last_Update");
                String lastUpdatedBy = resultSet.getString("Last_Updated_By");
                int customerID = resultSet.getInt("Customer_ID");
                int userID = resultSet.getInt("User_ID");
                int contactID = resultSet.getInt("Contact_ID");

                //Create the Appointment in the table
                Appointment appointment = new Appointment(appointmentID, title, description, location, type, start, end,
                        createDate, createdBy, lastUpdate, lastUpdatedBy, customerID,userID, contactID);

                appointments.add(appointment);
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    private Map<String, Map<String, Integer>> processAppointments(List<Appointment> appointments) {
        Map<String, Map<String, Integer>> appointmentsByTypeAndMonth = new HashMap<>();

        // Iterate through appointments to count occurrences by type and month
        for (Appointment appointment : appointments) {
            String type = appointment.getType();
            String month = getMonthName(appointment.getStart().getMonthValue());

            // If the type is not already in the map, add it
            appointmentsByTypeAndMonth.putIfAbsent(type, new HashMap<>());

            // Get the map for the type and update the count for the month
            Map<String, Integer> countByMonth = appointmentsByTypeAndMonth.get(type);
            countByMonth.put(month, countByMonth.getOrDefault(month, 0) + 1);
        }

        return appointmentsByTypeAndMonth;
    }

    private String getMonthName(int monthValue) {
        DateFormatSymbols dfs = new DateFormatSymbols();
        return dfs.getMonths()[monthValue - 1];
    }

    private String formatReportContent(Map<String, Map<String, Integer>> appointmentsByTypeAndMonth) {
        StringBuilder reportContent = new StringBuilder();

        // Iterate through the map and format the report content
        for (Map.Entry<String, Map<String, Integer>> entry : appointmentsByTypeAndMonth.entrySet()) {
            String type = entry.getKey();
            Map<String, Integer> countByMonth = entry.getValue();

            reportContent.append(resourceBundle.getString("report.type")).append(": ").append(type).append("\n");

            for (Map.Entry<String, Integer> monthEntry : countByMonth.entrySet()) {
                String month = monthEntry.getKey();
                int count = monthEntry.getValue();

                reportContent.append("  ").append(month).append(": ").append(count).append("\n");
            }

            reportContent.append("\n");
        }

        return reportContent.toString();
    }

}
