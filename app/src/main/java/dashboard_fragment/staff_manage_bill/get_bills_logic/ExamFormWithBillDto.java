package dashboard_fragment.staff_manage_bill.get_bills_logic;

import java.io.Serializable;
import java.util.List;

public class ExamFormWithBillDto implements Serializable {
    private Long id;
    private String ngay_kham;
    private PatientWrapper patient;
    private MedicalRecordWrapper medical_record;

    public Long getId() { return id; }
    public String getNgay_kham() { return ngay_kham; }
    public PatientWrapper getPatient() { return patient; }
    public MedicalRecordWrapper getMedical_record() { return medical_record; }

    public static class PatientWrapper implements Serializable {
        private Long id;
        private String ho_ten;
        private String so_dien_thoai;
        private String dia_chi;

        public Long getId() { return id; }
        public String getHo_ten() { return ho_ten; }
        public String getSo_dien_thoai() { return so_dien_thoai; }
        public String getDia_chi() { return dia_chi; }

        public void setId(Long id) { this.id = id; }
        public void setHo_ten(String ho_ten) { this.ho_ten = ho_ten; }
        public void setSo_dien_thoai(String so_dien_thoai) { this.so_dien_thoai = so_dien_thoai; }
        public void setDia_chi(String dia_chi) { this.dia_chi = dia_chi; }
    }

    public static class MedicalRecordWrapper implements Serializable {
        private BillSummaryDto bill;
        private List<MedicalRecordClinicalDto> medical_record_clinical;
        private List<MedicalRecordMedicineDto> medical_record_medicine;

        public BillSummaryDto getBill() { return bill; }
        public List<MedicalRecordClinicalDto> getMedical_record_clinical() { return medical_record_clinical; }
        public List<MedicalRecordMedicineDto> getMedical_record_medicine() { return medical_record_medicine; }
    }

    public static class BillSummaryDto implements Serializable {
        private Long id;
        private String created_at;
        private String phuong_thuc_thanh_toan;
        private String trang_thai_thanh_toan;
        private Double tong_thanh_toan;

        public Long getId() { return id; }
        public String getCreated_at() { return created_at; }
        public String getPhuong_thuc_thanh_toan() { return phuong_thuc_thanh_toan; }
        public String getTrang_thai_thanh_toan() { return trang_thai_thanh_toan; }
        public Double getTong_thanh_toan() { return tong_thanh_toan; }
    }
    public static class MedicalRecordClinicalDto implements Serializable {
        private Long id;
        private ClinicalDetails clinical;

        public Long getId() { return id; }
        public ClinicalDetails getClinical() { return clinical; }

        public static class ClinicalDetails implements Serializable {
            private Long id;
            private String ten_dich_vu;
            private Double don_gia; // Fixed to match your 'don_gia' numeric column

            public Long getId() { return id; }
            public String getTen_dich_vu() { return ten_dich_vu; }
            public Double getDon_gia() { return don_gia; }
        }
    }

    public static class MedicalRecordMedicineDto implements Serializable {
        private Long id;
        private Long so_luong;
        private MedicineDetails medicine;

        public Long getId() { return id; }
        public Long getSo_luong() { return so_luong; }
        public MedicineDetails getMedicine() { return medicine; }

        public static class MedicineDetails implements Serializable {
            private Long id;
            private String ten_thuoc;
            private String don_vi;
            private Double don_gia; // Fixed to match your 'don_gia' numeric column

            public Long getId() { return id; }
            public String getTen_thuoc() { return ten_thuoc; }
            public String getDon_vi() { return don_vi; }
            public Double getDon_gia() { return don_gia; }
        }
    }
}
