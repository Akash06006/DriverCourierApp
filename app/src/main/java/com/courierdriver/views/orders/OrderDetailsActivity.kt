package com.courierdriver.views.orders

import android.Manifest
import android.annotation.TargetApi
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.courierdriver.R
import com.courierdriver.utils.BaseActivity
import com.courierdriver.common.UtilsFunctions
import  com.courierdriver.constants.GlobalConstants
import com.courierdriver.databinding.ActivityOrderDetailsBinding

import  com.courierdriver.maps.FusedLocationClass
import  com.courierdriver.model.CommonModel
import  com.courierdriver.model.order.ListsResponse
import  com.courierdriver.model.order.OrdersDetailResponse
import  com.courierdriver.sharedpreference.SharedPrefClass
import com.courierdriver.viewmodels.order.OrderDetailViewModel
import  com.courierdriver.viewmodels.order.OrderViewModel
import com.example.courier.model.order.CancelReasonsListResponse
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.gson.JsonObject
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import kotlin.collections.ArrayList

class OrderDetailsActivity : BaseActivity(), OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    GoogleMap.OnCameraIdleListener {
    private lateinit var activityCreateOrderBinding : ActivityOrderDetailsBinding
    private lateinit var orderViewModel : OrderDetailViewModel
    var vehicleList = ArrayList<ListsResponse.VehicleData>()
   // var bannersList = ArrayList<ListsResponse.BannersData>()
    var deliveryTypeList = ArrayList<ListsResponse.DeliveryOptionData>()
    var weightList = ArrayList<ListsResponse.WeightData>()
    private var check : Int = 0
    private var confirmationDialog : Dialog? = null
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private var mGoogleMap : GoogleMap? = null
    private var mPermissionCheck = false
    private var dialog : Dialog? = null
    private var locationDialog : Dialog? = null
    private var mGoogleApiClient : GoogleApiClient? = null
    private var click_settings = 1
    private var click_gps = 1
    private var mHandler = Handler()
    private var mLatitude : String? = null
    private var mLongitude : String? = null
    private var scan = 0
    private var start = 0
    private var permanent_deny = 0
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private var mContext : Context? = null
    private var locationManager : LocationManager? = null;
    private val MIN_TIME = 400;
    private val MIN_DISTANCE = 1000;
    private var mFusedLocationClass : FusedLocationClass? = null
    internal var mFusedLocationClient : FusedLocationProviderClient? = null
    private var mLocation : Location? = null
    internal var cameraZoom = 16.0f
    private var mAddress = ""
    var orderId = ""
    internal lateinit var mLastLocation : Location
    internal lateinit var mLocationCallback : LocationCallback
    internal var mCurrLocationMarker : Marker? = null
    internal lateinit var mLocationRequest : LocationRequest
    var reasons = java.util.ArrayList<String>()
    var cancelledCharges = "0"
    override fun getLayoutId() : Int {
        return R.layout.activity_order_details
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onResume() {
        if (click_settings > 0) {
            checkPermission()
        }
        /* if (click_gps > 0) {
             checkGPS()
             click_gps = 0
         }*/

        super.onResume()

        if (UtilsFunctions.isNetworkConnected()) {
            startProgressDialog()
            orderViewModel.orderDetail(orderId)
           // orderViewModel.cancelReason(orderId)
        }
    }

    override fun initViews() {
        // Initialize the SDK
        Places.initialize(applicationContext, getString(R.string.maps_api_key))
        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this)

        if (UtilsFunctions.isNetworkConnected()) {
            startProgressDialog()
        }
        activityCreateOrderBinding = viewDataBinding as ActivityOrderDetailsBinding
        orderViewModel = ViewModelProviders.of(this).get(OrderDetailViewModel::class.java)
        activityCreateOrderBinding.orderDetailViewModel = orderViewModel
        mContext = this
        val userImage =
            SharedPrefClass().getPrefValue(this, GlobalConstants.USER_IMAGE).toString()
        Glide.with(this).load(userImage).placeholder(R.drawable.ic_user)
            .into(activityCreateOrderBinding.toolbarCommon.imgRight)
        // activityCreateOrderBinding.toolbarCommon.imgToolbarText.text = "Order #123"
        val supportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment?
        supportMapFragment?.getMapAsync(this)
        dialog = Dialog(this)
        reasons.add("Select Reason")
        orderId = intent.extras?.get("id").toString()
        val activeOrder = intent.extras?.get("active").toString()
        if (activeOrder.equals("true")) {
            activityCreateOrderBinding.bottomButtons.visibility = View.VISIBLE
        } else {
            activityCreateOrderBinding.bottomButtons.visibility = View.GONE
        }
        // Specify the types of place data to return.
        orderViewModel.cancelReasonRes().observe(this,
            Observer<CancelReasonsListResponse> { response->
                stopProgressDialog()

                if (response != null) {
                    val message = response.message
                    when {
                        response.code == 200 -> {
                            for (count in 0 until response.data!!.size) {
                                reasons.add(response.data!![count].reason!!)

                            }
                            //reasons.add("Other Reason")
                            // activityCreateOrderBinding.orderDetailModel = response.data
                        }
                        else -> message?.let { UtilsFunctions.showToastError(it) }
                    }
                }
            })

        orderViewModel.cancelOrderRes().observe(this,
            Observer<CommonModel> { response->
                stopProgressDialog()

                if (response != null) {
                    val message = response.message
                    when {
                        response.code == 200 -> {
                            showToastSuccess(
                                "Order Cancelled Successfully"
                            )
                            finish()
                            // activityCreateOrderBinding.orderDetailModel = response.data
                        }
                        else -> message?.let { UtilsFunctions.showToastError(it) }
                    }
                }
            })


        orderViewModel.orderDetailRes().observe(this,
            Observer<OrdersDetailResponse> { response->
                stopProgressDialog()

                if (response != null) {
                    val message = response.message
                    when {
                        response.code == 200 -> {
                            activityCreateOrderBinding.orderDetailModel = response.data
                            cancelledCharges = response.data?.cancellationCharges!!
                            activityCreateOrderBinding.toolbarCommon.imgToolbarText.text =
                                "Order #" + response.data?.orderNo!!
                            val source = LatLng(
                                response.data?.pickupAddress?.lat!!.toDouble(),
                                response.data?.pickupAddress?.long!!.toDouble()
                            )

                            mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLng(source))
                            mGoogleMap!!.animateCamera(CameraUpdateFactory.zoomTo(16f))
                            var isSourceAdded = false
                            var destination : LatLng?
                            for (item in response.data?.deliveryAddress!!) {
                                destination = LatLng(
                                    item.lat!!.toDouble(),
                                    item.long!!.toDouble()
                                )
                                var oldLatLong = LatLng(0.0, 0.0)
                                if (!isSourceAdded) {
                                    isSourceAdded = true
                                    drawPolyline(source, destination, true)
                                    oldLatLong = destination
                                } else {
                                    drawPolyline(oldLatLong, destination, false)
                                    oldLatLong = destination
                                }
                            }
                            //drawPolyline()
                        }
                        else -> message?.let { UtilsFunctions.showToastError(it) }
                    }
                }
            })



