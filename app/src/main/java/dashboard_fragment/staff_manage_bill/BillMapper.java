package dashboard_fragment.staff_manage_bill;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dashboard_fragment.staff_manage_bill.get_bills_logic.ExamFormWithBillDto;
import dashboard_fragment.staff_manage_bill.get_bills_logic.ExamFormWithBillDto.BillSummaryDto;

public final class BillMapper {
    private static final String PAID_STATUS = "Đã thanh toán";

    private BillMapper() {}

    public static List<StaffInvoiceItem> fromExamForms(List<ExamFormWithBillDto> forms) {
        List<StaffInvoiceItem> items = new ArrayList<>();
        if (forms == null) {
            return items;
        }

        for (ExamFormWithBillDto form : forms) {
            if (form == null
                    || form.getMedical_record() == null
                    || form.getMedical_record().getBill() == null) {
                continue;
            }

            String patientName = resolvePatientName(form);
            String displayDate = formatDateString(form.getNgay_kham());

            for (BillSummaryDto bill : form.getMedical_record().getBill()) {
                if (bill == null || bill.getId() == null) {
                    continue;
                }

                boolean paid = PAID_STATUS.equals(bill.getTrang_thai_thanh_toan());
                long amount =
                        bill.getTong_thanh_toan() != null ? Math.round(bill.getTong_thanh_toan()) : 0L;
                String date =
                        displayDate != null ? displayDate : formatDateString(bill.getCreated_at());

                // Fixed: Passing 'form' as the 7th argument matching the new constructor signatures
                items.add(
                        new StaffInvoiceItem(
                                String.valueOf(bill.getId()),
                                patientName,
                                date != null ? date : "--",
                                amount,
                                paid,
                                bill.getPhuong_thuc_thanh_toan(),
                                form
                        ));
            }
        }

        return items;
    }

    private static String resolvePatientName(ExamFormWithBillDto form) {
        if (form.getPatient() != null) {
            String hoTen = form.getPatient().getHo_ten();
            if (hoTen != null && !hoTen.isEmpty()) {
                return hoTen;
            }
        }
        return "--";
    }

    private static String formatDateString(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd"
        };

        for (String pattern : patterns) {
            try {
                Date parsed = new SimpleDateFormat(pattern, Locale.getDefault()).parse(raw);
                if (parsed != null) {
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsed);
                }
            } catch (ParseException ignored) {
            }
        }

        if (raw.length() >= 10 && raw.charAt(4) == '-' && raw.charAt(7) == '-') {
            String[] parts = raw.substring(0, 10).split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        }

        return raw;
    }
}