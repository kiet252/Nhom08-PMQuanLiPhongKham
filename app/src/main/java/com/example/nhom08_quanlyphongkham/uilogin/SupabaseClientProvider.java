package com.example.nhom08_quanlyphongkham.uilogin;

import android.content.Context;
import com.example.nhom08_quanlyphongkham.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClientProvider {
    private static final String SUPABASE_URL = "https://waiuciilyysobnvcwshd.supabase.co/";
    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            // Interceptor để log (tiện debug)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        // TỰ ĐỘNG GẮN TOKEN VÀO MỌI REQUEST
                        Request original = chain.request();
                        String token = SharedPrefManager.getInstance(context).getToken();

                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Content-Type", "application/json");

                        if (token != null && !token.isEmpty()) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }

                        return chain.proceed(requestBuilder.build());
                    })
                    .authenticator((route, response) -> {
                        // TỰ ĐỘNG REFRESH KHI GẶP LỖI 401
                        if (response.code() == 401) {
                            String refreshToken = SharedPrefManager.getInstance(context).getRefreshToken();
                            if (refreshToken == null || refreshToken.isEmpty()) return null;

                            // Gọi đồng bộ (execute) để lấy token mới
                            AuthApiService authApi = new Retrofit.Builder()
                                    .baseUrl(SUPABASE_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()
                                    .create(AuthApiService.class);

                            retrofit2.Response<LoginResponse> refreshRes = authApi.refreshToken(
                                    context.getString(R.string.abAIkey),
                                    new RefreshTokenRequest(refreshToken)
                            ).execute();

                            if (refreshRes.isSuccessful() && refreshRes.body() != null) {
                                String newToken = refreshRes.body().getAccess_token();
                                String newRefresh = refreshRes.body().getRefresh_token();

                                // Lưu lại token mới
                                SharedPrefManager.getInstance(context).saveTokens(newToken, newRefresh);

                                // Thử lại request cũ với token mới
                                return response.request().newBuilder()
                                        .header("Authorization", "Bearer " + newToken)
                                        .build();
                            }
                        }
                        return null;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SUPABASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}