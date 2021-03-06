package com.courierdriver.views

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import com.courierdriver.R
import com.courierdriver.application.MyApplication
import com.courierdriver.constants.GlobalConstants
import com.courierdriver.databinding.ActivitySplashBinding
import com.courierdriver.sharedpreference.SharedPrefClass
import com.courierdriver.utils.BaseActivity
import com.courierdriver.views.authentication.LoginActivity
import com.courierdriver.views.home.LandingActivty
import com.courierdriver.views.orders.OrderDetailsActivity
import com.courierdriver.views.tutorials.TutorialActivity
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import java.util.*

class SplashActivity : BaseActivity() {
    private var mActivitySplashBinding : ActivitySplashBinding? = null
    private var sharedPrefClass : SharedPrefClass? = null
    private var mContext : Context? = null

    override fun getLayoutId() : Int {
        return R.layout.activity_splash
    }

    override fun initViews() {
        mContext = this
        mActivitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)


        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null,
                HttpMethod.DELETE,
                GraphRequest.Callback {
                    AccessToken.setCurrentAccessToken(null)
                    LoginManager.getInstance().logOut()
                    //finish()
                }).executeAsync()
        }

        sharedPrefClass = SharedPrefClass()
        val token : String? = "sd"

        if (token != null) {
            sharedPrefClass!!.putObject(
                applicationContext,
                GlobalConstants.NOTIFICATION_TOKEN,
                token
            )
        }

        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    checkScreenType()
                }
            }
        }, 3000)
    }

    private fun checkScreenType() {
        var login = ""
        if (checkObjectNull(
                SharedPrefClass().getPrefValue(
                    MyApplication.instance,
                    "isLogin"
                )
            )
        )
            login = sharedPrefClass!!.getPrefValue(this, "isLogin").toString()
        val intent = if (login == "true") {
             Intent(this, LandingActivty::class.java)
           // Intent(this, TutorialActivity::class.java)
        } else {
             Intent(this, LoginActivity::class.java)
           // Intent(this, TutorialActivity::class.java)

        }

        startActivity(intent)
        finish()
    }

}
