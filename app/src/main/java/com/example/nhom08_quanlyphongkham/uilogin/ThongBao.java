package com.example.nhom08_quanlyphongkham.uilogin;

import com.example.nhom08_quanlyphongkham.UserProfile;

public class ThongBao
{
    private Integer id;
    private String tieu_de;
    private String noi_dung;

    public ThongBao(String tieu_de, String noi_dung)
    {
        this.tieu_de = tieu_de;
        this.noi_dung = noi_dung;
    }

    public String getTieu_de()
    {
        return tieu_de;
    }

    public String getNoi_dung()
    {
        return noi_dung;
    }
    public int getId() { return id; }
}