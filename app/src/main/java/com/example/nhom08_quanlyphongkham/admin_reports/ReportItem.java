package com.example.nhom08_quanlyphongkham.admin_reports;

public class ReportItem {
    private String id;
    private String ngay_kham;
    private PatientInfo patient_name; 
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

    public long getPhi_kham() { 
        return phi_kham; 
    }
}
