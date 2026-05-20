package com.example.nhom08_quanlyphongkham.uilogin;

public class ThongBao
{
    private Integer id;

    private String tieu_de;
    private String noi_dung;
    private String vai_tro;

    public ThongBao(String tieu_de, String noi_dung)
    {
        this.tieu_de = tieu_de;
        this.noi_dung = noi_dung;
    }

    public ThongBao(String tieu_de, String noi_dung, String vai_tro)
    {
        this.tieu_de = tieu_de;
        this.noi_dung = noi_dung;
        this.vai_tro = vai_tro;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }
    public String getTieu_de()
    {
        return tieu_de;
    }

    public void setTieu_de(String tieu_de)
    {
        this.tieu_de = tieu_de;
    }

    public String getNoi_dung() {
        return noi_dung;
    }

    public void setNoi_dung(String noi_dung) {
        this.noi_dung = noi_dung;
    }

    public String getVai_tro() {
        return vai_tro;
    }
    public void setVai_tro(String vai_tro) {
        this.vai_tro = vai_tro;
    }
}
