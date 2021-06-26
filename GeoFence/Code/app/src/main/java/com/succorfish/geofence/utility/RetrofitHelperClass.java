package com.succorfish.geofence.utility;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RetrofitHelperClass {
    public static OkHttpClient getClientWithAutho(String username,String password) {
//        final String encoding = Base64.encodeToString((mPreferenceHelper.getUserName() + ":" + mPreferenceHelper.getUserPassword()).getBytes(), Base64.DEFAULT);
//        System.out.println("encoding-"+encoding);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.connectTimeout(1, TimeUnit.MINUTES);
        httpClient.readTimeout(2, TimeUnit.MINUTES);
        httpClient.writeTimeout(5, TimeUnit.MINUTES);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                String authToken = Credentials.basic(username, password);
//                String credentials = mPreferenceHelper.getUserName() + ":" + mPreferenceHelper.getUserPassword();
//                final String basic =
//                        "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
                System.out.println("encoding basic-" + authToken);
                Request request = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Authorization", authToken)
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }

        });

        OkHttpClient client = httpClient.build();
        return client;
    }

    public static boolean haveInternet(Activity activity) {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            return true;
        }
        return true;
    }

    public static OkHttpClient getSimpleClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .build();
        return client;
    }


}
