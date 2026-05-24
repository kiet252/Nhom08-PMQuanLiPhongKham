package com.example.nhom08_quanlyphongkham.uilogin;

public class RecoverPasswordRequest {
    private String email;

    public RecoverPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}