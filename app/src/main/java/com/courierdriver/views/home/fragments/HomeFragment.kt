package com.courierdriver.views.home.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.courierdriver.R
import com.courierdriver.adapters.orders.HomeOrdersAdapter
import com.courierdriver.common.UtilsFunctions
import com.courierdriver.constants.GlobalConstants
import com.courierdriver.databinding.FragmentHomeBinding
import com.courierdriver.maps.FusedLocationClass
import com.courierdriver.model.CommonModel
import com.courierdriver.model.order.OrderListModel
import com.courierdriver.sharedpreference.SharedPrefClass
import com.courierdriver.utils.BaseFragment
import com.courierdriver.viewmodels.home.HomeViewModel
import com.google.android.gms.location.*

class
HomeFragment : BaseFragment() {
    private var mFusedLocationClass: FusedLocationClass? = null
    private lateinit var homeViewModel: HomeViewModel
    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var currentLat = ""
    var currentLong = ""
    private lateinit var fragmentHomeBinding: FragmentHomeBinding
    private var orderList: ArrayList<OrderListModel.Body>? = null
    private var homeOrdersAdapter: HomeOrdersAdapter? = null
    private var orderStatus = 1

    override fun initView() {
        fragmentHomeBinding = viewDataBinding as FragmentHomeBinding
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        fragmentHomeBinding.homeViewModel = homeViewModel
        mFusedLocationClass = FusedLocationClass(activity)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)

        sharedPrefValue()
        setToolbarTextIcons()
        viewClicks()

        getOrderList(1)
        getOrderListObserver()
        acceptOrderObserver()
        cancelOrderObserver()
    }

    private fun viewClicks() {
        homeViewModel.isClick().observe(
            this, Observer<String>(function =
            fun(it: String?) {
                when (it) {
                    "tv_available" -> {
                        clearList()
                        orderStatus = 1

                        fragmentHomeBinding.tvAvailable.background.setColorFilter(
                            ContextCompat.getColor(baseActivity, R.color.colorRed),
                            PorterDuff.Mode.SRC_ATOP
                        )
                        fragmentHomeBinding.tvAvailable.setTextColor(
                            ContextCompat.getColor(
                                baseActivity,
                                R.color.colorWhite
                            )
                        )
                        fragmentHomeBinding.tvActive.background.setColorFilter(
                            ContextCompat.getColor(baseActivity, R.color.colorWhite),
                            PorterDuff.Mode.SRC_ATOP
                        )
                        fragmentHomeBinding.tvActive.setTextColor(
                            ContextCompat.getColor(
                                baseActivity,
                                R.color.colorBlack
                            )
                        )
                        fragmentHomeBinding.tvCompleted.background.setColorFilter(
                            ContextCompat.getColor(baseActivity, R.color.colorWhite),
                            PorterDuff.Mode.SRC_ATOP
                        )
                        fragmentHomeBinding.tvCompleted.setTextColor(
                            ContextCompat.getColor(
                                baseActivity,
                                R.color.colorBlack
                            )
                        )
                        getOrderList(1)
                    }
                    "tv_active" -> {
                        clearList()
                        orderStatus = 2
                        fragmentHomeBinding.tvActive.background.setColorFilter(
                            ContextCompat.getColor(baseActivity, R.color.colorRed),
                            PorterDuff.Mode.SRC_ATOP
                        )
                        fragmentHomeBinding.tvActive.setTextColor(
                            ContextCompat.getColor(
                                baseActivity,
                                R.color.colorWhite
                            )
                        )
                        fragmentHomeBinding.tvAvailable.background.setColorFilter(
                            ContextCompat.getColor(baseActivity, R.color.colorWhite),
                            PorterDuff.Mode.SRC_ATOP
                        )
                        fragmentHomeBinding.tvAvailable.setTextColor(
                            ContextCompat.getColor(
                                baseActivity,
                                R.color.colorBlack
                            )
                        )
                        fragmentHomeBinding.tvCompleted.background.setColorFilter(
                            ContextCompat.getColor(baseActivity, R.color.colorWhite),
                            PorterDuff.Mode.SRC_ATOP
                        )
                        fragmentHomeBinding.tvCompleted.setTextColor(
                            ContextCompat.getColor(
                                baseActivity,
                                R.color.colorBlack
                            )
                        )
                        getOrderList(2)
                    }
                    "tv_completed" -> {
                        clearList()
                        orderStatus = 3

                        fragmentHomeBinding.tvCompleted.background.setColorFilter(
                            ContextCompat.getColor(baseActivity, R.color.colorRed),
                            PorterDuff.Mode.SRC_ATOP
                        )
                        fragmentHomeBinding.tvCompleted.setTextColor(
                            ContextCompat.getColor(
                                baseActivity,
                                R.color.colorWhite
                            )
                        )
                        fragmentHomeBinding.tvActive.background.setColorFilter(
                            ContextCompat.getColor(baseActivity, R.color.colorWhite),
                            PorterDuff.Mode.SRC_ATOP
                        )
                        fragmentHomeBinding.tvActive.setTextColor(
                            ContextCompat.getColor(
                                baseActivity,
                                R.color.colorBlack
                            )
                        )
                        fragmentHomeBinding.tvAvailable.background.setColorFilter(
                            ContextCompat.getColor(baseActivity, R.color.colorWhite),
                            PorterDuff.Mode.SRC_ATOP
                        )
                        fragmentHomeBinding.tvAvailable.setTextColor(
                            ContextCompat.getColor(
                                baseActivity,
                                R.color.colorBlack
                            )
                        )
                        getOrderList(3)
                    }
                }
            })
        )
    }

    private fun clearList() {
        if (orderList != null)
            orderList!!.clear()
        if (homeOrdersAdapter != null)
            homeOrdersAdapter!!.notifyDataSetChanged()
    }


    //region API_CALL
    fun getOrderList(orderStatus:Int) {
        homeViewModel.orderList(orderStatus.toString(), "0.0", "0.0")
    }

    fun acceptOrder(id: String?) {
        homeViewModel.acceptOrder(id!!)
    }

    fun cancelOrder(id: String?) {
        homeViewModel.cancelOrder(id!!)
    }
    //endregion


    //region Observers
    private fun getOrderListObserver() {
        homeViewModel.getOrderListData().observe(this,
            Observer<OrderListModel> { response ->
                if (response != null) {
                    val message = response.message
                    when (response.code) {
                        200 -> {
                            if (response.body!!.isNotEmpty()) {
                                orderList = response.body
                                setAdapter()
                            }
                        }
                        else -> UtilsFunctions.showToastError(message!!)
                    }
                } else {
                    UtilsFunctions.showToastError(resources.getString(R.string.internal_server_error))
                }
            })
    }

    private fun acceptOrderObserver() {
        homeViewModel.acceptOrderData().observe(this,
            Observer<CommonModel> { response ->
                if (response != null) {
                    val message = response.message
                    when (response.code) {
                        200 -> {
                            UtilsFunctions.showToastSuccess(message!!)
                        }
                        else -> UtilsFunctions.showToastError(message!!)
                    }
                } else {
                    UtilsFunctions.showToastError(resources.getString(R.string.internal_server_error))
                }
            })
    }

    private fun cancelOrderObserver() {
        homeViewModel.cancelOrderData().observe(this,
            Observer<CommonModel> { response ->
                if (response != null) {
                    val message = response.message
                    when (response.code) {
                        200 -> {
                            UtilsFunctions.showToastSuccess(message!!)
                        }
                        else -> UtilsFunctions.showToastError(message!!)
                    }
                } else {
                    UtilsFunctions.showToastError(resources.getString(R.string.internal_server_error))
                }
            })
    }
    //endregion

    private fun setAdapter() {
        val linearLayoutManager = LinearLayoutManager(baseActivity)
        homeOrdersAdapter = HomeOrdersAdapter(this, orderList!!, orderStatus)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        fragmentHomeBinding.rvOrderList.layoutManager = linearLayoutManager
        fragmentHomeBinding.rvOrderList.adapter = homeOrdersAdapter
    }

    private fun sharedPrefValue() {
        val name =
            SharedPrefClass().getPrefValue(activity!!, GlobalConstants.USERNAME).toString()
        val userImage =
            SharedPrefClass().getPrefValue(activity!!, GlobalConstants.USER_IMAGE)
                .toString()
    }

    private fun setToolbarTextIcons() {
        fragmentHomeBinding.toolbarCommon.toolbar.setImageResource(R.drawable.ic_back_white)
        fragmentHomeBinding.toolbarCommon.imgRight.visibility = View.GONE
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(activity!!) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        currentLat = location.latitude.toString()
                        currentLong = location.longitude.toString()
                        /*  Handler().postDelayed({
                              callSocketMethods("updateVehicleLocation")
                          }, 2000)
  */
                    }
                }
            } else {
                Toast.makeText(activity, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission", "RestrictedApi")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )

    }

    private
    val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            currentLat = mLastLocation.latitude.toString()
            currentLong = mLastLocation.longitude.toString()
            /*Handler().postDelayed({
                callSocketMethods("updateVehicleLocation")
            }, 2000)*/

        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_home
    }
}