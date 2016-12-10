package com.friendoye.ilchallenge

import android.content.Context
import android.net.ConnectivityManager

val Context.isInternetConnected: Boolean
    get() {
        val connManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val isConnected = connManager.activeNetworkInfo?.isConnected?: false
        return isConnected
    }