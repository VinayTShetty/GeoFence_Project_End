package com.succorfish.geofence.interfaces;

import com.succorfish.geofence.customObjects.LoginData;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface API {
    @GET("device/get-token/{id}")    //asset/getForAccountWithoutDevice/{id}    // device/get-token/{id}
    Call<String> getDeviceToken(@Path("id") String id);
    @GET("user/getOwn")
    Call<LoginData> userLoginAPI();
    @POST("asset/search")
    Call<String> getAllAssetList(@Query("view") String param1, @Body RequestBody params);
    @GET("waypoint/getLatest/{id}")
    Call<String> getLatLongOfAsset(@Path("id") String id);

}
