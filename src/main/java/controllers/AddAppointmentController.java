package controllers;

import com.mysql.cj.x.protobuf.MysqlxPrepare;
import helper.JDBC;
import helper.AppointmentTimeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.LocalTimeStringConverter;
import javafx.util.converter.TimeStringConverter;
import models.Appointment;
import models.UserHandler;
import org.w3c.dom.Text;

import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

/**
 * Controller class for adding appointments
 */
public class AddAppointmentController {

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblAppointmentID;

    @FXML
    private Label lblAppointmentTitle;

    @FXML
    private Label lblAppointmentDescription;

    @FXML
    private Label lblAppointmentLocation;

    @FXML
    private Label lblAppointmentContact;

    @FXML
    private Label lblAppointmentType;

    @FXML
    private Label lblAppointmentStartDate;

    @FXML
    private Label lblAppointmentEndDate;

    @FXML
    private Label lblAppointmentStartTime;

    @FXML
    private Label lblAppointmentEndTime;

    @FXML
    private TextField tfAAAppointmentID;

    @FXML
    private TextField tfAAAppointmentTitle;

    @FXML
    private TextField tfAAAppointmentDescription;

    @FXML
    private TextField tfAAAppointmentLocation;

    @FXML
    private ComboBox<LocalTime> cbAAAppointmentStartTime;

    @FXML
    private ComboBox<LocalTime> cbAAAppointmentEndTime;

    @FXML
    private TextField tfAAAppointmentType;

    @FXML
    private ComboBox<String> cbAAAppointmentContact;

    @FXML
    private DatePicker dpAAAppointmentStartDate;

    @FXML
    private DatePicker dpAAAppointmentEndDate;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private ResourceBundle resourceBundle;

    private int customerID;

    /**
     * Initializes the controller.
     *
     * Two Lambda expressions are used to set on click actions for the save and cancel buttons.
     */
    @FXML
    private void initialize() {

        resourceBundle = ResourceBundle.getBundle("language", Locale.getDefault());

        //Set Text based on language
        lblTitle.setText(resourceBundle.getString("addAppointment.title"));
        lblAppointmentID.setText(resourceBundle.getString("addAppointment.appointmentID"));
        lblAppointmentTitle.setText(resourceBundle.getString("addAppointment.appointmentTitle"));
        lblAppointmentDescription.setText(resourceBundle.getString("addAppointment.appointmentDescription"));
        lblAppointmentLocation.setText(resourceBundle.getString("addAppointment.appointmentLocation"));
        lblAppointmentContact.setText(resourceBundle.getString("addAppointment.appointmentContact"));
        lblAppointmentType.setText(resourceBundle.getString("addAppointment.appointmentType"));
        lblAppointmentStartDate.setText(resourceBundle.getString("addAppointment.startDate"));
        lblAppointmentEndDate.setText(resourceBundle.getString("addAppointment.endDate"));
        lblAppointmentStartTime.setText(resourceBundle.getString("addAppointment.startTime"));
        lblAppointmentEndTime.setText(resourceBundle.getString("addAppointment.endTime"));
        btnSave.setText(resourceBundle.getString("addAppointment.save"));
        btnCancel.setText(resourceBundle.getString("addAppointment.cancel"));

        /**
         * Sets the action for the save button.
         * When the save button is clicked, it invokes the saveAppointment() method.
         */
        btnSave.setOnAction(event -> saveAppointment());
        /**
         * Sets the action for the cancel button.
         * When the cancel button is clicked, it invokes the closeAddAppointmentWindow() method.
         */
        btnCancel.setOnAction(event -> closeAddAppointmentWindow());

        populateAppointmentID();
        populateContactComboBox();
        populateTimeComboBoxes();

        // populate start and end times
        //List<LocalTime> timeSlots = generateTimeSlots();
        //cbAAAppointmentStartTime.getItems().addAll(timeSlots);
        //cbAAAppointmentStartTime.setConverter(new TimeStringConverter());

        //Listner for the end time comboBox
        //cbAAAppointmentStartTime.valueProperty().addListener((observable, oldValue, newValue) -> {
        //    updateEndTimeComboBox();
        //});
    }

