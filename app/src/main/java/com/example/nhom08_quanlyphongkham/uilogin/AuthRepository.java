package com.example.nhom08_quanlyphongkham.uilogin;

import dashboard_fragment.change_password_request.UpdatePasswordRequest;
import retrofit2.Call;

public class AuthRepository {
    private final AuthApiService authApiService;
    private final String apiKey;

    public AuthRepository(String apiKey) {
        this.apiKey = apiKey;
        authApiService = SupabaseClientProvider
                .getClient()
                .create(AuthApiService.class);
    }

    public Call<LoginResponse> login(String email, String password) {
        return authApiService.login(apiKey, new LoginRequest(email, password));
    }

    public Call<LoginResponse> updatePassword(String accessToken, String newPassword) {
        return authApiService.updatePassword(
                apiKey,
                "Bearer " + accessToken,
                new UpdatePasswordRequest(newPassword)
        );
    }

    public Call<java.util.List<ThongBao>> layDanhSachThongBao() {
        return authApiService.layDanhSachThongBao(apiKey, "Bearer " + apiKey);
    }

    public Call<java.util.List<ThongBao>> themThongBao(String tieuDe, String noiDung) {
        ThongBao tb = new ThongBao(tieuDe, noiDung);
        return authApiService.themThongBao(apiKey, "Bearer " + apiKey, tb);
    }
    public Call<java.util.List<ThongBao>> layDanhSachThongBaoAdmin() {
        return authApiService.layDanhSachThongBaoAdmin(apiKey, "Bearer " + apiKey);
    }

    public Call<java.util.List<ThongBao>> themThongBaoAdmin(String tieuDe, String noiDung) {
        ThongBao tb = new ThongBao(tieuDe, noiDung);
        return authApiService.themThongBaoAdmin(apiKey, "Bearer " + apiKey, tb);
    }
    public Call<Void> xoaThongBaoAdmin(int id) {
        return authApiService.xoaThongBaoAdmin(apiKey, "Bearer " + apiKey, "eq." + id);
    }
    public Call<Void> xoaThongBao(int id) {
        return authApiService.xoaThongBao(apiKey, "Bearer " + apiKey, "eq." + id);
    }
}
