package com.example.nhom08_quanlyphongkham.uilogin;

public class VerifyOtpRequest {
    private String email;
    private String token;
    private String type;

    public VerifyOtpRequest(String email, String token, String type) {
        this.email = email;
        this.token = token;
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }
}