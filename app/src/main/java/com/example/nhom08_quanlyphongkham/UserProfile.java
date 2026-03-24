package com.example.nhom08_quanlyphongkham;

import java.io.Serializable;
import java.util.Date;

public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    private String email;
    private String user_name;
    private String ho_ten;
    private Date ngay_sinh;
    private String so_dien_thoai;
    private String dia_chi;
    private String gioitinh;
    private String chuc_vu;
    private String id;
    public String getEmail() {
        return email;
    }
    public String getID() {
        return id;
    }
    public String getUser_name() {
        return user_name;
    }
    public String getHo_ten() {
        return ho_ten;
    }
    public Date getNgay_sinh(){
        return ngay_sinh;
    }
    public String getSo_dien_thoai() {
        return so_dien_thoai;
    }
    public String getDia_chi() {
        return dia_chi;
    }
    public String getGioitinh() {
        return gioitinh;
    }
    public String getChuc_vu() {
        return chuc_vu;
    }
}