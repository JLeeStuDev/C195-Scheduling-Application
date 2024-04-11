package controllers;

import helper.AppointmentTimeManager;
import helper.JDBC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.converter.LocalTimeStringConverter;
import models.UserHandler;

import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The UpdateAppointmentController class manages the functionality of updating appointments.
 */
public class UpdateAppointmentController {

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblAppointmentID;

    @FXML
    private TextField tfUAAppointmentID;

    @FXML
    private Label lblAppointmentTitle;

    @FXML
    private TextField tfUAAppointmentTitle;

    @FXML
    private Label lblAppointmentDescription;

    @FXML
    private TextField tfUAAppointmentDescription;

    @FXML
    private Label lblAppointmentLocation;

    @FXML
    private TextField tfUAAppointmentLocation;

    @FXML
    private Label lblAppointmentContact;

    @FXML
    private ComboBox<String> cbUAAppointmentContact;

    @FXML
    private Label lblAppointmentType;

    @FXML
    private TextField tfUAAppointmentType;

    @FXML
    private Label lblAppointmentStartDate;

    @FXML
    private DatePicker dpUAAppointmentStartDate;

    @FXML
    private Label lblAppointmentEndDate;

    @FXML
    private DatePicker dpUAAppointmentEndDate;

    @FXML
    private Label lblAppointmentStartTime;

    @FXML
    private ComboBox<LocalTime> cbUAAppointmentStartTime;

    @FXML
    private Label lblAppointmentEndTime;

    @FXML
    private ComboBox<LocalTime> cbUAAppointmentEndTime;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private int customerID;

    private ResourceBundle resourceBundle;

    private static final LocalTime startTime = LocalTime.of(8,0);
    private static final LocalTime endTime = LocalTime.of(22,0);
    private static final int timeInterval = 15;

    /**
     * Initializes the Update Appointment view with existing appointment data.
     * Sets labels, button text, and populates fields with data.
     * Defines actions for save and cancel buttons.
     * Populates contact combo box and time combo boxes.
     *
     * @param appointmentID The ID of the appointment to be updated
     *
     * Two Lambda expressions used here for save and cancel button on click definitions.
     */
    @FXML
    public void initialize(int appointmentID) {
        resourceBundle = ResourceBundle.getBundle("language", Locale.getDefault());
        // Set labels and button text
        lblTitle.setText(resourceBundle.getString("UpdateAppointment.label.title"));
        lblAppointmentID.setText(resourceBundle.getString("UpdateAppointment.label.appointmentID"));
        lblAppointmentTitle.setText(resourceBundle.getString("UpdateAppointment.label.appointmentTitle"));
        lblAppointmentDescription.setText(resourceBundle.getString("UpdateAppointment.label.appointmentDescription"));
        lblAppointmentLocation.setText(resourceBundle.getString("UpdateAppointment.label.appointmentLocation"));
        lblAppointmentContact.setText(resourceBundle.getString("UpdateAppointment.label.appointmentContact"));
        lblAppointmentType.setText(resourceBundle.getString("UpdateAppointment.label.appointmentType"));
        lblAppointmentStartDate.setText(resourceBundle.getString("UpdateAppointment.label.startDate"));
        lblAppointmentEndDate.setText(resourceBundle.getString("UpdateAppointment.label.endDate"));
        lblAppointmentStartTime.setText(resourceBundle.getString("UpdateAppointment.label.startTime"));
        lblAppointmentEndTime.setText(resourceBundle.getString("UpdateAppointment.label.endTime"));
        btnSave.setText(resourceBundle.getString("UpdateAppointment.button.save"));
        btnCancel.setText(resourceBundle.getString("UpdateAppointment.button.cancel"));

        /**
         * Sets an action event handler for the save button.
         * When the button is clicked, the saveAppointment method is invoked.
         *
         * @param event The action event triggered by clicking the button.
         */
        btnSave.setOnAction(event -> saveAppointment());

        /**
         * Sets an action event handler for the cancel button.
         * When the button is clicked, the closeUpdateAppointmentWindow method is invoked.
         *
         * @param event The action event triggered by clicking the button.
         */
        btnCancel.setOnAction(event -> closeUpdateAppointmentWindow());

        // Populate contact combo box
        populateContactComboBox();

        // Populate start time and end time combo boxes
        populateTimeComboBoxes();

        // Populate fields with existing appointment data
        populateFields(appointmentID);
    }

