package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Appointment;
import models.BillingEntry;
import models.Customer;

import java.sql.Array;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller class for Billing Reports.
 */
public class BillingReportController {

    @FXML
    private Label billingReportLabel;

    @FXML
    private TableView<BillingEntry> billingTable;

    @FXML
    private TableColumn<BillingEntry, Integer> customerIDColumn;

    @FXML
    private TableColumn<BillingEntry, String> customerNameColumn;

    @FXML
    private TableColumn<BillingEntry, Double> amountPaidColumn;

    @FXML
    private Button btnClose;

    private ResourceBundle resourceBundle;

    private List<Customer> customerList;

    /**
     * Initializes the billing report UI components and populates the billing table.
     */
    @FXML
    public void initialize() {

        resourceBundle = ResourceBundle.getBundle("language", Locale.getDefault());

        billingReportLabel.setText(resourceBundle.getString("BillingReport.label.title"));
        btnClose.setText(resourceBundle.getString("BillingReport.button.close"));

        configureTableColumns();
        populateBillingTable();
    }

    /**
     * closes the billing report window.
     */
    @FXML
    public void closeWindow() {
        Stage stage = (Stage) billingReportLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Configures the columns of the billing table.
     */
    private void configureTableColumns() {
        customerIDColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        amountPaidColumn.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));

        customerIDColumn.setText(resourceBundle.getString("BillingReport.column.customerID"));
        customerNameColumn.setText(resourceBundle.getString("BillingReport.column.customerName"));
        amountPaidColumn.setText(resourceBundle.getString("BillingReport.column.amountPaid"));
    }

    /**
     * Populates the billing table with data.
     */
    private void populateBillingTable() {

        List<Customer> customers = Customer.getAllCustomers();

        List<BillingEntry> billingEntries = new ArrayList<>();

        for (Customer customer : customers) {
            double totalAmount = calculateTotalAmountForCustomer(customer);
            billingEntries.add(new BillingEntry(customer.getCustomerID(), customer.getCustomerName(), totalAmount));
        }
        ObservableList<BillingEntry> observableBillingEntries = FXCollections.observableList(billingEntries);
        billingTable.setItems(observableBillingEntries);
    }

    /**
     * Calculates the total amount to be paid by a customer.
     *
     * @param customer  The customer for whom the total amount is to be calculated.
     * @return          The total amount to be paid by the customer.
     */
    private double calculateTotalAmountForCustomer(Customer customer) {

        List<Appointment> appointments = Appointment.getAppointmentForCustomer(customer.getCustomerID());

        int totalDurationInMinutes = 0;

        for (Appointment appointment : appointments) {
            totalDurationInMinutes += calculateDurationInMinutes(appointment.getStart(), appointment.getEnd());
        }

        double ratePerHour = 100.0;
        double totalAmount = ((double) totalDurationInMinutes /60)*ratePerHour;

        return totalAmount;
    }

    /**
     * Calculates the duration in minutes between two LocalDateTime instances.
     *
     * @param start The start time.
     * @param end   The end time.
     * @return      The duration in Minutes.
     */
    private int calculateDurationInMinutes(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        return (int) duration.toMinutes();
    }

}
