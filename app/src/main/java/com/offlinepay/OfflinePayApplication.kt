package com.offlinepay

import android.app.Application
import com.offlinepay.di.AppContainer
import com.offlinepay.di.DefaultAppContainer

class OfflinePayApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
