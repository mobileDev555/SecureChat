package com.realapps.chat.data.network;


import com.realapps.chat.BuildConfig;


public final class  ApiEndPoints {

    public static final String checkLicence = BuildConfig.BASE_URL + "check_licence";
    public static final String update_app_info = BuildConfig.BASE_URL + "update_app_version";
    public static final String add_user_details = BuildConfig.BASE_URL + "add_user_details";
    public static final String EccUpdate = BuildConfig.BASE_URL + "Add_Update_ECCKey";
    public static final String get_user_details = BuildConfig.BASE_URL + "get_user_details";
    public static final String create_group = BuildConfig.BASE_URL + "create_group";
    public static final String URL_FETCH_ECC_KEYS = BuildConfig.BASE_URL + "key_fetch";
    public static final String URL_FETCH_GROUP_ECC_KEYS = BuildConfig.BASE_URL + "get_ecc_key_using_ecc_id_array";
    public static final String URL_UPLOADING_MULTIMEDIA_SINGLE = BuildConfig.BASE_URL + "save_message";
    public static final String URL_UPLOADING_MULTIMEDIA_GROUP = BuildConfig.BASE_URL + "save_group_message";
    public static final String END_POINT_REMOVE_GROUP_MEMBER = BuildConfig.BASE_URL + "delete_group_member";
    public static final String END_POINT_ADD_GROUP_MEMBER = BuildConfig.BASE_URL + "add_group_member";
    public static final String END_POINT_GET_GROUP_DETAIL = BuildConfig.BASE_URL + "group_details";
    public static final String END_POINT_UPDATE_USER_DETAIL = BuildConfig.BASE_URL + "add_user_details";
    public static final String END_POINT_UPDATE_GROUP_NAME = BuildConfig.BASE_URL + "update_group_name";
    public static final String END_POINT_GET_GROUP_DETAILS = BuildConfig.BASE_URL + "update_group_name";
    public static final String END_POINT_GET_USER_STATUS = BuildConfig.BASE_URL + "get_user_details_by_id";
    public static final String check_licence_call = BuildConfig.BASE_URL + "check_licence_call";

    public static final String add_user_details_call = BuildConfig.BASE_URL + "add_user_details_call";
    public static final String get_user_details_call = BuildConfig.BASE_URL + "get_user_details_call";
    public static final String END_POINT_GET_CALL_LOGIN_TOKEN = BuildConfig.BASE_IP + "api.php";
    // public static final String END_POINT_GET_CALL_LOGIN_TOKEN =  " http://api.realapps.ro/api.php:8711" ;

}
