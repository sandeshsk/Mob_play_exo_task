package com.example.mobiotic.network;

import com.example.mobiotic.BuildConfig;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiCaller {

    private static volatile ApiCaller instance;
    private final ServiceApi servicesApi;

    public static ApiCaller getInstance() {
        ApiCaller localInstance = instance;
        if (localInstance == null) {
            synchronized (ApiCaller.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ApiCaller();
                }
            }
        }
        return localInstance;
    }

    private ApiCaller() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(interceptor);
        }
        OkHttpClient client = clientBuilder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        servicesApi = retrofit.create(ServiceApi.class);
    }

    public ServiceApi getServicesApi() {
        return servicesApi;
    }
}

