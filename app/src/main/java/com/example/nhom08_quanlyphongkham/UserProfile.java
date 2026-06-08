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

    private String anh_dai_dien;
    private String trang_thai_hoat_dong;
    private String face_id;
    private String android_id;

    private Date created_at;
    private String id;
    public UserProfile() {
    }

    public UserProfile(String id, String email, String user_name, String ho_ten, Date ngay_sinh, String so_dien_thoai, String dia_chi, String gioitinh, String chuc_vu, String anh_dai_dien, String trang_thai_hoat_dong) {
        this.id = id;
        this.email = email;
        this.user_name = user_name;
        this.ho_ten = ho_ten;
        this.ngay_sinh = ngay_sinh;
        this.so_dien_thoai = so_dien_thoai;
        this.dia_chi = dia_chi;
        this.gioitinh = gioitinh;
        this.chuc_vu = chuc_vu;
        this.anh_dai_dien = anh_dai_dien;
        this.trang_thai_hoat_dong = trang_thai_hoat_dong;

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
    public String getAnh_dai_dien() {return anh_dai_dien; }
    public Date getCreated_at() {return created_at; }
    public String getFace_id() { return face_id; }
    public String getAndroid_id() { return android_id; }

    public String getTrang_thai_hoat_dong() {
        return trang_thai_hoat_dong;
    }
}