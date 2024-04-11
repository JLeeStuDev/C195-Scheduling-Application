package controllers;

import helper.JDBC;
import helper.AppointmentTimeManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import javafx.stage.Screen;
import javafx.stage.Stage;
import models.Appointment;
import models.Customer;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.attribute.UserPrincipal;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the main menu.
 */
public class MainMenuController {

    @FXML
    private BorderPane bpMainMenuMainPane;

    @FXML
    private TableView<Customer> tvMainMenuCustomerTable;

    @FXML
    private TableView<Appointment> tvMainMenuAppointmentTable;

    @FXML
    private TableColumn<Customer, Integer> tvcCustomerID;

    @FXML
    private TableColumn<Customer, String> tvcCustomerName;

    @FXML
    private TableColumn<Customer, String> tvcAddress;

    @FXML
    private TableColumn<Customer, String> tvcPostalCode;

    @FXML
    private TableColumn<Customer, String> tvcPhone;

    @FXML
    private TableColumn<Customer, LocalDateTime> tvcCustomerCreateDate;

    @FXML
    private TableColumn<Customer, String> tvcCustomerCreatedBy;

    @FXML
    private TableColumn<Customer, Timestamp> tvcCustomerLastUpdate;

    @FXML
    private TableColumn<Customer, String> tvcCustomerLastUpdatedBy;

    @FXML
    private TableColumn<Customer, Integer> tvcDivisionID;

    @FXML
    private TableColumn<Appointment, Integer> tvcAppointmentID;

    @FXML
    private TableColumn<Appointment, String> tvcTitle;

    @FXML
    private TableColumn<Appointment, String> tvcDescription;

    @FXML
    private TableColumn<Appointment, String> tvcContact;

    @FXML
    private TableColumn<Appointment, String> tvcLocation;

    @FXML
    private TableColumn<Appointment, String> tvcType;

    @FXML
    private TableColumn<Appointment, LocalDateTime> tvcStart;

    @FXML
    private TableColumn<Appointment, LocalDateTime> tvcEnd;

    @FXML
    private TableColumn<Appointment, LocalDateTime> tvcAppointmentCreateDate;

    @FXML
    private TableColumn<Appointment, String> tvcAppointmentCreatedBy;

    @FXML
    private TableColumn<Appointment, Timestamp> tvcAppointmentLastUpdate;

    @FXML
    private TableColumn<Appointment, String> tvcAppointmentLastUpdatedBy;

    @FXML
    private TableColumn<Appointment, Integer> tvcAppointmentCustomerID;

    @FXML
    private TableColumn<Appointment, Integer> tvcUserID;

    @FXML
    private TableColumn<Appointment, Integer> tvcContactID;

    @FXML
    private Label lblAppointments;

    @FXML
    private Button btnAddCustomer;

    @FXML
    private Button btnDeleteCustomer;

    @FXML
    private Button btnRefreshCustomer;

    @FXML
    private Button btnUpdateCustomer;

    @FXML
    private Button btnAddAppointment;

    @FXML
    private Button btnCancelAppointment;

    @FXML
    private Button btnUpdateAppointment;

    @FXML
    private Button btnRefreshAppointment;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabAllAppointments;

    @FXML
    private Tab tabByWeek;

    @FXML
    private Tab tabByMonth;

    @FXML
    private Menu reportsMenu;

    @FXML
    private MenuItem appointmentReport;

    @FXML
    private MenuItem scheduleReport;

    @FXML
    private MenuItem billingReport;

    private ResourceBundle resourceBundle;

    private boolean isUserInteraction = true;

