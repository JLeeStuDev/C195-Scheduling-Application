package controllers;

import com.mysql.cj.x.protobuf.MysqlxPrepare;
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
 * Controller class for updating customer information.
 */
public class UpdateCustomerController {

    @FXML
    private TextField tfUCCustomerID;

    @FXML
    private TextField tfUCCustomerName;

    @FXML
    private TextField tfUCAddress;

    @FXML
    private TextField tfUCPostalCode;

    @FXML
    private TextField tfUCPhoneNumber;

    @FXML
    private ComboBox<String> cbUCFirstLevelDivision;

    @FXML
    private ComboBox<String> cbUCCountry;

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
    private Label lblUCFirstLevelDivision;

    @FXML
    private Label lblCountry;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private ResourceBundle resourceBundle;

    /**
     * Initializes the update customer form with data.
     *
     * @param customerID The ID of the customer to update.
     *
     * Lambda expression used here:
     * Adds a listener to the selectedItemProperty of the country selection combo box.
     * When the selected item changes, the countrySelection method is called.
     */
    @FXML
    public void initialize(int customerID) {

        resourceBundle = ResourceBundle.getBundle("language", Locale.getDefault());

        // Set labels and button text
        lblTitle.setText(resourceBundle.getString("UpdateCustomer.label.title"));
        lblCustomerName.setText(resourceBundle.getString("UpdateCustomer.label.customerName"));
        lblAddress.setText(resourceBundle.getString("UpdateCustomer.label.address"));
        lblPostalCode.setText(resourceBundle.getString("UpdateCustomer.label.postalCode"));
        lblPhoneNumber.setText(resourceBundle.getString("UpdateCustomer.label.phone"));
        lblUCFirstLevelDivision.setText(resourceBundle.getString("UpdateCustomer.label.division"));
        btnSave.setText(resourceBundle.getString("UpdateCustomer.label.save"));
        btnCancel.setText(resourceBundle.getString("UpdateCustomer.label.cancel"));

        populateCountryComboBox();
        populateFields(customerID);

        /**
         * Adds a listener to the selectedItemProperty of the country selection combo box.
         * When the selected item changes, the countrySelection method is called.
         *
         * @param observable The property being observed for changes.
         * @param oldValue   The previous selected item in the combo box.
         * @param newValue   The newly selected item in the combo box.
         */
        cbUCCountry.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            countrySelection();
        });
    }

    /**
     * Handles the selection of a country in the country combo box.
     */
    @FXML
    private void countrySelection() {

        String selectedCountry = cbUCCountry.getValue();

        if (selectedCountry != null && !selectedCountry.isEmpty()) {

            int countryID = getCountryID(selectedCountry);

            if (countryID != -1) {
                populateDivisionComboBox(countryID);
            }
        }

        else {
            cbUCFirstLevelDivision.getItems().clear();
        }

    }

    /**
     * Updates the customer information in the database.
     */
    @FXML
    private void updateCustomer() {

        //Error Check
        if (tfUCCustomerName.getText().isEmpty() ||
                tfUCAddress.getText().isEmpty() ||
                tfUCPostalCode.getText().isEmpty() ||
                tfUCPhoneNumber.getText().isEmpty() ||
                cbUCFirstLevelDivision.getSelectionModel().isEmpty() ||
                cbUCCountry.getSelectionModel().isEmpty()) {

            String failure = resourceBundle.getString("AddCustomer.error.notAllInformation");
            System.out.println(failure);
            showErrorAlert("Missing Data", failure);
            return;
        }

        if (tfUCCustomerName.getText().length() > 50) {
            String failure = resourceBundle.getString("AddCustomer.error.nameTooLong");
            System.out.println(failure);
            showErrorAlert("Customer Name Error", failure);
            return;
        }
        if (tfUCAddress.getText().length() > 100) {
            String failure = resourceBundle.getString("AddCustomer.error.addressTooLong");
            System.out.println(failure);
            showErrorAlert("Address Error", failure);
            return;
        }
        if (tfUCPostalCode.getText().length() > 50) {
            String failure = resourceBundle.getString("AddCustomer.error.postalCodeTooLong");
            System.out.println(failure);
            showErrorAlert("Postal Code Error", failure);
            return;
        }
        if (tfUCPhoneNumber.getText().length() > 50) {
            String failure = resourceBundle.getString("AddCustomer.error.phoneNumberTooLong");
            System.out.println(failure);
            showErrorAlert("Phone Number Error", failure);
            return;
        }

        // Saves customer if error handling is fine
        updateCustomerInDatabase();
        closeUpdateCustomerWindow();
    }

    /**
     * Updates the customer information in the database.
     */
    private void updateCustomerInDatabase() {

        JDBC.openConnection();
        String query = "UPDATE customers " +
                "SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, " +
                "Last_Update = ?, Last_Updated_By = ?, Division_ID = ? " +
                "WHERE Customer_ID = ?";

        // Get Values for updated customer
        int customerID = Integer.parseInt(tfUCCustomerID.getText());
        String customerName = tfUCCustomerName.getText();
        String address = tfUCAddress.getText();
        String postalCode = tfUCPostalCode.getText();
        String phone = tfUCPhoneNumber.getText();
        Timestamp lastUpdate = Timestamp.valueOf(LocalDateTime.now());
        String lastUpdatedBy = UserHandler.getLoggedInUser();
        int divisionID = cbUCFirstLevelDivision.getSelectionModel().getSelectedIndex() + 1;

        // Execute the SQL statement
        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, customerName);
            statement.setString(2, address);
            statement.setString(3, postalCode);
            statement.setString(4, phone);
            statement.setTimestamp(5, lastUpdate);
            statement.setString(6, lastUpdatedBy);
            statement.setInt(7, divisionID);
            statement.setInt(8, customerID);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                String success = resourceBundle.getString("UpdateCustomer.success.updateCustomer");
                System.out.println(success);
                showSuccessAlert("Customer Updated", success);
            } else {
                String failure = resourceBundle.getString("UpdateCustomer.error.customerUpdate");
                System.out.println(failure);
                showErrorAlert("Update Failed", failure);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("UpdateCustomer.error.customerUpdate");
            System.out.println(failure);
            showErrorAlert("Update Failed", failure);
        } finally {
            JDBC.closeConnection();
        }
    }

    /**
     * Closes the update customer window.
     */
    @FXML
    private void closeUpdateCustomerWindow() {
        Stage stage = (Stage) tfUCCustomerID.getScene().getWindow();
        stage.close();
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

            cbUCCountry.setItems(countries);
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
     * Populates the division combo box with divisions based on the selected country.
     * @param countryID The ID of the selected country.
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

            cbUCFirstLevelDivision.setItems(divisions);
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = "Error getting divisions.";
            System.out.println(failure);
            showErrorAlert("Division Error", failure);
        }

        finally {
            JDBC.closeConnection();
        }

    }

    /**
     * Populates the fields of the update customer form with existing customer data.
     * @param customerID The ID of the customer.
     */
    public void populateFields(int customerID) {

        JDBC.openConnection();
        String query = "SELECT c.*, d.Division, co.Country " +
                       "FROM customers c " +
                       "JOIN first_level_divisions d ON c.Division_ID = d.Division_ID " +
                       "JOIN countries co ON d.Country_ID = co.Country_ID " +
                       "WHERE c.Customer_ID = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerID);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                tfUCCustomerID.setText(String.valueOf(customerID));
                tfUCCustomerName.setText(resultSet.getString("Customer_Name"));
                tfUCAddress.setText(resultSet.getString("Address"));
                tfUCPostalCode.setText(resultSet.getString("Postal_Code"));
                tfUCPhoneNumber.setText(resultSet.getString("Phone"));
                cbUCCountry.setValue(resultSet.getString("Country"));
                cbUCFirstLevelDivision.setValue(resultSet.getString("Division"));
            }

            int countryID = getCountryID(cbUCCountry.getValue());

            if (countryID != -1) {
                populateDivisionComboBox(countryID);
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("UpdateCustomer.error.getCustomer");
            System.out.println(failure);
            showErrorAlert("Customer Data Error", failure);
        }

        finally {
            JDBC.closeConnection();
        }

    }

    /**
     * Retrieves the ID of the country based on the country name.
     * @param countryName The name of the country.
     * @return The ID of the country, or -1 if not found.
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
     * Displays a success alert dialog.
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

    /**
     * Displays an error alert dialog.
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

}
