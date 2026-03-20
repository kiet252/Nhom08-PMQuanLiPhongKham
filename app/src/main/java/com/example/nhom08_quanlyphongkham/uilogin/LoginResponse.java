package com.example.nhom08_quanlyphongkham.uilogin;
public class LoginResponse {
    private String access_token;
    private String refresh_token;
    private String token_type;

    public String getAccess_token() {
        return access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public static class User {
        private String id;
        private String email;

        public String getId() { return id; }
        public String getEmail() { return email; }
    }
}
