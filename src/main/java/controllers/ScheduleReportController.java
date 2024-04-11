package controllers;

import helper.JDBC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Appointment;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * The ScheduleReportController class controls the functionality of the Schedule Report view.
 */
public class ScheduleReportController {

    @FXML
    private Label scheduleReportLabel;

    @FXML
    private ComboBox<String> contactComboBox;

    @FXML
    private TableView<Appointment> scheduleTable;

    @FXML
    private TableColumn<Appointment, Integer> appointmentIdColumn;

    @FXML
    private TableColumn<Appointment, String> titleColumn;

    @FXML
    private TableColumn<Appointment, String> typeColumn;

    @FXML
    private TableColumn<Appointment, String> descriptionColumn;

    @FXML
    private TableColumn<Appointment, String> startDateColumn;

    @FXML
    private TableColumn<Appointment, String> startTimeColumn;

    @FXML
    private TableColumn<Appointment, String> endDateColumn;

    @FXML
    private TableColumn<Appointment, String> endTimeColumn;

    @FXML
    private TableColumn<Appointment, Integer> customerIdColumn;

    @FXML
    private Button btnClose;

    private ResourceBundle resourceBundle;

    /**
     * Initializes the Schedule Report view.
     * Sets up labels, combo boxes, and table columns.
     * Populates the contact combo box.
     * Defines a listener for combo box value changes.
     */
    @FXML
    public void initialize() {
        resourceBundle = ResourceBundle.getBundle("language", Locale.getDefault());
        scheduleReportLabel.setText(resourceBundle.getString("ScheduleReport.label.title"));
        contactComboBox.setPromptText(resourceBundle.getString("ScheduleReport.comboBox.default"));
        btnClose.setText(resourceBundle.getString("ScheduleReport.button.close"));

        configureTableColumns();
        scheduleTable.getItems().clear();
        populateContactComboBox();

        /**
         * Adds a listener to the value property of the contact combo box.
         * Invokes the refreshSchedule method when a new value is selected.
         *
         * @param obs       The observable value being observed.
         * @param oldValue  The previous value of the combo box.
         * @param newValue  The newly selected value of the combo box.
         */
        contactComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                refreshSchedule();
            }
        });
    }

    /**
     * Refreshes the schedule table based on the selected contact.
     * Retrieves appointments for the selected contact and updates the table accordingly.
     */
    @FXML
    private void refreshSchedule() {
        String selectedContact = contactComboBox.getValue();
        if (selectedContact != null) {
            List<Appointment> appointments = retrieveAppointmentsForContact(selectedContact);
            scheduleTable.getItems().setAll(appointments);
        }
        else {
            scheduleTable.getItems().clear();
        }
    }

    /**
     * Closes the Schedule Report window.
     */
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) scheduleReportLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Configures the table columns with their respective properties.
     * Sets column headings based on resource bundle values.
     */
    private void configureTableColumns() {
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));

        appointmentIdColumn.setText(resourceBundle.getString("ScheduleReport.tableColumn.appointmentID"));
        titleColumn.setText(resourceBundle.getString("ScheduleReport.tableColumn.title"));
        typeColumn.setText(resourceBundle.getString("ScheduleReport.tableColumn.type"));
        descriptionColumn.setText(resourceBundle.getString("ScheduleReport.tableColumn.description"));
        startDateColumn.setText(resourceBundle.getString("ScheduleReport.tableColumn.startDate"));
        startTimeColumn.setText(resourceBundle.getString("ScheduleReport.tableColumn.startTime"));
        endDateColumn.setText(resourceBundle.getString("ScheduleReport.tableColumn.endDate"));
        endTimeColumn.setText(resourceBundle.getString("ScheduleReport.tableColumn.endTime"));
        customerIdColumn.setText(resourceBundle.getString("ScheduleReport.tableColumn.customerID"));
    }

    /**
     * Populates the contact combo box with contact names retrieved from the database.
     */
    private void populateContactComboBox() {

        ObservableList<String> contacts = FXCollections.observableArrayList();

        JDBC.openConnection();
        String query = "SELECT Contact_Name from contacts";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String contact = resultSet.getString("Contact_Name");
                contacts.add(contact);
            }

            contactComboBox.setItems(contacts);
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("AddAppointment.error.populateContacts");
            System.out.println(failure);
        }

        finally {
            JDBC.closeConnection();
        }
    }

    /**
     * Retrieves appointments associated with a specific contact from the database.
     * @param contactName The name of the contact
     * @return A list of appointments for the specified contact
     */
    private List<Appointment> retrieveAppointmentsForContact(String contactName) {

        List<Appointment> appointments = new ArrayList<>();

        int getContactID = retrieveContactID(contactName);

        JDBC.openConnection();
        String query = "SELECT * FROM appointments WHERE Contact_ID = ?";
        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getContactID);
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
                int contactID = getContactID;

                //Create the Appointment in the table
                Appointment appointment = new Appointment(appointmentID, title, description, location, type, start, end,
                        createDate, createdBy, lastUpdate, lastUpdatedBy, customerID,userID, contactID);

                appointments.add(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    /**
     * Retrieves the ID of a contact based on the contact name from the database.
     * @param contactName The name of the contact
     * @return The ID of the contact
     */
    private int retrieveContactID(String contactName) {
        int contactID = -1;

        JDBC.openConnection();
        String query = "SELECT Contact_ID FROM contacts WHERE Contact_Name = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, contactName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                contactID = resultSet.getInt("Contact_ID");
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("AddAppointment.error.invalidContactName");
            System.out.println(failure);
            //showErrorAlert("Invalid Contact Name", failure);
        }

        finally {
            JDBC.closeConnection();
        }

        return contactID;
    }

}