    /**
     * Closes the Update Appointment window.
     */
    @FXML
    private void closeUpdateAppointmentWindow() {
        Stage stage = (Stage) tfUAAppointmentID.getScene().getWindow();
        stage.close();
    }

    /**
     * Populates the start time and end time combo boxes with time slots.
     * Updates the end time combo box based on the selected start time.
     */
    @FXML
    private void populateTimeComboBoxes() {

        //Populate Start Time Combo
        List<LocalTime> timeSlots = generateTimeSlots();
        cbUAAppointmentStartTime.getItems().addAll(timeSlots);
        cbUAAppointmentStartTime.setConverter(new LocalTimeStringConverter());

        /**
         * Adds a listener to the value property of the start time combo box.
         * When the value changes, the updateEndTimeComboBox method is called.
         *
         * @param obs      The observable value being observed.
         * @param ov       The previous value of the start time combo box.
         * @param nv       The newly selected value of the start time combo box.
         */
        cbUAAppointmentStartTime.valueProperty().addListener((obs, ov, nv) -> {
            updateEndTimeComboBox();
        });

    }

    /**
     * Saves the appointment details to the database.
     * Performs error checks and displays error alerts if necessary.
     */
    @FXML
    private void saveAppointment() {

        //Error Check
        if (tfUAAppointmentTitle.getText().isEmpty() ||
                tfUAAppointmentDescription.getText().isEmpty() ||
                tfUAAppointmentLocation.getText().isEmpty() ||
                cbUAAppointmentContact.getSelectionModel().isEmpty() ||
                tfUAAppointmentType.getText().isEmpty() ||
                dpUAAppointmentStartDate.getValue() == null ||
                dpUAAppointmentEndDate.getValue() == null ||
                cbUAAppointmentStartTime.getValue() == null ||
                cbUAAppointmentEndTime.getValue() == null) {

            String failure = resourceBundle.getString("AddAppointment.error.notAllInformation");
            System.out.println(failure);
            showErrorAlert("Missing Data", failure);
            return;
        }

        LocalDate startDate = dpUAAppointmentStartDate.getValue();
        LocalTime startTime = cbUAAppointmentStartTime.getValue();
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);

        LocalDate endDate = dpUAAppointmentEndDate.getValue();
        LocalTime endTime = cbUAAppointmentEndTime.getValue();
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

        if (!AppointmentTimeManager.isWithinBusinessHours(String.valueOf(startDateTime)) || !AppointmentTimeManager.isWithinBusinessHours(String.valueOf(endDateTime))) {
            String failure = resourceBundle.getString("AddAppointment.error.AppointmentTimesInvalid");
            System.out.println(failure);
            showErrorAlert("Appointment Error", failure);
            return;
        }

        if (tfUAAppointmentTitle.getText().length() > 50) {
            String failure = resourceBundle.getString("AddAppointment.error.titleTooLong");
            System.out.println(failure);
            showErrorAlert("Title Error", failure);
            return;
        }

        if (tfUAAppointmentDescription.getText().length() > 50) {
            String failure = resourceBundle.getString("AddAppointment.error.descriptionTooLong");
            System.out.println(failure);
            showErrorAlert("Description Error", failure);
            return;
        }

        if (tfUAAppointmentLocation.getText().length() > 50) {
            String failure = resourceBundle.getString("AddAppointment.error.locationTooLong");
            System.out.println(failure);
            showErrorAlert("Location Error", failure);
            return;
        }

        // Check if end date is before start date (if necessary)
        if (endDate.isBefore(startDate)) {
            String failure = resourceBundle.getString("AddAppointment.error.endDateBeforeStartDate");
            System.out.println(failure);
            showErrorAlert("End Date Error", failure);
            return;
        }

        // Check if end time is before start time
        if (endDate.isEqual(startDate) && endTime.isBefore(startTime)) {
            String failure = resourceBundle.getString("AddAppointment.error.endTimeBeforeStartTime");
            System.out.println(failure);
            showErrorAlert("End Time Error", failure);
            return;
        }

        if (isOverlappingAppointment(customerID, startDateTime, endDateTime)) {
            String failure = resourceBundle.getString("AddAppointment.error.OverlappingAppointment");
            System.out.println(failure);
            showErrorAlert("OverlappingAppointment", failure);
            return;
        }