    /**
     * Saves the appointment
     * Validates user input and saves the appointment details to the database.
     */
    @FXML
    private void saveAppointment() {

        //Error Check
        if (tfAAAppointmentTitle.getText().isEmpty() ||
                tfAAAppointmentDescription.getText().isEmpty() ||
                tfAAAppointmentLocation.getText().isEmpty() ||
                cbAAAppointmentContact.getSelectionModel().isEmpty() ||
                tfAAAppointmentType.getText().isEmpty() ||
                dpAAAppointmentStartDate.getValue() == null ||
                dpAAAppointmentEndDate.getValue() == null ||
                cbAAAppointmentStartTime.getValue() == null ||
                cbAAAppointmentEndTime.getValue() == null) {

            String failure = resourceBundle.getString("AddAppointment.error.notAllInformation");
            System.out.println(failure);
            showErrorAlert("Missing Data", failure);
            return;
        }

        LocalDate startDate = dpAAAppointmentStartDate.getValue();
        LocalTime startTime = cbAAAppointmentStartTime.getValue();
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);

        LocalDate endDate = dpAAAppointmentEndDate.getValue();
        LocalTime endTime = cbAAAppointmentEndTime.getValue();
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

        if (!AppointmentTimeManager.isWithinBusinessHours(String.valueOf(startDateTime)) || !AppointmentTimeManager.isWithinBusinessHours(String.valueOf(endDateTime))) {
            String failure = resourceBundle.getString("AddAppointment.error.AppointmentTimesInvalid");
            System.out.println(failure);
            showErrorAlert("Appointment Error", failure);
            return;
        }

        if (tfAAAppointmentTitle.getText().length() > 50) {
            String failure = resourceBundle.getString("AddAppointment.error.titleTooLong");
            System.out.println(failure);
            showErrorAlert("Title Error", failure);
            return;
        }

        if (tfAAAppointmentDescription.getText().length() > 50) {
            String failure = resourceBundle.getString("AddAppointment.error.descriptionTooLong");
            System.out.println(failure);
            showErrorAlert("Description Error", failure);
            return;
        }

        if (tfAAAppointmentLocation.getText().length() > 50) {
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
        closeAddAppointmentWindow();
    }

    /**
     * Closes the add appointment window
     */
    @FXML
    private void closeAddAppointmentWindow() {
        Stage stage = (Stage) tfAAAppointmentID.getScene().getWindow();
        stage.close();
    }

    /**
     * method to populate the combo boxes for time based on local time
     *
     * A lmbda expression is used to update the end time combo box based on the newly selected value in the start box.
     */
    @FXML
    private void populateTimeComboBoxes() {

        //Populate Start Time Combo
        List<LocalTime> timeSlots = generateTimeSlots();
        cbAAAppointmentStartTime.getItems().addAll(timeSlots);
        cbAAAppointmentStartTime.setConverter(new LocalTimeStringConverter());

        //Update end time combo based on start time
        cbAAAppointmentStartTime.valueProperty().addListener((obs, ov, nv) -> {
            updateEndTimeComboBox();
        });

    }

