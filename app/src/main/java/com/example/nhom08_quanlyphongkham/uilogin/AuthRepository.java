package com.example.nhom08_quanlyphongkham.uilogin;

import android.content.Context;

import com.example.nhom08_quanlyphongkham.R;

import dashboard_fragment.account_change_password_request.UpdatePasswordRequest;
import retrofit2.Call;

public class AuthRepository {
    private final AuthApiService authApiService;
    private final Context context;
    private final String apiKey;

    public AuthRepository(Context context) {
        this.context = context;
        this.apiKey = context.getString(R.string.abAIkey);
        this.authApiService = SupabaseClientProvider
                .getClient(context)
                .create(AuthApiService.class);
    }

    public Call<LoginResponse> login(String email, String password) {
        return authApiService.login(new LoginRequest(email, password));
    }

    public Call<LoginResponse> refreshToken(String refreshToken) {
        return authApiService.refreshToken(
                context.getString(R.string.abAIkey),
                new RefreshTokenRequest(refreshToken)
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
    public Call<LoginResponse> updatePassword(String newPassword) {
        return authApiService.updatePassword(new UpdatePasswordRequest(newPassword));
    }
}
