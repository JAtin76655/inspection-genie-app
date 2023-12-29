package com.summonelec.inspectiongenie

import android.app.Application
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        TransferNetworkLossHandler.getInstance(applicationContext)
    }
}