        // Save the appointment if all the error handling is alright
        saveAppointmentToDatabase();
        closeUpdateAppointmentWindow();
    }

    /**
     * Populates the contact combo box with contacts retrieved from the database.
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

            cbUAAppointmentContact.setItems(contacts);
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("AddAppointment.error.populateContacts");
            System.out.println(failure);
            showErrorAlert("Contact Error", failure);
        }

        finally {
            JDBC.closeConnection();
        }
    }

    /**
     * Saves the appointment details to the database.
     */
    private void saveAppointmentToDatabase() {
        String query = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, Type = ?, Start = ?, End = ?, Last_Update = ?, Last_Updated_By = ?, Contact_ID = ? WHERE Appointment_ID = ?";

        //Retrieve Data
        int appointmentID = Integer.parseInt(tfUAAppointmentID.getText());
        String title = tfUAAppointmentTitle.getText();
        String description = tfUAAppointmentDescription.getText();
        String location = tfUAAppointmentLocation.getText();
        String type = tfUAAppointmentType.getText();
        LocalDateTime startDateTime = LocalDateTime.of(dpUAAppointmentStartDate.getValue(), cbUAAppointmentStartTime.getValue());
        LocalDateTime endDateTime = LocalDateTime.of(dpUAAppointmentEndDate.getValue(), cbUAAppointmentEndTime.getValue());
        Timestamp lastUpdate = Timestamp.valueOf(LocalDateTime.now());
        String lastUpdatedBy = UserHandler.getLoggedInUser();
        int contactID = retrieveContactID(cbUAAppointmentContact.getValue());

        try {
            JDBC.openConnection();
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setString(3, location);
            statement.setString(4, type);
            statement.setTimestamp(5, Timestamp.valueOf(startDateTime));
            statement.setTimestamp(6, Timestamp.valueOf(endDateTime));
            statement.setTimestamp(7, lastUpdate);
            statement.setString(8, lastUpdatedBy);
            statement.setInt(9, contactID);
            statement.setInt(10, appointmentID);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                String success = resourceBundle.getString("UpdateAppointment.success.AppointmentUpdated");
                System.out.println(success);
                showSuccessAlert("Appointment Updated", success);
            } else {
                String failure = resourceBundle.getString("AddAppointment.error.AppointmentNotAdded");
                System.out.println(failure);
                showErrorAlert("Appointment Data Error", failure);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("AddAppointment.error.AppointmentNotAdded");
            System.out.println(failure);
            showErrorAlert("Appointment Data Error", failure);
        } finally {
            JDBC.closeConnection();
        }
    }

    /**
     * Generates time slots at 15-minute intervals from the minimum time of the day to the maximum time of the day minus 15 minutes.
     * @return A list of LocalTime objects representing the time slots.
     */
    private List<LocalTime> generateTimeSlots() {
        List<LocalTime> timeSlots = new ArrayList<>();
        LocalTime time = LocalTime.MIN; // Initialize to minimum time of the day
        LocalTime maxTime = LocalTime.MAX.minusMinutes(15); // Maximum time of the day minus 15 minutes
        while (time.isBefore(maxTime)) { // Check if time is before maximum time of the day
            timeSlots.add(time);
            time = time.plusMinutes(15);
        }
        return timeSlots;
    }

    /**
     * Updates the end time combo box based on the selected start time.
     */
    private void updateEndTimeComboBox() {

        //Clear Existing Items
        cbUAAppointmentEndTime.getItems().clear();

        //Get Slected Start time
        LocalTime startTime = cbUAAppointmentStartTime.getValue();

        if (startTime == null) {
            return;
        }

        // restrict time selection based on date selection
        List<LocalTime> timeSlots = generateTimeSlots();
        if (dpUAAppointmentStartDate.getValue() != dpUAAppointmentEndDate.getValue()) {
            for (LocalTime time : timeSlots) {
                if (time.isAfter(startTime)) {
                    cbUAAppointmentEndTime.getItems().add(time);
                }
            }
        }

        else {
            cbUAAppointmentEndTime.getItems().addAll(timeSlots);
        }
        cbUAAppointmentEndTime.setConverter(new LocalTimeStringConverter());
    }

    /**
     * Populates fields with appointment data retrieved from the database.
     * @param appointmentID The ID of the appointment to populate fields for.
     */
    public void populateFields(int appointmentID) {

        JDBC.openConnection();
        String query = "SELECT * FROM appointments WHERE Appointment_ID = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, appointmentID);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                tfUAAppointmentID.setText(String.valueOf(appointmentID));
                tfUAAppointmentTitle.setText(resultSet.getString("Title"));
                tfUAAppointmentDescription.setText(resultSet.getString("Description"));
                tfUAAppointmentLocation.setText(resultSet.getString("Location"));
                tfUAAppointmentType.setText(resultSet.getString("Type"));

                // Convert UTC to local time for start and end dates
                String startTimeUTC = resultSet.getString("Start");
                String endTimeUTC = resultSet.getString("End");
                String startTimeLocal = AppointmentTimeManager.convertUTCToLocal(startTimeUTC);
                String endTimeLocal = AppointmentTimeManager.convertUTCToLocal(endTimeUTC);

                dpUAAppointmentStartDate.setValue(LocalDate.parse(startTimeLocal.substring(0, 10)));
                dpUAAppointmentEndDate.setValue(LocalDate.parse(endTimeLocal.substring(0, 10)));
                cbUAAppointmentStartTime.setValue(LocalTime.parse(startTimeLocal.substring(11)));
                cbUAAppointmentEndTime.setValue(LocalTime.parse(endTimeLocal.substring(11)));

                int contactID = resultSet.getInt("Contact_ID");
                String contactName = getContactName(contactID);
                cbUAAppointmentContact.setValue(contactName);

                customerID = resultSet.getInt("Customer_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("UpdateAppointment.error.getAppointment");
            System.out.println(failure);
            showErrorAlert("Appointment Data Error", failure);
        } finally {
            JDBC.closeConnection();
        }

    }

    /**
     * Retrieves the name of the contact associated with the given contact ID.
     * @param contactID The ID of the contact.
     * @return The name of the contact.
     */
    private String getContactName(int contactID) {

        String contactName = "";
        JDBC.openConnection();
        String query = "SELECT Contact_Name FROM contacts WHERE Contact_ID = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, contactID);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                contactName = resultSet.getString("Contact_Name");
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("UpdateAppointment.error.getContactName");
            System.out.println(failure);
            showErrorAlert("Contact Name Error", failure);
        }

        finally {
            JDBC.closeConnection();
        }
        return contactName;
    }

    /**
     * Retrieves the ID of the contact associated with the given contact name.
     * @param contactName The name of the contact.
     * @return The ID of the contact, or -1 if not found.
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
            showErrorAlert("Invalid Contact Name", failure);
        }

        finally {
            JDBC.closeConnection();
        }

        return contactID;
    }

    /**
     * Generates time options between the specified start and end times at the given interval.
     * @param startTime The start time.
     * @param endTime The end time.
     * @param intervalMinutes The interval in minutes between time options.
     * @return A list of time options as strings.
     */
    private List<String> generateTimeOptions(LocalTime startTime, LocalTime endTime, int intervalMinutes) {

        List<String> timeOptions = new ArrayList<>();

        while (startTime.isBefore(endTime) || startTime.equals(endTime)) {
            timeOptions.add(startTime.toString());
            startTime = startTime.plusMinutes(intervalMinutes);
        }
        return timeOptions;
    }

    /**
     * Checks if there is any overlapping appointment for the given customer ID within the specified time frame.
     * @param customerID The ID of the customer.
     * @param startDateTime The start date and time of the appointment.
     * @param endDateTime The end date and time of the appointment.
     * @return True if there is an overlapping appointment, false otherwise.
     */
    private boolean isOverlappingAppointment(int customerID, LocalDateTime startDateTime, LocalDateTime endDateTime) {

        JDBC.openConnection();
        String query = "SELECT COUNT(*) FROM appointments WHERE Customer_ID = ? " +
                "AND ((Start BETWEEN ? AND ?) OR (End BETWEEN ? AND ?) OR " +
                "(? BETWEEN Start AND END) OR (? BETWEEN Start AND End))";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerID);
            statement.setTimestamp(2, Timestamp.valueOf(startDateTime));
            statement.setTimestamp(3, Timestamp.valueOf(endDateTime));
            statement.setTimestamp(4, Timestamp.valueOf(startDateTime));
            statement.setTimestamp(5, Timestamp.valueOf(endDateTime));
            statement.setTimestamp(6, Timestamp.valueOf(startDateTime));
            statement.setTimestamp(7, Timestamp.valueOf(endDateTime));

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            JDBC.closeConnection();
        }
        return false;
    }

    /**
     * Displays an error alert dialog with the specified title and message.
     * @param title The title of the alert.
     * @param message The message content of the alert.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a success alert dialog with the specified title and message.
     * @param title The title of the alert.
     * @param message The message content of the alert.
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
