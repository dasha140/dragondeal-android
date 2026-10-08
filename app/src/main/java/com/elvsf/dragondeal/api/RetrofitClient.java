package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Legacy Retrofit holder — kept for backward compatibility.
 * New code should use Koin injection instead.
 *
 * When used directly (without Koin), creates its own OkHttpClient
 * with a no-op AuthInterceptor (no auth tokens will be added).
 */
public class RetrofitClient {

    private static Retrofit retrofit;

    private RetrofitClient() {
    }

    /**
     * @deprecated Use Koin injection instead: get(Retrofit.class)
     * Kept only for transitional compatibility.
     */
    @Deprecated
    public static Retrofit getInstance(android.content.Context context) {
        return getRetrofit();
    }

    /**
     * Returns the shared Retrofit instance.
     * When called via Koin module, the Koin-provided instance is returned.
     * When called directly (deprecated), lazily creates one.
     */
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(loggingInterceptor)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }

        return retrofit;
    }
}
