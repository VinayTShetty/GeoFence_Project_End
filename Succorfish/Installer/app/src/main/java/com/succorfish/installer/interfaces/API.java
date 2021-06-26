package com.succorfish.installer.interfaces;


import com.succorfish.installer.Vo.VoGetDeviceInfo;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.Vo.VoLoginData;
import com.succorfish.installer.Vo.VoReportResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Jaydeep on 27-02-2017.
 * API
 */

public interface API {

    // SUCCORFISH SERVER

    @GET("user/getOwn")
    Call<VoLoginData> userLoginAPI();

    @GET("device/getForImei/{imeiNo}")
    Call<VoGetDeviceInfo> getDeviceInfoFromImei(@Path("imeiNo") String imeiNo);

    @GET("waypoint/getLatest/{id}")
    Call<String> getTestDetailsAPI(@Path("id") String id);

    @GET("asset/getForAccountWithoutDevice/{id}")
    Call<String> getAllAssetList(@Path("id") String id, @Header("ManagedAccountId") String ManagedAccountId);

    @GET("asset/getForAccountWithDevice/{id}")
    Call<String> getAllAssetListWithAccount(@Path("id") String id, @Header("ManagedAccountId") String ManagedAccountId);

    @POST("device/install/{id}")
    Call<String> saveInstallationData(@Path("id") String id, @Body RequestBody params);

    @POST("device/uninstall/{id}")
    Call<String> saveUnInstallationData(@Path("id") String id, @Body RequestBody body);

    @POST("device/inspect/{id}")
    Call<String> saveInspectionData(@Path("id") String id, @Body RequestBody params);

    @Multipart
    @POST("device-history/uploadBinaryFile/{id}")
    Call<Void> saveInstallationPhotoData(@Path("id") String id, @Part MultipartBody.Part file);

    @GET("device-history/getInstalledFor/{id}")
    Call<String> getAllInstallationHistory(@Path("id") String id);

    @GET("device-history/getUninstalledFor/{id}")
    Call<String> getAllUnInstallationHistory(@Path("id") String id);

    @GET("device-history/getInspectedFor/{id}")
    Call<String> getAllInspectionHistory(@Path("id") String id);

    @GET("device-history/getLatestInstallFor/{id}")
    Call<VoInstallationResponse> getDeviceLatestInstallAPI(@Path("id") String id);

    @POST("device-history/complete/{id}")
    Call<VoInstallationResponse> completeInstallationHistory(@Path("id") String id);

    @GET("file/getForId/{id}")
    Call<VoReportResponse> getReportData(@Path("id") String id, @Query("origin") String zipCode);

}