    /**
     * populates appointment ID to be the next highest available integer
     */
    private void populateAppointmentID() {

        JDBC.openConnection();
        String query = "SELECT MAX(Appointment_ID) FROM appointments";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            //Get maximum Customer ID
            if (resultSet.next()) {
                int maxAppointmentID = resultSet.getInt(1);

                tfAAAppointmentID.setText(String.valueOf(maxAppointmentID + 1));
            }

            JDBC.closeConnection();
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("AddAppointment.error.retrieveAppointmentID");
            System.out.println(failure);
            showErrorAlert("Appointment ID Error", failure);
        }
    }

    /**
     * populates the contact combo box
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

            cbAAAppointmentContact.setItems(contacts);
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
     * saves the appointment to the database, assuming all error checks are met
     */
    private void saveAppointmentToDatabase() {

        String query = "INSERT INTO appointments (Appointment_ID, Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        //Retrieve Data
        int appointmentID = Integer.parseInt(tfAAAppointmentID.getText());
        String title = tfAAAppointmentTitle.getText();
        String description = tfAAAppointmentDescription.getText();
        String location = tfAAAppointmentLocation.getText();
        String type = tfAAAppointmentType.getText();
        LocalDateTime startDateTime = LocalDateTime.of(dpAAAppointmentStartDate.getValue(), cbAAAppointmentStartTime.getValue());
        LocalDateTime endDateTime = LocalDateTime.of(dpAAAppointmentEndDate.getValue(), cbAAAppointmentEndTime.getValue());

        //String startTimeUTC = AppointmentTimeManager.convertLocalToUTC(startDateTime.toString());
        //String endTimeUTC = AppointmentTimeManager.convertLocalToUTC(endDateTime.toString());

        LocalDateTime createDate = LocalDateTime.now();
        String createdBy = UserHandler.getLoggedInUser();
        Timestamp lastUpdate = Timestamp.valueOf(LocalDateTime.now());
        String lastUpdatedBy = createdBy;
        int customer_ID = customerID;
        int userID = retrieveLoggedInUserID();
        int contactID = retrieveContactID(cbAAAppointmentContact.getValue());

        try {
            JDBC.openConnection();
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, appointmentID);
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setString(4, location);
            statement.setString(5, type);
            statement.setTimestamp(6,Timestamp.valueOf(startDateTime));
            statement.setTimestamp(7, Timestamp.valueOf(endDateTime));
            statement.setTimestamp(8, Timestamp.valueOf(createDate));
            statement.setString(9, createdBy);
            statement.setTimestamp(10, lastUpdate);
            statement.setString(11, lastUpdatedBy);
            statement.setInt(12, customer_ID);
            statement.setInt(13, userID);
            statement.setInt(14, contactID);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                String success = resourceBundle.getString("AddAppointment.success.AppointmentAdded");
                System.out.println(success);
                showSuccessAlert("Appointment Added", success);
            }

            else {
                String failure = resourceBundle.getString("AddAppointment.error.AppointmentNotAdded");
                System.out.println(failure);
                showErrorAlert("Appointment Data Error", failure);
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("AddAppointment.error.AppointmentNotAdded");
            System.out.println(failure);
            showErrorAlert("Appointment Data Error", failure);
        }
        finally {
            JDBC.closeConnection();
        }
    }

    /**
     * retrieves the logged in user
     * @return userID
     */
    private int retrieveLoggedInUserID() {
        String loggedInUsername = UserHandler.getLoggedInUser();
        int userID = UserHandler.getUserIDByUsername(loggedInUsername);
        return userID;
    }

    /**
     * Generates time slots for the time comboboxes
     * @return List LocalTime timeSlots
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
     * Updates the end time combo box based on the selected start time
     */
    private void updateEndTimeComboBox() {

        //Clear Existing Items
        cbAAAppointmentEndTime.getItems().clear();

        //Get Slected Start time
        LocalTime startTime = cbAAAppointmentStartTime.getValue();

        if (startTime == null) {
            return;
        }

        // restrict time selection based on date selection
        List<LocalTime> timeSlots = generateTimeSlots();
        if (dpAAAppointmentStartDate.getValue() != dpAAAppointmentEndDate.getValue()) {
            for (LocalTime time : timeSlots) {
                if (time.isAfter(startTime)) {
                    cbAAAppointmentEndTime.getItems().add(time);
                }
            }
        }

        else {
            cbAAAppointmentEndTime.getItems().addAll(timeSlots);
        }
        cbAAAppointmentEndTime.setConverter(new LocalTimeStringConverter());
    }

    /**
     * Custom StringConverter for LocalTime
     */
    private class TimeStringConverter extends StringConverter<LocalTime> {
        @Override
        public String toString(LocalTime time) {
            if (time == null) {
                return "";
            }
            return time.toString();
        }

        @Override
        public LocalTime fromString(String string) {
            if (string == null || string.isEmpty()) {
                return null;
            }
            return LocalTime.parse(string);
        }
    }

    /**
     * Retrieves the contact ID given a contactNAme
     * @param contactName   The name of the contact.
     * @return              ContactID that corresponds with the contactName
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
     * Controller for the add Appointment screen
     * @param customerID    customerID of the appointment
     */
    public AddAppointmentController(int customerID) {
        this.customerID = customerID;
    }

    /**
     * Checks if there is any overlapping appointment.
     *
     * @param customerID    The ID of the customer.
     * @param startDateTime The start date and time of the appointment.
     * @param endDateTime   The end date and time of the appointment.
     * @return              True if there is an overlapping appointment, false otherwise.
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
     * Success alert dialog box
     * @param title     Title of the dialog box.
     * @param message   Message in the dialog box.
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Error alert dialog box
     * @param title     Title of the dialog box
     * @param message   Message in the dialog box
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
