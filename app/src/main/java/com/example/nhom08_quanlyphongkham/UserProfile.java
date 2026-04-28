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
    public UserProfile() {
    }

    public UserProfile(String id, String email, String user_name, String ho_ten, Date ngay_sinh, String so_dien_thoai, String dia_chi, String gioitinh, String chuc_vu) {
        this.id = id;
        this.email = email;
        this.user_name = user_name;
        this.ho_ten = ho_ten;
        this.ngay_sinh = ngay_sinh;
        this.so_dien_thoai = so_dien_thoai;
        this.dia_chi = dia_chi;
        this.gioitinh = gioitinh;
        this.chuc_vu = chuc_vu;
    }

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