    /**
     * Initializes the main menu controller.
     *
     * Two lambda expressions are used here.
     * Adds a listener to the selected item property of the tab pane.
     * Executes the handleTabSelection method if the user is interacting with the tab pane.
     *
     *  Adds a listener to the selected item property of the main menu customer table.
     *  Populates appointments for the selected customer if the selection is not null and if the user is interacting.
     *  Sets the label text accordingly and handles the selected tab.
     */
    @FXML
    public void initialize() {

        initializeMainMenu();
        initializeContactColumn();

        // Select Weekly Appointments tab by default
        tabPane.getSelectionModel().select(tabAllAppointments);

        /**
         * Adds a listener to the selected item property of the tab pane.
         * Executes the handleTabSelection method if the user is interacting with the tab pane.
         *
         * @param observable The observable value being observed.
         * @param oldTab     The previously selected tab.
         * @param newTab     The newly selected tab.
         */
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (isUserInteraction) {
                handleTabSelection(newTab);
            }
        });

        /**
         * Adds a listener to the selected item property of the main menu customer table.
         * Populates appointments for the selected customer if the selection is not null and if the user is interacting.
         * Sets the label text accordingly and handles the selected tab.
         *
         * @param obs           The observable value being observed.
         * @param oldSelection  The previously selected item.
         * @param newSelection  The newly selected item.
         */
        tvMainMenuCustomerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                if (isUserInteraction) {
                    populateAppointmentsForCustomer(newSelection.getCustomerID());
                    lblAppointments.setText(resourceBundle.getString("MainMenu.label.appointments") + " for " + newSelection.getCustomerName());
                }

                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                handleTabSelection(selectedTab);
            } else {
                tvMainMenuAppointmentTable.getItems().clear();
                lblAppointments.setText(resourceBundle.getString("MainMenu.label.appointments"));
            }
        });
    }

    /**
     * Deletes a customer from the database and table view.
     */
    @FXML
    private void DeleteCustomer() {

        Customer selectedCustomer = tvMainMenuCustomerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer != null) {
            int customerID = selectedCustomer.getCustomerID();

            if (hasAppointments(customerID)) {
                String failure = resourceBundle.getString("MainMenu.error.customerHasAppointments");
                System.out.println(failure);
                showErrorAlert("Customer Has Appointments", failure);
            }

            else {

                removeCustomer(customerID);
                tvMainMenuCustomerTable.getItems().remove(selectedCustomer);
                System.out.println("Customer successfully deleted.");
            }
        }

        else {
            String failure = resourceBundle.getString("MainMenu.error.customerNotSelected");
            System.out.println(failure);
            showErrorAlert("Customer Not Selected", failure);
        }
    }

    /**
     * Deletes an appointment from the database and the table view.
     */
    @FXML
    private void DeleteAppointment() {

        Appointment selectedAppointment = tvMainMenuAppointmentTable.getSelectionModel().getSelectedItem();

        if (selectedAppointment != null) {

            int appointmentID = selectedAppointment.getAppointmentID();
            String appointmentType = selectedAppointment.getType();

            removeAppointment(appointmentID, appointmentType);
            tvMainMenuAppointmentTable.getItems().remove(selectedAppointment);
            System.out.println("Appointment successfully deleted.");
        }

        else {
            String failure = resourceBundle.getString("MainMenu.error.appointmentNotSelected");
            System.out.println(failure);
            showErrorAlert("Appointment Not Selected", failure);
        }
    }

    /**
     * Opens a new window to add a customer.
     *
     *  Uses FXMLLoader to load the AddCustomer.fxml file and initializes the scene with it.
     *
     * @throws IOException if an error occurs while loading the FXML file.
     */
    @FXML
    private void AddCustomer() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddCustomer.fxml"));
            Parent root = loader.load();

            //Create new stage
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            //set the stage
            stage.setScene(scene);

            //Lights Camera Action
            stage.show();
        }

        catch (IOException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("MainMenu.error.addCustomer");
            System.out.println(failure);
            showErrorAlert("Add Customer Loading Error", failure);
        }
    }

    /**
     * Opens a new window to add an appointment for the given customer.
     *
     * @param customerID the ID of the customer for whom the appointment is being added.
     *
     *  Uses FXMLLoader to load the AddAppointment.fxml file, sets the controller,
     * and initializes the scene with it.
     *
     * @throws IOException if an error occurs while loading the FXML file.
     */
    @FXML
    private void AddAppointment(int customerID) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddAppointment.fxml"));
            AddAppointmentController controller = new AddAppointmentController(customerID);
            loader.setController(controller);
            Parent root = loader.load();

            //Create new stage
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            //set the stage
            stage.setScene(scene);

            //Lights Camera Action
            stage.show();
        }

        catch (IOException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("MainMenu.error.addAppointment");
            System.out.println(failure);
            showErrorAlert("Add Appointment Loading Error", failure);
        }
    }

    /**
     * Refreshes the customer table by clearing it and repopulating it with updated data.
     *
     *  Clears the table and calls the populateCustomerTable() method to refill it with updated data.
     */
    @FXML
    private void RefreshCustomers() {
        //Clear the table
        ObservableList<Customer> items = tvMainMenuCustomerTable.getItems();
        items.clear();

        populateCustomerTable();
    }

    /**
     * Opens a new window to update the selected customer's information.
     *
     *  Uses FXMLLoader to load the UpdateCustomer.fxml file, initializes the controller with the selected customer's ID,
     * and shows the stage with the updated customer information.
     *
     * @throws RuntimeException if an error occurs while loading the FXML file.
     */
    @FXML
    private void UpdateCustomer() {

        Customer selectedCustomer = tvMainMenuCustomerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer != null) {

            int customerID = selectedCustomer.getCustomerID();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UpdateCustomer.fxml"));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            UpdateCustomerController updateCustomerController = loader.getController();
            updateCustomerController.initialize(customerID);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        }

        else {
            String failure = resourceBundle.getString("MainMenu.error.general");
            System.out.println(failure);
            showErrorAlert("Customer Data Error", failure);
        }

    }

    /**
     * Retrieves the ID of the selected customer from the customer table.
     *
     * @return the ID of the selected customer, or -1 if no customer is selected.
     */
    @FXML
    private int HandleCustomerSelection() {

        Customer selectedCustomer = tvMainMenuCustomerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            return selectedCustomer.getCustomerID();
        }
        return -1;
    }

    /**
     * Handles adding an appointment for the selected customer.
     *
     *  Gets the customer ID using HandleCustomerSelection() and calls AddAppointment(customerID).
     */
    @FXML
    private void HandleAddAppointment() {
        int customerID = HandleCustomerSelection();
        AddAppointment(customerID);
    }

    /**
     * Refreshes the appointments by clearing and repopulating the customer table
     * and appointments for the selected customer.
     *
     *  Calls RefreshCustomers() to clear and repopulate the customer table
     * and then calls populateAppointmentsForCustomer(selectedCustomerID) to refill appointments.
     * Also selects the "All Appointments" tab.
     */
    @FXML
    private void RefreshAppointments() {
        int selectedCustomerID = HandleCustomerSelection();
        RefreshCustomers();
        populateAppointmentsForCustomer(selectedCustomerID);
        // Reselect the "All Appointments" tab
        tabPane.getSelectionModel().select(tabAllAppointments);
    }

    /**
     * Opens a new window to update the selected appointment's information.
     *
     *  Uses FXMLLoader to load the UpdateAppointment.fxml file, initializes the controller with the selected appointment's ID,
     * and shows the stage with the updated appointment information.
     *
     * @throws IOException if an error occurs while loading the FXML file.
     */
    @FXML
    private void UpdateAppointment() {

        int selectedAppointmentID = getSelectedAppointmentID();

        if (selectedAppointmentID != -1) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UpdateAppointment.fxml"));
            Parent root;

            try {
                root = loader.load();
                UpdateAppointmentController controller = loader.getController();
                controller.initialize(selectedAppointmentID);
                Stage stage = new Stage();
                stage.setTitle("Update Appointment");
                stage.setScene(new Scene(root));
                stage.show();
            }

            catch (IOException e) {
                e.printStackTrace();
                String failure = resourceBundle.getString("MainMenu.error.appointmentNotLoaded");
                System.out.println(failure);
                showErrorAlert("Appointment Error", failure);
            }
        }

        else {
            String failure = "No appointment selected to update.";
            showErrorAlert("Appointment Error", failure);
        }
    }

    /**
     * Generates and displays an appointment report.
     *
     *  Uses FXMLLoader to load the AppointmentReport.fxml file, initializes the controller,
     * and shows the stage with the appointment report.
     *
     * @throws IOException if an error occurs while loading the FXML file.
     */
    @FXML
    private void generateAppointmentReport() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AppointmentReport.fxml"));
            Parent root = loader.load();
            AppointmentReportController controller = loader.getController();
            controller.initialize();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Appointment Report");
            stage.show();
        }
        catch (IOException e) {

            e.printStackTrace();
            showErrorAlert("Error", "Failed to generate appointment report.");
        }

    }

    /**
     * Generates and displays a schedule report.
     *
     *  Uses FXMLLoader to load the ScheduleReport.fxml file, initializes the controller,
     * and shows the stage with the schedule report.
     *
     * @throws IOException if an error occurs while loading the FXML file.
     */
    @FXML
    private void generateScheduleReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ScheduleReport.fxml"));
            Parent root = loader.load();
            ScheduleReportController controller = loader.getController();
            controller.initialize();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Schedule Report");
            stage.show();
        }
        catch (IOException e) {

            e.printStackTrace();
            showErrorAlert("Error", "Failed to generate schedule report.");
        }
    }

    /**
     * Generates and displays a billing report.
     *
     *  Uses FXMLLoader to load the BillingReport.fxml file, initializes the controller,
     * and shows the stage with the billing report.
     *
     * @throws IOException if an error occurs while loading the FXML file.
     */
    @FXML
    private void generateBillingReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BillingReport.fxml"));
            Parent root = loader.load();
            BillingReportController controller = loader.getController();
            controller.initialize();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Billing Report");
            stage.show();
        }
        catch (IOException e) {

            e.printStackTrace();
            showErrorAlert("Error", "Failed to generate billing report.");
        }
    }

    /**
     * Initializes the main menu by setting up table views, columns, and buttons.
     *
     * Sets up localization, resizes the menu dynamically, initializes table view columns,
     * sets up reports menu, and populates the customer table.
     *
     * A Lambda expression is used here:
     *
     * Adds a listener to the selected item property of the main menu customer table.
     * Populates appointments for the selected customer if the selection is not null and if the user is interacting.
     * Sets the label text accordingly and handles the selected tab.
     */
    public void initializeMainMenu() {

        //System.out.println(username + " has successfully logged in and main menu successfully launched.");

        resourceBundle = ResourceBundle.getBundle("language", Locale.getDefault());

        //Resize the menu dynamically
        AnchorPane.setTopAnchor(bpMainMenuMainPane, 0.0);
        AnchorPane.setBottomAnchor(bpMainMenuMainPane, 0.0);
        AnchorPane.setLeftAnchor(bpMainMenuMainPane, 0.0);
        AnchorPane.setRightAnchor(bpMainMenuMainPane, 0.0);

        //Calculate Tableview Width
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double customerTableWidth = screenWidth * 0.53;
        tvMainMenuCustomerTable.setPrefWidth(customerTableWidth);

        double appointmentTableWidth = screenWidth * 0.42;
        tvMainMenuAppointmentTable.setPrefWidth(appointmentTableWidth);

        //Set Column Policies
        tvMainMenuCustomerTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tvMainMenuAppointmentTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        //Initialize Customer TableView Columns
        tvcCustomerID.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        tvcCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        tvcAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        tvcPostalCode.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        tvcPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));;
        tvcCustomerCreateDate.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        tvcCustomerCreatedBy.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
        tvcCustomerLastUpdate.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));
        tvcCustomerLastUpdatedBy.setCellValueFactory(new PropertyValueFactory<>("lastUpdatedBy"));
        tvcDivisionID.setCellValueFactory(new PropertyValueFactory<>("divisionID"));

        tvcCustomerID.setText(resourceBundle.getString("MainMenu.customerTable.column.customerID"));
        tvcCustomerName.setText(resourceBundle.getString("MainMenu.customerTable.column.customerName"));
        tvcAddress.setText(resourceBundle.getString("MainMenu.customerTable.column.address"));
        tvcPostalCode.setText(resourceBundle.getString("MainMenu.customerTable.column.postalCode"));
        tvcPhone.setText(resourceBundle.getString("MainMenu.customerTable.column.phone"));
        tvcCustomerCreateDate.setText(resourceBundle.getString("MainMenu.customerTable.column.createDate"));
        tvcCustomerCreatedBy.setText(resourceBundle.getString("MainMenu.customerTable.column.createdBy"));
        tvcCustomerLastUpdate.setText(resourceBundle.getString("MainMenu.customerTable.column.lastUpdate"));
        tvcCustomerLastUpdatedBy.setText(resourceBundle.getString("MainMenu.customerTable.column.lastUpdatedBy"));
        tvcDivisionID.setText(resourceBundle.getString("MainMenu.customerTable.column.divisionID"));

        //Initialize Appointments Tabs
        tabAllAppointments.setText(resourceBundle.getString("MainMenu.appointmentTable.tab.all"));
        tabByWeek.setText(resourceBundle.getString("MainMenu.appointmentTable.tab.week"));
        tabByMonth.setText(resourceBundle.getString("MainMenu.appointmentTable.tab.month"));

        //Initialize Appointments TableView Columns
        tvcAppointmentID.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
        tvcTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        tvcDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tvcLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        tvcContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        tvcType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tvcStart.setCellValueFactory(new PropertyValueFactory<>("start"));
        tvcEnd.setCellValueFactory(new PropertyValueFactory<>("end"));
        //tvcAppointmentCreateDate.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        //tvcAppointmentCreatedBy.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
        //tvcAppointmentLastUpdate.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));
        //tvcAppointmentLastUpdatedBy.setCellValueFactory(new PropertyValueFactory<>("lastUpdatedBy"));
        tvcAppointmentCustomerID.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        tvcUserID.setCellValueFactory(new PropertyValueFactory<>("userID"));
        //tvcContactID.setCellValueFactory(new PropertyValueFactory<>("contactID"));

        tvcAppointmentID.setText(resourceBundle.getString("MainMenu.appointmentTable.column.appointmentID"));
        tvcTitle.setText(resourceBundle.getString("MainMenu.appointmentTable.column.title"));
        tvcDescription.setText(resourceBundle.getString("MainMenu.appointmentTable.column.description"));
        tvcLocation.setText(resourceBundle.getString("MainMenu.appointmentTable.column.location"));
        tvcContact.setText(resourceBundle.getString("MainMenu.appointmentTable.column.contact"));
        tvcType.setText(resourceBundle.getString("MainMenu.appointmentTable.column.type"));
        tvcStart.setText(resourceBundle.getString("MainMenu.appointmentTable.column.start"));
        tvcEnd.setText(resourceBundle.getString("MainMenu.appointmentTable.column.end"));
        //tvcAppointmentCreateDate.setText(resourceBundle.getString("MainMenu.appointmentTable.column.createDate"));
        //tvcAppointmentCreatedBy.setText(resourceBundle.getString("MainMenu.appointmentTable.column.createdBy"));
        //tvcAppointmentLastUpdate.setText(resourceBundle.getString("MainMenu.appointmentTable.column.lastUpdate"));
        //tvcAppointmentLastUpdatedBy.setText(resourceBundle.getString("MainMenu.appointmentTable.column.lastUpdatedBy"));
        tvcAppointmentCustomerID.setText(resourceBundle.getString("MainMenu.appointmentTable.column.customerID"));
        tvcUserID.setText(resourceBundle.getString("MainMenu.appointmentTable.column.userID"));
        //tvcContactID.setText(resourceBundle.getString("MainMenu.appointmentTable.column.contactID"));

        /**
         * Adds a listener to the selected item property of the main menu customer table.
         * Populates appointments for the selected customer if the selection is not null and if the user is interacting.
         * Sets the label text accordingly and handles the selected tab.
         *
         * @param obs           The observable value being observed.
         * @param oldSelection  The previously selected item.
         * @param newSelection  The newly selected item.
         */
        tvMainMenuCustomerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateAppointmentsForCustomer(newSelection.getCustomerID());

                lblAppointments.setText(resourceBundle.getString("MainMenu.label.appointments") + " for " + newSelection.getCustomerName());
            }

            // Apply appropriate appointment filtering based on the currently selected tab
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab == tabAllAppointments) {
                displayAllAppointments();
            } else if (selectedTab == tabByWeek) {
                sortWeeklyAppointments(tvMainMenuCustomerTable.getSelectionModel().selectedItemProperty().getValue().getCustomerID());
            } else if (selectedTab == tabByMonth) {
                sortMonthlyAppointments(tvMainMenuCustomerTable.getSelectionModel().selectedItemProperty().getValue().getCustomerID());
            }

            else {
                //clear appointments table if no customers are selected
                tvMainMenuAppointmentTable.getItems().clear();

                lblAppointments.setText(resourceBundle.getString("MainMenu.label.appointments"));
            }
        });

        // Set button text retrieved from resource bundle
        btnAddCustomer.setText(resourceBundle.getString("MainMenu.button.addCustomer"));
        btnDeleteCustomer.setText(resourceBundle.getString("MainMenu.button.deleteCustomer"));
        btnRefreshCustomer.setText(resourceBundle.getString("MainMenu.button.refreshCustomers"));
        btnUpdateCustomer.setText(resourceBundle.getString("MainMenu.button.updateCustomer"));
        btnAddAppointment.setText(resourceBundle.getString("MainMenu.button.addAppointment"));
        btnCancelAppointment.setText(resourceBundle.getString("MainMenu.button.cancelAppointment"));
        btnRefreshAppointment.setText(resourceBundle.getString("MainMenu.button.refreshAppointment"));
        btnUpdateAppointment.setText(resourceBundle.getString("MainMenu.button.updateAppointment"));

        //set up reports menu
        reportsMenu.setText(resourceBundle.getString("MainMenu.reports.menu"));
        appointmentReport.setText(resourceBundle.getString("MainMenu.reports.appointmentReport"));
        scheduleReport.setText(resourceBundle.getString("MainMenu.reports.scheduleReport"));

        populateCustomerTable();
    }

    /**
     * Populates the customer table with data retrieved from the database.
     *
     * Opens a database connection, executes a query to retrieve customer data,
     * creates Customer objects from the result set, and adds them to the table.
     */
    public void populateCustomerTable() {

        JDBC.openConnection();
        String query = "SELECT * FROM customers";

        try {
            System.out.println("Attempting to fill customer table.");

            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                //Get Customer Data from DB
                int customerID = resultSet.getInt("Customer_ID");
                String customerName = resultSet.getString("Customer_Name");
                String address = resultSet.getString("Address");
                String postalCode = resultSet.getString("Postal_Code");
                String phone = resultSet.getString("Phone");
                LocalDateTime customerCreateDate = resultSet.getTimestamp("Create_Date").toLocalDateTime();
                String customerCreatedBy = resultSet.getString("Created_By");
                Timestamp customerLastUpdate = resultSet.getTimestamp("Last_Update");
                String customerLastUpdatedBy = resultSet.getString("Last_Updated_By");
                int divisionID = resultSet.getInt("Division_ID");

                //Make the Customer
                Customer customer = new Customer(customerID, customerName, address, postalCode, phone, customerCreateDate, customerCreatedBy, customerLastUpdate, customerLastUpdatedBy, divisionID);
                tvMainMenuCustomerTable.getItems().add(customer);
                System.out.println("Customer Successfully Added!");
            }

            JDBC.closeConnection();
        }

        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Customer was not added correctly.");
        }
    }

    /**
     * Initializes the contact column in the appointments table.
     *
     * Sets up a cell value factory for the contact column to display contact names.
     *
     * A lambda expression is used here
     * Sets the cell value factory for the contact column in the appointments table view.
     * Retrieves the contact name for the specified appointment and returns it as a SimpleStringProperty.
     */
    private void initializeContactColumn() {

        /**
         * Sets the cell value factory for the contact column in the appointments table view.
         * Retrieves the contact name for the specified appointment and returns it as a SimpleStringProperty.
         *
         * @param cellData The appointment data for the cell.
         * @return         The contact name as a SimpleStringProperty.
         */
        tvcContact.setCellValueFactory(cellData -> {
            Appointment appointment = cellData.getValue();
            int contactID = appointment.getContactID();

            String contactName = lookupContactName(contactID);

            return new SimpleStringProperty(contactName);
        });
    }

    /**
     * Retrieves the ID of the selected appointment from the appointments table.
     *
     * @return the ID of the selected appointment, or -1 if no appointment is selected.
     */
    private int getSelectedAppointmentID() {
        Appointment selectedAppointment = tvMainMenuAppointmentTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            return selectedAppointment.getAppointmentID();
        } else {
            return -1; // Return -1 if no appointment is selected
        }
    }

    /**
     * Sorts appointments for the selected customer by week and displays them in the appointments table.
     *
     * @param customerID the ID of the selected customer.
     *
     * Retrieves the current date, calculates the start and end dates of the week,
     * filters appointments within the week range, and adds them to the appointments table.
     */
    private void sortWeeklyAppointments(int customerID) {

        LocalDate currentDate = LocalDate.now();

        LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        /**
         * Filters the list of appointments for a customer to get weekly appointments.
         *
         * @param customerID   The ID of the customer.
         * @param startOfWeek  The start date of the week.
         * @param endOfWeek    The end date of the week.
         * @return             A list of weekly appointments for the customer.
         */
        List<Appointment> weeklyAppointments = getAppointmentsForCustomer(customerID).stream()
                .filter(appointment -> {
                    LocalDate appointmentDate = appointment.getStart().toLocalDate();
                    return !appointmentDate.isBefore(startOfWeek) && !appointmentDate.isAfter(endOfWeek);
                })
                .collect(Collectors.toList());

        tvMainMenuAppointmentTable.getItems().clear();
        tvMainMenuAppointmentTable.getItems().addAll(weeklyAppointments);
    }

    /**
     * Sorts appointments for the selected customer by month and displays them in the appointments table.
     *
     * @param customerID the ID of the selected customer.
     *
     * Retrieves the current date, calculates the start and end dates of the month,
     * filters appointments within the month range, and adds them to the appointments table.
     */
    private void sortMonthlyAppointments(int customerID) {

        LocalDate currentDate = LocalDate.now();

        LocalDate startOfMonth = currentDate.withDayOfMonth(1);
        LocalDate endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());

        /**
         * Filters the list of appointments for a customer to get monthly appointments.
         *
         * @param customerID    The ID of the customer.
         * @param startOfMonth  The start date of the month.
         * @param endOfMonth    The end date of the month.
         * @return              A list of monthly appointments for the customer.
         */
        List<Appointment> monthlyAppointments = getAppointmentsForCustomer(customerID).stream()
                .filter(appointment -> {
                    LocalDate appointmentDate = appointment.getStart().toLocalDate();
                    return !appointmentDate.isBefore(startOfMonth) && !appointmentDate.isAfter(endOfMonth);
                })
                .collect(Collectors.toList());

        tvMainMenuAppointmentTable.getItems().clear();
        tvMainMenuAppointmentTable.getItems().addAll(monthlyAppointments);
    }

    /**
     * Displays all appointments by refreshing the appointments table.
     */
    private void displayAllAppointments() {
        RefreshAppointments();
    }

    /**
     * Populates the appointments table with appointments for the selected customer.
     *
     * @param searchableCustomerID the ID of the selected customer.
     *
     *  Opens a database connection, executes a query to retrieve appointments for the selected customer,
     * creates Appointment objects from the result set, and adds them to the appointments table.
     */
    private void populateAppointmentsForCustomer(int searchableCustomerID) {

        JDBC.openConnection();
        String query = "SELECT * FROM appointments WHERE Customer_ID = ?";

        try {
            System.out.println("Attempting to populate the appointments table based on customer selected.");

            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, searchableCustomerID);
            ResultSet resultSet = statement.executeQuery();

            //Clear out the appointments table so it is fresh
            tvMainMenuAppointmentTable.getItems().clear();

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

                tvMainMenuAppointmentTable.getItems().add(appointment);
            }
        }

        catch (SQLException e) {
            System.out.println("There was an error when processing appointments for customer: " + searchableCustomerID + ".");
        }
    }

    /**
     * Checks if the customer has appointments.
     *
     * @param customerID the ID of the customer.
     * @return true if the customer has appointments, false otherwise.
     *
     *  Opens a database connection, executes a query to count appointments for the customer,
     * and returns true if the count is greater than 0.
     */
    private boolean hasAppointments(int customerID) {

        JDBC.openConnection();
        String query = "SELECT COUNT(*) FROM appointments WHERE Customer_ID = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerID);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }

        catch (SQLException e) {
            System.out.println("There was an issue with finding appointements for this customer.");
            e.printStackTrace();
        }

        finally {
            JDBC.closeConnection();
        }

        return false;
    }

    /**
     * Removes a customer from the database.
     *
     * @param customerID the ID of the customer to be removed.
     *
     *  Opens a database connection, executes a delete query to remove the customer,
     * and displays a success or error message.
     */
    private void removeCustomer(int customerID) {

        JDBC.openConnection();
        String query = "DELETE FROM customers WHERE Customer_ID = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerID);
            int rowsAffected =statement.executeUpdate();

            if (rowsAffected > 0) {
                String success = resourceBundle.getString("MainMenu.success.customerDeleted");
                System.out.println(success);
                showSuccessAlert("Customer Removed", success);
            }
            else {
                String failure = resourceBundle.getString("MainMenu.error.customerNotFound");
                System.out.println(failure);
                showErrorAlert("Customer Not Found", failure);
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("MainMenu.error.general");
            System.out.println(failure);
            showErrorAlert("Customer Data Error", failure);
        }
        finally {
            JDBC.closeConnection();
        }
    }

    /**
     * Retrieves the name of a contact from the database.
     *
     * @param contactID the ID of the contact.
     * @return the name of the contact.
     *
     *  Opens a database connection, executes a query to retrieve the contact name,
     * and returns the name if found.
     */
    private String lookupContactName(int contactID) {

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
            String failure = "Could not get the contact name.";
            System.out.println(failure);
            showErrorAlert("Contact Name Error", failure);
        }
        return contactName;
    }

    /**
     * Handles tab selection events.
     *
     * @param selectedTab the selected tab.
     *
     *  Depending on the selected tab, it either displays all appointments,
     * sorts appointments by week or month, or does nothing.
     */
    private void handleTabSelection(Tab selectedTab) {
        if (selectedTab == tabAllAppointments) {
            displayAllAppointments();
        }
        else if (selectedTab == tabByWeek) {

            int selectedCustomerID = HandleCustomerSelection();

            if (selectedCustomerID != -1) {
                sortWeeklyAppointments(selectedCustomerID);
            }
        }
        else if (selectedTab == tabByMonth) {

            int selectedCustomerID = HandleCustomerSelection();

            if (selectedCustomerID != -1) {
                sortMonthlyAppointments(selectedCustomerID);
            }
        }
    }

    /**
     * Retrieves appointments for a specific customer from the database.
     *
     * @param customerId the ID of the customer.
     * @return a list of appointments for the customer.
     *
     *  Opens a database connection, executes a query to retrieve appointments for the customer,
     * creates Appointment objects from the result set, and returns the list of appointments.
     */
    private List<Appointment> getAppointmentsForCustomer(int customerId) {
        List<Appointment> appointments = new ArrayList<>();

        // Assuming you have a method to retrieve appointments from the database
        // Replace this logic with your actual database query
        JDBC.openConnection();
        String query = "SELECT * FROM appointments WHERE Customer_ID = ?";
        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, customerId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int appointmentID = resultSet.getInt("Appointment_ID");
                String title = resultSet.getString("Title");
                String description = resultSet.getString("Description");
                String location = resultSet.getString("Location");
                String type = resultSet.getString("Type");
                LocalDateTime start = resultSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime end = resultSet.getTimestamp("End").toLocalDateTime();
                LocalDateTime createDate = resultSet.getTimestamp("Create_Date").toLocalDateTime();
                String createdBy = resultSet.getString("Created_By");
                Timestamp lastUpdate = resultSet.getTimestamp("Last_Update");
                String lastUpdatedBy = resultSet.getString("Last_Updated_By");
                int userID = resultSet.getInt("User_ID");
                int contactID = resultSet.getInt("Contact_ID");
                // Create Appointment object and add to list
                Appointment appointment = new Appointment(appointmentID, title, description, location, type, start, end,
                        createDate, createdBy, lastUpdate, lastUpdatedBy, customerId, userID, contactID);
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error fetching appointments for customer: " + customerId);
        } finally {
            JDBC.closeConnection();
        }

        return appointments;
    }

    /**
     * Removes an appointment from the database.
     *
     * @param appointmentID the ID of the appointment to be removed.
     * @param appointmentType the type of the appointment.
     *
     *  Opens a database connection, executes a delete query to remove the appointment,
     * and displays a success or error message.
     */
    private void removeAppointment(int appointmentID, String appointmentType) {

        JDBC.openConnection();
        String query = "DELETE FROM appointments WHERE Appointment_ID = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, appointmentID);
            int rowsAffected =statement.executeUpdate();

            if (rowsAffected > 0) {
                String success = resourceBundle.getString("MainMenu.success.appointment") + " " +
                        appointmentID + " " +
                        resourceBundle.getString("MainMenu.success.appointmentType") + " " +
                        appointmentType + " " +
                        resourceBundle.getString("MainMenu.success.appointmentDeleted");
                System.out.println(success);
                showSuccessAlert("Appointment Removed", success);
            }
            else {
                String failure = resourceBundle.getString("MainMenu.error.appointmentNotFound");
                System.out.println(failure);
                showErrorAlert("Appointment Not Found", failure);
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
            String failure = resourceBundle.getString("MainMenu.error.general");
            System.out.println(failure);
            showErrorAlert("Appointment Data Error", failure);
        }
        finally {
            JDBC.closeConnection();
        }
    }

    /**
     * Displays a success alert dialog.
     *
     * @param title the title of the alert dialog.
     * @param message the message to be displayed.
     *
     *  Creates an alert dialog of type INFORMATION with the specified title and message,
     * then displays the dialog and waits for user interaction.
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
     *
     * @param title the title of the alert dialog.
     * @param message the message to be displayed.
     *
     *  Creates an alert dialog of type ERROR with the specified title and message,
     * then displays the dialog and waits for user interaction.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
