package models;

/**
 * Represents a billing entry.
 */
public class BillingEntry {
    private int customerID;
    private String customerName;
    private double amountPaid;

    /**
     * Constructs a BillingEntry object with the specified parameters.
     * @param customerID The ID of the customer associated with the billing entry.
     * @param customerName The name of the customer associated with the billing entry.
     * @param amountPaid The amount paid for the billing entry.
     */
    public BillingEntry(int customerID, String customerName, double amountPaid) {
        this.customerID = customerID;
        this.customerName = customerName;
        this.amountPaid = amountPaid;
    }

    public int getCustomerID() {
        return customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getAmountPaid() {
        return amountPaid;
    }
}