package com.example.nhom08_quanlyphongkham.uilogin;

import android.content.Context;
import com.example.nhom08_quanlyphongkham.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClientProvider {
    public static final String SUPABASE_URL = "https://waiuciilyysobnvcwshd.supabase.co/";
    public static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = SharedPrefManager.getInstance(context).getToken();

                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Content-Type", "application/json")
                                .header("apikey", SUPABASE_ANON_KEY);

                        if (token != null && !token.isEmpty() && !original.url().toString().contains("auth/v1/token")) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }

                        return chain.proceed(requestBuilder.build());
                    })
                    .authenticator((route, response) -> {
                        if (response.code() == 401) {
                            String refreshToken = SharedPrefManager.getInstance(context).getRefreshToken();
                            if (refreshToken == null || refreshToken.isEmpty()) return null;

                            AuthApiService authApi = new Retrofit.Builder()
                                    .baseUrl(SUPABASE_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()
                                    .create(AuthApiService.class);

                            retrofit2.Response<LoginResponse> refreshRes = authApi.refreshToken(
                                    SUPABASE_ANON_KEY,
                                    new RefreshTokenRequest(refreshToken)
                            ).execute();

                            if (refreshRes.isSuccessful() && refreshRes.body() != null) {
                                String newToken = refreshRes.body().getAccess_token();
                                String newRefresh = refreshRes.body().getRefresh_token();

                                SharedPrefManager.getInstance(context).saveTokens(newToken, newRefresh);

                                return response.request().newBuilder()
                                        .header("Authorization", "Bearer " + newToken)
                                        .header("apikey", SUPABASE_ANON_KEY)
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
