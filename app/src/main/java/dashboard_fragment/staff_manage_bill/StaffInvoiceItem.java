package dashboard_fragment.staff_manage_bill;

public class StaffInvoiceItem {
    private final String id;
    private final String patientName;
    private final String date;
    private final long amount;
    private final boolean paid;
    private final String paymentMethod;

    public StaffInvoiceItem(String id, String patientName, String date, long amount,
                            boolean paid, String paymentMethod) {
        this.id = id;
        this.patientName = patientName;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
        this.paymentMethod = paymentMethod;
    }

    public String getId() {
        return id;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDate() {
        return date;
    }

    public long getAmount() {
        return amount;
    }

    public boolean isPaid() {
        return paid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
