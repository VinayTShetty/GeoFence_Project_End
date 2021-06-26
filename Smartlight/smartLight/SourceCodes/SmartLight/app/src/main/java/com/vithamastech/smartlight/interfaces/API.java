package com.vithamastech.smartlight.interfaces;


import com.vithamastech.smartlight.Vo.VoAddDeviceData;
import com.vithamastech.smartlight.Vo.VoAddGroupData;
import com.vithamastech.smartlight.Vo.VoDeviceTypeList;
import com.vithamastech.smartlight.Vo.YoutubeVideosRespData;
import com.vithamastech.smartlight.Vo.VoLoginData;
import com.vithamastech.smartlight.Vo.VoLogout;
import com.vithamastech.smartlight.Vo.VoServerDeviceList;
import com.vithamastech.smartlight.Vo.VoServerGroupList;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Jaydeep on 27-02-2017.
 */

public interface API {

    /* Retrofit API Call Methods */
    @FormUrlEncoded
    @POST("login")
    Call<VoLoginData> userLoginAPI(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("signup")
    Call<VoLoginData> userRegisterAPI(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("check_mobile_number")
    Call<VoLoginData> checkUserAlreadyRegistered(@FieldMap Map<String, String> mHashMap);

//    @FormUrlEncoded
//    @POST("change_password")
//    Call<VoLogout> changeUserPassword(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("check_user_details")
    Call<VoLogout> authenticateUserCheck(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("set_reset_password")
    Call<VoLogout> userChangePasswordAPI(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("logout")
    Call<VoLogout> userLogoutAPI(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("save_device")
    Call<VoAddDeviceData> addDeviceAPI(@FieldMap Map<String, String> mHashMap);

//    @FormUrlEncoded
//    @POST("update_device")
//    Call<VoAddDeviceData> updateDeviceAPI(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("get_all_device")
    Call<VoServerDeviceList> getAllDeviceListAPI(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("get_device_type")
    Call<VoDeviceTypeList> getDeviceTypeListAPI(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("delete_everything")
    Call<VoLogout> resetAllDeviceAPI(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("save_group")
        // there are two method of save group
    Call<VoAddGroupData> addGroupAPI(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("save_group")
    Call<String> addGroupAPIWithStringResponse(@FieldMap Map<String, String> mHashMap);

    @FormUrlEncoded
    @POST("get_all_group")
    Call<VoServerGroupList> getAllGroupListAPI(@FieldMap Map<String, String> mHashMap);

    @GET("smartlight/api/video")
    Call<YoutubeVideosRespData> getYouTubeVideos();
}