        orderViewModel.isClick().observe(
            this, Observer<String>(function =
            fun(it : String?) {
                when (it) {
                    "btnCancel" -> {
                        // Set the fields to specify which types of place data to
                        showCancelReasonDialog()
                    }
                    "btnSchedule" -> {
                        val intent = Intent(this, CreateOrderActivty::class.java)
                        intent.putExtra("id", orderId)
                        startActivity(intent)
                    }
                }
            })
        )

    }

    /*override fun onMapReady(googleMap : GoogleMap) {
        this.mGoogleMap = googleMap
        mGoogleMap!!.setMinZoomPreference(5f)
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.isTrafficEnabled = false
        googleMap.isMyLocationEnabled = true
        //mHandler.postDelayed(mRunnable, 500)
        mPermissionCheck = false
        check = 0

    }*/
    //region mp
    override fun onMapReady(googleMap : GoogleMap) {
        this.mGoogleMap = googleMap

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                buildGoogleApiClient()
                //mGoogleMap!!.isMyLocationEnabled = true
            }
        } else {
            buildGoogleApiClient()
            // mGoogleMap!!.isMyLocationEnabled = true
        }
        //mGoogleMap?.uiSettings?.setAllGesturesEnabled(true)
        // mGoogleMap?.uiSettings?.isScrollGesturesEnabled = true
        mGoogleMap!!.uiSettings.isZoomControlsEnabled = true
        //    mGoogleMap?.setOnCameraIdleListener(this)
    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    override fun onConnected(bundle : Bundle?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
            mGoogleApiClient
        )
        if (mLastLocation != null) {
            val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("Current Position")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        }
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 50000 //5 seconds
        mLocationRequest.fastestInterval = 50000 //5 seconds
        mLocationRequest.smallestDisplacement = 0.1f //added
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient,
            mLocationRequest,
            this
        )
    }

    override fun onLocationChanged(location : Location) {
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        //Place current location marker
        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        //mCurrLocationMarker = mMap!!.addMarker(markerOptions)
        //move map camera
        /*  mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
          mGoogleMap!!.animateCamera(CameraUpdateFactory.zoomTo(14f))*/
        //stop location updates
        if (mGoogleApiClient != null) {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
        }
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    override fun onConnectionFailed(connectionResult : ConnectionResult) {
        Toast.makeText(applicationContext, "connection failed", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionSuspended(p0 : Int) {
        Toast.makeText(applicationContext, "connection suspended", Toast.LENGTH_SHORT).show()
    }

    //endregion
    fun checkPermission() {
        if (!mPermissionCheck) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mContext != null)
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (dialog != null) {
                            dialog!!.dismiss()
                        }
                        mPermissionCheck = true
                    } else {
                        if (dialog != null) {
                            dialog!!.dismiss()
                        }
                        mPermissionCheck = false

                        requestPermissions(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
            } else {
                if (dialog != null) {
                    dialog!!.dismiss()
                }
                mPermissionCheck = true
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode : Int, permissions : Array<String>,
        grantResults : IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION && permissions.size > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dialog!!.dismiss()
                    mPermissionCheck = true
                } else {
                    if (scan > 0) {
                        scan = 0
                    } else {
                        val permission1 = permissions[0]
                        val showRationale = shouldShowRequestPermissionRationale(permission1)
                        if (click_settings > 0) {
                            click_settings = 0
                            dialog!!.show()
                        } else {
                            if (!showRationale && permanent_deny > 0) {
                                click_settings++
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts("package", this.packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            } else if (permanent_deny == 0 && !showRationale) {
                                dialog!!.show()
                                permanent_deny++
                            } else {
                                permanent_deny++
                                dialog!!.show()
                            }
                        }

                    }
                }
            }
        }
    }

    fun bitmapDescriptorFromVector(context : Context, @DrawableRes vectorDrawableResourceId : Int) : BitmapDescriptor {
        var background =
            ContextCompat.getDrawable(this, vectorDrawableResourceId/*R.drawable.ic_app*/);
        background?.setBounds(
            0,
            0,
            background.getIntrinsicWidth(),
            background.getIntrinsicHeight()
        )
        var vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable?.setBounds(
            0,
            0,
            vectorDrawable.getIntrinsicWidth(),
            vectorDrawable.getIntrinsicHeight()
        )
        var bitmap = Bitmap.createBitmap(
            vectorDrawable!!.getIntrinsicWidth(),
            vectorDrawable.getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888
        )
        var canvas = Canvas(bitmap)
        // background?.draw(canvas)
        vectorDrawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showCancelReasonDialog() {
        confirmationDialog = Dialog(this, R.style.transparent_dialog)
        confirmationDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)


        confirmationDialog?.setContentView(R.layout.cancel_dialog)
        confirmationDialog?.setCancelable(true)

        confirmationDialog?.window!!.setLayout(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        confirmationDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val edtReason = confirmationDialog?.findViewById<EditText>(R.id.edtReason)
        val llCancelCharges = confirmationDialog?.findViewById<LinearLayout>(R.id.llCancelCharges)
        val btnSend = confirmationDialog?.findViewById<Button>(R.id.btnSend)
        val txtCharges = confirmationDialog?.findViewById<TextView>(R.id.txtCharges)
        val spReason = confirmationDialog?.findViewById<Spinner>(R.id.spReason)
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, reasons
        )

        adapter.setDropDownViewResource(R.layout.spinner_item);

        if (!cancelledCharges.equals("0") || !TextUtils.isEmpty(cancelledCharges)) {
            llCancelCharges?.visibility = View.VISIBLE
            txtCharges?.text = cancelledCharges
        } else {
            llCancelCharges?.visibility = View.GONE
        }

        spReason?.adapter = adapter
        var pos = 0
        var otherReason = "false"
        spReason?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent : AdapterView<*>,
                view : View, position : Int, id : Long
            ) {
                //if (position != 0) {
                pos = position
                edtReason?.setText("")
                otherReason = "false"
                if (reasons[pos].equals("Other")) {
                    otherReason = "true"
                    edtReason?.visibility = View.VISIBLE
                } else {
                    otherReason = "false"
                    edtReason?.visibility = View.GONE
                }
                /* } else {
                     //regionId = "0"
                     //regionPos = position
                 }
 */
            }

            override fun onNothingSelected(parent : AdapterView<*>) {
                // write code to perform some action
            }
        }


        btnSend?.setOnClickListener {
            val mJsonObject = JsonObject()
            mJsonObject.addProperty(
                "orderId", orderId
            )
            if (otherReason.equals("true")) {
                mJsonObject.addProperty(
                    "cancellationReason",
                    edtReason?.text.toString().trim() //completedorder?.empId
                )
                mJsonObject.addProperty(
                    "otherReason", edtReason?.text.toString().trim()
                )

            } else {
                mJsonObject.addProperty(
                    "cancellationReason", reasons[pos]//completedorder?.empId
                )
                mJsonObject.addProperty(
                    "otherReason", reasons[pos]
                )
            }
            if (pos != 0) {
                if (otherReason.equals("true")) {
                    if (TextUtils.isEmpty(edtReason?.text.toString().trim())) {
                        edtReason?.error = "Please enter reason"
                    } else {
                        if (UtilsFunctions.isNetworkConnected()) {
                            startProgressDialog()
                            orderViewModel.cancelOrder(mJsonObject)
                            confirmationDialog?.dismiss()
                        }
                    }
                } else {
                    if (UtilsFunctions.isNetworkConnected()) {
                        startProgressDialog()
                        orderViewModel.cancelOrder(mJsonObject)
                        confirmationDialog?.dismiss()
                    }
                }

            } else {
                UtilsFunctions.showToastError("Please select reason")
            }
        }
        /*imgCross?.setOnClickListener {
            confirmationDialog?.dismiss()
        }*/
        if (!confirmationDialog?.isShowing()!!) {
            confirmationDialog?.show()
        }

    }

    override fun onCameraIdle() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun drawPolyline(
        sourceLatLng : LatLng,
        destinationLatLng : LatLng,
        isSourceAdded : Boolean
    ) {
        var path : MutableList<LatLng> = ArrayList()
        val context = GeoApiContext.Builder()
            .apiKey(getString(R.string.maps_api_key))
            .build()
        val req = DirectionsApi.getDirections(
            context,
            /*sourceLatLng.latitude.toString().plus(",").plus(sourceLatLng.longitude),
            destinationLatLng.latitude.toString().plus(",").plus(destinationLatLng.longitude)*/
            sourceLatLng.latitude.toString().plus(",").plus(sourceLatLng.longitude.toString()),
            destinationLatLng.latitude.toString().plus(",").plus(destinationLatLng.longitude.toString())
        )
        try {
            val res = req.await()
            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.isNotEmpty()) {
                val route = res.routes[0]
                if (route.legs != null) {
                    for (i in 0 until route.legs.size) {
                        val leg = route.legs[i]
                        if (leg.steps != null) {
                            for (j in 0 until leg.steps.size) {
                                val step = leg.steps[j]
                                if (step.steps != null && step.steps.isNotEmpty()) {
                                    for (k in 0 until step.steps.size) {
                                        val step1 = step.steps[k]
                                        val points1 = step1.polyline
                                        if (points1 != null) {
                                            //Decode polyline and add points to latLongList of route coordinates
                                            val coords1 = points1.decodePath()
                                            for (coord1 in coords1) {
                                                // path.add(LatLng(coord1.lat, coord1.lng))
                                                path.add(LatLng(coord1.lat, coord1.lng))
                                            }
                                        }
                                    }
                                } else {
                                    val points = step.polyline
                                    if (points != null) {
                                        //Decode polyline and add points to latLongList of route coordinates
                                        val coords = points.decodePath()
                                        for (coord in coords) {
                                            path.add(LatLng(coord.lat, coord.lng))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                //  mGoogleMap.drawPolyline("Destination is not detected,unable to draw path")
                Log.d("MapPath", "Unable to draw path")
            }
        } catch (ex : Exception) {
            ex.printStackTrace()
        }

        drawLine(path, sourceLatLng, destinationLatLng, isSourceAdded)

    }

    var polyPath : MutableList<LatLng>? = null
    private fun drawLine(
        path : MutableList<LatLng>,
        sourceLatLng : LatLng,
        destinationLatLng : LatLng, isSourceAdded : Boolean
    ) {
        polyPath = path
        // mGoogleMap?.clear()
        var ic_source : BitmapDescriptor
        if (isSourceAdded) {
            ic_source = bitmapDescriptorFromVector(
                this,
                R.drawable.ic_source
            )//ic_source
        } else {
            /* ic_source = BitmapDescriptorFactory.fromResource(R.drawable.ic_destination)*/
            ic_source = bitmapDescriptorFromVector(
                this,
                R.drawable.ic_destination
            )
        }
        // var ic_destination = BitmapDescriptorFactory.fromResource(R.drawable.ic_destination)
        var ic_destination = bitmapDescriptorFromVector(
            this,
            R.drawable.ic_destination
        )
        //var icon = bitmapDescriptorFromVector(this, R.drawable.ic_map_pin)
        //  if (isSourceAdded) {
        mGoogleMap!!.addMarker(
            MarkerOptions()
                .position(LatLng(sourceLatLng.latitude, sourceLatLng.longitude))
                .icon(ic_source)
        )
        //  }
        mGoogleMap!!.addMarker(
            MarkerOptions()
                .position(LatLng(destinationLatLng.latitude, destinationLatLng.longitude))
                //.snippet(points[0].longitude.toString() + "")
                .icon(ic_destination)
        )
        if (polyPath!!.size > 0) {
            val opts = PolylineOptions().addAll(path).color(R.color.colorPrimary).width(16f)
            /*polylineFinal = */mGoogleMap?.addPolyline(opts)
        }

    }
    /*fun bitmapDescriptorFromVector(
        context : Context
    ) : BitmapDescriptor {
        val background = ContextCompat.getDrawable(context, R.drawable.ic_map_pin_filled_blue_48dp);
        background?.setBounds(
            0,
            0,
            background!!.getIntrinsicWidth(),
            background.getIntrinsicHeight()
        );
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable?.setBounds(
            40,
            20,
            vectorDrawable.getIntrinsicWidth() + 40,
            vectorDrawable.getIntrinsicHeight() + 20
        );
        var bitmap = Bitmap.createBitmap(
            background!!.getIntrinsicWidth(),
            background.getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888
        );
        var canvas = Canvas(bitmap);
        background?.draw(canvas);
        vectorDrawable?.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }*/

}