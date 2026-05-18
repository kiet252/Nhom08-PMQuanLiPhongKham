package dashboard_fragment.staff_manage_bill;

import java.io.Serializable;
import dashboard_fragment.staff_manage_bill.get_bills_logic.ExamFormWithBillDto;

public class StaffInvoiceItem implements Serializable {
    private final String id;
    private final String patientName;
    private final String date;
    private final long amount;
    private final boolean paid;
    private final String paymentMethod;
    private final ExamFormWithBillDto originalForm;
    public StaffInvoiceItem(String id, String patientName, String date, long amount,
                            boolean paid, String paymentMethod, ExamFormWithBillDto originalForm) {
        this.id = id;
        this.patientName = patientName;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
        this.paymentMethod = paymentMethod;
        this.originalForm = originalForm;
    }

    public String getId() { return id; }
    public String getPatientName() { return patientName; }
    public String getDate() { return date; }
    public long getAmount() { return amount; }
    public boolean isPaid() { return paid; }
    public String getPaymentMethod() { return paymentMethod; }
    public ExamFormWithBillDto getOriginalForm() {
        return originalForm;
    }
}