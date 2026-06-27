package dashboard_fragment.staff_manage_bill;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nhom08_quanlyphongkham.R;

import java.util.List;
import dashboard_fragment.staff_manage_bill.get_bills_logic.ExamFormWithBillDto;

public class BillRowBinder {

    public static void bindServices(LinearLayout container, List<ExamFormWithBillDto.MedicalRecordClinicalDto> services) {
        container.removeAllViews();
        if (services == null) return;

        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        for (ExamFormWithBillDto.MedicalRecordClinicalDto dto : services) {
            if (dto == null || dto.getClinical() == null) continue;

            ExamFormWithBillDto.MedicalRecordClinicalDto.ClinicalDetails details = dto.getClinical();
            View row = inflater.inflate(R.layout.staff_item_service_medicine_bill_row, container, false);

            TextView tvItemName = row.findViewById(R.id.tvItemName);
            TextView tvItemQuantity = row.findViewById(R.id.tvItemQuantity);
            TextView tvItemPrice = row.findViewById(R.id.tvItemPrice);
            TextView tvItemTotal = row.findViewById(R.id.tvItemTotal);

            double price = dto.getDon_gia_luc_chi_dinh() != null ? dto.getDon_gia_luc_chi_dinh() : 0.0;

            tvItemName.setText(details.getTen_dich_vu() != null ? details.getTen_dich_vu() : "--");
            tvItemQuantity.setText("1"); // Services default to 1
            tvItemPrice.setText(String.format("%,.0f", price));
            tvItemTotal.setText(String.format("%,.0fđ", price));

            container.addView(row);
        }
    }

    public static void bindMedicines(LinearLayout container, List<ExamFormWithBillDto.MedicalRecordMedicineDto> medicines) {
        container.removeAllViews();
        if (medicines == null) return;

        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        for (ExamFormWithBillDto.MedicalRecordMedicineDto dto : medicines) {
            if (dto == null || dto.getMedicine() == null) continue;

            ExamFormWithBillDto.MedicalRecordMedicineDto.MedicineDetails details = dto.getMedicine();
            View row = inflater.inflate(R.layout.staff_item_service_medicine_bill_row, container, false);

            TextView tvItemName = row.findViewById(R.id.tvItemName);
            TextView tvItemQuantity = row.findViewById(R.id.tvItemQuantity);
            TextView tvItemPrice = row.findViewById(R.id.tvItemPrice);
            TextView tvItemTotal = row.findViewById(R.id.tvItemTotal);

            long qty = dto.getSo_luong() != null ? dto.getSo_luong() : 0L;
            double price = dto.getDon_gia_luc_ke_don() != null ? dto.getDon_gia_luc_ke_don() : 0.0;
            double total = qty * price;

            String label = details.getTen_thuoc() != null ? details.getTen_thuoc() : "--";
            if (details.getDon_vi() != null && !details.getDon_vi().isEmpty()) {
                label += " (" + details.getDon_vi() + ")";
            }

            tvItemName.setText(label);
            tvItemQuantity.setText(String.valueOf(qty));
            tvItemPrice.setText(String.format("%,.0f", price));
            tvItemTotal.setText(String.format("%,.0fđ", total));

            container.addView(row);
        }
    }

    public static double calculateGrandTotal(ExamFormWithBillDto examFormData) {
        if (examFormData == null || examFormData.getMedical_record() == null) {
            return 0.0;
        }

        double total = 0.0;
        ExamFormWithBillDto.MedicalRecordWrapper record = examFormData.getMedical_record();

        if (record.getMedical_record_clinical() != null) {
            for (ExamFormWithBillDto.MedicalRecordClinicalDto dto : record.getMedical_record_clinical()) {
                if (dto != null && dto.getDon_gia_luc_chi_dinh() != null) {
                    total += dto.getDon_gia_luc_chi_dinh();
                }
            }
        }

        if (record.getMedical_record_medicine() != null) {
            for (ExamFormWithBillDto.MedicalRecordMedicineDto dto : record.getMedical_record_medicine()) {
                if (dto != null && dto.getDon_gia_luc_ke_don() != null) {
                    long qty = dto.getSo_luong() != null ? dto.getSo_luong() : 0L;
                    total += (qty * dto.getDon_gia_luc_ke_don());
                }
            }
        }

        return total;
    }
}
