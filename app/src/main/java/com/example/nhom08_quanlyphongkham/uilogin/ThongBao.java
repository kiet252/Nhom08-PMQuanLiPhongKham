package com.example.nhom08_quanlyphongkham.uilogin;

public class ThongBao {
    // Dùng Integer (viết hoa chữ I) để nó không bị tự động gán bằng 0
    private Integer id;

    private String tieu_de;
    private String noi_dung;

    // Constructor để tạo thông báo mới (không cần truyền id)
    public ThongBao(String tieu_de, String noi_dung) {
        this.tieu_de = tieu_de;
        this.noi_dung = noi_dung;
    }

    // Getter và Setter cho id (Cái này sẽ sửa lỗi chữ đỏ tb.getId())
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Getter và Setter cho tieu_de
    public String getTieu_de() {
        return tieu_de;
    }

    public void setTieu_de(String tieu_de) {
        this.tieu_de = tieu_de;
    }

    // Getter và Setter cho noi_dung
    public String getNoi_dung() {
        return noi_dung;
    }

    public void setNoi_dung(String noi_dung) {
        this.noi_dung = noi_dung;
    }
}