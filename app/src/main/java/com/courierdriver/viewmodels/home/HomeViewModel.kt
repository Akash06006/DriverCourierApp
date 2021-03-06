package com.courierdriver.viewmodels.home

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.courierdriver.common.UtilsFunctions
import com.courierdriver.model.CancelReasonModel
import com.courierdriver.model.CommonModel
import com.courierdriver.model.order.OrderListModel
import com.courierdriver.viewmodels.BaseViewModel
import com.example.services.repositories.home.HomeRepository
import com.google.gson.JsonObject

class HomeViewModel : BaseViewModel() {
    private val mIsUpdating = MutableLiveData<Boolean>()
    private val isClick = MutableLiveData<String>()
    private var homeRepository = HomeRepository()
    private var getOrderList: MutableLiveData<OrderListModel>? = MutableLiveData()
    private var acceptOrderList: MutableLiveData<CommonModel>? = MutableLiveData()
    private var cancelOrderList: MutableLiveData<CommonModel>? = MutableLiveData()
    private var cancelReasonList: MutableLiveData<CancelReasonModel>? = MutableLiveData()

    init {
        getOrderList = homeRepository.getOrderList(null, null, null, getOrderList)
        acceptOrderList = homeRepository.acceptOrder(null, acceptOrderList)
        cancelOrderList = homeRepository.cancelOrder(null, cancelOrderList)
        cancelReasonList = homeRepository.cancellationReason(cancelReasonList)
    }

    fun orderList(orderStatus: String, driverLat: String, driverLong: String) {
        if (UtilsFunctions.isNetworkConnected()) {
            getOrderList =
                homeRepository.getOrderList(orderStatus, driverLat, driverLong, getOrderList)
            mIsUpdating.postValue(true)
        }
    }

    fun getOrderListData(): LiveData<OrderListModel> {
        return getOrderList!!
    }

    fun acceptOrder(id: String) {
        if (UtilsFunctions.isNetworkConnected()) {
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", id)
            acceptOrderList = homeRepository.acceptOrder(jsonObject, acceptOrderList)
            mIsUpdating.postValue(true)
        }
    }

    fun acceptOrderData(): LiveData<CommonModel> {
        return acceptOrderList!!
    }

    fun cancelOrder(
        id: String,
        cancellationReason: String,
        otherReason: String
    ) {
        if (UtilsFunctions.isNetworkConnected()) {
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", id)
            jsonObject.addProperty("cancellationReason", cancellationReason)
            jsonObject.addProperty("otherReason", otherReason)
            cancelOrderList = homeRepository.cancelOrder(jsonObject, cancelOrderList)
            mIsUpdating.postValue(true)
        }
    }

    fun cancelOrderData(): LiveData<CommonModel> {
        return cancelOrderList!!
    }

    fun cancelReason() {
        if (UtilsFunctions.isNetworkConnected()) {
            cancelReasonList = homeRepository.cancellationReason(cancelReasonList)
            mIsUpdating.postValue(true)
        }
    }

    fun cancelReasonData(): LiveData<CancelReasonModel> {
        return cancelReasonList!!
    }

    override fun isLoading(): LiveData<Boolean> {
        return mIsUpdating
    }

    override fun isClick(): LiveData<String> {
        return isClick
    }

    override fun clickListener(v: View) {
        isClick.value = v.resources.getResourceName(v.id).split("/")[1]
    }
}