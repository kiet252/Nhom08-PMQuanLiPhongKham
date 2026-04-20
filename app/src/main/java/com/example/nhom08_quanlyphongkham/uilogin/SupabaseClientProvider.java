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
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = SharedPrefManager.getInstance(context).getToken();

                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Content-Type", "application/json")
                                // ADD THIS: Now you don't need apikey in your Interface methods!
                                .header("apikey", context.getString(R.string.abAIkey));

                        // Only add Bearer token if it exists and we aren't already on an auth path
                        if (token != null && !token.isEmpty() && !original.url().toString().contains("auth/v1/token")) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }

                        return chain.proceed(requestBuilder.build());
                    })
                    .authenticator((route, response) -> {
                        if (response.code() == 401) {
                            String refreshToken = SharedPrefManager.getInstance(context).getRefreshToken();
                            if (refreshToken == null || refreshToken.isEmpty()) return null;

                            // Internal AuthApi for refreshing (No interceptor to avoid loops)
                            AuthApiService authApi = new Retrofit.Builder()
                                    .baseUrl(SUPABASE_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()
                                    .create(AuthApiService.class);

                            // We pass apikey manually here because this internal Retrofit has no interceptor
                            retrofit2.Response<LoginResponse> refreshRes = authApi.refreshToken(
                                    context.getString(R.string.abAIkey),
                                    new RefreshTokenRequest(refreshToken)
                            ).execute();

                            if (refreshRes.isSuccessful() && refreshRes.body() != null) {
                                String newToken = refreshRes.body().getAccess_token();
                                String newRefresh = refreshRes.body().getRefresh_token();

                                SharedPrefManager.getInstance(context).saveTokens(newToken, newRefresh);

                                return response.request().newBuilder()
                                        .header("Authorization", "Bearer " + newToken)
                                        .header("apikey", context.getString(R.string.abAIkey))
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