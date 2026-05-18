package dashboard_fragment.staff_manage_bill.get_bills_logic;

import java.util.List;

public class ExamFormWithBillDto {
    private Long id;
    private String ngay_kham;
    private PatientWrapper patient;
    private MedicalRecordWrapper medical_record;

    public Long getId() {
        return id;
    }

    public String getNgay_kham() {
        return ngay_kham;
    }

    public PatientWrapper getPatient() {
        return patient;
    }

    public MedicalRecordWrapper getMedical_record() {
        return medical_record;
    }

    public static class PatientWrapper {
        private Long id; // Matches bigint in PostgreSQL
        private String ho_ten;
        private String so_dien_thoai;
        private String dia_chi;

        public Long getId() {
            return id;
        }

        public String getHo_ten() {
            return ho_ten;
        }

        public String getSo_dien_thoai() {
            return so_dien_thoai;
        }

        public String getDia_chi() {
            return dia_chi;
        }
    }

    public static class MedicalRecordWrapper {
        private List<BillSummaryDto> bill;

        public List<BillSummaryDto> getBill() {
            return bill;
        }
    }

    public static class BillSummaryDto {
        private Long id;
        private String created_at;
        private String phuong_thuc_thanh_toan;
        private String trang_thai_thanh_toan;
        private Double tong_thanh_toan;

        public Long getId() {
            return id;
        }

        public String getCreated_at() {
            return created_at;
        }

        public String getPhuong_thuc_thanh_toan() {
            return phuong_thuc_thanh_toan;
        }

        public String getTrang_thai_thanh_toan() {
            return trang_thai_thanh_toan;
        }

        public Double getTong_thanh_toan() {
            return tong_thanh_toan;
        }
    }
}