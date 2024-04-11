package models;

import helper.JDBC;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an appointment.
 */
public class Appointment {
    private int appointmentID;
    private String title;
    private String description;
    private String location;
    private String type;
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;
    private int customerID;
    private int userID;
    private int contactID;

    //New Split properties
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;

    /**
     * Default constructor.
     */
    public Appointment() {}

    /**
     * Parameterized constructor to initialize appointment properties.
     * @param appointmentID The appointment ID.
     * @param title The title of the appointment.
     * @param description The description of the appointment.
     * @param location The location of the appointment.
     * @param type The type of the appointment.
     * @param start The start time of the appointment.
     * @param end The end time of the appointment.
     * @param createDate The creation date of the appointment.
     * @param createdBy The user who created the appointment.
     * @param lastUpdate The last update timestamp of the appointment.
     * @param lastUpdatedBy The user who last updated the appointment.
     * @param customerID The ID of the customer associated with the appointment.
     * @param userID The ID of the user associated with the appointment.
     * @param contactID The ID of the contact associated with the appointment.
     */
    public Appointment(int appointmentID, String title, String description, String location, String type,
                       LocalDateTime start, LocalDateTime end, LocalDateTime createDate, String createdBy,
                       Timestamp lastUpdate, String lastUpdatedBy, int customerID, int userID, int contactID) {
        this.appointmentID = appointmentID;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.start = start;
        this.end = end;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdatedBy = lastUpdatedBy;
        this.customerID = customerID;
        this.userID = userID;
        this.contactID = contactID;

        this.startDate = start.toLocalDate();
        this.startTime = start.toLocalTime();
        this.endDate = end.toLocalDate();
        this.endTime = end.toLocalTime();
    }

    //Getters
    public int getAppointmentID() { return appointmentID; }
    public String getTitle() { return title;}
    public String getDescription() {return description;}
    public String getLocation() {return location;}
    public String getType() {return type;}
    public LocalDateTime getStart() {return start;}
    public LocalDateTime getEnd() {return end;}
    public LocalDateTime getCreateDate() {return createDate;}
    public String getCreatedBy() {return createdBy;}
    public Timestamp getLastUpdate() {return lastUpdate;}
    public String getLastUpdatedBy() {return lastUpdatedBy;}
    public int getCustomerID() {return customerID;};
    public int getUserID() {return userID;}
    public int getContactID() {return contactID;}
    public LocalDate getStartDate() { return startDate;}
    public LocalTime getStartTime() {return startTime;}
    public LocalDate getEndDate() {return endDate;}
    public LocalTime getEndTime(){return endTime;}

    //Setters
    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setContactID(int contactID) {
        this.contactID = contactID;
    }

    //Special Cases
    /**
     * Retrieves upcoming appointments for a specified user.
     * @param userID The ID of the user for whom to retrieve upcoming appointments.
     * @return A list of upcoming appointments for the specified user.
     */
    public static List<Appointment> getUpcomingAppointmentsForUser(int userID) {
        List<Appointment> upcomingAppointments = new ArrayList<>();
        // SQL query to retrieve upcoming appointments for the user
        String query = "SELECT * FROM appointments WHERE User_ID = ? AND Start >= NOW()";
        // Open DB connection
        JDBC.openConnection();
        try (Connection connection = JDBC.connection;
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userID);
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
                int dbUserID = resultSet.getInt("User_ID");
                int contactID = resultSet.getInt("Contact_ID");

                //Create the Appointment in the table
                Appointment appointment = new Appointment(appointmentID, title, description, location, type, start, end,
                        createDate, createdBy, lastUpdate, lastUpdatedBy, customerID,userID, contactID);

                upcomingAppointments.add(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exception
        } finally {
            // Close DB connection
            JDBC.closeConnection();
        }
        return upcomingAppointments;
    }

    /**
     * Retrieves appointments for a specified customer.
     * @param customerID The ID of the customer for whom to retrieve appointments.
     * @return A list of appointments for the specified customer.
     */
    public static List<Appointment> getAppointmentForCustomer(int customerID) {

        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT * FROM appointments WHERE Customer_ID = ?";

        JDBC.openConnection();

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerID);
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
                int dbCustomerID = resultSet.getInt("Customer_ID");
                int userID = resultSet.getInt("User_ID");
                int contactID = resultSet.getInt("Contact_ID");

                //Create the Appointment in the table
                Appointment appointment = new Appointment(appointmentID, title, description, location, type, start, end,
                        createDate, createdBy, lastUpdate, lastUpdatedBy, customerID, userID, contactID);

                appointments.add(appointment);
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            JDBC.closeConnection();
        }

        return appointments;
    }

}
