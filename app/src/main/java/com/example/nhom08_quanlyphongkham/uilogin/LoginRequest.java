package com.example.nhom08_quanlyphongkham.uilogin;
public class LoginRequest {
    private String email;
    private String password;
    private String chucvu;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getChuc_vu() {
        return chucvu;
    }

    public void setChuc_vu(String chuc_vu) {
        this.chucvu = chuc_vu;
    }
}
