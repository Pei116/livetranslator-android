package com.wwk.livetranslator.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wwk.livetranslator.Application;
import com.wwk.livetranslator.BuildConfig;
import com.wwk.livetranslator.R;
import com.wwk.livetranslator.helper.BuildInfo;
import com.wwk.livetranslator.manager.InstallationManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wwk on 11/20/17.
 * Copyright © 2017 WYFI, Inc. All rights reserved
 */

public class APIClient {

    static public OkHttpClient.Builder buildOkHttpClient(boolean useGoogle) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (!BuildInfo.isProduction()) {
            // Logging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(loggingInterceptor);

            // SSL
            if (!useGoogle) {
                try {
                    setSSLConfig(builder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // For debug only
//            httpClientBuilder.hostnameVerifier((s, sslSession) -> true);
        }
        builder.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder();

            requestBuilder.addHeader("Accept", "application/json");
            requestBuilder.addHeader("X-Client-OS", BuildInfo.osVersion());
            requestBuilder.addHeader("X-Client-Build", BuildInfo.buildVersion());
            requestBuilder.addHeader("X-Client-AppName", "WYFI Connect");
            requestBuilder.addHeader("X-Client-DeviceId", InstallationManager.getInstance().getDeviceId());

            return chain.proceed(requestBuilder.build());
        });

        return builder;
    }

    static public Retrofit getClient(boolean useGoogle) {

        OkHttpClient.Builder httpClientBuilder = buildOkHttpClient(useGoogle);
        httpClientBuilder.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder();

            return chain.proceed(requestBuilder.build());
        });

        return new Retrofit.Builder()
                .addConverterFactory(buildGsonConverter())
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(httpClientBuilder.build())
                .build();
    }

    private static void setSSLConfig(OkHttpClient.Builder clientBuilder) throws CertificateException, IOException,
            KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        // Loading CAs from an InputStream
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        Certificate ca;
        try (InputStream caInput = Application.getInstance().getResources().openRawResource(R.raw.ca)) {
            ca = cf.generateCertificate(caInput);
        }

        // Creating a KeyStore containing our trusted CAs
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Creating a TrustManager that trusts the CAs in our KeyStore.
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        TrustManager[] trustManagers = tmf.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

        // Creating an SSLSocketFactory that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { trustManager }, null);

        clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
    }


    private static GsonConverterFactory buildGsonConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        // Adding custom deserializers
        Gson gson = gsonBuilder.create();

        return GsonConverterFactory.create(gson);
    }

}
