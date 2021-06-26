package com.succorfish.fisherman.interfaces;


import com.succorfish.fisherman.Vo.VoLoginData;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Jaydeep on 27-02-2017.
 */

public interface API {

    // SUCCORFISH SERVER

    @GET("user/getOwn")
    Call<VoLoginData> userLoginAPI();

    @GET("waypoint/getLatest/{id}")
    Call<String> getTestDetailsAPI(@Path("id") String id);

    @POST("asset/search")
    Call<String> getAllAssetList(@Query("view") String param1, @Body RequestBody params);

    @GET("event/getLatest/{type}/{id}")
    Call<String> getEventServices(@Path("type") String eventType, @Path("id") String id);

}