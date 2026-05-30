package com.example.nhom08_quanlyphongkham.admin_reports;

public class ReportItem {
    private String id;
    private String ngay_kham;
    private PatientInfo patient_name; 
    private MedicalRecordInfo medical_record;
    private String trang_thai;
    private long phi_kham;

    public static class PatientInfo {
        private String id;
        private String cccd;
        private String ho_ten;
        
        public String getId() { return id; }
        public String getCccd() { return cccd; }
        public String getHo_ten() { return ho_ten; }
    }

    public static class MedicalRecordInfo {
        private BillInfo bill;

        public BillInfo getBill() { return bill; }
    }

    public static class BillInfo {
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

    public String getId() { 
        return id != null ? id : ""; 
    }

    public String getNgay_kham() { 
        return ngay_kham != null ? ngay_kham : ""; 
    }
    
    public String getPatientName() { 
        return (patient_name != null && patient_name.getHo_ten() != null) ? patient_name.getHo_ten() : "Không rõ"; 
    }

    public String getPatientId() {
        return (patient_name != null && patient_name.getId() != null) ? patient_name.getId() : "";
    }

    public String getPatientCccd() {
        return (patient_name != null && patient_name.getCccd() != null) ? patient_name.getCccd() : "";
    }
    
    public String getTrang_thai() { 
        return trang_thai != null ? trang_thai : ""; 
    }

    public BillInfo getBillInfo() {
        return medical_record != null ? medical_record.getBill() : null;
    }

    public long getPhi_kham() { 
        return phi_kham; 
    }

    public double getTongThanhToan() {
        if (medical_record == null
                || medical_record.getBill() == null
                || medical_record.getBill().getTong_thanh_toan() == null) {
            return 0;
        }
        return medical_record.getBill().getTong_thanh_toan();
    }

    public double getTongDoanhThu() {
        return phi_kham + getTongThanhToan();
    }
}
