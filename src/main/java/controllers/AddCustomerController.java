package controllers;

import helper.JDBC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Customer;
import models.UserHandler;
import org.w3c.dom.Text;

import javax.xml.transform.Result;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller class for adding a new customer.
 */
public class AddCustomerController {

    @FXML
    private TextField tfACCustomerID;

    @FXML
    private TextField tfACCustomerName;

    @FXML
    private TextField tfACAddress;

    @FXML
    private TextField tfACPostalCode;

    @FXML
    private TextField tfACPhoneNumber;

    @FXML
    private ComboBox<String> cbACFirstLevelDivision;

    @FXML
    private ComboBox<String> cbACCountry;

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblCustomerName;

    @FXML
    private Label lblAddress;

    @FXML
    private Label lblPostalCode;

    @FXML
    private Label lblPhoneNumber;

    @FXML
    private Label lblACFirstLevelDivision;

    @FXML
    private Label lblCountry;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private ResourceBundle resourceBundle;

    /**
     * Initializes the controller.
     *
     * A lambda expression is used here.
     * Listens for changes in the selected item of the country combo box.
     * When the selected country changes, it invokes the countrySelection() method.
     */
    @FXML
    private void initialize() {

        resourceBundle = ResourceBundle.getBundle("language", Locale.getDefault());

        // Set labels and buttons text
        lblTitle.setText(resourceBundle.getString("AddCustomer.label.title"));
        lblCustomerName.setText(resourceBundle.getString("AddCustomer.label.customerName"));
        lblAddress.setText(resourceBundle.getString("AddCustomer.label.address"));
        lblPostalCode.setText(resourceBundle.getString("AddCustomer.label.postalCode"));
        lblPhoneNumber.setText(resourceBundle.getString("AddCustomer.label.phone"));
        lblACFirstLevelDivision.setText(resourceBundle.getString("AddCustomer.label.division"));
        btnSave.setText(resourceBundle.getString("AddCustomer.label.save"));
        btnCancel.setText(resourceBundle.getString("AddCustomer.label.cancel"));

        populateCustomerID();
        populateCountryComboBox();

        /**
         * Listens for changes in the selected item of the country combo box.
         * When the selected country changes, it invokes the countrySelection() method.
         *
         * @param observable    The object being observed for changes.
         * @param oldValue      The previous value of the selected Item.
         * @param newValue      The new value of the selected item.
         */
        cbACCountry.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            countrySelection();
        });

    }

    /**
     * Saves the customer details to the database.
     */
    @FXML
    private void saveCustomer() {

        //Error Check
        if (tfACCustomerName.getText().isEmpty() ||
            tfACAddress.getText().isEmpty() ||
            tfACPostalCode.getText().isEmpty() ||
            tfACPhoneNumber.getText().isEmpty() ||
            cbACFirstLevelDivision.getSelectionModel().isEmpty() ||
            cbACCountry.getSelectionModel().isEmpty()) {

            String failure = resourceBundle.getString("AddCustomer.error.notAllInformation");
            System.out.println(failure);
            showErrorAlert("Missing Data", failure);
            return;
        }

        if (tfACCustomerName.getText().length() > 50) {
            String failure = resourceBundle.getString("AddCustomer.error.nameTooLong");
            System.out.println(failure);
            showErrorAlert("Customer Name Error", failure);
            return;
        }
        if (tfACAddress.getText().length() > 100) {
            String failure = resourceBundle.getString("AddCustomer.error.addressTooLong");
            System.out.println(failure);
            showErrorAlert("Address Error", failure);
            return;
        }
        if (tfACPostalCode.getText().length() > 50) {
            String failure = resourceBundle.getString("AddCustomer.error.postalCodeTooLong");
            System.out.println(failure);
            showErrorAlert("Postal Code Error", failure);
            return;
        }
        if (tfACPhoneNumber.getText().length() > 50) {
            String failure = resourceBundle.getString("AddCustomer.error.phoneNumberTooLong");
            System.out.println(failure);
            showErrorAlert("Phone Number Error", failure);
            return;
        }

        // Saves customer if error handling is fine
        saveCustomerToDatabase();
        closeAddCustomerWindow();
    }

    /**
     * Handles the selection of a country in the country combo box.
     */
    @FXML
    private void countrySelection() {

        String selectedCountry = cbACCountry.getValue();

        if (selectedCountry != null && !selectedCountry.isEmpty()) {

            int countryID = getCountryID(selectedCountry);

            if (countryID != -1) {
                populateDivisionComboBox(countryID);
            }
        }

        else {
            cbACFirstLevelDivision.getItems().clear();
        }

    }

    /**
     * Populates the customerID field with the next available ID.
     */
    private void populateCustomerID() {

        JDBC.openConnection();
        String query = "SELECT MAX(Customer_ID) FROM customers";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            //Get maximum Customer ID
            if (resultSet.next()) {
                int maxCustomerID = resultSet.getInt(1);

                tfACCustomerID.setText(String.valueOf(maxCustomerID + 1));
            }

            JDBC.closeConnection();
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("AddCustomer.error.retrieveCustomerID");
            System.out.println(failure);
            showErrorAlert("Customer ID Error", failure);
        }
    }

    /**
     * Populates the country combo box with country names.
     */
    private void populateCountryComboBox() {

        ObservableList<String> countries = FXCollections.observableArrayList();

        JDBC.openConnection();
        String query = "SELECT Country from countries";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String country = resultSet.getString("Country");
                countries.add(country);
            }

            cbACCountry.setItems(countries);
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("AddCustomer.error.populateCountries");
            System.out.println(failure);
            showErrorAlert("Country Error", failure);
        }

        finally {
            JDBC.closeConnection();
        }
    }

    /**
     * populates the division combo box
     *
     * @param countryID country ID of the division selection
     */
    private void populateDivisionComboBox(int countryID) {

        ObservableList<String> divisions = FXCollections.observableArrayList();

        JDBC.openConnection();
        String query = "SELECT Division FROM first_level_divisions WHERE Country_ID = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, countryID);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String division = resultSet.getString("Division");
                divisions.add(division);
            }

            cbACFirstLevelDivision.setItems(divisions);
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("AddCustomer.error.divisionRetrieval");
            System.out.println(failure);
            showErrorAlert("Division Error", failure);
        }

        finally {
            JDBC.closeConnection();
        }

    }

    /**
     * Saves the customer to the database
     */
    private void saveCustomerToDatabase() {

        String query = "INSERT INTO Customers (Customer_ID, Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        //Get Values for new customer
        int customerID = Integer.parseInt(tfACCustomerID.getText());
        String customerName = tfACCustomerName.getText();
        String address = tfACAddress.getText();
        String postalCode = tfACPostalCode.getText();
        String phone = tfACPhoneNumber.getText();
        LocalDateTime createDate = LocalDateTime.now();
        String createdBy = UserHandler.getLoggedInUser();
        Timestamp lastUpdate = Timestamp.valueOf(LocalDateTime.now());
        String lastUpdatedBy = createdBy;
        int divisionID = cbACFirstLevelDivision.getSelectionModel().getSelectedIndex() + 1;

        if (!duplicateCustomer(customerName, address, postalCode, phone)) {

            try {
                JDBC.openConnection();
                Connection connection = JDBC.connection;
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, customerID);
                statement.setString(2, customerName);
                statement.setString(3, address);
                statement.setString(4, postalCode);
                statement.setString(5, phone);
                statement.setTimestamp(6, Timestamp.valueOf(createDate));
                statement.setString(7, createdBy);
                statement.setTimestamp(8, lastUpdate);
                statement.setString(9, lastUpdatedBy);
                statement.setInt(10, divisionID);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    String success = resourceBundle.getString("AddCustomer.success.customerAdded");
                    System.out.println(success);
                    showSuccessAlert("Customer Added", success);
                } else {
                    String failure = resourceBundle.getString("AddCustomer.error.addCustomer");
                    System.out.println(failure);
                    showErrorAlert("Failed Customer", failure);
                }
            }

            catch (SQLException e) {
                e.printStackTrace();
                String failure = resourceBundle.getString("AddCustomer.error.addCustomerDatabase");
                System.out.println(failure);
                showErrorAlert("Failed Adding Customer", failure);
            }
        }

    }

    /**
     * closes the add Customer Window
     */
    @FXML
    private void closeAddCustomerWindow() {
        Stage stage = (Stage) tfACCustomerID.getScene().getWindow();
        stage.close();
    }

    /**
     * Retreieves the country ID for the given country name.
     *
     * @param countryName   The name of the country
     * @return              The country ID.
     */
    private int getCountryID(String countryName) {

        int countryID = -1;

        JDBC.openConnection();
        String query = "SELECT Country_ID FROM countries WHERE Country = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, countryName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                countryID = resultSet.getInt("Country_ID");
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("AddCustomer.error.retrieveCountryID");
            System.out.println(failure);
            showErrorAlert("Country ID Error", failure);
        }

        finally {
            JDBC.closeConnection();
        }

        return countryID;
    }

    /**
     * Checks if a customer with the given details already exists
     * @param customerName  The name of the customer
     * @param address       The address of the customer.
     * @param postalCode    The postal code of the customer.
     * @param phone         The phone number of the customer.
     * @return              True if a duplicate customer exists, false otherwise.
     */
    private boolean duplicateCustomer(String customerName, String address, String postalCode, String phone) {

        boolean isDuplicate = false;

        JDBC.openConnection();
        String query = "SELECT * FROM Customers WHERE Customer_Name = ? AND Address = ? AND Postal_Code = ? AND Phone = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, customerName);
            statement.setString(2, address);
            statement.setString(3, postalCode);
            statement.setString(4, phone);
            ResultSet resultSet = statement.executeQuery();

            isDuplicate = resultSet.next();
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = "There was an error when trying to check for duplicate entries.";
            System.out.println(failure);
            showErrorAlert("Duplication Error", failure);
        }

        finally {
            JDBC.closeConnection();
        }

        return isDuplicate;
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
     * Failure alert dialog box
     * @param title     Title of the dialog box.
     * @param message   Message in the dialog box.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
