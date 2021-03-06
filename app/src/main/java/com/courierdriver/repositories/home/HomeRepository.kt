package com.example.services.repositories.home

import androidx.lifecycle.MutableLiveData
import com.courierdriver.R
import com.courierdriver.api.ApiClient
import com.courierdriver.api.ApiResponse
import com.courierdriver.api.ApiService
import com.courierdriver.application.MyApplication
import com.courierdriver.common.UtilsFunctions
import com.courierdriver.model.CancelReasonModel
import com.courierdriver.model.CommonModel
import com.courierdriver.model.order.OrderListModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import retrofit2.Response

class HomeRepository {
    private var data1: MutableLiveData<CommonModel>? = MutableLiveData()
    private val gson = GsonBuilder().serializeNulls().create()

    fun getOrderList(
        orderStatus: String?,
        driverLat: String?,
        driverLong: String?,
        orderList: MutableLiveData<OrderListModel>?
    ): MutableLiveData<OrderListModel> {
        if (UtilsFunctions.isNetworkConnected() && orderStatus != null) {
            val mApiService = ApiService<JsonObject>()
            mApiService.get(
                object : ApiResponse<JsonObject> {
                    override fun onResponse(mResponse: Response<JsonObject>) {
                        val data = gson.fromJson<OrderListModel>(
                            "" + mResponse.body()!!,
                            OrderListModel::class.java
                        )
                        orderList!!.postValue(data)
                    }

                    override fun onError(mKey: String) {
                        orderList!!.value = null
                        UtilsFunctions.showToastError(MyApplication.instance.getString(R.string.internal_server_error))
                    }
                }, ApiClient.getApiInterface().orderList(orderStatus!!, driverLat!!, driverLong!!)
            )
        }
        return orderList!!
    }

    fun acceptOrder(
        jsonObject: JsonObject?,
        acceptOrderData: MutableLiveData<CommonModel>?
    ): MutableLiveData<CommonModel> {
        if (UtilsFunctions.isNetworkConnected() && jsonObject != null) {
            val mApiService = ApiService<JsonObject>()
            mApiService.get(
                object : ApiResponse<JsonObject> {
                    override fun onResponse(mResponse: Response<JsonObject>) {
                        val data = gson.fromJson<CommonModel>(
                            "" + mResponse.body()!!,
                            CommonModel::class.java
                        )
                        acceptOrderData!!.postValue(data)
                    }

                    override fun onError(mKey: String) {
                        acceptOrderData!!.value = null
                        UtilsFunctions.showToastError(MyApplication.instance.getString(R.string.internal_server_error))
                    }
                }, ApiClient.getApiInterface().acceptOrder(jsonObject)
            )
        }
        return acceptOrderData!!
    }

    fun cancelOrder(
        jsonObject: JsonObject?,
        cancelOrderData: MutableLiveData<CommonModel>?
    ): MutableLiveData<CommonModel> {
        if (UtilsFunctions.isNetworkConnected() && jsonObject != null) {
            val mApiService = ApiService<JsonObject>()
            mApiService.get(
                object : ApiResponse<JsonObject> {
                    override fun onResponse(mResponse: Response<JsonObject>) {
                        val data = gson.fromJson<CommonModel>(
                            "" + mResponse.body()!!,
                            CommonModel::class.java
                        )
                        cancelOrderData!!.postValue(data)
                    }

                    override fun onError(mKey: String) {
                        cancelOrderData!!.value = null
                        UtilsFunctions.showToastError(MyApplication.instance.getString(R.string.internal_server_error))
                    }
                }, ApiClient.getApiInterface().cancelRequests(jsonObject)
            )
        }
        return cancelOrderData!!
    }

    fun cancellationReason(cancellationReasonData: MutableLiveData<CancelReasonModel>?): MutableLiveData<CancelReasonModel> {
        if (UtilsFunctions.isNetworkConnected()) {
            val mApiService = ApiService<JsonObject>()
            mApiService.get(
                object : ApiResponse<JsonObject> {
                    override fun onResponse(mResponse: Response<JsonObject>) {
                        val data = gson.fromJson<CancelReasonModel>(
                            "" + mResponse.body()!!,
                            CancelReasonModel::class.java
                        )
                        cancellationReasonData!!.postValue(data)
                    }

                    override fun onError(mKey: String) {
                        cancellationReasonData!!.value = null
                        UtilsFunctions.showToastError(MyApplication.instance.getString(R.string.internal_server_error))
                    }
                }, ApiClient.getApiInterface().cancelReasons()
            )
        }
        return cancellationReasonData!!
    }
}