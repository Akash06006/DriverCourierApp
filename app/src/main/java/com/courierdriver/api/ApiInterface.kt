package com.courierdriver.api

import com.courierdriver.model.order.CreateOrdersInput
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface ApiInterface {
    @get:GET("user-profile-detail/")
    val profileData : Call<JsonObject>

    @Multipart
    @POST("delivery/auth/update")
    fun callUpdateProfile(
        @PartMap mHashMap : HashMap<String,
                RequestBody>, @Part image : MultipartBody.Part?
    ) : Call<JsonObject>

    @Multipart
    @POST("delivery/auth/uploadDoc")
    fun callverifyDoc(
        @PartMap mHashMap : HashMap<String,
                RequestBody>, poaFront : MultipartBody.Part?, poaBack : MultipartBody.Part?, licenseFront : MultipartBody.Part?, licenseBack : MultipartBody.Part?, panCard : MultipartBody.Part?
    ) : Call<JsonObject>

    @GET("delivery/auth/profile")
    fun getProfile() : Call<JsonObject>

    @GET("mobile/auth/getRegion")
    fun getRegions() : Call<JsonObject>

    @GET("mobile/vehicle/getList")
    fun getLists() : Call<JsonObject>

    @POST("mobile/auth/login")
    fun callLogin(@Body jsonObject : JsonObject) : Call<JsonObject>
    @POST("delivery/auth/login")
    fun calldriverLogin(@Body jsonObject : JsonObject) : Call<JsonObject>

    @POST("mobile/auth/signup")
    fun callSignup(@Body jsonObject : JsonObject) : Call<JsonObject>

    @POST("delivery/auth/signup")
    fun calldriverSignup(@Body jsonObject : JsonObject) : Call<JsonObject>

    @POST("delivery/auth/verify")
    fun callVerifyUser(@Body jsonObject : JsonObject) : Call<JsonObject>

    @POST("delivery/auth/userByPhonenumber")
    fun callForgotPassword(@Body mJsonObject : JsonObject) : Call<JsonObject>//(@Query("countryCode") countryCode : String, @Query("phoneNumber") phoneNumber : String) : Call<JsonObject>

    @POST("verify-otp/")
    fun otpVerify(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("delivery/auth/forgotPassword")
    fun resetPassword(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @PUT("change-password/")
    fun changePassword(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("auth/logout/")
    fun callLogout(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("mobile/orders/priceCalc")
    fun calculatePrice(@Body jsonObject : JsonObject) : Call<JsonObject>

    @GET("delivery/orders/getCancelReasons")
    fun cancelReason() : Call<JsonObject>

    @POST("mobile/orders/create")
    fun createOrder(@Body jsonObject : CreateOrdersInput?) : Call<JsonObject>

    @GET("mobile/orders/transactionHistory")
    fun getTansactionHistory() : Call<JsonObject>

    @GET("mobile/orders/list")
    fun getOrderList(@Query("orderStatus") orderStatus : String?) : Call<JsonObject>

    @POST("mobile/orders/paymentStatus")
    fun updatePaymentSuccess(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("mobile/coupan/applyCoupan")
    fun applyCoupon(@Body jsonObject : JsonObject) : Call<JsonObject>

    @POST("mobile/coupan/removeCoupan")
    fun removeCoupon(@Body jsonObject : JsonObject) : Call<JsonObject>

    @POST("delivery/orders/cancelReq")
    fun cancelOrder(@Body jsonObject : JsonObject) : Call<JsonObject>

    @GET("https://maps.googleapis.com/maps/api/distancematrix/json")
    fun getDistanceInfo(
        @QueryMap parameters : HashMap<String?, String?>
    ) : Call<JsonObject>?

    @GET("delivery/orders/detail/{id}")
    fun orderDetail(@Path("id") slotId : String?) : Call<JsonObject>

    @GET("outlet-services/")
    fun getHomeList(@Query("page") page : String, @Query("limit") limit : String, @Query("companyId") companyId : String) : Call<JsonObject>

    @POST("outlet-group-services/")
    fun getClassesList(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("outlet-therapy-services/")
    fun getTherpyList(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("schedule-services-by-date/")
    fun getSlotByDate(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("up-coming-bookings/")
    fun getUpcomingBookings(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("service-booking-history/")
    fun getBookingHistory(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("service-booking-cancel-list/")
    fun getCancelledHistory(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("update-user-setting/")
    fun updateUserSetting(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @GET("booking-rule-list/")
    fun bookingRules() : Call<JsonObject>

    @GET("booking-detail/{slot_id}/")
    fun bookingDetail(@Path("slot_id") slotId : String) : Call<JsonObject>

    @PUT("cancel-schedule-service/{id}/")
    fun cancelSrviceSlot(@Path("id") id : String) : Call<JsonObject>

    @POST("book-schedule-service/")
    fun bookSrviceSlot(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @GET("employee-detail/{id}/")
    fun getEmployeeDetail(@Path("id") id : String) : Call<JsonObject>

    @POST("book-for-guest/")
    fun bookForGuest(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("outlet-group-services-by-date/")
    fun getClassesListByDate(@Body mJsonObject: JsonObject): Call<JsonObject>

    @GET("delivery/orders/list")
    fun orderList(
        @Query("orderStatus") orderStatus: String,
        @Query("driverLat") driverLat: String,
        @Query("driverLong") driverLong: String
    ): Call<JsonObject>

    @POST("delivery/orders/acceptOrder")
    fun acceptOrder(@Body mJsonObject: JsonObject): Call<JsonObject>

    @POST("delivery/orders/orderPickup")
    fun pickupOrder(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("delivery/orders/markComplete")
    fun completeOrder(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @POST("delivery/orders/cancelReq")
    fun cancelRequests(@Body mJsonObject : JsonObject) : Call<JsonObject>

    @GET("delivery/orders/cancelReasons")
    fun cancelReasons(): Call<JsonObject>
}