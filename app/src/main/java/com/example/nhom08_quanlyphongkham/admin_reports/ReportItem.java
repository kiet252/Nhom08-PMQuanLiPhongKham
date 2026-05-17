package com.example.nhom08_quanlyphongkham.admin_reports;

public class ReportItem {
    private String id;
    private String ngay_kham;
    private PatientInfo patient_name; // Khớp với alias trong query select
    private String trang_thai;
    private long phi_kham;

    public static class PatientInfo {
        private String ho_ten;
        public String getHo_ten() { return ho_ten; }
    }

    // Các hàm Getter theo chuẩn snake_case để khớp với ReportsActivity_Admin
    public String getId() { 
        return id != null ? id : ""; 
    }

    public String getNgay_kham() { 
        return ngay_kham != null ? ngay_kham : ""; 
    }
    
    public String getPatientName() { 
        return (patient_name != null && patient_name.getHo_ten() != null) ? patient_name.getHo_ten() : "Không rõ"; 
    }
    
    public String getTrang_thai() { 
        return trang_thai != null ? trang_thai : ""; 
    }

    public long getPhi_kham() { 
        return phi_kham; 
    }
}
